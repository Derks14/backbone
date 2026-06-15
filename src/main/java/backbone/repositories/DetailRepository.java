package backbone.repositories;

import backbone.models.Detail;
import backbone.models.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DetailRepository extends MongoRepository<Detail, String> {
    Page<Detail> findAllBy(TextCriteria criteria, Pageable pageable);

}
