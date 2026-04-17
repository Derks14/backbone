package backbone.services;

import backbone.configs.ApplicationProperties;
import backbone.configs.BackboneException;
import backbone.dto.Res;
import backbone.models.spotify.CurrentlyPlaying;
import backbone.models.spotify.SpotifyAuthorisationToken;
import backbone.models.spotify.SpotifyToken;
import backbone.models.spotify.QueueResponse;
import backbone.repositories.SpotifyAuthorisationTokenRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class SpotifyService {
    /*
    1. request authorization to access data -> display scopes and prompt user to login if required -> log in authorize access
    2. ( code, state ) => request access tokens and refresh tokens [ client_id, client_secret, grant_type, code, redirect_uri] -> return access and refresh tokens
    3. ( access_token, token_type, expires_in, refresh_token) => use access token in requests to web api [ access token] => return requested data
    3. ( access_token, token_type, expires_in, refresh_token ) => use access token in requests to web api [ access_token] => return new access token
     */

    private final RestClient spotifyClient;
    private final ApplicationProperties applicationProperties;
    private final RestClient spotifyAccountsClient;
    private final SpotifyAuthorisationTokenRepo spotifyAuthorisationTokenRepo;
    private final RedisTemplate<String, Object> redisTemplate;


    // self injection, because @cacheable bypasses the proxy making it not work



    public SpotifyService(SpotifyAuthorisationTokenRepo tokenRepo,
                          @Qualifier("spotifyClient") RestClient spotifyClient,
                          @Qualifier("spotifyAccountsClient") RestClient spotifyAccountsClient,
                          ApplicationProperties applicationProperties, ApplicationContext context, CacheManager cacheManager, SpotifyAuthorisationTokenRepo spotifyAuthorisationTokenRepo, RedisTemplate<String, Object> redisTemplate) {
        this.spotifyClient = spotifyClient;
        this.applicationProperties = applicationProperties;
        this.spotifyAccountsClient = spotifyAccountsClient;
        this.spotifyAuthorisationTokenRepo = spotifyAuthorisationTokenRepo;
        this.redisTemplate = redisTemplate;
    }



    public String buildAuthoriseUrl(String sessionId) {
        String scopes = "user-read-currently-playing user-read-playback-state user-read-recently-played";
        String encodedRedirect = applicationProperties.getSpotifyRedirectUri();

        // we will have to store this into redis and retrieve later for security reasons ( prevent cross-site request forgery )
        String state = UUID.randomUUID().toString(); // https://datatracker.ietf.org/doc/html/rfc6749#section-4.1

        return "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + applicationProperties.getSpotifyClientId() +
                "&scope=" + scopes.replace(" ", "%20") +
                "&redirect_uri=" + encodedRedirect +
                "&state=" + state;
    }

    public boolean verifyCallbackRequest(String state, String sessionId) {
        // we will check out redis database for very fast verification
        // we to reduce latency and request time, we dont need a whole db for this.
        // also its done once in the lifetime. so chill

        return true;
    }

    public void exchangeCodeForToken(String code, String sessionId) {
        log.info("[{}] creating request for code exchange", sessionId);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", applicationProperties.getSpotifyRedirectUri());

        SpotifyAuthorisationToken authorisationToken = spotifyAccountsClient.post()
                .headers(
                        httpHeaders -> {
                            httpHeaders.set("Authorization", buildBase64String());
                            httpHeaders.set("Content-Type", String.valueOf(MediaType.APPLICATION_FORM_URLENCODED));
                })
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus( HttpStatusCode::is4xxClientError, SpotifyService::is4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, SpotifyService::is5xxServerError)
                .onStatus(HttpStatusCode::isError, SpotifyService::unknownError)
                .body(SpotifyAuthorisationToken.class);

        if (Objects.isNull(authorisationToken))
            throw new BackboneException("",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "successful authorization request to spotify returned null");

        log.info("[{}] code exchange for auth tokens successful", sessionId);

        this.saveAccessToken(authorisationToken, sessionId);
    }

    public SpotifyAuthorisationToken refreshAccessTokens(String sessionId) {
        log.info("[{}] creating request to refresh our existing access tokens ", sessionId);

        SpotifyToken token = spotifyAuthorisationTokenRepo.findFirstByOrderByIdAsc()
                .orElseThrow( () -> new BackboneException(HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND, "could not find original token object with refresh token"));

        if (Objects.isNull(token.getRefreshToken())) {
            throw new BackboneException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    HttpStatus.INTERNAL_SERVER_ERROR, "retrieved token doesn't have refresh token");
        }


        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", token.getRefreshToken());

        SpotifyAuthorisationToken authorisationToken = spotifyAccountsClient.post()
                .headers( httpHeaders -> {
                    httpHeaders.set("Content-Type", String.valueOf(MediaType.APPLICATION_FORM_URLENCODED));
                    httpHeaders.set("Authorization", buildBase64String());
                }).accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus( HttpStatusCode::is4xxClientError, SpotifyService::is4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, SpotifyService::is5xxServerError)
                .onStatus(HttpStatusCode::isError, SpotifyService::unknownError)
                .body(SpotifyAuthorisationToken.class);


        if (Objects.isNull(authorisationToken))
            throw new BackboneException("",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "successful authorization request to spotify returned null");

        // this runs in async
        this.saveAccessToken(authorisationToken, sessionId);

        return authorisationToken;
    }

//    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public Res<CurrentlyPlaying> retrieveCurrentlyPlaying() {
        log.info("about to retrieve currently playing from spotify");

        SpotifyToken spotifyToken = generateActiveToken();

        CurrentlyPlaying currentlyPlaying = spotifyClient.get()
                .uri("/me/player/currently-playing")
                .header("Authorization", "Bearer %s".formatted(spotifyToken.getAccessToken()))
                .retrieve()
                .onStatus( HttpStatusCode::is4xxClientError, SpotifyService::is4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, SpotifyService::is5xxServerError)
                .onStatus(HttpStatusCode::isError, SpotifyService::unknownError)
                .body(CurrentlyPlaying.class);

        log.info("currently playing song has been retrieved");



        Res<CurrentlyPlaying> response = Res.<CurrentlyPlaying>builder()
                .message("currently playing song has been retrieved")
                .data(currentlyPlaying)
                .build();

        if (Objects.isNull(currentlyPlaying)) {
            log.info("user's spotify is currently idle");
            response.setMessage("user's spotify is currently idle");
        }
        return response;
    }

    @Cacheable(value = "spotify", key = "'queue'", unless = "true")
    public Res<QueueResponse> retrieveUserQueue() {
        log.info("retrieving current user queue");


        SpotifyToken token = generateActiveToken();

        QueueResponse userQueue = spotifyClient.get()
                .uri("/me/player/queue")
                .header("Authorization", "Bearer %s".formatted(token.getAccessToken()))
                .retrieve()
                .onStatus( HttpStatusCode::is4xxClientError, SpotifyService::is4xxClientError)
                .onStatus(HttpStatusCode::is5xxServerError, SpotifyService::is5xxServerError)
                .onStatus(HttpStatusCode::isError, SpotifyService::unknownError)
                .body(QueueResponse.class);

        Res<QueueResponse> response = Res.<QueueResponse>builder()
                .message("current user queue has been retrieved")
                .data(userQueue)
                .build();

        if (Objects.isNull(userQueue) || Objects.isNull(userQueue.getCurrentlyPlaying())) {
            log.info("users spotify is idle at the moment");
            response.setMessage("user's spotify is currently idle");
        } else {
            cacheItem("queue", response, Duration.ofMillis(userQueue.getCurrentlyPlaying().getDurationMs()));
        }
        log.info("user active queue successfully retrieved");
        return response;
    }

    private void cacheItem(String key, Object value, Duration ttl) {
        this.redisTemplate.opsForValue().set("spotify::"+ key, value, ttl);
    }

    private String buildBase64String( ) {
        String authorisationString = null;
        try {
            String clientCredentials = "%s:%s".formatted(applicationProperties.getSpotifyClientId(), applicationProperties.getSpotifyClientSecret());
            String encoded = Base64.getEncoder().encodeToString(clientCredentials.getBytes(StandardCharsets.UTF_8));
            authorisationString = "Basic %s".formatted(encoded);
        } catch (Exception e) {
            log.error("Failed to build base 64 string");
            throw new RuntimeException(e);
        }
        return authorisationString;
    }

    private SpotifyToken generateActiveToken() {

        SpotifyToken spotifyToken = spotifyAuthorisationTokenRepo.findFirstByOrderByIdDesc()
                .orElseThrow( () -> new BackboneException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR, "illegal state. no refresh token available, user not connected"));

        boolean isAccessTokenExpired = Instant.now().isAfter(spotifyToken.getAccessTokenExpiresAt());
        if (isAccessTokenExpired) {
            log.info("we have an expired token, about to request new ones");
            spotifyToken.setAccessToken(refreshAccessTokens("").getAccessToken());
        }

        return spotifyToken;
    }

    @Async
    public void saveAccessToken(SpotifyAuthorisationToken authorisationToken, String sessionId) {

        try {
            log.info("[{}] new access tokens retrieved. access tokens : {}", sessionId, authorisationToken.getAccessToken());

            Instant accessTokenExpiresAt = Instant.now().plusSeconds(authorisationToken.getExpiresIn() - 60);

            SpotifyToken freshToken = SpotifyToken.builder()
                    .accessToken(authorisationToken.getAccessToken())
                    .refreshToken(authorisationToken.getRefreshToken())
                    .tokenType(authorisationToken.getTokenType())
                    .scope(authorisationToken.getScope())
                    .expiresIn(authorisationToken.getExpiresIn())
                    .accessTokenExpiresAt(accessTokenExpiresAt)
                    .build();

            spotifyAuthorisationTokenRepo.insert(freshToken);
            log.info("[{}] tokens saved successfully", sessionId);

        } catch (Exception e) {
            throw new BackboneException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), HttpStatus.INTERNAL_SERVER_ERROR, "access token storage failed");
        }

    }

    private static void is4xxClientError(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.error("Something wrong with the data were sending. response: {}", response.getStatusText());

        throw new BackboneException(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST,
                "something wrong with the requests we are sending");
    }

    private static void  is5xxServerError(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.error("spotify failed to process our request: {} ", response.getBody().toString());

        throw new BackboneException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "something wrong with the requests we are sending");

    }

    private static void unknownError(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.error("we hit an unknown block {}", response.getStatusText());
        throw new BackboneException(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "something wrong with the requests we are sending");
    }

}
