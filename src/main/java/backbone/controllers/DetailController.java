package backbone.controllers;


import backbone.dto.FetchRequest;
import backbone.dto.Res;
import backbone.models.Detail;
import backbone.models.DetailStatus;
import backbone.services.DetailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/details")
public class DetailController {
    private final DetailService detailService;

    public DetailController(DetailService detailService) {
        this.detailService = detailService;
    }

    @GetMapping
    public ResponseEntity<Res<List<Detail>>> fetch(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) DetailStatus published,
            HttpServletRequest request
    ) {
        String sessionId = request.getSession().getId();
        log.info("[{} new request to fetch details] ", sessionId);

        FetchRequest fetchRequest = FetchRequest.builder()
                .page(page)
                .size(size)
                .search(Strings.trimToNull(search))
                .build();

        Res<List<Detail>> response = detailService.fetchDetails(fetchRequest, published, sessionId);
        response.setTimestamp(Instant.now());
        return ResponseEntity.ok(response);
    }


    @GetMapping("{id}")
    public ResponseEntity<Res<Detail>> fetchDetail(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to fetch single detail ", sessionId);

        Detail detail = detailService.getDetail(id, sessionId);
        Res<Detail> response = new Res<>("single detail fetched successfully", detail, request.getRequestURI());

        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<Res<Detail>> createDetail(@RequestBody Detail detailDto, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to add new detail", sessionId);

        Detail detail = detailService.addDetail(detailDto, sessionId);
        Res<Detail> response = new Res<>("detail draft created successfully", detail, request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<Res<Detail>> updateDetail(@RequestBody Detail detail,
                                                      @PathVariable String id,
                                                      HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to update detail ", sessionId);

        Detail updatedDetail = this.detailService.updateDetail(detail, id, sessionId);

        Res<Detail> response = Res.<Detail>builder()
                .message("detail updated successfully")
                .data(updatedDetail)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<Res> deleteDetail(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to delete detail ", sessionId);
        Res response = this.detailService.deleteDetail(id, sessionId);
        return ResponseEntity.ok(response);
    }
}
