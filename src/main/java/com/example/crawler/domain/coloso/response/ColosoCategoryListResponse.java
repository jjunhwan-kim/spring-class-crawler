package com.example.crawler.domain.coloso.response;

import lombok.Data;

import java.util.List;

@Data
public class ColosoCategoryListResponse {

    private List<Category> data;

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
