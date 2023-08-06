package com.example.crawler.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OnlineClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;

    private String title;

    private String url;

    private String imageUrl;

    private Integer price;

    private String instructor;

    private String category1;

    private String category2;

    @Column(columnDefinition = "TEXT")
    private String category3;

    @Column(columnDefinition = "TEXT")
    private String category4;

    @Column(columnDefinition = "TEXT")
    private String category5;

    public OnlineClass(String source,
                       String title,
                       String url,
                       String imageUrl,
                       Integer price,
                       String instructor,
                       String category1,
                       String category2,
                       String category3,
                       String category4,
                       String category5) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.imageUrl = imageUrl;
        this.price = price;
        this.instructor = instructor;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.category4 = category4;
        this.category5 = category5;
    }

    public void update(String source,
                               String imageUrl,
                               Integer price,
                               String instructor,
                               String category1,
                               String category2,
                               String category3,
                               String category4,
                               String category5) {
        this.source = source;
        this.imageUrl = imageUrl;
        this.price = price;
        this.instructor = instructor;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.category4 = category4;
        this.category5 = category5;
    }
}
