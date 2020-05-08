package dev.biomfire.homeserver.anime;


import dev.biomfire.homeserver.anime.model.Anime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnimeHeaderRepository extends MongoRepository<Anime, String> {
    Anime findByMalID(Integer id);
}