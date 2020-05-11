package dev.biomfire.homeserver.anime.model;

import lombok.Data;


@Data
public class Episode {
    private Integer episodeNumber;
    private String title;
    private String synopsis;
    private String location;
}
