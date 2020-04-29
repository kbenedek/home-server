package dev.biomfire.homeserver;

import dev.biomfire.homeserver.WebCrawlers.NyaaCrawler.NyaaCrawler;
import dev.biomfire.homeserver.WebCrawlers.TorrentResults;
import dev.biomfire.homeserver.model.Anime;
import dev.biomfire.homeserver.utils.OnlineAPIWrappers.IOnlineAnimeAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/anime")
@Slf4j
public class AnimeController {
    @Autowired
    private AnimeRepository repository;
    @Autowired
    private IOnlineAnimeAPI onlineAnimeAPI;


    @GetMapping("")
    ResponseEntity<Anime> getAnimeID(@RequestParam Integer malID) {
        Anime foundanime = repository.findByMalID(malID);
        if(foundanime != null){
            return new ResponseEntity<>(foundanime, HttpStatus.OK);
        }
        else {
            foundanime = getAnimeFromOnline(malID);
            if(foundanime != null){
                return new ResponseEntity<>(foundanime, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }
    @GetMapping("/list")
    ResponseEntity<List<Anime>> listAllLocalAnime(){
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnime(@RequestParam(value="q") String title){
        try {
            return new ResponseEntity<>(onlineAnimeAPI.findTopTenAnimeByTitle(title), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/searchtorrent")
    public ResponseEntity<TorrentResults> searchTorrent(@RequestParam(value="q") String searchTerm){
        TorrentResults returnValue = new NyaaCrawler().searchAnime(searchTerm);
        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

    @GetMapping("/downloadtorrent")
    public ResponseEntity<?> downloadTorrent(@RequestParam(value="q") String searchTerm, @RequestParam(value = "hash") Integer hash, @RequestParam Integer index){
        TorrentResults returnValue = new NyaaCrawler().searchAnime(searchTerm);
            String magnetLink = returnValue.getTorrentList().get(index).getMagnetURL();
            return new ResponseEntity<>(HttpStatus.OK);
    }

    private Anime getAnimeFromOnline( Integer id) {
        try {
            return onlineAnimeAPI.findAnimeByMALID(id);
        } catch (Exception e) {
            return null;
        }
    }
}
