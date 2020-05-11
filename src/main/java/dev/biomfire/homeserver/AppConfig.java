package dev.biomfire.homeserver;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Getter
    final String torrentBaseSavePath = "/home/biomfire/Downloads";

}
