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

    private String sourceId;

    private String url;

    private String price;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private String originalMainCategory;

    private String originalSubCategory;

    private String mainCategory;

    private String subCategory;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TINYINT")
    private Boolean deleted;

    public Lecture(String title,
                   String source,
                   String sourceId,
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
        this.sourceId = sourceId;
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
        this.deleted = false;
    }

    public void delete() {
        this.deleted = true;
    }

    public void update(Lecture lecture) {
        this.title = lecture.getTitle();
        this.url = lecture.getUrl();
        this.price = lecture.getPrice();
        this.name = lecture.getName();
        this.imageUrl = lecture.getImageUrl();
        this.originalMainCategory = lecture.getOriginalMainCategory();
        this.originalSubCategory = lecture.getOriginalSubCategory();
        this.keywords = lecture.getKeywords();
        this.content = lecture.getContent();
        this.deleted = false;
    }
}
