package dev.biomfire.homeserver.torrent.WebCrawlers;


import java.util.List;

public interface TorrentCrawler {
     List<Torrent> searchAnime(String searchTerm);
}
