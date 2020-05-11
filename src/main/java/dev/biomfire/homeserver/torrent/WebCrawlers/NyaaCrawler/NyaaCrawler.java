package dev.biomfire.homeserver.torrent.WebCrawlers.NyaaCrawler;

import dev.biomfire.homeserver.torrent.WebCrawlers.Torrent;
import dev.biomfire.homeserver.torrent.WebCrawlers.TorrentCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NyaaCrawler implements TorrentCrawler {

    @Override
    public List<Torrent> searchAnime(String searchTerm) {
        try {
            return trySearchAnime(searchTerm);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private @NotNull List<Torrent> trySearchAnime(@NotNull String searchTerm) throws IOException {
        Document doc = Jsoup.connect("https://nyaa.si/?f=0&c=0_0&q=" + searchTerm + "&s=seeders&o=desc").get();
        Elements torrentRows = doc.select(".torrent-list tbody tr");
        return extractTitleAndMagnetFromTorrentRows(torrentRows);
    }

    private @NotNull List<Torrent> extractTitleAndMagnetFromTorrentRows(@NotNull Elements torrentRows) {
        ArrayList<Torrent> returnList = new ArrayList<>();
        for (Element torrentRow : torrentRows) {
            String torrentTitle = getTorrentTitle(torrentRow);
            String magnetLink = getMagnetLink(torrentRow);
            returnList.add(new Torrent(torrentTitle, magnetLink));
        }
        return returnList;
    }

    private String getMagnetLink(@NotNull Element torrentRow) {
        Element torrentMagnetLinkCell = torrentRow.child(2);
        return torrentMagnetLinkCell.child(1).attr("href");
    }

    private String getTorrentTitle(@NotNull Element torrentRow) {
        Element torrentTitleCell = torrentRow.child(1);
        String torrentTitle;
        if (torrentTitleCell.childNodeSize() == 3) {
            torrentTitle = torrentTitleCell.child(0).html();
        } else if (torrentTitleCell.childNodeSize() == 5) {
            torrentTitle = torrentTitleCell.child(1).html();
        } else {
            throw new RuntimeException();
        }
        return torrentTitle;
    }
}

