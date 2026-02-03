package backbone.repositories;


import backbone.models.Copy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopyRepository extends MongoRepository<Copy, String> {
}
