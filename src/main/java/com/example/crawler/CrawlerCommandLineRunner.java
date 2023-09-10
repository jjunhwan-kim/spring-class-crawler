package com.example.crawler;

import com.example.crawler.fastcampus.FastCampusCrawler;
import com.example.crawler.inflearn.InflearnCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class CrawlerCommandLineRunner implements CommandLineRunner {

    private final InflearnCrawler inflearnCrawler;
    private final FastCampusCrawler fastCampusCrawler;

    @Override
    public void run(String... args) {
        inflearnCrawler.get();
        fastCampusCrawler.get();
    }
}
