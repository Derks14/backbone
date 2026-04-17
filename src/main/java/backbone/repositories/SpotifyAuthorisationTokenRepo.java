package backbone.repositories;

import backbone.models.spotify.SpotifyAuthorisationToken;
import backbone.models.spotify.SpotifyToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface SpotifyAuthorisationTokenRepo extends MongoRepository<SpotifyToken, String> {

    Optional<SpotifyToken> findFirstByOrderByCreatedAsc();
    Optional<SpotifyToken> findFirstByOrderByIdAsc();
    Optional<SpotifyToken> findFirstByOrderByIdDesc();
}
