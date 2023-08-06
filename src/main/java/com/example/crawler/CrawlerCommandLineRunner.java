package com.example.crawler;

import com.example.crawler.coloso.ColosoCrawler;
import com.example.crawler.fastcampus.api.FastCampusApiCrawler;
import com.example.crawler.fastcampus.parsing.FastCampusCrawler;
import com.example.crawler.inflearn.InflearnCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("!test")
@Component
public class CrawlerCommandLineRunner implements CommandLineRunner {

    private final FastCampusApiCrawler fastCampusApiCrawler;
    private final FastCampusCrawler fastCampusCrawler;
    private final InflearnCrawler inflearnCrawler;
    private final ColosoCrawler colosoCrawler;

    @Override
    public void run(String... args) throws Exception {
//        inflearnCrawler.activate();
//        colosoCrawler.activate();
//        fastCampusCrawler.activate();
        fastCampusApiCrawler.activate();
    }
}
