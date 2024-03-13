package com.example.crawler.domain.coloso;

import com.example.crawler.domain.coloso.response.ColosoCategoriesResponse;
import com.example.crawler.domain.coloso.response.ColosoCourseResponse;
import com.example.crawler.domain.common.Category;
import com.example.crawler.domain.lecture.Lecture;
import com.example.crawler.domain.lecture.LectureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ColosoCrawler {

    private static final String SOURCE = "coloso";
    private static final String CATEGORIES_URL = "https://coloso.co.kr/api/displays";
    private static final String CATEGORY_COURSES_URL = "https://coloso.co.kr/category";
    private static final String COURSE_URL = "https://coloso.co.kr/api/catalogs/courses";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LectureService lectureService;

    public void get() {
        log.info("==================================================");
        log.info("Coloso Crawler Start");
        log.info("==================================================");
        log.info("Get Coloso categories..");
        ColosoCategoriesResponse response = getCategories();
        List<Category> categories = convertCategories(response);

        log.info("==================================================");
        log.info("Get Coloso coureses..");
        List<ColosoCourse> courses = getCourses(categories);
        List<Lecture> lectures = convertCourses(courses);

        log.info("==================================================");
        log.info("Save Coloso coureses..");
        lectureService.saveOrUpdateLectures(SOURCE, lectures);

        log.info("==================================================");
        log.info("Coloso Crawler End");
        log.info("==================================================");
    }

    private ColosoCategoriesResponse getCategories() {

        ColosoCategoriesResponse response = restTemplate.getForObject(CATEGORIES_URL, ColosoCategoriesResponse.class);

        try {
            Thread.sleep(500);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }

        if (response == null) {
            throw new IllegalStateException("Coloso category list read failed");
        }

        return response;
    }

    private boolean isHideMenu(ColosoCategoriesResponse.Category category) {

        ColosoCategoriesResponse.Category.Extras extras = category.getExtras();

        Boolean hideMenu = extras.getHideMenu();

        return hideMenu != null && hideMenu;
    }

    private List<Category> convertCategories(ColosoCategoriesResponse response) {

        List<Category> convertedCategories = new ArrayList<>();
        List<ColosoCategoriesResponse.Category> mainCategories = response.getData();

        for (ColosoCategoriesResponse.Category mainCategory : mainCategories) {

            if (isHideMenu(mainCategory)) {
                continue;
            }

            List<ColosoCategoriesResponse.Category> subCategories = mainCategory.getChildren();

            List<Category> subCategoryList = new ArrayList<>();

            for (ColosoCategoriesResponse.Category subCategory : subCategories) {

                if (isHideMenu(mainCategory)) {
                    continue;
                }

                subCategoryList.add(new Category(mainCategory.getId(), subCategory.getTitle(), Collections.emptyList()));
            }

            if (subCategoryList.isEmpty()) {
                continue;
            }

            convertedCategories.add(new Category(mainCategory.getId(), mainCategory.getTitle(), subCategoryList));
        }

        return convertedCategories;
    }

    private void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }


    private List<ColosoCourse> getCourses(List<Category> categories) {

        List<ColosoCourse> colosoCourses = new ArrayList<>();

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {

                String mainCategoryTitle = category.getTitle();
                String subCategoryTitle = subCategory.getTitle();
                Long subCategoryId = subCategory.getId();

                String url = CATEGORY_COURSES_URL + "/" + subCategoryId;

                try {
                    log.info("Main Category: {}, Sub Category: {}", mainCategoryTitle, subCategoryTitle);

                    Connection connection = Jsoup.connect(url);
                    Document document = connection.get();
                    delay(500);

                    Elements listElements = document.select("section > ul > li");

                    for (Element listElement : listElements) {
                        Element anchorElement = listElement.select("> a").first();
                        if (anchorElement == null) {
                            log.error("Course url read failed, anchor tag does not exists, {}, {}", mainCategoryTitle, subCategoryTitle);
                            continue;
                        }

                        String courseUrl = anchorElement.attr("abs:href");

                        // 강의 상세 조회
                        connection = Jsoup.connect(courseUrl);

                        try {
                            document = connection.get();
                        } catch (Exception exception) {
                            log.error("Course read failed, {}, {}", mainCategoryTitle, subCategoryTitle);
                            continue;
                        }

                        Element scriptElement = document.select("script[type=\"application/ld+json\"]").first();
                        if (scriptElement == null) {
                            log.error("<script type\"application/ld+json\"> does not exists");
                            continue;
                        }

                        String json = scriptElement.data();
                        Product product;

                        try {
                            product = objectMapper.readValue(json, Product.class);
                        } catch (Exception exception) {
                            log.error("application/ld+json parsing failed");
                            continue;
                        }

                        Long id = product.getProductId();

                        List<Product.Offer> offers = product.getOffers();
                        if (offers.isEmpty()) {
                            log.error("Product offer is empty");
                            continue;
                        }

                        Product.Offer offer = offers.get(0);
                        List<Product.Offer.PriceSpecification> priceSpecifications = offer.getPriceSpecifications();
                        if (priceSpecifications.isEmpty()) {
                            log.error("Price specification is empty");
                            continue;
                        }
                        Product.Offer.PriceSpecification priceSpecification = priceSpecifications.get(0);
                        Long price = priceSpecification.getPrice();

                        ColosoCourseResponse response = restTemplate.getForObject(COURSE_URL + "?id=" + id.toString(), ColosoCourseResponse.class);
                        delay(500);

                        if (response == null) {
                            log.error("Course read failed");
                            continue;
                        }

                        List<ColosoCourseResponse.Course> courses = response.getCourses();
                        if (courses.isEmpty()) {
                            log.error("Price specification is empty");
                            continue;
                        }

                        ColosoCourseResponse.Course course = courses.get(0);

                        String title = course.getPublicTitle();
                        String instructor = course.getPublicDescription();
                        String keywords = course.getKeywords();
                        String imageUrl = course.getDesktopCardAsset();

                        StringBuilder sb = new StringBuilder();
                        ColosoCourseResponse.Course.Extras extras = course.getExtras();
                        if (extras != null) {
                            String text1 = extras.getAdditionalText1();
                            String text2 = extras.getAdditionalText2();
                            String text3 = extras.getAdditionalText3();
                            if (StringUtils.hasText(text1)) {
                                if (StringUtils.hasText(sb.toString())) {
                                    sb.append(" ");
                                }
                                sb.append(text1);
                            }
                            if (StringUtils.hasText(text2)) {
                                if (StringUtils.hasText(sb.toString())) {
                                    sb.append(" ");
                                }
                                sb.append(text2);
                            }
                            if (StringUtils.hasText(text3)) {
                                if (StringUtils.hasText(sb.toString())) {
                                    sb.append(" ");
                                }
                                sb.append(text3);
                            }
                        }

                        String description = "";
                        if (StringUtils.hasText(sb.toString())) {
                            description = sb.toString().replaceAll("\\s", " ").replaceAll(" {2,}", " ");
                        }

                        ColosoCourse colosoCourse = new ColosoCourse(id,
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
                        colosoCourses.add(colosoCourse);
                        log.info("{}", colosoCourse);
                    }

                } catch (Exception exception) {
                    log.error("Coloso courses read failed, {}, {}", mainCategoryTitle, subCategoryTitle);
                }
            }
        }

        return colosoCourses;
    }

    public List<Lecture> convertCourses(List<ColosoCourse> courses) {

        List<Lecture> lectures = new ArrayList<>();
        // 중복 제거
        Set<Long> sourceIds = new HashSet<>();

        for (ColosoCourse course : courses) {

            Long id = course.getId();
            if (sourceIds.contains(id)) {
                continue;
            } else {
                sourceIds.add(id);
            }

            String title = course.getTitle().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            Long price = course.getPrice();
            String description = course.getDescription().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String keywords = course.getKeywords().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String instructor = course.getInstructor().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String mainCategory = course.getMainCategory().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String subCategory = course.getSubCategory().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String url = course.getUrl().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String imageUrl = course.getImageUrl().replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();

            Optional<ColosoCategoryMap> convertedCategory = Arrays.stream(ColosoCategoryMap.values()).filter(colosoCategoryMap ->
                            colosoCategoryMap.getOriginalMainCategory().equals(mainCategory) &&
                                    colosoCategoryMap.getOriginalSubCategory().equals(subCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("ColosoCategoryMap conversion failed! Main ColosoCategoryMap: {}, Sub ColosoCategoryMap: {}", mainCategory, subCategory);
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
