package com.example.crawler.domain.inflearn;

import com.example.crawler.domain.AbstractCrawler;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class InflearnCrawler extends AbstractCrawler {

    private static final String SOURCE = "Inflearn";
    private static final String COURSE_URL = "https://www.inflearn.com/courses";
    private final ObjectMapper objectMapper;
    private final ApiService apiService;
    private final LectureService lectureService;

    @Override
    protected String getSource() {
        return SOURCE;
    }

    public void get() {

        log.info("==================================================");
        log.info("Inflearn Crawler Start");
        log.info("==================================================");
        log.info("Get Inflearn categories..");
        List<Category> categories = getAndConvertCategories();
        printCategories(categories);
        validateCategories(categories, InflearnCategoryMap.values());

        log.info("==================================================");
        log.info("Get Inflearn courses..");
        getAndSaveCourses(categories);

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
                if (subCategory.hasClass("accordion-content--all")) {
                    continue;
                }
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

    public void getAndSaveCourses(List<Category> categories) {

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {
                String mainCategoryTitle = category.getTitle();
                String subCategoryTitle = subCategory.getTitle();

                log.info("Main Category: {}, Sub Category: {}", mainCategoryTitle, subCategoryTitle);

                try {
                    List<Course> courses = getCourses(category, subCategory);
                    List<Lecture> lectures = convertCourses(courses, InflearnCategoryMap.values());
                    lectureService.saveOrUpdateLectures(SOURCE, lectures);
                } catch (Exception exception) {
                    log.error("Main Category: {}, Sub Category: {} failed, {}", mainCategoryTitle, subCategoryTitle, exception.getMessage());
                }
            }
        }
    }

    private List<Course> getCourses(Category category, Category subCategory) {

        List<Course> inflearnCourses = new ArrayList<>();

        String url = subCategory.getUrl();

        // 강의 목록 페이지
        Document document;
        try {
            document = apiService.get(url);
        } catch (Exception exception) {
            log.error("Courses fetch failed. {}", url);
            return Collections.emptyList();
        }

        long lastPageNumber;

        Element lastPageListElement = document.select("ul.pagination-list > li:last-of-type").first();
        if (lastPageListElement == null) {
            log.error("Courses pagination list parsing failed. \"ul.pagination-list > li\" not found. {}", url);
            return Collections.emptyList();
        }

        Element lastPageAnchor = lastPageListElement.select("a").first();
        if (lastPageAnchor == null) {
            log.error("Courses pagination list parsing failed. anchor tag does not exists. {}", url);
            return Collections.emptyList();
        }

        try {
            String lastPage = lastPageAnchor.text();
            lastPageNumber = Long.parseLong(lastPage);
        } catch (Exception exception) {
            log.error("Courses pagination list parsing failed. last page number parsing failed. {}", url);
            return Collections.emptyList();
        }

        for (int i = 1; i <= lastPageNumber; i++) {
            String coursesUrl = url + "?page=" + i;

            inflearnCourses.addAll(getCourses(coursesUrl));
        }

        return inflearnCourses;
    }

    private List<Course> getCourses(String url) {

        List<Course> inflearnCourses = new ArrayList<>();

        // 강의 목록 페이지
        Document document;
        try {
            document = apiService.get(url);
        } catch (Exception exception) {
            log.error("Courses fetch failed. {}", url);
            return Collections.emptyList();
        }

        Elements courseDivisions = document.select("div.card.course.course_card_item");

        for (Element courseDivision : courseDivisions) {

            Element courseAnchor = courseDivision.select("> a").first();

            String courseUrl;
            if (courseAnchor == null) {
                log.error("Course anchor tag does not exist.");
                continue;
            }

            courseUrl = courseAnchor.attr("abs:href");

            String imageUrl = "";
            Element image = courseAnchor.select("div.card-image > figure > img").first();

            if (image == null) {
                image = courseAnchor.select("div.card-image > section > video > source").first();
                if (image == null) {
                    log.error("Course image tag does not exist.");
                } else {
                    imageUrl = image.attr("abs:src");
                }
            } else {
                imageUrl = image.attr("abs:src");
            }

            imageUrl = imageUrl.replace(".mp4", "");

            Element courseTitleDivision = courseAnchor.select("div.course_title").first();

            if (courseTitleDivision == null) {
                log.error("Course title division tag does not exist.");
                continue;
            }

            String courseTitle = courseTitleDivision.text();

            if (!StringUtils.hasText(courseTitle)) {
                log.error("Course title does not exist.");
                continue;
            }

            Element courseDataDivision = courseAnchor.select("div.course-data").first();

            if (courseDataDivision == null) {
                log.error("Course data division tag does not exist.");
                continue;
            }

            String courseDataJsonString = courseDataDivision.attr("fxd-data");

            // 강의 제목에 따옴표가 있는 경우 이스케이프 처리
            if (courseTitle.contains("\"")) {
                String escapedCourseTitle = courseTitle.replaceAll("\"", "\\\\\"");
                courseDataJsonString = courseDataJsonString.replace(courseTitle, escapedCourseTitle);
            }

            InflearnCourse course;
            try {
                course = objectMapper.readValue(courseDataJsonString, InflearnCourse.class);
            } catch (IOException exception) {
                log.error("Course data json string parsing failed.");
                continue;
            }

            if (course == null) {
                log.error("Course data json string parsing failed.");
                continue;
            }

            Long id = course.getId();
            String title = course.getTitle();
            String instructor = course.getInstructor();
            String price = course.getSellingPrice();
            String mainCategoryTitle = course.getFirstCategory().split(",")[0];
            String subCategoryTitle = course.getSecondCategory().split(",")[0];
            String tag = course.getTag();

            Course inflearnCourse = new Course(
                    id,
                    title,
                    price,
                    "",
                    tag,
                    instructor,
                    mainCategoryTitle,
                    subCategoryTitle,
                    courseUrl,
                    imageUrl);

            log.info("{}", inflearnCourse);

            inflearnCourses.add(inflearnCourse);
        }

        return inflearnCourses;
    }
}
