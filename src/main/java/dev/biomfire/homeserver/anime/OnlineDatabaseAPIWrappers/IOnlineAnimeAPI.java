package dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers;


import dev.biomfire.homeserver.anime.model.Anime;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOnlineAnimeAPI {
    List<Integer> searchAnime(String query);
    Anime getAnimeByID(Integer id);
}
