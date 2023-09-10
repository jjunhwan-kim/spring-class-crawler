package com.example.crawler.fastcampus;

import lombok.Data;

import java.util.List;

@Data
public class CategoryReadResponseWrapper {
    private CategoryReadResponse data;

    @Data
    public static class CategoryReadResponse {
        private List<Category> categoryMenu;

        @Data
        public static class Category {
            private String title;
            private String titleLink;
            private Long categoryId;
            private Long subCategoryId;
            private List<Category> children;
        }
    }
}
