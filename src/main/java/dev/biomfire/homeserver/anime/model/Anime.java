package dev.biomfire.homeserver.anime.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anime {

    private @Id String id;
    private Integer malID;
    private String title;

    private Double score;
    private String synopsis;

    private String location;
    private String imageLocation;

    public Anime(Integer malID, String title, Double score, String synopsis, String location, String imageLocation) {
        this.malID = malID;
        this.title = title;
        this.score = score;
        this.synopsis = synopsis;
        this.location = location;
        this.imageLocation = imageLocation;
    }
}
