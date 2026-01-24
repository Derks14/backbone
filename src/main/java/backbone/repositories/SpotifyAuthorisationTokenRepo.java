package backbone.repositories;

import backbone.models.spotify.SpotifyAuthorisationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotifyAuthorisationTokenRepo extends MongoRepository<SpotifyAuthorisationToken, String> {
}
