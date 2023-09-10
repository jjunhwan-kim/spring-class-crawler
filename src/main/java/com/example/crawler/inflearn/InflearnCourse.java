package com.example.crawler.inflearn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InflearnCourse {

    @JsonProperty("course_identification")
    private Long id;

    @JsonProperty("course_title")
    private String title;

    @JsonProperty("course_level")
    private String level;

    @JsonProperty("first_category")
    private String firstCategory;

    @JsonProperty("second_category")
    private String secondCategory;

    @JsonProperty("skill_tag")
    private String tag;

    @JsonProperty("seq0_instructor_name")
    private String instructor;

    @JsonProperty("reg_price")
    private String regPrice;

    @JsonProperty("reg_vat_price")
    private String regVatPrice;

    @JsonProperty("selling_price")
    private String sellingPrice;

    private String courseUrl;

    private String courseImageUrl;

    public void updateUrl(String courseUrl, String courseImageUrl) {
        this.courseUrl = courseUrl;
        this.courseImageUrl = courseImageUrl;
    }

    @Override
    public String toString() {
        return id + "\t" +
                title + "\t" +
                instructor + "\t" +
                sellingPrice + "\t" +
                firstCategory + "\t" +
                secondCategory + "\t" +
                tag + "\t" +
                courseUrl + "\t" +
                courseImageUrl;
    }
}
