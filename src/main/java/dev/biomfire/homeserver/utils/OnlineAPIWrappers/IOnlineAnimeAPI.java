package dev.biomfire.homeserver.utils.OnlineAPIWrappers;

import dev.biomfire.homeserver.model.Anime;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public interface IOnlineAnimeAPI {
    Anime findAnimeByMALID(Integer malID) throws InterruptedException, ExecutionException, TimeoutException;
    List<Anime> findTopTenAnimeByTitle(String title) throws InterruptedException, ExecutionException, TimeoutException;
}
