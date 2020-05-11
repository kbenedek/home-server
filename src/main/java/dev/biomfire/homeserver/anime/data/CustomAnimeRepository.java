package dev.biomfire.homeserver.anime.data;

import dev.biomfire.homeserver.anime.model.Episode;

import java.util.List;

public interface CustomAnimeRepository {
    List<Episode> findEpisodeByMalIDAndEpisodeNumber(Integer id, Integer EpisodeNumber);
}
