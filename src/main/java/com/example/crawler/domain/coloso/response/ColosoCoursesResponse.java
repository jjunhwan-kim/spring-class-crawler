package com.example.crawler.domain.coloso.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ColosoCoursesResponse {

    @JsonProperty("data")
    private List<Course> courses;

    @Data
    public static class Course {
        private Long id;
        private String slug;
        private String instructor;
        private String publicTitle;
        private String publicDescription;
        private String keywords;
        private String desktopCoverImage;
        private String mobileCoverImage;
        private String desktopCardAsset;
        private Extras extras;

        @Data
        public static class Extras {
            private String additionalText1;
            private String additionalText2;
            private String additionalText3;
            private String displayKeywords;
        }
    }
}
