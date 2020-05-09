package dev.biomfire.homeserver.torrent.WebCrawlers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Torrent {
    String name;
    String magnetURL;
}
