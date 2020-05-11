package dev.biomfire.homeserver.torrent;

import dev.biomfire.homeserver.AppConfig;
import dev.biomfire.homeserver.torrent.WebCrawlers.Torrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/api/torrent")
public class TorrentController {
    AppConfig config;
    TorrentService torrentService;

    @Autowired
    public TorrentController(TorrentService torrentService) {
        this.torrentService = torrentService;
    }


    @GetMapping("/searchtorrent")
    public ResponseEntity<List<Torrent>> searchTorrent(@RequestParam(value = "q") String searchTerm) {
        List<Torrent> returnValue = torrentService.searchAnime(searchTerm);
        return new ResponseEntity<>(returnValue, HttpStatus.OK);
    }

    @GetMapping("/download")
    public ResponseEntity<Integer> downloadTorrent(@RequestParam(value = "m") String magnetLink) {
        torrentService.downloadMagnetLink(magnetLink, config.getTorrentBaseSavePath());
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


}
