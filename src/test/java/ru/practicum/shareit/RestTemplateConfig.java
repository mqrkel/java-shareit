package ru.practicum.shareit;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@TestConfiguration
public class RestTemplateConfig {

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        return new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}