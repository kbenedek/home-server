package dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.MALWrapper;

import dev.biomfire.homeserver.anime.OnlineDatabaseAPIWrappers.IOnlineAnimeAPI;
import dev.biomfire.homeserver.anime.model.Anime;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MALCrawler implements IOnlineAnimeAPI {
    static final String searchURLTemplate = "https://myanimelist.net/anime.php?q=%s";
    static final String animeURLTemplate = "https://myanimelist.net/anime/%d/";
    static private class MALIDFromURLExtractor{
        static final Pattern p = Pattern.compile("(?<=myanimelist\\.net/anime/)(\\d+(?=/))");
        static public Integer getID(String URL){
            Matcher m = p.matcher(URL);
            if(m.find()) {
                return Integer.parseInt(m.group(0));
            }
            else{
                throw new RuntimeException("ID not found");
            }
        }
    }

    @Override
    public List<Integer> searchAnime(String query){
        try {
            String searchURL = String.format(searchURLTemplate, query);
            Document doc = Jsoup.connect(searchURL).get();
            Elements animes_trs = doc.select(".list table tbody").first().children();
            //Table header solved in website with another tr, removing it
            animes_trs.remove(0);
            List<Integer> returnList = new ArrayList<>();
            for(var listElement : animes_trs){
                String animeURL = listElement.child(0).child(0).child(0).attr("href");
                Integer malID = MALIDFromURLExtractor.getID(animeURL);
                returnList.add(malID);
                log.info(String.format("Found anime with ID = %d", malID));
            }
            return returnList;
        }
        catch (Exception ignored){
            return null;
        }
    }

    //TODO: GENRES, STATUS, EPISODES
    @Override
    public Anime getAnimeByID(Integer id) {
        try {
            Anime returnAnime = new Anime();
            String animeURL = String.format(animeURLTemplate, id);
            Document doc = Jsoup.connect(animeURL).get();

            returnAnime.setTitle(getTitle(doc));

            returnAnime.setScore(getScore(doc));

            returnAnime.setImageLocation(getImageLocation(doc));

            returnAnime.setSynopsis(getSynopsis(doc));

            returnAnime.setNumberOfEpisodes(getAnimeNumberOfEpisodes(doc));

            returnAnime.setStatus(getStatus(doc));



            return returnAnime;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Anime.Status getStatus(Document doc) {
        String status = getStringUnderInformationTag(doc, 3);
        return processStatusString(status);
    }

    private Anime.Status processStatusString(String status) {
        switch (Objects.requireNonNull(status)) {
            case "Currently Airing":
                return Anime.Status.AIRING;
            case "Finished Airing":
                return Anime.Status.FINISHED;
            case "Not yet aired ":
                return Anime.Status.UPCOMING;
            default:
                throw new RuntimeException("Unknown Status string: " + status);
        }
    }

    private Integer getAnimeNumberOfEpisodes(Document doc) {
        String numberOfEpisodes = getStringUnderInformationTag(doc, 2);
        if(!numberOfEpisodes.equals("Unknown")){
            return Integer.parseInt(numberOfEpisodes);
        }
        else{
            return null;
        }
    }

    private String getStringUnderInformationTag(Document doc, int depth) {
        Element sidebar =   doc.select("table tbody tr td.borderClass div").first();
        for(Element sidebarElement : sidebar.children()){
            if(sidebarElement.ownText().equals("Information")){
                Element currentElement = sidebarElement;
                for(int i = 0; i < depth; i++){
                    currentElement = currentElement.nextElementSibling();
                }
                return currentElement.ownText();
            }
        }
        throw new RuntimeException("No Information tag found in sidebar");
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
        return Double.parseDouble(doc.select("span.score-label").html());
    }

    private String getTitle(Document doc) {
        return doc.select(".h1-title span").html();
    }
}
