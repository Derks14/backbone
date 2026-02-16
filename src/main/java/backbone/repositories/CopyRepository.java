package backbone.repositories;


import backbone.models.Copy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopyRepository extends MongoRepository<Copy, String> {
    Page<Copy> findAllBy(TextCriteria criteria, Pageable pageable);
}
