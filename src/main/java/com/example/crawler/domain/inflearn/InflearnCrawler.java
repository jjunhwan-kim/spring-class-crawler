package com.example.crawler.domain.inflearn;

import com.example.crawler.domain.common.ApiService;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InflearnCrawler {

    private static final String SOURCE = "inflearn";
    private static final String COURSE_URL = "https://www.inflearn.com/courses";
    private final ObjectMapper objectMapper;
    private final ApiService apiService;
    private final LectureService lectureService;

    public void get() {

        log.info("==================================================");
        log.info("Inflearn Crawler Start");
        log.info("==================================================");
        log.info("Get Inflearn categories..");
        List<Category> categories = getAndConvertCategories();
        validateCategories(categories);

        log.info("==================================================");
        log.info("Get Inflearn courses..");
        //getAndSaveCourses(categories);

        log.info("==================================================");
        log.info("Inflearn Crawler End");
        log.info("==================================================");
    }

    private List<Category> getAndConvertCategories() {

        List<Category> convertedMainCategories = new ArrayList<>();

        Document document;

        try {
            document = apiService.get(COURSE_URL);
        } catch (Exception exception) {
            log.error("Inflearn categories read failed.");
            throw new IllegalStateException("Inflearn categories read failed.");
        }

        Elements mainCategoryDivisions = document.select("nav > div.accordion");

        for (Element mainCategoryDivision : mainCategoryDivisions) {

            Element mainCategory = mainCategoryDivision.select("> div > p.accordion-header-text").first();

            if (mainCategory == null) {
                continue;
            }

            String mainCategoryTitle = mainCategory.text().trim();

            List<Category> convertedSubCategories = new ArrayList<>();

            Elements subCategories = mainCategoryDivision.select("> div.accordion-body > a.accordion-content");

            for (Element subCategory : subCategories) {
                String subCategoryTitle = subCategory.text().trim();
                String url = subCategory.attr("abs:href");

                convertedSubCategories.add(new Category(
                        null,
                        subCategoryTitle,
                        url,
                        Collections.emptyList()));
            }

            if (convertedSubCategories.isEmpty()) {
                continue;
            }

            convertedMainCategories.add(new Category(null,
                    mainCategoryTitle,
                    "",
                    convertedSubCategories));
        }

        return convertedMainCategories;
    }

    private void validateCategories(List<Category> categories) {

        Map<String, Boolean> categoryExistenceMap = Arrays.stream(InflearnCategoryMap.values())
                .map(categoryMap -> categoryMap.getOriginalMainCategory() + "," + categoryMap.getOriginalSubCategory())
                .collect(Collectors.toMap(key -> key, value -> false));

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {

                String key = category.getTitle() + "," + subCategory.getTitle();
                Boolean exists = categoryExistenceMap.get(key);

                if (exists == null) {
                    log.error("Inflearn categories validation failed, not match category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Coloso categories validation failed, not match category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                }

                if (exists) {
                    log.error("Inflearn categories validation failed, duplicated category. Main Category: {}, Sub Category: {}", category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException("Coloso categories validation failed, duplicated category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                } else {
                    categoryExistenceMap.put(key, true);
                }
            }
        }
    }

    private List<InflearnCourse> getCourses(List<InflearnCategory> categories) {

        List<InflearnCourse> courses = new ArrayList<>();

        for (InflearnCategory category : categories) {

            log.info("==================================================");
            log.info("Get inflearn courses from {}", category);
            log.info("==================================================");

            String categoryUrl = category.getUrl();
            Connection connection = Jsoup.connect(categoryUrl);

            long lastPageNumber;

            try {
                Document document = connection.get();

                Element lastPageListElement = document.select("ul.pagination-list > li:last-of-type").first();
                if (lastPageListElement == null) {
                    log.error("Inflearn course document pagination list parsing failed, {}", category);
                    continue;
                }

                Element lastPageAnchor = lastPageListElement.select("a").first();
                if (lastPageAnchor == null) {
                    log.error("Inflearn course document pagination list parsing failed, {}", category);
                    continue;
                }
                String lastPage = lastPageAnchor.text();
                log.info("ColosoCategoryMap last page is {}", lastPage);

                lastPageNumber = Long.parseLong(lastPage);

            } catch (IOException exception) {
                log.error("Inflearn course document read failed", exception);
                continue;
            } catch (Exception exception) {
                log.error("Inflearn course document parsing failed", exception);
                continue;
            }

            for (int i = 1; i <= lastPageNumber; i++) {
                String courseListPageUrl = categoryUrl + "?page=" + i;
                courses.addAll(getCourses(courseListPageUrl));
            }

            log.info("==================================================");
        }

        return courses;
    }

    private List<InflearnCourse> getCourses(String url) {

        List<InflearnCourse> courses = new ArrayList<>();

        Connection connection = Jsoup.connect(url);

        try {
            Document document = connection.get();

            log.info("==================================================");
            log.info("Get inflearn courses from {}", url);
            log.info("==================================================");

            Elements courseDivisions = document.select("div.card.course.course_card_item");

            for (Element courseDivision : courseDivisions) {

                Element courseAnchor = courseDivision.select("> a").first();

                if (courseAnchor == null) {
                    log.error("Inflearn course document course anchor tag parsing failed");
                    continue;
                }

                String courseUrl = courseAnchor.attr("abs:href");
                Element courseImage = courseAnchor.select("div.card-image > figure > img").first();

                if (courseImage == null) {
                    courseImage = courseAnchor.select("div.card-image > section > video > source").first();
                    if (courseImage == null) {
                        log.error("Inflearn course document course image tag parsing failed");
                        continue;
                    }
                }

                String courseImageUrl = courseImage.attr("abs:src");
                courseImageUrl = courseImageUrl.replace(".mp4", "");

                if (!StringUtils.hasText(courseImageUrl)) {
                    log.error("Inflearn course document course image tag parsing failed");
                    continue;
                }

                Element courseDataDivision = courseAnchor.select("div.course-data").first();

                if (courseDataDivision == null) {
                    log.error("Inflearn course document course data division tag parsing failed");
                    continue;
                }

                String courseDataJsonString = courseDataDivision.attr("fxd-data");
                InflearnCourse course = null;
                try {
                    course = objectMapper.readValue(courseDataJsonString, InflearnCourse.class);
                } catch (IOException exception) {
                    log.error("Inflearn course document course data json string parsing failed");
                }

                if (course == null) {
                    continue;
                }

                course.updateUrl(courseUrl, courseImageUrl);

                log.info("Course, {}", course);
                courses.add(course);
            }
        } catch (IOException exception) {
            log.error("Inflearn course document read failed", exception);
        } catch (Exception exception) {
            log.error("Inflearn course document parsing failed", exception);
        }

        return courses;
    }

    public List<Lecture> convertCourses(List<InflearnCourse> courses) {

        List<Lecture> lectures = new ArrayList<>();
        // 중복 제거
        Set<Long> sourceIds = new HashSet<>();

        for (InflearnCourse course : courses) {

            Long id = course.getId();
            if (sourceIds.contains(id)) {
                continue;
            } else {
                sourceIds.add(id);
            }

            String title = course.getTitle().replaceAll(" {2,}", " ").trim();
            String instructor = course.getInstructor().replaceAll(" {2,}", " ").trim();
            String price = course.getSellingPrice().replaceAll(" {2,}", " ").trim();
            String originalMainCategory = course.getFirstCategory().replaceAll(" {2,}", " ").trim();
            String originalSubCategory = course.getSecondCategory().replaceAll(" {2,}", " ").trim();
            String tag = course.getTag().replaceAll(" {2,}", " ").trim();
            String courseUrl = course.getCourseUrl().replaceAll(" {2,}", " ").trim();
            String courseImageUrl = course.getCourseImageUrl().replaceAll(" {2,}", " ").trim();
            String convertedOriginalMainCategory = originalMainCategory.split(",")[0].trim();
            String convertedOriginalSubCategory = originalSubCategory.split(",")[0].trim();

            Optional<InflearnCategoryMap> convertedCategory = Arrays.stream(InflearnCategoryMap.values()).filter(inflearnCategoryMap ->
                            inflearnCategoryMap.getOriginalMainCategory().equals(convertedOriginalMainCategory) &&
                                    inflearnCategoryMap.getOriginalSubCategory().equals(convertedOriginalSubCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("ColosoCategoryMap conversion failed! Main ColosoCategoryMap: {}, Sub ColosoCategoryMap: {}", originalMainCategory, originalSubCategory);
                continue;
            }

            Lecture lecture = new Lecture(
                    title,
                    SOURCE,
                    id.toString(),
                    courseUrl,
                    price,
                    instructor,
                    courseImageUrl,
                    originalMainCategory,
                    originalSubCategory,
                    convertedCategory.get().getConvertedMainCategory(),
                    convertedCategory.get().getConvertedSubCategory(),
                    tag,
                    "");

            lectures.add(lecture);
        }

        return lectures;
    }
}
