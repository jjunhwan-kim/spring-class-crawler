package com.example.crawler.domain.coloso;

import com.example.crawler.domain.coloso.response.ColosoCategoriesResponse;
import com.example.crawler.domain.coloso.response.ColosoCoursesResponse;
import com.example.crawler.domain.common.ApiService;
import com.example.crawler.domain.common.Category;
import com.example.crawler.domain.common.Course;
import com.example.crawler.domain.lecture.Lecture;
import com.example.crawler.domain.lecture.LectureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Component
public class ColosoCrawler {

    private static final String SOURCE = "coloso";
    private static final String CATEGORIES_URL = "https://coloso.co.kr/api/displays";
    private static final String COURSES_URL = "https://coloso.co.kr/category/{subCategoryId}";
    private static final String COURSE_URL = "https://coloso.co.kr/api/catalogs/courses";
    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";

    private final ObjectMapper objectMapper;
    private final ApiService apiService;
    private final LectureService lectureService;

    public void get() {
        log.info("==================================================");
        log.info("Coloso Crawler Start");
        log.info("==================================================");
        log.info("Get Coloso categories..");
        ColosoCategoriesResponse response = getCategories();
        List<Category> categories = convertCategories(response);
        validateCategories(categories);

        log.info("==================================================");
        log.info("Get Coloso courses..");
        getAndSaveCourses(categories);

        log.info("==================================================");
        log.info("Coloso Crawler End");
        log.info("==================================================");
    }

    private ColosoCategoriesResponse getCategories() {

        ColosoCategoriesResponse response = apiService.get(CATEGORIES_URL, ColosoCategoriesResponse.class);

        if (response == null || response.getCategories() == null || response.getCategories().isEmpty()) {
            log.error("Coloso categories read failed. {}", CATEGORIES_URL);
            throw new IllegalStateException("Coloso categories read failed. " + CATEGORIES_URL);
        }

        return response;
    }

    private List<Category> convertCategories(ColosoCategoriesResponse response) {

        List<Category> convertedMainCategories = new ArrayList<>();
        List<ColosoCategoriesResponse.Category> mainCategories = response.getCategories();

        for (ColosoCategoriesResponse.Category mainCategory : mainCategories) {

            if (isHideMenu(mainCategory)) {
                continue;
            }

            List<ColosoCategoriesResponse.Category> subCategories = mainCategory.getChildren();

            List<Category> convertedSubCategories = new ArrayList<>();

            for (ColosoCategoriesResponse.Category subCategory : subCategories) {

                if (isHideMenu(mainCategory)) {
                    continue;
                }

                convertedSubCategories.add(new Category(subCategory.getId(), subCategory.getTitle().trim(), Collections.emptyList()));
            }

            if (convertedSubCategories.isEmpty()) {
                continue;
            }

            convertedMainCategories.add(new Category(mainCategory.getId(), mainCategory.getTitle().trim(), convertedSubCategories));
        }

        return convertedMainCategories;
    }

    private void validateCategories(List<Category> categories) {

        Map<String, Boolean> categoryExistenceMap = Arrays.stream(ColosoCategoryMap.values())
                .map(categoryMap -> categoryMap.getOriginalMainCategory() + "," + categoryMap.getOriginalSubCategory())
                .collect(Collectors.toMap(key -> key, value -> false));

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {

                String key = category.getTitle() + "," + subCategory.getTitle();
                Boolean exists = categoryExistenceMap.get(key);

                if (exists == null) {
                    log.error("Coloso categories validation failed, not match category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Coloso categories validation failed, not match category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                }

                if (exists) {
                    log.error("Coloso categories validation failed, duplicated category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Coloso categories validation failed, duplicated category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
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

        List<Course> colosoCourses = new ArrayList<>();

        String mainCategoryTitle = category.getTitle();
        String subCategoryTitle = subCategory.getTitle();
        Long subCategoryId = subCategory.getId();

        String url = UriComponentsBuilder.fromUriString(COURSES_URL)
                .buildAndExpand(subCategoryId.toString())
                .toUriString();

        // 강의 목록 페이지
        Document document;
        try {
            document = apiService.get(url);
        } catch (Exception exception) {
            log.error("Courses fetch failed. {}", url);

            return Collections.emptyList();
        }

        Elements listElements = document.select("section > ul > li");

        for (Element listElement : listElements) {
            Element anchorElement = listElement.select("> a").first();
            if (anchorElement == null) {
                log.error("Course url read failed, anchor tag does not exists. {}", url);
                throw new IllegalStateException("Course url read failed, anchor tag does not exists. " + url);
            }

            String courseUrl = anchorElement.attr("abs:href");

            // 강의 상세 페이지
            try {
                document = apiService.get(courseUrl);
            } catch (Exception exception) {
                log.error("Course fetch failed. {}", courseUrl);
                continue;
            }

            // 강의 상세 페이지 script 요소 조회(type="application/ld+json")
            Element scriptElement = document.select("script[type=\"application/ld+json\"]").first();
            if (scriptElement == null) {
                log.error("<script type\"application/ld+json\"> does not exists.");
                continue;
            }

            String json = scriptElement.data();
            Product product;

            try {
                product = objectMapper.readValue(json, Product.class);
            } catch (Exception exception) {
                log.error("application/ld+json parsing failed.");
                continue;
            }

            Long id = product.getProductId();

            List<Product.Offer> offers = product.getOffers();
            if (offers.isEmpty()) {
                log.error("Product offer is empty.");
                continue;
            }

            Product.Offer offer = offers.get(0);
            List<Product.Offer.PriceSpecification> priceSpecifications = offer.getPriceSpecifications();
            if (priceSpecifications.isEmpty()) {
                log.error("Price specification is empty.");
                continue;
            }

            Product.Offer.PriceSpecification priceSpecification = priceSpecifications.get(0);
            Long price = priceSpecification.getPrice();


            url = UriComponentsBuilder.fromUriString(COURSE_URL)
                    .queryParam("id", id.toString())
                    .build()
                    .toUriString();

            // 강의 API
            ColosoCoursesResponse response;
            try {
                response = apiService.get(url, ColosoCoursesResponse.class);
            } catch (Exception exception) {
                log.error("Course fetch failed. {}", url);
                continue;
            }

            if (response == null) {
                log.error("Course read failed. {}", url);
                continue;
            }

            List<ColosoCoursesResponse.Course> courses = response.getCourses();
            if (courses.isEmpty()) {
                log.error("Course is empty.");
                continue;
            }

            ColosoCoursesResponse.Course course = courses.get(0);

            String title = course.getPublicTitle();
            String instructor = course.getPublicDescription();
            String keywords = course.getKeywords();
            String imageUrl = course.getDesktopCardAsset();

            StringBuilder sb = new StringBuilder();
            ColosoCoursesResponse.Course.Extras extras = course.getExtras();
            if (extras != null) {
                String text1 = extras.getAdditionalText1();
                String text2 = extras.getAdditionalText2();
                String text3 = extras.getAdditionalText3();
                if (StringUtils.hasText(text1)) {
                    sb.append(text1);
                }

                if (StringUtils.hasText(text2)) {
                    sb.append(" ");
                    sb.append(text2);
                }

                if (StringUtils.hasText(text3)) {
                    sb.append(" ");
                    sb.append(text3);
                }
            }

            String description = sb.toString();

            Course colosoCourse = new Course(id,
                    title,
                    price,
                    description,
                    keywords,
                    instructor,
                    mainCategoryTitle,
                    subCategoryTitle,
                    courseUrl,
                    imageUrl
            );
            log.info("{}", colosoCourse);

            colosoCourses.add(colosoCourse);
        }

        return colosoCourses;
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

            Optional<ColosoCategoryMap> convertedCategory = Arrays.stream(ColosoCategoryMap.values()).filter(colosoCategoryMap ->
                            colosoCategoryMap.getOriginalMainCategory().equals(mainCategory) &&
                                    colosoCategoryMap.getOriginalSubCategory().equals(subCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("Coloso category map conversion failed. Main Category: {}, Sub Category: {}", mainCategory, subCategory);
                throw new IllegalStateException("Coloso category map conversion failed. Main Category: " + mainCategory + " " + "Sub Category: " + subCategory);
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

    private boolean isHideMenu(ColosoCategoriesResponse.Category category) {

        ColosoCategoriesResponse.Category.Extras extras = category.getExtras();

        Boolean hideMenu = extras.getHideMenu();

        return hideMenu != null && hideMenu;
    }
}
