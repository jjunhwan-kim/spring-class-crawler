package com.example.crawler.domain.fastcampus.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCoursesResponse {

    @JsonProperty("data")
    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("categoryInfo")
        private CategoryInfo categoryInfo;

        @lombok.Data
        public static class CategoryInfo {

            private List<Course> courses;

            @lombok.Data
            public static class Course {
                private Long id;
                private String slug;
                private String publicTitle;
                private String publicDescription;
                private List<String> keywords;
                private String desktopCardAsset;
            }
        }
    }
}
