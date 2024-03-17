package com.example.crawler.domain.coloso.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ColosoCategoriesResponse {

    @JsonProperty("data")
    private List<Category> categories;

    @Data
    public static class Category {
        private Long id;
        private String title;
        private List<Category> children;
        private Extras extras;

        @Data
        public static class Extras {
            private Boolean hideMenu;
        }
    }
}
