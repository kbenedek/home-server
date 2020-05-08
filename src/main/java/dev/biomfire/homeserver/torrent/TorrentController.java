package dev.biomfire.homeserver.torrent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.libtorrent4j.*;
import org.libtorrent4j.alerts.AddTorrentAlert;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.MetadataReceivedAlert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
@RequestMapping("/torrent")
@AllArgsConstructor
public class TorrentController {

    @GetMapping("/download")
    public ResponseEntity<Integer> downloadTorrent(@RequestParam(value = "m") String magnetLink) {
        final CountDownLatch signal = new CountDownLatch(1);
        final SessionManager s = new SessionManager();

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
                        for (int i = 0; i < ti.numFiles(); i++)
                            log.info(String.format("priority=%-8sfile=%s",
                                    p[i],
                                    ti.files().fileName(i)));
                        th.prioritizeFiles(p);
                        break;
                    case TORRENT_FINISHED:
                        log.info("Torrent finished\n");
                        signal.countDown();
                        break;
                }
            }
        };
        s.addListener(l);
        try {
            s.start();

            waitForNodesInDHT(s);

            log.info("About to download magnet: " + magnetLink);
            s.download(magnetLink, Paths.get("/home/biomfire/Downloads").toFile());
            signal.wait();
            log.info("Session stopped");
            s.stop();
        } catch (Throwable ignored) {
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
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
