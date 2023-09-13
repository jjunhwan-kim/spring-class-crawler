package com.example.crawler.coloso;

import com.example.crawler.domain.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ColosoCrawler {

    private static final String SOURCE = "coloso";
    private static final String BASE_URL = "https://coloso.co.kr/";
    private static final String CATEGORIES_URL = BASE_URL + "/api/displays";
    //    private static final String CATEGORY_COURSES_URL = BASE_URL + "/.api/www/categories/{subCategoryId}/page";
//    private static final String COURSE_URL = BASE_URL + "/.api/www/courses/{id}/products";
    private final RestTemplate restTemplate;
    private final LectureService lectureService;


    public void get() {

        log.info("==================================================");
        log.info("Get coloso campus categories");
        log.info("==================================================");
        List<ColosoCategoryListReadResponse.Category> categories = getCategories();
        log.info("==================================================");
//
//        log.info("Get fast campus courses");
//        log.info("==================================================");
//        List<FastCampusCourse> courses = getCourses(categories);
//
//        log.info("Convert fast campus courses to lectures and save lectures");
//        log.info("==================================================");
//        List<Lecture> lectures = convertCourses(courses);
//        lectureService.saveOrUpdateLectures(SOURCE, lectures);
    }

    private List<ColosoCategoryListReadResponse.Category> getCategories() {

        ColosoCategoryListReadResponse response = restTemplate.getForObject(CATEGORIES_URL, ColosoCategoryListReadResponse.class);

        if (response == null) {
            log.error("Coloso course categories read failed, {}", CATEGORIES_URL);
            throw new RuntimeException("Coloso course categories read failed");
        }

        List<ColosoCategoryListReadResponse.Category> categories = response.getCategories();

        for (ColosoCategoryListReadResponse.Category category : categories) {
            String mainCategory = category.getTitle();
            List<ColosoCategoryListReadResponse.Category> children = category.getChildren();
            for (ColosoCategoryListReadResponse.Category child : children) {
                String subCategory = child.getTitle();
                log.info("{}, {}", mainCategory, subCategory);
            }
        }

        return categories;
    }
}
