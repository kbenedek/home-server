package dev.biomfire.homeserver.anime.WebCrawlers;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class TorrentResults {
    @Getter
    List<Torrent> torrentList;
    @Getter
    Integer hash;

    public void setTorrentList(List<Torrent> torrentList) {
        this.torrentList = torrentList;
        this.hash = torrentList.hashCode();
    }

    public TorrentResults(List<Torrent> torrentList) {
        this.torrentList = torrentList;
        this.hash = torrentList.hashCode();
    }
}
