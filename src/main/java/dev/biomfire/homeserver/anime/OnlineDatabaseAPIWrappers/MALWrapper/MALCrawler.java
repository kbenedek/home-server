package dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.MALWrapper;

import dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.IOnlineAnimeAPI;
import dev.biomfire.homeserver.anime.model.Anime;
import dev.biomfire.homeserver.anime.model.Episode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MALCrawler implements IOnlineAnimeAPI {
    static final String searchURLTemplate = "https://myanimelist.net/anime.php?q=%s";
    static final String animeURLTemplate = "https://myanimelist.net/anime/%d/";
    static final String episodeURLTemplate = "https://myanimelist.net/anime/%d/%s/episode/%d";

    static private class MALIDFromURLExtractor {
        static final Pattern p = Pattern.compile("(?<=myanimelist\\.net/anime/)(\\d+(?=/))");

        static public Integer getID(String URL) {
            Matcher m = p.matcher(URL);
            if (m.find()) {
                return Integer.parseInt(m.group(0));
            } else {
                throw new RuntimeException("ID not found");
            }
        }
    }

    @Override
    public List<Integer> searchAnime(String query, Integer numberOfResults) {
        try {
            String searchURL = String.format(searchURLTemplate, query);
            Document doc = Jsoup.connect(searchURL).get();
            Elements animes_trs = doc.select(".list table tbody").first().children();
            //Table header solved in website with another tr, removing it
            animes_trs.remove(0);
            List<Integer> returnList = new ArrayList<>();
            for (int i = 0; i < animes_trs.size() && i < numberOfResults; i++) {
                Element listElement = animes_trs.get(i);
                String animeURL = listElement.child(0).child(0).child(0).attr("href");
                Integer malID = MALIDFromURLExtractor.getID(animeURL);
                returnList.add(malID);
            }
            return returnList;
        } catch (Exception ignored) {
            throw new RuntimeException("Can't search for Anime's, using query" +query +" and number of results" +numberOfResults);
        }
    }

    @Override
    public Anime getAnimeByID(Integer id) {
        try {
            Anime returnAnime = new Anime();
            String animeURL = String.format(animeURLTemplate, id);
            Document doc = Jsoup.connect(animeURL).get();

            returnAnime.setMalID(id);

            returnAnime.setTitle(getTitle(doc));

            returnAnime.setScore(getScore(doc));

            returnAnime.setImageLocation(getImageLocation(doc));

            returnAnime.setSynopsis(getSynopsis(doc));

            returnAnime.setNumberOfEpisodes(getAnimeNumberOfEpisodes(doc));

            returnAnime.setStatus(getStatus(doc));

            returnAnime.setGenres(getGenres(doc));

            returnAnime.setPopularity(getMembers(doc));

            returnAnime.setEpisodes(new ArrayList<>());
            
            return returnAnime;

        }catch (RuntimeException e){
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get Anime: " + id +" " + e.toString());
        }
    }

    @Override
    public Episode getEpisode(Anime anime, Integer episodeNumber){
        try {
            String episodeURL = episodeURLTemplate.formatted(anime.getMalID(), anime.getTitle(), episodeNumber);
            return extractEpisodeData(episodeURL);
        }
        catch (Exception ignored){
            throw new RuntimeException("Failed to get Episode"+anime.getId() + "/" + episodeNumber);
        }
    }

    private Integer getMembers(Document doc) {
        String memberString = getStringUnderInformationTag(doc, "Members:");
        return Integer.parseInt(memberString.replaceAll(",", ""));
    }

    private List<Episode> getEpisodes(Document doc) throws IOException {
        List<Episode> episodes = new ArrayList<>();
        String episodesURL = doc.select("div#horiznav_nav ul li").get(2).child(0).attr("href");
        Document episodesDoc = Jsoup.connect(episodesURL).get();
        for (Element episodeElement : episodesDoc.select("table.ascend tbody tr td.episode-title a")) {
            String episodeURL = episodeElement.attr("href");
            Episode episode = extractEpisodeData(episodeURL);
            episodes.add(episode);
        }
        return episodes;
    }

    private Episode extractEpisodeData(String episodeURL) throws IOException {
        Document episodedoc = Jsoup.connect(episodeURL).get();
        Episode episode = new Episode();
        episode.setEpisodeNumber(Integer.parseInt(episodedoc.select("h2.fs18.lh11 span.fw-n").first().ownText().replace("#", "").replace(" -", "")));
        episode.setTitle(episodedoc.select("h2.fs18.lh11").first().ownText());
        episode.setSynopsis(episodedoc.select("div.pt8.pb8").first().ownText());
        episode.setLocation("");
        return episode;
    }


    private List<Anime.Genres> getGenres(Document doc) {
        /*Somehow in the HTML every genre is displayed twice, one with display: none attribute. It is more general to
        keep the getStringUnderInformationTag as is, and remove the duplicates here, as this is the only tag where
        this problem persists*/
        String duplicatedGenres = getStringUnderInformationTag(doc, "Genres:");
        String[] stringArr = duplicatedGenres.split(",");
        stringArr = removeDuplicates(stringArr);
        List<Anime.Genres> animeGenres = new ArrayList<>();
        for (String genre : stringArr) {
            animeGenres.add(processGenres(genre));
        }
        return animeGenres;
    }

    private String[] removeDuplicates(String[] stringArr) {
        return Arrays.stream(stringArr).distinct().toArray(String[]::new);
    }

    private Anime.Status getStatus(Document doc) {
        String status = getStringUnderInformationTag(doc, "Status:");
        return processStatusString(status);
    }

    private Anime.Status processStatusString(String status) {
        switch (Objects.requireNonNull(status)) {
            case "Currently Airing":
                return Anime.Status.AIRING;
            case "Finished Airing":
                return Anime.Status.FINISHED;
            case "Not yet aired":
                return Anime.Status.UPCOMING;
            default:
                throw new RuntimeException("Unknown Status string: " + status);
        }
    }

    private Integer getAnimeNumberOfEpisodes(Document doc) {
        String numberOfEpisodes = getStringUnderInformationTag(doc, "Episodes:");
        if (!numberOfEpisodes.equals("Unknown")) {
            return Integer.parseInt(numberOfEpisodes);
        } else {
            return null;
        }
    }

    private String getStringUnderInformationTag(Document doc, String tag) {
        Elements sidebarInformation = doc.select("table tbody tr td.borderClass div div span");
        for (Element sidebarElement : sidebarInformation) {
            if (sidebarElement.ownText().equals(tag)) {
                if (sidebarElement.parent().childrenSize() == 1) {
                    return sidebarElement.parent().ownText();
                } else {
                    return extractSiblingElementsOwnStrings(sidebarElement);
                }
            }
        }
        throw new RuntimeException("Tag not found in side bar");
    }

    private String extractSiblingElementsOwnStrings(Element element) {
        Elements siblingElements = element.parent().children();
        siblingElements.remove(0);
        StringBuilder returnString = new StringBuilder();
        for (Element siblingElement : siblingElements) {
            returnString.append(siblingElement.ownText()).append(",");
        }
        return returnString.toString();
    }

    private String getSynopsis(Document doc) {
        String regexForHTMLNewLine = "(?i)<br */?>\\s";
        String newLineCharacter = "\n";
        return doc.select("span[itemprop=\"description\"]").html().replaceAll(regexForHTMLNewLine, newLineCharacter);
    }

    private String getImageLocation(Document doc) {
        return doc.select("table tbody tr td.borderClass div div a img").attr("data-src");
    }

    private Double getScore(Document doc) {
        String scoreString = doc.select("span.score-label").html();
        if (!scoreString.equals("N/A")) {
            return Double.parseDouble(scoreString);
        } else {
            return null;
        }
    }

    private String getTitle(Document doc) {
        return doc.select(".h1-title span").html();
    }

    private Anime.Genres processGenres(String genre) {
        switch (genre) {
            case "Action":
                return Anime.Genres.ACTION;
            case "Demons":
                return Anime.Genres.DEMONS;
            case "Kids":
                return Anime.Genres.KIDS;
            case "Harem":
                return Anime.Genres.HAREM;
            case "Romance":
                return Anime.Genres.ROMANCE;
            case "Shoujo":
                return Anime.Genres.SHOUJO;
            case "Space":
                return Anime.Genres.SPACE;
            case "Vampire":
                return Anime.Genres.VAMPIRE;
            case "Adventure":
                return Anime.Genres.ADVENTURE;
            case "Drama":
                return Anime.Genres.DRAMA;
            case "Magic":
                return Anime.Genres.MAGIC;
            case "Mystery":
                return Anime.Genres.MYSTERY;
            case "Samurai":
                return Anime.Genres.SAMURAI;
            case "Shoujo Ai":
                return Anime.Genres.SHOUJO_AI;
            case "Sports":
                return Anime.Genres.SPORTS;
            case "Yaoi":
                return Anime.Genres.YAOI;
            case "Cars":
                return Anime.Genres.CARS;
            case "Ecchi":
                return Anime.Genres.ECCHI;
            case "Historical":
                return Anime.Genres.HISTORICAL;
            case "Martial Arts":
                return Anime.Genres.MARTIAL_ARTS;
            case "Shounen":
                return Anime.Genres.SHOUNEN;
            case "Parody":
                return Anime.Genres.PARODY;
            case "Super Power":
                return Anime.Genres.SUPER_POWER;
            case "Yuri":
                return Anime.Genres.YURI;
            case "Comedy":
                return Anime.Genres.COMEDY;
            case "Fantasy":
                return Anime.Genres.FANTASY;
            case "Horror":
                return Anime.Genres.HORROR;
            case "Mecha":
                return Anime.Genres.MECHA;
            case "Police":
                return Anime.Genres.POLICE;
            case "Sci-Fi":
                return Anime.Genres.SCI_FI;
            case "Shounen Ai":
                return Anime.Genres.SHOUNEN_AI;
            case "Supernatural":
                return Anime.Genres.SUPERNATURAL;
            case "Dementia":
                return Anime.Genres.DEMENTIA;
            case "Game":
                return Anime.Genres.GAME;
            case "Josei":
                return Anime.Genres.JOSEI;
            case "Military":
                return Anime.Genres.MILITARY;
            case "Psychological":
                return Anime.Genres.PSYCHOLOGICAL;
            case "Seinen":
                return Anime.Genres.SEINEN;
            case "Slice of Life":
                return Anime.Genres.SLICE_OF_LIFE;
            case "Thriller":
                return Anime.Genres.THRILLER;
            case "Music":
                return Anime.Genres.MUSIC;
            case "School":
                return Anime.Genres.SCHOOL;
            case "Hentai":
                return Anime.Genres.HENTAI;
            default:
                throw new RuntimeException("Can't find genre: " + genre);
        }
    }
}
