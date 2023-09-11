package com.example.crawler.fastcampus;

import com.example.crawler.domain.Lecture;
import com.example.crawler.domain.LectureService;
import com.example.crawler.inflearn.InflearnCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class FastCampusCrawler {

    private static final String SOURCE = "fastcampus";
    private static final String BASE_URL = "https://fastcampus.co.kr";
    private static final String CATEGORIES_URL = BASE_URL + "/.api/www/categories/all";
    private static final String CATEGORY_COURSES_URL = BASE_URL + "/.api/www/categories/{subCategoryId}/page";
    private static final String COURSE_URL = "https://fastcampus.co.kr/.api/www/courses/{id}/products";
    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\t\n\r\u2028\u2029\u0085]";
    private final RestTemplate restTemplate;
    private final LectureService lectureService;

    public void get() {

        log.info("==================================================");
        log.info("Get fast campus categories");
        log.info("==================================================");
        List<CategoryReadResponseWrapper.CategoryReadResponse.Category> categories = getCategories();
        log.info("==================================================");

        log.info("Get fast campus courses");
        log.info("==================================================");
        List<FastCampusCourse> courses = getCourses(categories);

        log.info("Convert fast campus courses to lectures and save lectures");
        log.info("==================================================");
        List<Lecture> lectures = convertCourses(courses);
        lectureService.saveOrUpdateLectures(SOURCE, lectures);
    }

    private List<CategoryReadResponseWrapper.CategoryReadResponse.Category> getCategories() {

        CategoryReadResponseWrapper categoryReadResponseWrapper = restTemplate.getForObject(CATEGORIES_URL, CategoryReadResponseWrapper.class);

        if (categoryReadResponseWrapper == null) {
            log.error("Fast Campus course categories read failed, {}", CATEGORIES_URL);
            throw new RuntimeException("Fast Campus course categories read failed");
        }

        return categoryReadResponseWrapper.getData().getCategoryMenu();
    }

    public List<FastCampusCourse> getCourses(List<CategoryReadResponseWrapper.CategoryReadResponse.Category> categories) {

        List<FastCampusCourse> fastCampusCourses = new ArrayList<>();

        for (CategoryReadResponseWrapper.CategoryReadResponse.Category category : categories) {

            String categoryTitle = category.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, "");

            List<CategoryReadResponseWrapper.CategoryReadResponse.Category> subCategories = category.getChildren();

            for (CategoryReadResponseWrapper.CategoryReadResponse.Category subCategory : subCategories) {

                String subCategoryTitle = subCategory.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, "");
                Long subCategoryId = subCategory.getSubCategoryId();

                if (subCategoryId == null) {
                    log.error("sub category id is null, {}, {}", categoryTitle, subCategoryTitle);
                    continue;
                }

                String categoryCoursesUrl = CATEGORY_COURSES_URL.replace("{subCategoryId}", subCategoryId.toString());

                CategoryCoursesReadResponseWrapper categoryCoursesReadResponseWrapper = restTemplate.getForObject(categoryCoursesUrl, CategoryCoursesReadResponseWrapper.class);

                if (categoryCoursesReadResponseWrapper == null) {
                    log.error("Fast Campus courses read failed, {}", categoryCoursesUrl);
                    continue;
                }

                List<CategoryCoursesReadResponseWrapper.CategoryCoursesReadResponse.CategoryInfo.Course> courses = categoryCoursesReadResponseWrapper.getData().getCategoryInfo().getCourses();

                for (CategoryCoursesReadResponseWrapper.CategoryCoursesReadResponse.CategoryInfo.Course course : courses) {

                    Long id = course.getId();
                    String slug = course.getSlug().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String description = course.getPublicDescription().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String title = course.getPublicTitle().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String keywords = String.join(",", course.getKeywords()).replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String url = (BASE_URL + "/" + slug).replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    String imageURl = course.getDesktopCardAsset().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();

                    String courseApiUrl = COURSE_URL.replace("{id}", id.toString());
                    CourseReadResponseWrapper courseReadResponseWrapper = restTemplate.getForObject(courseApiUrl, CourseReadResponseWrapper.class);

                    if (courseReadResponseWrapper == null) {
                        log.error("Fast Campus course read failed, {}", courseApiUrl);
                        continue;
                    }

                    List<CourseReadResponseWrapper.CourseReadResponse.Product> products = courseReadResponseWrapper.getData().getProducts();

                    Long price;

                    if (products.isEmpty()) {
                        price = 0L;
                    } else {
                        price = products.get(0).getSalePrice();
                    }

                    String instructor = courseReadResponseWrapper.getData().getCourse().getInstructor();
                    if (instructor == null) {
                        instructor = "";
                    } else {
                        instructor = instructor.replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll(" {2,}", " ").trim();
                    }

                    FastCampusCourse fastCampusCourse = new FastCampusCourse(
                            id,
                            title,
                            price,
                            description,
                            keywords,
                            instructor,
                            categoryTitle,
                            subCategoryTitle,
                            url,
                            imageURl);

                    log.info("Course, {}", fastCampusCourse);

                    fastCampusCourses.add(fastCampusCourse);
                }
            }
        }

        return fastCampusCourses;
    }

    public List<Lecture> convertCourses(List<FastCampusCourse> courses) {

        List<Lecture> lectures = new ArrayList<>();
        // 중복 제거
        Set<Long> sourceIds = new HashSet<>();

        for (FastCampusCourse course : courses) {

            Long id = course.getId();
            if (sourceIds.contains(id)) {
                continue;
            } else {
                sourceIds.add(id);
            }

            String title = course.getTitle();
            Long price = course.getPrice();
            String description = course.getDescription();
            String keywords = course.getKeywords();
            String instructor = course.getInstructor();
            String mainCategory = course.getMainCategory();
            String subCategory = course.getSubCategory();
            String url = course.getUrl();
            String imageUrl = course.getImageUrl();

            Optional<Category> convertedCategory = Arrays.stream(Category.values()).filter(category ->
                            category.getOriginalMainCategory().equals(mainCategory) &&
                                    category.getOriginalSubCategory().equals(subCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("Category conversion failed! Main Category: {}, Sub Category: {}", mainCategory, subCategory);
                continue;
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
}
