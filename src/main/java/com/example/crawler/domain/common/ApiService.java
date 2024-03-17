package com.example.crawler.domain.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiService {

    private final RestTemplate restTemplate;

    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(delay = 10000))
    public <T> T get(String url, Class<T> responseType) {
        log.info("Request URL: {}", url);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return restTemplate.getForObject(url, responseType);
    }

    @Retryable(retryFor = {Exception.class}, backoff = @Backoff(delay = 10000))
    public Document get(String url) throws IOException {
        log.info("Request URL: {}", url);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Connection connection = Jsoup.connect(url);
        return connection.get();
    }
}
