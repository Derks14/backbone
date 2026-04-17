package backbone.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfig {

    @Bean
    public RestClient spotifyClient() {
        String BASE_URL = "https://api.spotify.com/v1";

        return RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .build();
    }


    @Bean
    public RestClient spotifyAccountsClient() {
        String BASE_URL = "https://accounts.spotify.com/api/token";

        return RestClient.builder()
                .baseUrl(BASE_URL)
//                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }



}
