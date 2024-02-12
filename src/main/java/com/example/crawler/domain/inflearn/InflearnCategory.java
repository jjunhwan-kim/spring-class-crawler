package com.example.crawler.domain.inflearn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InflearnCategory {
    private final String mainCategoryName;
    private final String subCategoryName;
    private final String url;

    @Override
    public String toString() {
        return mainCategoryName + ", " + subCategoryName + ", " + url;
    }
}
