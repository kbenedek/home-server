package dev.biomfire.homeserver.anime.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Anime {
    public enum Genres {
        ACTION,
        DEMONS,
        HAREM,
        KIDS,
        ROMANCE,
        SHOUJO,
        SPACE,
        VAMPIRE,
        ADVENTURE,
        DRAMA,
        MAGIC,
        MYSTERY,
        SAMURAI,
        SHOUJO_AI,
        SPORTS,
        YAOI,
        CARS,
        ECCHI,
        HISTORICAL,
        MARTIAL_ARTS,
        PARODY,
        SHOUNEN,
        SUPER_POWER,
        YURI,
        COMEDY,
        FANTASY,
        HORROR,
        MECHA,
        POLICE,
        SCI_FI,
        SHOUNEN_AI,
        SUPERNATURAL,
        DEMENTIA,
        GAME,
        JOSEI,
        MILITARY,
        PSYCHOLOGICAL,
        SEINEN,
        SLICE_OF_LIFE,
        THRILLER,
        MUSIC,
        SCHOOL,
        HENTAI
    }

    private @Id String id;
    private @Indexed Integer malID;
    private String title;
    private Integer popularity;

    private Double score;
    private String imageLocation;
    private List<Genres> genres;

    public enum Status {
        AIRING,
        FINISHED,
        UPCOMING
    }

    private Status status;
    private String synopsis;
    //Not necessarly equal to episodes.size(), as we may know how many episodes there will be, but they are not out yet
    private Integer numberOfEpisodes;
    private List<Episode> episodes;
}
