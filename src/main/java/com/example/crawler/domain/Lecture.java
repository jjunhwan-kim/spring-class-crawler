package com.example.crawler.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String source;

    private String url;

    private String price;

    private String name;

    private String imageUrl;

    private String originalMainCategory;

    private String originalSubCategory;

    private String mainCategory;

    private String subCategory;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(columnDefinition = "TEXT")
    private String content;

    public Lecture(String title,
                   String source,
                   String url,
                   String price,
                   String name,
                   String imageUrl,
                   String originalMainCategory,
                   String originalSubCategory,
                   String mainCategory,
                   String subCategory,
                   String keywords,
                   String content) {
        this.title = title;
        this.source = source;
        this.url = url;
        this.price = price;
        this.name = name;
        this.imageUrl = imageUrl;
        this.originalMainCategory = originalMainCategory;
        this.originalSubCategory = originalSubCategory;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.keywords = keywords;
        this.content = content;
    }
}
