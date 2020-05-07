package dev.biomfire.homeserver.anime;

import dev.biomfire.homeserver.anime.WebCrawlers.NyaaCrawler.NyaaCrawler;
import dev.biomfire.homeserver.anime.WebCrawlers.TorrentResults;
import dev.biomfire.homeserver.anime.model.Anime;
import dev.biomfire.homeserver.anime.OnlineAPIWrappers.IOnlineAnimeAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Long.min;

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
        if (foundanime != null) {
            return new ResponseEntity<>(foundanime, HttpStatus.OK);
        } else {
            foundanime = getAnimeFromOnline(malID);
            if (foundanime != null) {
                return new ResponseEntity<>(foundanime, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/list")
    ResponseEntity<List<Anime>> listAllLocalAnime() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Anime>> searchAnime(@RequestParam(value = "q") String title) {
        try {
            return new ResponseEntity<>(onlineAnimeAPI.findTopTenAnimeByTitle(title), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchtorrent")
    public ResponseEntity<TorrentResults> searchTorrent(@RequestParam(value = "q") String searchTerm) {
        TorrentResults returnValue = new NyaaCrawler().searchAnime(searchTerm);
        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

    @GetMapping("/downloadtorrent")
    public ResponseEntity<?> downloadTorrent(@RequestParam(value = "q") String searchTerm, @RequestParam(value = "hash") Integer hash, @RequestParam Integer index) {
        TorrentResults returnValue = new NyaaCrawler().searchAnime(searchTerm);
        String magnetLink = returnValue.getTorrentList().get(index).getMagnetURL();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("play/{id}/full")
    public ResponseEntity<UrlResource> getFullVideo(@PathVariable String id) {
        UrlResource video = null;
        try {
            video = new UrlResource("file:/home/biomfire/Downloads/test.mp4");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(video);
    }

    @GetMapping("play/{id}")
    public ResponseEntity<ResourceRegion> getVideo(@PathVariable String id, @RequestHeader HttpHeaders headers) throws IOException {
        UrlResource video = new UrlResource("file:/home/biomfire/Downloads/test.mp4");
        ResourceRegion region = resourceRegion(video, headers);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }

    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
        long contentLength = video.contentLength();
        if(!headers.getRange().isEmpty()){
            HttpRange range = headers.getRange().get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = min(1000000L, end-start+1);
            return new ResourceRegion(video, start, rangeLength);
        }
        else{
            long rangeLength = min(1000000L, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }

    }

    private Anime getAnimeFromOnline(Integer id) {
        try {
            return onlineAnimeAPI.findAnimeByMALID(id);
        } catch (Exception e) {
            return null;
        }
    }
}
