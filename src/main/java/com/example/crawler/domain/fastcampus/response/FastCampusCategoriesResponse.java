package com.example.crawler.domain.fastcampus.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FastCampusCategoriesResponse {

    private Data data;

    @lombok.Data
    public static class Data {

        @JsonProperty("categoryMenu")
        private List<Category> categories;

        @lombok.Data
        public static class Category {
            private String title;
            private String titleLink;
            private Long categoryId;
            private Long subCategoryId;
            private List<Category> children;
        }
    }
}
