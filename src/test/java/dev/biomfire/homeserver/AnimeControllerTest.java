package dev.biomfire.homeserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class AnimeControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void getAnime_thenNotImplemented() throws Exception{
        mvc.perform(get("/api/anime/").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotImplemented());
    }
}