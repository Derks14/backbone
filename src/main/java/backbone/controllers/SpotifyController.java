package backbone.controllers;


import backbone.dto.Res;
import backbone.models.spotify.CurrentlyPlaying;
import backbone.models.spotify.QueueResponse;
import backbone.services.SpotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("api/window")
@Slf4j
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    public ResponseEntity<String> home(HttpServletRequest request){
        String sessionId = request.getSession().getId();

        return ResponseEntity.ok("ok spotify");
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();

        String url = spotifyService.buildAuthoriseUrl(sessionId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(value = "code", required = false) String code,
                                           @RequestParam(value = "state", required = false) String state,
                                           HttpServletRequest request) {
        String sessionId = request.getSession().getId();

        // todo we have to use redis to check out state and see if its the same. ( implement this after caching the project)

        if (Objects.nonNull(code)) {
            log.info("[{}] user accepted the authorization request and logged in successful ", sessionId);
            spotifyService.exchangeCodeForToken(code, sessionId);
            // simple text response for demo; you'd redirect to your front
            return ResponseEntity.ok("Spotify connected. you can close this tab and return to your app");

        } else {
            log.error("[{}] user denied our application access to their resource", sessionId);
            return ResponseEntity.unprocessableEntity().build();
        }
    }


    @GetMapping("/playing")
    public ResponseEntity<Res> nowPlaying(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to get currently playing data from spotify", sessionId);

        Res<CurrentlyPlaying> response = spotifyService.retrieveCurrentlyPlaying();

        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/queue")
    public ResponseEntity<Res<QueueResponse>> hello(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to get current queue from spotify", sessionId);

        Res<QueueResponse> response = spotifyService.retrieveUserQueue();

        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());

        return ResponseEntity.ok(response);
    }
 }
