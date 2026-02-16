package backbone.services;

import backbone.configs.BackboneException;
import backbone.dto.*;
import backbone.models.Copy;
import backbone.models.records.CopyStatus;
import backbone.repositories.CopyRepository;
import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CopyService {
    private final CopyRepository copyRepository;

    public CopyService(CopyRepository repository) {
        this.copyRepository = repository;
    }

    public Res<List<Copy>> fetchCopies(CopyRequest request, String sessionId) {
        log.info("[{}] processing request to fetch copies ", sessionId);

        PageRequest pageRequest = PageRequest.of(request.page(), request.size());

        // when you add authentication
        // add option where everyone sees published copies and authenticated users see published items

        Page<Copy> copiesInPages;

        try {
            if (Objects.isNull(request.search())) {
                copiesInPages = copyRepository.findAll(pageRequest);
            }
            else {
                log.info("[{}] full text search for request processing - searching : {}  ", sessionId, request.search());
                TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(request.search());

                Sort sort = Sort.by("score");
                pageRequest.withSort(sort);
                copiesInPages = copyRepository.findAllBy(criteria, pageRequest);
            }
        } catch (Exception e) {
            log.error("[{}] ", sessionId);
            throw new BackboneException("special code", HttpStatus.INTERNAL_SERVER_ERROR, "process to fetch copies from store failed", e.getCause());
        }
        PaginationMeta pagination = PaginationMapper.from(copiesInPages);

        List<Copy> copies = copiesInPages.getContent();

        return Res.<List<Copy>>builder()
                .message("copies fetched successfully")
                .data(copies)
                .pagination(pagination)
                .build();
    }

    // add copy
    public Copy addCopy(CopyDto copyDto, String sessionId) {
        log.info("[{}] processing request add new copy for the page ", sessionId);
        Copy copy = Copy.builder()
                .title(copyDto.getTitle())
                .icon(copyDto.getIcon())
                .tags(copyDto.getTags())
                .status(CopyStatus.DRAFT)
                .description(copyDto.getDescription())
                .category(copyDto.getCategory())
                .build();
        try {
           copy = copyRepository.insert(copy);
        } catch (DuplicateKeyException e) {
            log.error("[{}] duplicate key exception : {}", sessionId, e.getMessage());
            throw new BackboneException("", HttpStatus.CONFLICT, "copy with title already exists", e.getCause());
        }
        return copy;
    }


    // update copy
    public Copy updateCopy(CopyDto newData, String id, String sessionId) {
        log.info("[{}] processing update requests to update copy", sessionId);

        Copy copy = copyRepository.findById(id)
                .orElseThrow( () -> new BackboneException("", HttpStatus.NOT_FOUND, "copy with id: %s, could not be found"));

        Copy newCopy = Copy.builder()
                .category(newData.getCategory())
                .title(newData.getTitle())
                .description(newData.getDescription())
                .status(newData.getStatus())
                .tags(newData.getTags())
                .icon(newData.getIcon())
                .build();

        newCopy.setId(copy.getId());

        copy = copyRepository.save(newCopy);

        log.info("[{}] new copy updated successfully ", sessionId);

        // go ahead and delete cache

        return copy;
    }

    public Copy getCopy(String id, String sessionId) {
        log.info("[{}] processing request to get single copy", sessionId);

        Copy copy = copyRepository.findById(id)
                .orElseThrow( () -> new BackboneException("", HttpStatus.NOT_FOUND, "copy with id: %s, could not be found ".formatted(id)) );

        log.info("[{}] copy found and return ", sessionId);
        return copy;
    }


    public Res deleteCopy(String id, String sessionId) {
        // this should clear cache
        log.info("[{}] processing requests to delete copy ", sessionId);

        Copy copy = copyRepository.findById(id)
                .orElseThrow( () -> new BackboneException("", HttpStatus.NOT_FOUND, "copy with id: %s, could not be found".formatted(id)));

        // future scope update the document instead and put in a scheduler that deletes all those documents after 30 days
        // read on where we should do this in the application or create a cron job that does this in the database
        // or maybe we can create this service in the database

        copyRepository.delete(copy);
        log.info("[{}] old copy deleted successfully", sessionId );

        return Res.builder()
                .message("copy deleted successfully")
                .build();
    }

}
