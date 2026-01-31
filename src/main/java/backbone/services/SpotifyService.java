package backbone.services;

import backbone.configs.ApplicationProperties;
import backbone.dto.SpotifyCredentials;
import backbone.models.spotify.CurrentlyPlaying;
import backbone.models.spotify.SpotifyAuthorisationToken;
import backbone.repositories.SpotifyAuthorisationTokenRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SpotifyService {

    private final SpotifyAuthorisationTokenRepo tokenRepo;
    private final RestClient spotifyClient;
    private final ApplicationProperties applicationProperties;
    private final RestClient spotifyAccountsClient;

    private final ApplicationContext context;


    // self injection, because @cacheable bypasses the proxy making it not work



    public SpotifyService(SpotifyAuthorisationTokenRepo tokenRepo,
                          @Qualifier("spotifyClient") RestClient spotifyClient,
                          @Qualifier("spotifyAccountsClient") RestClient spotifyAccountsClient,
                          ApplicationProperties applicationProperties, ApplicationContext context) {
        this.tokenRepo = tokenRepo;
        this.spotifyClient = spotifyClient;
        this.applicationProperties = applicationProperties;
        this.spotifyAccountsClient = spotifyAccountsClient;
//        this.context = context;
        this.context = context;
    }

//    @Cacheable(value = "spotifyAuthToken")
    public SpotifyAuthorisationToken retrieveAuthToken(String sessionId) {
//
//        SpotifyCredentials credentials = SpotifyCredentials.builder()
//                .grantType("client_credentials")
//                .clientSecret(applicationProperties.getSpotifyClientSecret())
//                .clientId(applicationProperties.getSpotifyClientId())
//                .build();

        LinkedMultiValueMap<String, String> credentials = new  LinkedMultiValueMap<>();
        credentials.add("grant_type", "client_credentials");
        credentials.add("client_id", applicationProperties.getSpotifyClientId());
        credentials.add("client_secret", applicationProperties.getSpotifyClientSecret());


        log.info("[{}] retrieving authentication tokens from spotify. credentials: {}", sessionId, credentials);

        SpotifyAuthorisationToken token = spotifyAccountsClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(credentials)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        ((request, response) -> {
                            log.error("Something terrible happened {}", response.getStatusText());
                        })
                ).onStatus(HttpStatusCode::isError, ((request, response) -> {
                    log.error("failed to retrieve auth tokens: response {}", response.getStatusText());
                }))
                .body(SpotifyAuthorisationToken.class);

        log.info("[{}] spotify authorisation retrieved successfully, saving to database. token: {}", sessionId, token);

        return token;
    }

//    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    public CurrentlyPlaying retrieveCurrentlyPlaying() {
        log.info("about to retrieve currently playing from spotify");

        SpotifyAuthorisationToken token = this.context.getBean(SpotifyService.class).retrieveAuthToken("sdfasfasdf");

        log.info("generated token : {}",token.toString() );

        CurrentlyPlaying currentlyPlaying = spotifyClient.get()
                .uri("/me/player/currently-playing")
                .header("Authorization", "Bearer %s".formatted(token.getAccessToken()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        ((request, response) -> {
//                            throw new ArticleNotFoundException(response);
                            log.error("Something wrong with the data were sending. response: {}", response.getStatusText());
                        })
                ).onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
//                            throw new ArticleNotFoundException(response);
                            log.error("something wrong happened to the server: {} ", response.getStatusText());
                }))
                .body(CurrentlyPlaying.class);

        log.info("currently playing song has been retrieved");
        return currentlyPlaying;
    }

}
