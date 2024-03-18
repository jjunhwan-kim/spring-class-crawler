package com.example.crawler.domain.fastcampus;

import com.example.crawler.domain.AbstractCrawler;
import com.example.crawler.domain.coloso.ColosoCategoryMap;
import com.example.crawler.domain.common.ApiService;
import com.example.crawler.domain.common.Category;
import com.example.crawler.domain.common.Course;
import com.example.crawler.domain.fastcampus.response.FastCampusCategoriesResponse;
import com.example.crawler.domain.fastcampus.response.FastCampusCourseResponse;
import com.example.crawler.domain.fastcampus.response.FastCampusCoursesResponse;
import com.example.crawler.domain.lecture.Lecture;
import com.example.crawler.domain.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FastCampusCrawler extends AbstractCrawler {

    private static final String SOURCE = "Fast Campus";
    private static final String BASE_URL = "https://fastcampus.co.kr/";
    private static final String CATEGORIES_URL = "https://fastcampus.co.kr/.api/www/categories/all";
    private static final String COURSES_URL = "https://fastcampus.co.kr/.api/www/categories/{subCategoryId}/page";
    private static final String COURSE_URL = "https://fastcampus.co.kr/.api/www/courses/{id}/products";
    private final ApiService apiService;
    private final LectureService lectureService;

    @Override
    protected String getSource() {
        return SOURCE;
    }

    public void get() {

        log.info("==================================================");
        log.info("Fast Campus Crawler Start");
        log.info("==================================================");
        log.info("Get Fast Campus categories..");
        FastCampusCategoriesResponse response = getCategories();
        List<Category> categories = convertCategories(response);
        printCategories(categories);
        validateCategories(categories, FastCampusCategoryMap.values());

        log.info("==================================================");
        log.info("Get Fast Campus courses..");
        getAndSaveCourses(categories);

        log.info("==================================================");
        log.info("Fast Campus Crawler End");
        log.info("==================================================");
    }

    private FastCampusCategoriesResponse getCategories() {

        FastCampusCategoriesResponse response = apiService.get(CATEGORIES_URL, FastCampusCategoriesResponse.class);

        if (response == null
                || response.getData() == null
                || response.getData().getCategories() == null
                || response.getData().getCategories().isEmpty()) {
            log.error("Fast Campus categories read failed.");
            throw new IllegalStateException("Fast Campus categories read failed.");
        }

        return response;
    }


    private List<Category> convertCategories(FastCampusCategoriesResponse response) {

        List<Category> convertedMainCategories = new ArrayList<>();
        List<FastCampusCategoriesResponse.Data.Category> mainCategories = response.getData().getCategories();

        for (FastCampusCategoriesResponse.Data.Category mainCategory : mainCategories) {

            if (mainCategory.getCategoryId() == null) {
                continue;
            }

            List<FastCampusCategoriesResponse.Data.Category> subCategories = mainCategory.getChildren();

            List<Category> convertedSubCategories = new ArrayList<>();

            for (FastCampusCategoriesResponse.Data.Category subCategory : subCategories) {

                Long subCategoryId = subCategory.getSubCategoryId();
                if (subCategoryId == null) {
                    continue;
                }

                String url = UriComponentsBuilder.fromUriString(COURSES_URL)
                        .buildAndExpand(subCategoryId.toString())
                        .toUriString();

                convertedSubCategories.add(new Category(subCategoryId,
                        subCategory.getTitle().trim(),
                        url,
                        Collections.emptyList()));
            }

            if (convertedSubCategories.isEmpty()) {
                continue;
            }

            convertedMainCategories.add(new Category(mainCategory.getCategoryId(),
                    mainCategory.getTitle().trim(),
                    "",
                    convertedSubCategories));
        }

        return convertedMainCategories;
    }

    public void getAndSaveCourses(List<Category> categories) {

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {
                String mainCategoryTitle = category.getTitle();
                String subCategoryTitle = subCategory.getTitle();

                log.info("Main Category: {}, Sub Category: {}", mainCategoryTitle, subCategoryTitle);

                try {
                    List<Course> courses = getCourses(category, subCategory);
                    List<Lecture> lectures = convertCourses(courses, FastCampusCategoryMap.values());
                    lectureService.saveOrUpdateLectures(SOURCE, lectures);
                } catch (Exception exception) {
                    log.error("Main Category: {}, Sub Category: {} failed, {}", mainCategoryTitle, subCategoryTitle, exception.getMessage());
                }
            }
        }
    }

    private List<Course> getCourses(Category category, Category subCategory) {

        List<Course> fastCampusCourses = new ArrayList<>();

        String mainCategoryTitle = category.getTitle();
        String subCategoryTitle = subCategory.getTitle();
        String url = subCategory.getUrl();

        // 강의 목록 API
        FastCampusCoursesResponse response;

        try {
            response = apiService.get(url, FastCampusCoursesResponse.class);
        } catch (Exception exception) {
            log.error("Courses fetch failed. {}", url);
            return Collections.emptyList();
        }

        if (response == null) {
            log.error("Courses read failed. {}", url);
            return Collections.emptyList();
        }

        List<FastCampusCoursesResponse.Data.CategoryInfo.Course> courses = response.getData().getCategoryInfo().getCourses();

        for (FastCampusCoursesResponse.Data.CategoryInfo.Course course : courses) {

            Long id = course.getId();
            String slug = course.getSlug();
            String description = course.getPublicDescription();
            String title = course.getPublicTitle();
            String keywords = String.join(",", course.getKeywords());
            String courseUrl = "";
            if (StringUtils.hasText(slug)) {
                courseUrl = (BASE_URL + "/" + slug);
            }
            String imageUrl = course.getDesktopCardAsset();

            url = UriComponentsBuilder.fromUriString(COURSE_URL)
                    .buildAndExpand(id.toString())
                    .toUriString();

            // 강의 API
            FastCampusCourseResponse courseResponse;
            try {
                courseResponse = apiService.get(url, FastCampusCourseResponse.class);
            } catch (Exception exception) {
                log.error("Course fetch failed. {}", url);
                continue;
            }

            if (courseResponse == null) {
                log.error("Course read failed. {}", url);
                continue;
            }

            List<FastCampusCourseResponse.Data.Product> products = courseResponse.getData().getProducts();

            String price;

            if (products.isEmpty() || products.get(0).getSalePrice() == null) {
                price = "0";
            } else {
                price = products.get(0).getSalePrice().toString();
            }

            String instructor = courseResponse.getData().getCourse().getInstructor();

            Course fastCampusCourse = new Course(
                    id,
                    title,
                    price,
                    description,
                    keywords,
                    instructor,
                    mainCategoryTitle,
                    subCategoryTitle,
                    courseUrl,
                    imageUrl);

            log.info("{}", fastCampusCourse);

            fastCampusCourses.add(fastCampusCourse);
        }

        return fastCampusCourses;
    }
}
