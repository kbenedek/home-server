package dev.biomfire.homeserver.anime.data;

import dev.biomfire.homeserver.anime.model.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.Assert;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class CustomAnimeRepositoryImpl implements CustomAnimeRepository {
    private final MongoOperations operations;

    @Autowired
    public CustomAnimeRepositoryImpl(MongoOperations operations) {
        Assert.notNull(operations, "MongoOperations must not be null!");
        this.operations = operations;
    }

    @Override
    public List<Episode> findEpisodeByMalIDAndEpisodeNumber(Integer id, Integer episodeNumber) {
        Aggregation aggregation = constructAggregation(id, episodeNumber);
        AggregationResults<Episode> results = operations.aggregate(aggregation, "anime", Episode.class);
        return results.getMappedResults();
    }

    private Aggregation constructAggregation(Integer id, Integer episodeNumber) {
        UnwindOperation unwindEpisodes = Aggregation.unwind("episodes");
        Criteria malIDIsSameAsId = Criteria.where("malID").is(id);
        Criteria episodeNumberIsSameAsEpisodeNumber = Criteria.where("episodes.episodeNumber").is(episodeNumber);
        MatchOperation matchMALIDandEpisodeNumber = Aggregation.match(new Criteria().andOperator(malIDIsSameAsId, episodeNumberIsSameAsEpisodeNumber));
        ReplaceRootOperation replaceRootToEpisodes = Aggregation.replaceRoot("episodes");
        return Aggregation.newAggregation(unwindEpisodes, matchMALIDandEpisodeNumber, replaceRootToEpisodes);
    }
}
