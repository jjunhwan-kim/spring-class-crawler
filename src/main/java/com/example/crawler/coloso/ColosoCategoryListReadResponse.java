package com.example.crawler.coloso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ColosoCategoryListReadResponse {

    @JsonProperty("data")
    private List<Category> categories;

    @Data
    public static class Category {
        private Long id;
        private String title;
        private List<Category> children;
    }
}
