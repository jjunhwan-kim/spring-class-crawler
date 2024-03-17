package com.example.crawler.domain.fastcampus.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCourseResponse {

    @JsonProperty("data")
    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("products")
        private List<Product> products;

        @JsonProperty("course")
        private Course course;

        @lombok.Data
        public static class Product {
            private Long id;
            private Long categoryId;
            private Long subCategoryId;
            private Long courseId;
            private Long listPrice;
            private Long salePrice;
        }

        @lombok.Data
        public static class Course {
            private Long id;
            private Long categoryId;
            private Long subCategoryId;
            private String instructor;
        }
    }
}
