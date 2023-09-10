package com.example.crawler.fastcampus;

import lombok.Data;

import java.util.List;

@Data
public class CategoryCoursesReadResponseWrapper {

    private CategoryCoursesReadResponse data;

    @Data
    public static class CategoryCoursesReadResponse {

        private CategoryInfo categoryInfo;

        @Data
        public static class CategoryInfo {

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
