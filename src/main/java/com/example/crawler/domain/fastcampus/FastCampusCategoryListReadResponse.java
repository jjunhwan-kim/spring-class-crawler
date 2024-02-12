package com.example.crawler.domain.fastcampus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCategoryListReadResponse {

    @JsonProperty("data")
    private Categories categories;

    @Data
    public static class Categories {

        @JsonProperty("categoryMenu")
        private List<Category> categories;

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
