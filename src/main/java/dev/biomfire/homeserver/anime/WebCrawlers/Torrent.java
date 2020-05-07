package dev.biomfire.homeserver.anime.WebCrawlers;

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
