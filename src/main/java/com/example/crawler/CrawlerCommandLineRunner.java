package com.example.crawler;

import com.example.crawler.domain.class101.Class101Crawler;
import com.example.crawler.domain.coloso.ColosoCrawler;
import com.example.crawler.domain.fastcampus.FastCampusCrawler;
import com.example.crawler.domain.inflearn.InflearnCrawler;
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
    private final ColosoCrawler colosoCrawler;
    private final Class101Crawler class101Crawler;

    @Override
    public void run(String... args) {
        //inflearnCrawler.get();
        //fastCampusCrawler.get();
        colosoCrawler.get();
        //class101Crawler.get();
    }
}
