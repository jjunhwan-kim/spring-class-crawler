package com.example.crawler.fastcampus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCategoryCoursesReadResponse {

    @JsonProperty("data")
    private CategoryCoursesReadResponse courses;

    @Data
    public static class CategoryCoursesReadResponse {

        @JsonProperty("categoryInfo")
        private Courses courses;

        @Data
        public static class Courses {

            private List<Course> courses;

            @Data
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
