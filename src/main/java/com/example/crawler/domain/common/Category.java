package com.example.crawler.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Category {

    private final Long id;

    private final String title;
    private final String url;

    private final List<Category> subCategories;
}
