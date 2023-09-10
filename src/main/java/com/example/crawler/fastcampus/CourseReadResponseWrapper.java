package com.example.crawler.fastcampus;

import lombok.Data;

import java.util.List;

@Data
public class CourseReadResponseWrapper {

    private CourseReadResponse data;

    @Data
    public static class CourseReadResponse {

        private List<Product> products;
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
