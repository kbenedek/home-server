package dev.biomfire.homeserver.utils.OnlineAPIWrappers.MALWrapper;

import com.github.doomsdayrs.jikan4java.core.Connector;
import com.github.doomsdayrs.jikan4java.core.search.animemanga.AnimeSearch;
import com.github.doomsdayrs.jikan4java.types.main.anime.animePage.AnimePageAnime;
import dev.biomfire.homeserver.model.Anime;
import dev.biomfire.homeserver.utils.OnlineAPIWrappers.IOnlineAnimeAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class MALWrapper implements IOnlineAnimeAPI {
    public static Anime getAnime(com.github.doomsdayrs.jikan4java.types.main.anime.Anime MALAnime){
        return new Anime(MALAnime.mal_id, MALAnime.title, MALAnime.score, MALAnime.synopsis, null, MALAnime.imageURL);
    }
    @Override
    public Anime findAnimeByMALID(Integer id) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<com.github.doomsdayrs.jikan4java.types.main.anime.Anime> animePromise = new Connector().retrieveAnime(id);
        return getAnime(animePromise.get(5, TimeUnit.SECONDS));
    }
    public List<Anime> findTopTenAnimeByTitle(String title) throws InterruptedException, ExecutionException, TimeoutException {
        ArrayList<AnimePageAnime> animes =  new AnimeSearch().setQuery(title).setLimit(10).get().get(1, TimeUnit.SECONDS).animes;
        List<Anime> returnArray = new ArrayList<>();
        for(AnimePageAnime anime : animes){
            Thread.sleep(500);
            returnArray.add(findAnimeByMALID(anime.mal_id));

        }
        return returnArray;
    }
}
