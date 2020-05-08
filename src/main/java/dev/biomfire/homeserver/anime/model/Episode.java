package dev.biomfire.homeserver.anime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
public class Episode {
    @Id
    private String id;
    private String title;
    private String synopsis;
    private String location;
}
