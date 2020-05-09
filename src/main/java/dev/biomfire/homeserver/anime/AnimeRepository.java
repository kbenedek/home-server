package dev.biomfire.homeserver.anime;


import dev.biomfire.homeserver.anime.model.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface AnimeRepository extends MongoRepository<Anime, String> {
    Anime findByMalID(Integer id);
    Boolean existsByMalID(Integer id);
    Page<Anime> findByTitleContainingOrderByPopularityDesc(String title, Pageable pageable);
    List<Anime> findAll();
}
