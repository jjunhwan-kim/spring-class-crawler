package com.example.crawler;

import com.example.crawler.fastcampus.FastCampusCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class CrawlerCommandLineRunner implements CommandLineRunner {

    private final FastCampusCrawler crawler;

    @Override
    public void run(String... args) throws Exception {
        crawler.activate();
    }
}
