package dev.biomfire.homeserver.WebCrawlers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Torrent {
    String name;
    String magnetURL;
}
