package backbone.services;

import backbone.configs.BackboneException;
import backbone.dto.FetchRequest;
import backbone.dto.PaginationMapper;
import backbone.dto.PaginationMeta;
import backbone.dto.Res;
import backbone.models.Detail;
import backbone.models.DetailStatus;
import backbone.models.enums.ContentFormat;
import backbone.repositories.DetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DetailService {
    private final DetailRepository repository;
    private final MongoTemplate mongoTemplate;

    public DetailService(DetailRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public Res<List<Detail>> fetchDetails(FetchRequest request, DetailStatus published, String sessionId) {
        log.info("[{}] processing request to fetch details", sessionId);
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Query query;


        try {
            if (Objects.nonNull(request.getSearch())) {
                TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(request.getSearch());
                query = TextQuery.queryText(criteria);

                ((TextQuery) query).sortByScore();
            } else {
                query = new Query();
            }

            if (Objects.nonNull(published)) {
                query.addCriteria(Criteria.where("published").is(published));
            }

            query.with(pageRequest);

            List<Detail> results = mongoTemplate.find(query, Detail.class);

            Page<Detail> detailsInPages = PageableExecutionUtils.getPage(
                    results,
                    pageRequest,
                    () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Detail.class));

            PaginationMeta paginationMeta = PaginationMapper.from(detailsInPages);
            List<Detail> details = detailsInPages.getContent();

            log.info("[{}] details fetched successfully: pagination {}. data: {}", sessionId, paginationMeta, details);

            return Res.<List<Detail>>builder()
                    .message("details fetched successfully")
                    .data(results)
                    .pagination(paginationMeta)
                    .build();


        } catch (Exception e) {
            throw new BackboneException(
                    "fetch_from_store_error",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "process to fetch details from store failed",
                    e.getCause()
            );
        }


    }

    public Detail getDetail(String id, String sessionId) {
        log.info("[{}] processing request to fetch single detail", sessionId);

        Detail detail = repository.findById(id)
                .orElseThrow(() -> new BackboneException(
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND,
                        "detail with Id [%s] could not be found".formatted(id)
                ));

        log.info("[{}] detail with title: {}. found successfully", sessionId, detail.getTitle());
        return detail;
    }

    public Detail addDetail(Detail detail, String sessionId) {
        log.info("[{}] processing request to add detail", sessionId);

        Detail newDetail = new Detail();
        newDetail.setTitle(detail.getTitle());
        newDetail.setSubtitle(detail.getSubtitle());
        newDetail.setType(detail.getType());
        newDetail.setContentFormat(detail.getContentFormat() == null ? ContentFormat.MARKDOWN : detail.getContentFormat());
        newDetail.setContent(detail.getContent());
        newDetail.setStatus(DetailStatus.DRAFT);
        newDetail.setRelevance(detail.getRelevance());

        try {
            newDetail = repository.insert(newDetail);
        } catch (Exception e) {
            throw new BackboneException(
                    "create_detail_failed",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "detail creation failed",
                    e.getCause()
            );
        }

        log.info("[{}] new detail: {}, created successfully", sessionId, newDetail.getTitle());
        return newDetail;
    }

    public Detail updateDetail(Detail newData, String id, String sessionId) {
        log.info("[{}] processing request to update detail", sessionId);

        Detail detail = repository.findById(id)
                .orElseThrow(() -> new BackboneException(
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND,
                        "detail with id [%s] could not be found".formatted(id)
                ));

        try {
            detail.setTitle(newData.getTitle());
            detail.setSubtitle(newData.getSubtitle());
            detail.setType(newData.getType());
            detail.setContentFormat(newData.getContentFormat());
            detail.setContent(newData.getContent());
            detail.setRelevance(newData.getRelevance());
            detail.setStatus(newData.getStatus());

            detail = repository.save(detail);
        } catch (Exception e) {
            throw new BackboneException(
                    "detail_update_failed",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "detail update failed",
                    e.getCause()
            );
        }

        log.info("[{}] detail: {}, has been successfully updated", sessionId, detail.getTitle());
        return detail;
    }

    public Res deleteDetail(String id, String sessionId) {
        log.info("[{}] processing request to delete detail", sessionId);

        Detail detail = repository.findById(id)
                .orElseThrow(() -> new BackboneException(
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        HttpStatus.NOT_FOUND,
                        "detail with id: %s, could not be found".formatted(id)
                ));

        repository.delete(detail);

        log.info("[{}] detail deleted successfully", sessionId);
        return Res.builder().message("detail details deleted successfully").build();
    }
}
