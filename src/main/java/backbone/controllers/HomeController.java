package backbone.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class HomeController {

    @GetMapping
    public ResponseEntity<String> home(HttpServletRequest request) {
        String sessionId = request.getSession().getId();

        log.info("[{}] someone just hit the home server ", sessionId);

        return ResponseEntity.ok("Welcome Home Buddy");
    }
}
