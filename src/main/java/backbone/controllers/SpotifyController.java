package backbone.controllers;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spotify")
@Slf4j
public class SpotifyController {

    public ResponseEntity<String> home(HttpServletRequest request){
        String sessionId = request.getSession().getId();

        return ResponseEntity.ok("ok spotify");
    }
 }
