package com.example.crawler.inflearn;

import com.example.crawler.domain.Lecture;
import com.example.crawler.domain.LectureService;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class InflearnCrawler {

    private static final String SOURCE = "inflearn";
    private static final String URL = "https://www.inflearn.com";
    private static final String COURSE_URL = URL + "/courses";
    private final ObjectMapper objectMapper;
    private final LectureService lectureService;

    public void get() {

        log.info("==================================================");
        log.info("Get inflearn categories");
        log.info("==================================================");
        List<InflearnCategory> categories = getCategories();
        log.info("==================================================");

        log.info("Get inflearn courses");
        log.info("==================================================");
        List<InflearnCourse> courses = getCourses(categories);

        log.info("Convert inflearn courses to lectures and save lectures");
        log.info("==================================================");
        List<Lecture> lectures = convertCourses(courses);
        lectureService.saveOrUpdateLectures(SOURCE, lectures);
    }

    private List<InflearnCategory> getCategories() {

        Connection conn = Jsoup.connect(COURSE_URL);

        List<InflearnCategory> categories = new ArrayList<>();

        try {
            Document document = conn.get();

            Elements mainCategoryDivisions = document.select("nav > div.accordion");

            for (Element mainCategoryDivision : mainCategoryDivisions) {

                Element mainCategory = mainCategoryDivision.select("> div > p.accordion-header-text").first();

                if (mainCategory == null) {
                    continue;
                }

                Elements subCategories = mainCategoryDivision.select("> div.accordion-body > a.accordion-content");

                for (Element subCategory : subCategories) {
                    String mainCategoryName = mainCategory.text();
                    String subCategoryName = subCategory.text();
                    String url = subCategory.attr("abs:href");

                    log.info("Main={}, Sub={}, URL={}", mainCategoryName, subCategoryName, url);
                    categories.add(new InflearnCategory(mainCategoryName, subCategoryName, url));
                }
            }
        } catch (IOException exception) {
            log.error("Inflearn course categories document read failed", exception);
            throw new RuntimeException(exception);
        } catch (RuntimeException exception) {
            log.error("Inflearn course categories document parsing failed", exception);
            throw exception;
        }

        return categories;
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
                log.info("Category last page is {}", lastPage);

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

            Optional<Category> convertedCategory = Arrays.stream(Category.values()).filter(category ->
                            category.getOriginalMainCategory().equals(convertedOriginalMainCategory) &&
                                    category.getOriginalSubCategory().equals(convertedOriginalSubCategory))
                    .findFirst();

            if (convertedCategory.isEmpty()) {
                log.error("Category conversion failed! Main Category: {}, Sub Category: {}", originalMainCategory, originalSubCategory);
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
