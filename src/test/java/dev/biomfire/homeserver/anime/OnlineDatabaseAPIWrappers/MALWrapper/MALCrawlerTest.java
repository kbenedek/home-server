package dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.MALWrapper;

import dev.biomfire.homeserver.anime.model.Anime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MALCrawlerTest {
    MALCrawler crawler = new MALCrawler();

    @Test
    void searchAnime() {
        assertNull(crawler.searchAnime("", 20));
    }


    //It is normal if this changes, check the website, if it is not the same we have a problem.
    //This test takes a long time beacuse of episode parsing
    @Test
    void test_getAnime_OnePiece() {
        Anime anime = crawler.getAnimeByID(21);
        assertEquals(anime.getTitle(), "One Piece");
        assertNotNull(anime.getScore());
        assertTrue(anime.getScore() > 0 && anime.getScore() < 10);
        assertEquals(anime.getImageLocation(), "https://cdn.myanimelist.net/images/anime/6/73245.jpg");
        assertEquals(anime.getSynopsis(), "Gol D. Roger was known as the \"Pirate King,\" the strongest and most infamous being to have sailed the Grand Line. The capture and execution of Roger by the World Government brought a change throughout the world. His last words before his death revealed the existence of the greatest treasure in the world, One Piece. It was this revelation that brought about the Grand Age of Pirates, men who dreamed of finding One Piece—which promises an unlimited amount of riches and fame—and quite possibly the pinnacle of glory and the title of the Pirate King.\n" +
                "\n" +
                "Enter Monkey D. Luffy, a 17-year-old boy who defies your standard definition of a pirate. Rather than the popular persona of a wicked, hardened, toothless pirate ransacking villages for fun, Luffy’s reason for being a pirate is one of pure wonder: the thought of an exciting adventure that leads him to intriguing people and ultimately, the promised treasure. Following in the footsteps of his childhood hero, Luffy and his crew travel across the Grand Line, experiencing crazy adventures, unveiling dark mysteries and battling strong enemies, all in order to reach the most coveted of all fortunes—One Piece.\n" +
                "\n" +
                "[Written by MAL Rewrite]");
        //We don't know how many episodes are there. It may change in the future.
        assertNull(anime.getNumberOfEpisodes());
        assertEquals(anime.getStatus(), Anime.Status.AIRING);
        assertArrayEquals(anime.getGenres().toArray(), new Anime.Genres[]{Anime.Genres.ACTION, Anime.Genres.ADVENTURE, Anime.Genres.COMEDY, Anime.Genres.SUPER_POWER, Anime.Genres.DRAMA, Anime.Genres.FANTASY, Anime.Genres.SHOUNEN});
        assertEquals(anime.getPopularity(), 1029813, 10000 );
    }

    @Test
    void test_getAnime_Cowboybebop() {
        Anime anime = crawler.getAnimeByID(1);
        assertEquals(anime.getNumberOfEpisodes(), 26);
        assertEquals(anime.getStatus(), Anime.Status.FINISHED);
        assertArrayEquals(anime.getGenres().toArray(), new Anime.Genres[]{Anime.Genres.ACTION, Anime.Genres.ADVENTURE, Anime.Genres.COMEDY, Anime.Genres.DRAMA, Anime.Genres.SCI_FI, Anime.Genres.SPACE});
        assertEquals(anime.getPopularity(), 1007170, 10000);
    }
}