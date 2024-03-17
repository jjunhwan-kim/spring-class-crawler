package com.example.crawler.domain.fastcampus;

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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class FastCampusCrawler {

    private static final String SOURCE = "fastcampus";
    private static final String BASE_URL = "https://fastcampus.co.kr/";
    private static final String CATEGORIES_URL = "https://fastcampus.co.kr/.api/www/categories/all";
    private static final String COURSES_URL = "https://fastcampus.co.kr/.api/www/categories/{subCategoryId}/page";
    private static final String COURSE_URL = "https://fastcampus.co.kr/.api/www/courses/{id}/products";
    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";
    private final ApiService apiService;
    private final LectureService lectureService;

    public void get() {

        log.info("==================================================");
        log.info("Fast Campus Crawler Start");
        log.info("==================================================");
        log.info("Get Fast Campus categories..");
        FastCampusCategoriesResponse response = getCategories();
        List<Category> categories = convertCategories(response);
        printCategories(categories);
        validateCategories(categories);

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

    private void validateCategories(List<Category> categories) {

        Map<String, Boolean> categoryExistenceMap = Arrays.stream(FastCampusCategoryMap.values())
                .map(categoryMap -> categoryMap.getOriginalMainCategory() + "," + categoryMap.getOriginalSubCategory())
                .collect(Collectors.toMap(key -> key, value -> false));

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {

                String key = category.getTitle() + "," + subCategory.getTitle();
                Boolean exists = categoryExistenceMap.get(key);

                if (exists == null) {
                    log.error("Fast Campus categories validation failed, not match category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Fast Campus categories validation failed, not match category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                }

                if (exists) {
                    log.error("Fast Campus categories validation failed, duplicated category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Fast Campus categories validation failed, duplicated category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                } else {
                    categoryExistenceMap.put(key, true);
                }
            }
        }
    }

    public void getAndSaveCourses(List<Category> categories) {

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {
                String mainCategoryTitle = category.getTitle();
                String subCategoryTitle = subCategory.getTitle();

                log.info("Main Category: {}, Sub Category: {}", mainCategoryTitle, subCategoryTitle);

                try {
                    List<Course> courses = getCourses(category, subCategory);
                    List<Lecture> lectures = convertCourses(courses);
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
            String courseUrl = (BASE_URL + "/" + slug);
            String imageURl = course.getDesktopCardAsset();

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

            Long price;

            if (products.isEmpty()) {
                price = 0L;
            } else {
                price = products.get(0).getSalePrice();
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
                    imageURl);

            log.info("{}", fastCampusCourse);

            fastCampusCourses.add(fastCampusCourse);
        }

        return fastCampusCourses;
    }

    public List<Lecture> convertCourses(List<Course> courses) {

        List<Lecture> lectures = new ArrayList<>();

        for (Course course : courses) {

            Long id = course.getId();
            String title = course.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            Long price = course.getPrice();
            if (price == null) {
                price = 0L;
            }

            String description;
            String keywords;
            String instructor;
            String mainCategory;
            String subCategory;
            String url;
            String imageUrl;

            if (StringUtils.hasText(course.getDescription())) {
                description = course.getDescription().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                description = "";
            }
            if (StringUtils.hasText(course.getKeywords())) {
                keywords = course.getKeywords().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                keywords = "";
            }
            if (StringUtils.hasText(course.getInstructor())) {
                instructor = course.getInstructor().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                instructor = "";
            }
            mainCategory = course.getMainCategory();
            subCategory = course.getSubCategory();

            if (StringUtils.hasText(course.getCourseUrl())) {
                url = course.getCourseUrl().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                url = "";
            }
            if (StringUtils.hasText(course.getImageUrl())) {
                imageUrl = course.getImageUrl().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                imageUrl = "";
            }

            Optional<FastCampusCategoryMap> convertedCategory = Arrays.stream(FastCampusCategoryMap.values()).filter(fastCampusCategoryMap ->
                            fastCampusCategoryMap.getOriginalMainCategory().equals(mainCategory) &&
                                    fastCampusCategoryMap.getOriginalSubCategory().equals(subCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("Fast Campus category map conversion failed. Main Category: {}, Sub Category: {}", mainCategory, subCategory);
                throw new IllegalStateException("Fast Campus category map conversion failed. Main Category: " + mainCategory + " " + "Sub Category: " + subCategory);
            }

            String convertedMainCategory = convertedCategory.get().getConvertedMainCategory();
            String convertedSubCategory = convertedCategory.get().getConvertedSubCategory();

            Lecture lecture = new Lecture(title,
                    SOURCE,
                    id.toString(),
                    url,
                    price.toString(),
                    instructor,
                    imageUrl,
                    mainCategory,
                    subCategory,
                    convertedMainCategory,
                    convertedSubCategory,
                    keywords,
                    description);

            lectures.add(lecture);
        }

        return lectures;
    }

    private void printCategories(List<Category> categories) {
        for (Category category : categories) {
            List<Category> subCategories = category.getSubCategories();

            for (Category subCategory : subCategories) {
                log.info("{}\t{}\t{}", category.getTitle(), subCategory.getTitle(), subCategory.getUrl());
            }
        }
    }
}
