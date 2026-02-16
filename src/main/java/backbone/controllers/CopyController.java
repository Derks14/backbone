package backbone.controllers;

import backbone.dto.CopyDto;
import backbone.dto.CopyRequest;
import backbone.dto.Res;
import backbone.models.Copy;
import backbone.services.CopyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/copy")
public class CopyController {

    private final CopyService copyService;

    public CopyController(CopyService copyService) {
        this.copyService = copyService;
    }


    @GetMapping
    public ResponseEntity<Res<List<Copy>>> fetch( @RequestParam(required = false) String search,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "ASC") String sortDirection,
                                      HttpServletRequest request) {

        String sessionId = request.getSession().getId();
        log.info("[{}] request to fetch copies", sessionId);

        CopyRequest copyRequest = new CopyRequest(search, page, size, sortDirection);

        Res<List<Copy>> response = this.copyService.fetchCopies(copyRequest, sessionId);
        response.setPath(request.getRequestURI());
        response.setTimestamp(Instant.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<Res<Copy>> fetchSingleCopy(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to fetch single copy", sessionId);
        Copy copy = copyService.getCopy(id, sessionId);
        Res<Copy> response = new Res<>("copy fetched successfully", copy, request.getRequestURI());
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<Res<Copy>> addCopy(@RequestBody @Valid CopyDto copyDto, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to add new copies ", sessionId);
        Copy copy = this.copyService.addCopy(copyDto, sessionId);
        Res<Copy> response = new Res<>("", copy, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<Res<Copy>> updateCopy(@RequestBody @Valid CopyDto copyDto,
                                                @PathVariable String id,
                                                HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to update copy", sessionId);
        Copy copy = this.copyService.updateCopy(copyDto, id, sessionId);
        Res<Copy> res = Res.<Copy>builder()
                .message("copy successfully updated")
                .data(copy)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(res);
    }


    @DeleteMapping
    public ResponseEntity<Res> delete(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to delete copy", sessionId);
        Res response = this.copyService.deleteCopy(id, sessionId);
        return ResponseEntity.ok(response);
    }
}
