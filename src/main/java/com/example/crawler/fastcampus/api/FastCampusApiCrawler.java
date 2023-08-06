package com.example.crawler.fastcampus.api;

import com.example.crawler.domain.Lecture;
import com.example.crawler.domain.LectureService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class FastCampusApiCrawler {

    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\t\n\r\u2028\u2029\u0085]";
    private static final String SOURCE = "Fast Campus";
    private static final String BASE_URL = "https://fastcampus.co.kr";
    private static final String CATEGORIES_URL = BASE_URL + "/.api/www/categories/all";
    private static final String CATEGORY_CLASS_URL = BASE_URL + "/.api/www/categories/{subCategoryId}/page";
    private final RestTemplate restTemplate = new RestTemplate();
    private final LectureService lectureService;

    public void activate() {

        List<Lecture> lectures = new ArrayList<>();
        CategoryReadResponseWrapper categoryReadResponseWrapper = restTemplate.getForObject(CATEGORIES_URL, CategoryReadResponseWrapper.class);

        if (categoryReadResponseWrapper == null) {
            log.error("The return value of endpoint {} is null!", CATEGORIES_URL);
            return;
        }

        List<CategoryReadResponseWrapper.CategoryReadResponse.Category> categories = categoryReadResponseWrapper.getData().getCategoryMenu();

        for (CategoryReadResponseWrapper.CategoryReadResponse.Category category : categories) {

            String categoryTitle = category.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, "");

            List<CategoryReadResponseWrapper.CategoryReadResponse.Category> subCategories = category.getChildren();

            for (CategoryReadResponseWrapper.CategoryReadResponse.Category subCategory : subCategories) {

                String subCategoryTitle = subCategory.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, "");
                Long subCategoryId = subCategory.getSubCategoryId();

                if (subCategoryId == null) {
                    continue;
                }

                Optional<FastCampusCourseCategory> convertedCategory = Arrays.stream(FastCampusCourseCategory.values()).filter(fastCampusCourseCategory ->
                                fastCampusCourseCategory.getOriginalMainCategory().equals(categoryTitle) &&
                                        fastCampusCourseCategory.getOriginalSubCategory().equals(subCategoryTitle))
                        .findFirst();

                if (convertedCategory.isEmpty()) {
                    log.error("Category conversion failed! Main Category: {}, Sub Category: {}", categoryTitle, subCategoryTitle);
                    continue;
                }

                String convertedMainCategory = convertedCategory.get().getConvertedMainCategory();
                String convertedSubCategory = convertedCategory.get().getConvertedSubCategory();

                String categoryClassUrl = CATEGORY_CLASS_URL.replace("{subCategoryId}", subCategoryId.toString());

                CategoryClassReadResponseWrapper categoryClassReadResponseWrapper = restTemplate.getForObject(categoryClassUrl, CategoryClassReadResponseWrapper.class);

                if (categoryClassReadResponseWrapper == null) {
                    log.error("The return value of endpoint {} is null!", categoryClassUrl);
                    continue;
                }

                List<CategoryClassReadResponseWrapper.CategoryClassReadResponse.CategoryInfo.Course> courses = categoryClassReadResponseWrapper.getData().getCategoryInfo().getCourses();

                for (CategoryClassReadResponseWrapper.CategoryClassReadResponse.CategoryInfo.Course course : courses) {

                    String slug = course.getSlug().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String description = course.getPublicDescription().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String title = course.getPublicTitle().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String keywords = String.join(",", course.getKeywords()).replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    keywords = categoryTitle + "," + subCategoryTitle + "," + keywords;
                    String url = (BASE_URL + "/" + slug).replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String imageURl = course.getDesktopCardAsset().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();


                    Lecture lecture = new Lecture(title,
                            SOURCE,
                            url,
                            null,
                            null,
                            imageURl,
                            categoryTitle,
                            subCategoryTitle,
                            convertedMainCategory,
                            convertedSubCategory,
                            keywords,
                            description);

                    lectures.add(lecture);
                }
            }
        }

        lectureService.save(lectures);
    }

    @Data
    public static class CategoryReadResponseWrapper {
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

    @Data
    public static class CategoryClassReadResponseWrapper {

        private CategoryClassReadResponse data;

        @Data
        public static class CategoryClassReadResponse {

            private CategoryInfo categoryInfo;

            @Data
            public static class CategoryInfo {

                private List<Course> courses;

                @Data
                public static class Course {
                    private Long id;
                    private String slug;
                    private String publicTitle;
                    private String publicDescription;
                    private List<String> keywords;
                    private String desktopCardAsset;
                }
            }
        }
    }
}