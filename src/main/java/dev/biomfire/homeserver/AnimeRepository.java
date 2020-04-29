package dev.biomfire.homeserver;


import dev.biomfire.homeserver.model.Anime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnimeRepository extends MongoRepository<Anime, String> {
    Anime findByMalID(Integer id);
}
