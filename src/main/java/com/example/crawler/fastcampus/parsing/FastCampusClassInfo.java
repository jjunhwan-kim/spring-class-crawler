package com.example.crawler.fastcampus.parsing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FastCampusClassInfo {
    private final String title;
    private final String content;
    private final String imageUrl;
    private final String tags;
    private String url;

    public void updateUrl(String url) {
        this.url = url;
    }
}
