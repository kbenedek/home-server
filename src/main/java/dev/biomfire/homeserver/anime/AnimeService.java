package dev.biomfire.homeserver.anime;

import dev.biomfire.homeserver.AppConfig;
import dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.IOnlineAnimeAPI;
import dev.biomfire.homeserver.anime.data.AnimeRepository;
import dev.biomfire.homeserver.anime.model.Anime;
import dev.biomfire.homeserver.anime.model.Episode;
import dev.biomfire.homeserver.torrent.TorrentService;
import dev.biomfire.homeserver.torrent.WebCrawlers.Torrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class AnimeService {
    final private IOnlineAnimeAPI onlineAnimeAPI;
    final private AnimeRepository animeRepository;
    final private TorrentService torrentService;
    final private AppConfig appConfig;

    @Autowired
    public AnimeService(IOnlineAnimeAPI onlineAnimeAPI, AnimeRepository animeRepository, TorrentService torrentService, AppConfig appConfig) {
        this.onlineAnimeAPI = onlineAnimeAPI;
        this.animeRepository = animeRepository;
        this.torrentService = torrentService;
        this.appConfig = appConfig;
    }

    Page<Anime> searchAnime(String query, Integer pageNumber) {
        List<Integer> idList = onlineAnimeAPI.searchAnime(query, 20);
        for (Integer id : idList) {
            downloadAnimeDataIfNotAlreadyPresent(id);
        }
        Pageable request = PageRequest.of(pageNumber - 1, 10);
        return animeRepository.findByTitleContainingOrderByPopularityDesc(query, request);
    }

    Anime getAnime(Integer id) {
        downloadAnimeDataIfNotAlreadyPresent(id);
        return animeRepository.findByMalID(id);
    }

    void downloadAnimeDataIfNotAlreadyPresent(Integer id) {
        if (!animeRepository.existsByMalID(id)) {
            animeRepository.save(onlineAnimeAPI.getAnimeByID(id));
        }
    }

    void updateAnime(Integer id) {
        Anime oldAnime = getAnime(id);
        Anime anime = onlineAnimeAPI.getAnimeByID(id);
        anime.setId(oldAnime.getId());
        animeRepository.save(anime);
    }

    //TODO:Ne kelljen minden egyes alkalommal az egészet kiolvasni és visszamenteni, lehet ezt szebben is
    public void updateEpisode(Integer id, Integer episodeNumber) {
        Anime anime = getAnime(id);
        Episode newEpisode = onlineAnimeAPI.getEpisode(anime, episodeNumber);
        List<Episode> episodes = anime.getEpisodes();
        episodes.removeIf(episode -> episode.getEpisodeNumber().equals(newEpisode.getEpisodeNumber()));
        episodes.add(newEpisode);
        anime.setEpisodes(episodes);
        animeRepository.save(anime);
    }

    public Episode getEpisode(Integer id, Integer episodeNumber) {
        List<Episode> results = animeRepository.findEpisodeByMalIDAndEpisodeNumber(id, episodeNumber);
        if (results.size() == 0) {
            downloadEpisodeData(id, episodeNumber);
            results = animeRepository.findEpisodeByMalIDAndEpisodeNumber(id, episodeNumber);
        }
        if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new RuntimeException("Error getting episode %d %d ".formatted(id, episodeNumber));
        }
    }

    //TODO:Ne kelljen minden egyes alkalommal az egészet kiolvasni és visszamenteni, lehet ezt szebben is
    //TODO:Jelenleg ugyanaz mint az updateepizód, de mást akarunk csinálni ha optimalizálunk
    @SuppressWarnings("Duplicates")
    void downloadEpisodeData(Integer id, Integer episodeNumber) {
        Anime anime = getAnime(id);
        Episode newEpisode = onlineAnimeAPI.getEpisode(anime, episodeNumber);
        List<Episode> episodes = anime.getEpisodes();
        episodes.removeIf(episode -> episode.getEpisodeNumber().equals(newEpisode.getEpisodeNumber()));
        episodes.add(newEpisode);
        anime.setEpisodes(episodes);
        animeRepository.save(anime);
    }
    //TODO:Szűrjünk epizód szerint, ne az összes epizódot töltsük le
    public void downloadEpisodeVideo(Integer id, Integer episodeID) {
        Anime anime = getAnime(id);
        List<Torrent> results = torrentService.searchAnime(anime.getTitle());
        //TODO:Ne csak az első eredményt válasszuk ki, legyen benne valami logika
        String magnetLink = results.get(0).getMagnetURL();
        List<String> downloadedFiles= torrentService.downloadMagnetLink(magnetLink, appConfig.getTorrentBaseSavePath());
        /*TODO:Jelenleg eltávolítunk az eredmények közül mindent ami nem mp4vagy mkv,
            és benne van hogy Ending vagy Opening vagy Extras mappában van.
            Ezt lehetne jobban is csinálni.
         */
        downloadedFiles.removeIf(fileName -> fileName.matches("(^.*(?<!\\.mp4)(?<!\\.mkv)$|(^.*([Ee]nding|[Oo]pening|Extras/).*$))"));
        //TODO:Jelenleg csak a sorrendben adott számú file-hoz rendeljük a tömbeli elemet.
        //  Ezt is okosabban kéne csinálni
        if(anime.getNumberOfEpisodes() == downloadedFiles.size()){
            List<Episode> episodesWithLocation = anime.getEpisodes();
            for (int i = 0; i < episodesWithLocation.size(); i++){
                episodesWithLocation.get(i).setLocation(downloadedFiles.get(i));
            }
            anime.setEpisodes(episodesWithLocation);
            animeRepository.save(anime);
        }
        else{
            throw new RuntimeException("Can't process downloaded torrent\n" + downloadedFiles.size() +" "+ anime.getNumberOfEpisodes() + downloadedFiles.toString());
        }
    }
}
