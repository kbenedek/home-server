package dev.biomfire.homeserver;

import dev.biomfire.homeserver.anime.AnimeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
public class SmokeTest {
    @Autowired
    private AnimeController controller;

    @Test
    public void contexLoads(){
        assertThat(controller).isNotNull();
    }
}
