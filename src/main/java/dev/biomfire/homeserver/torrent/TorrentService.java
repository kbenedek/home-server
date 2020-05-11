package dev.biomfire.homeserver.torrent;

import dev.biomfire.homeserver.torrent.WebCrawlers.Torrent;
import dev.biomfire.homeserver.torrent.WebCrawlers.TorrentCrawler;
import lombok.extern.slf4j.Slf4j;
import org.libtorrent4j.*;
import org.libtorrent4j.alerts.AddTorrentAlert;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.MetadataReceivedAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TorrentService {
    TorrentCrawler torrentCrawler;

    @Autowired
    public TorrentService(TorrentCrawler torrentCrawler) {
        this.torrentCrawler = torrentCrawler;
    }

    public List<Torrent> searchAnime(String searchTerm) {
        return torrentCrawler.searchAnime(searchTerm);
    }
    //TODO:Taken from example code can be make nicer
    public List<String> downloadMagnetLink(String magnetLink, String savePath){
        final CountDownLatch signal = new CountDownLatch(1);
        final SessionManager s = new SessionManager();
        List<String> paths = new ArrayList<>();
        AlertListener l = new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                switch (alert.type()) {
                    case ADD_TORRENT:
                        log.info("Torrent added");
                        TorrentHandle th = ((AddTorrentAlert) alert).handle();
                        th.resume();
                        break;
                    case METADATA_RECEIVED:
                        th = ((MetadataReceivedAlert) alert).handle();
                        TorrentInfo ti = th.torrentFile();
                        Priority[] p = th.filePriorities();
                        p[0] = Priority.DEFAULT;

                        log.info("Expected priorities:");
                        for (int i = 0; i < ti.numFiles(); i++) {
                            log.info(String.format("priority=%-8sfile=%s",
                                    Priority.IGNORE,
                                    ti.files().filePath(i)));
                            log.info(savePath +"/"+ ti.files().filePath(i));
                            paths.add(savePath +"/"+ ti.files().filePath(i));
                        }
                        th.prioritizeFiles(p);
                        break;
                    case TORRENT_FINISHED:
                        log.info("Torrent finished\n");
                        signal.countDown();
                        break;
                    default:
                        break;
                }
            }
        };
        s.addListener(l);
        try {
            s.start();

            waitForNodesInDHT(s);

            log.info("About to download magnet: " + magnetLink);
            s.download(magnetLink, Paths.get(savePath).toFile());
            signal.await(10, TimeUnit.SECONDS);
            log.info("Session stopped");
            s.stop();
            log.info(paths.toString());
        } catch (Throwable ignored) {
        }
        return paths;
    }

    private static void waitForNodesInDHT(final SessionManager s) throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long nodes = s.stats().dhtNodes();
                if (nodes >= 10) {
                    log.info("DHT contains " + nodes + " nodes");
                    signal.countDown();
                    timer.cancel();
                }
            }
        }, 0, 1000);

        log.info("Waiting for nodes in DHT (10 seconds)...");
        boolean r = signal.await(10, TimeUnit.SECONDS);
        if (!r) {
            log.info("DHT bootstrap timeout");
            System.exit(0);
        }
    }
}
