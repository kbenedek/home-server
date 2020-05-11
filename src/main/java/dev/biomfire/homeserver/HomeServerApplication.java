package dev.biomfire.homeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//TODO: Robosztus hibakezelés
//TODO: Még több teszt
@SpringBootApplication
public class HomeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeServerApplication.class, args);
    }

}
