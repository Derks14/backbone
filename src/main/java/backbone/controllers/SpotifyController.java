package backbone.controllers;


import backbone.models.spotify.CurrentlyPlaying;
import backbone.services.SpotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/window")
@Slf4j
public class SpotifyController {



    public ResponseEntity<String> home(HttpServletRequest request){
        String sessionId = request.getSession().getId();

        return ResponseEntity.ok("ok spotify");
    }


    @GetMapping("/playing")
    public ResponseEntity<CurrentlyPlaying> nowPlaying(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to get currently playing data from spotify", sessionId);

//        CurrentlyPlaying currentlyPlaying = spotifyService.retrieveCurrentlyPlaying(sessionId);
        return ResponseEntity.ok(null);
    }
 }
