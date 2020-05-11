package dev.biomfire.homeserver.anime;

import dev.biomfire.homeserver.anime.model.Anime;
import dev.biomfire.homeserver.anime.model.Episode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;

@Controller
@RequestMapping("/api/anime")
@Slf4j
public class AnimeController {
    final private AnimeService animeService;

    @Autowired
    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Anime>> getAnime(@RequestParam(value = "q") String query,
                                                @RequestParam(value = "p", defaultValue = "1") Integer pageNumber) {
        try {
            return new ResponseEntity<>(animeService.searchAnime(query, pageNumber), HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Anime> getAnimeByID(@PathVariable Integer id,
                                              @RequestParam(name = "u", defaultValue = "false") Boolean update) {
        if (update) {
            animeService.updateAnime(id);
        }
        return new ResponseEntity<>(animeService.getAnime(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/{episodeNumber}")
    public ResponseEntity<Episode> getEpisodeByID(@PathVariable Integer id,
                                                  @PathVariable Integer episodeNumber,
                                                  @RequestParam(name = "u",defaultValue = "false") Boolean forceupdate){
        if(forceupdate) {
            animeService.updateEpisode(id, episodeNumber);
        }
        return new ResponseEntity<>(animeService.getEpisode(id, episodeNumber),HttpStatus.OK);
    }

    @GetMapping("/{id}/{episodeID}/play/full")
    public ResponseEntity<UrlResource> getFullVideo(@PathVariable Integer id, @PathVariable Integer episodeID) {
        Episode episode = animeService.getEpisode(id, episodeID);
        if(episode.getLocation().equals("")){
            log.info("Downloading: %d %d".formatted(id, episodeID));
            animeService.downloadEpisodeVideo(id, episodeID);
        }
        UrlResource video = null;
        try {
            video = new UrlResource("file:"+episode.getLocation());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(video);
    }

    //TODO:Ne az egész file-t játszuk le mindig
//    @GetMapping("{id}/{episodeID}/play")
//    public ResponseEntity<ResourceRegion> getVideo(@PathVariable String id, @RequestHeader HttpHeaders headers) throws IOException {
//        UrlResource video = new UrlResource("file:/home/biomfire/Downloads/test.mp4");
//        ResourceRegion region = resourceRegion(video, headers);
//        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
//                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
//                .body(region);
//    }
//
//    private ResourceRegion resourceRegion(UrlResource video, HttpHeaders headers) throws IOException {
//        long contentLength = video.contentLength();
//        if(!headers.getRange().isEmpty()){
//            HttpRange range = headers.getRange().get(0);
//            long start = range.getRangeStart(contentLength);
//            long end = range.getRangeEnd(contentLength);
//            long rangeLength = min(1000000L, end-start+1);
//            return new ResourceRegion(video, start, rangeLength);
//        }
//        else{
//            long rangeLength = min(1000000L, contentLength);
//            return new ResourceRegion(video, 0, rangeLength);
//        }
//
//    }
//
}
