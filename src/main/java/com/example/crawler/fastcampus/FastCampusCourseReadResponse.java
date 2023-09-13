package com.example.crawler.fastcampus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCourseReadResponse {

    @JsonProperty("data")
    private CourseInfo courseInfo;

    @Data
    public static class CourseInfo {

        @JsonProperty("products")
        private List<Product> products;

        @JsonProperty("course")
        private Course course;

        @Data
        public static class Product {
            private Long id;
            private Long categoryId;
            private Long subCategoryId;
            private Long courseId;
            private Long listPrice;
            private Long salePrice;
        }

        @Data
        public static class Course {
            private Long id;
            private Long categoryId;
            private Long subCategoryId;
            private String instructor;
        }
    }
}
