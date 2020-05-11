package dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers;


import dev.biomfire.homeserver.anime.model.Anime;
import dev.biomfire.homeserver.anime.model.Episode;

import java.util.List;

public interface IOnlineAnimeAPI {

    List<Integer> searchAnime(String query, Integer numberOfResults);

    Anime getAnimeByID(Integer id);

    Episode getEpisode(Anime anime, Integer episodeNumber);
}
