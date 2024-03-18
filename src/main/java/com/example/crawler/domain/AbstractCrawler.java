package com.example.crawler.domain;

import com.example.crawler.domain.common.Category;
import com.example.crawler.domain.common.Course;
import com.example.crawler.domain.lecture.Lecture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractCrawler {

    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";

    protected abstract String getSource();

    public <T extends Enum<T> & CategoryMap> void validateCategories(List<Category> categories, T[] categoryMaps) {

        Map<String, Boolean> categoryExistenceMap = Arrays.stream(categoryMaps)
                .map(categoryMap -> categoryMap.getOriginalMainCategory() + "," + categoryMap.getOriginalSubCategory())
                .collect(Collectors.toMap(key -> key, value -> false));

        for (Category category : categories) {

            for (Category subCategory : category.getSubCategories()) {

                String key = category.getTitle() + "," + subCategory.getTitle();
                Boolean exists = categoryExistenceMap.get(key);
                String source = getSource();

                if (exists == null) {
                    log.error("{} categories validation failed, not match category. Main Category: {}, Sub Category: {}", source, category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException(source + " Coloso categories validation failed, not match category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                }

                if (exists) {
                    log.error("{} categories validation failed, duplicated category. Main Category: {}, Sub Category: {}", source, category.getTitle(), subCategory.getTitle());
                    throw new IllegalStateException(source + " Coloso categories validation failed, duplicated category. Main Category: " + category.getTitle() + " " + "Sub Category: " + subCategory.getTitle());
                } else {
                    categoryExistenceMap.put(key, true);
                }
            }
        }
    }

    public <T extends Enum<T> & CategoryMap> List<Lecture> convertCourses(List<Course> courses, T[] categoryMaps) {

        List<Lecture> lectures = new ArrayList<>();
        Set<Long> sourceIds = new HashSet<>();

        for (Course course : courses) {

            // 중복 제거
            Long id = course.getId();
            if (id == null) {
                log.error("Course ID is null");
                continue;
            }

            if (sourceIds.contains(id)) {
                continue;
            } else {
                sourceIds.add(id);
            }

            if (!StringUtils.hasText(course.getTitle())) {
                log.error("Course title does not exist.");
                continue;
            }

            String title = course.getTitle().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            String price = course.getPrice();
            if (price == null) {
                price = "0";
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
            mainCategory = course.getMainCategory().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            subCategory = course.getSubCategory().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();

            if (StringUtils.hasText(course.getCourseUrl())) {
                url = course.getCourseUrl().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
                int index = url.indexOf("?");
                if (index != -1) {
                    url = url.substring(0, index);
                }
            } else {
                url = "";
            }
            if (StringUtils.hasText(course.getImageUrl())) {
                imageUrl = course.getImageUrl().replaceAll(LINE_SEPARATOR_PATTERN, " ").replaceAll("\\s", " ").replaceAll(" {2,}", " ").trim();
            } else {
                imageUrl = "";
            }

            Optional<T> convertedCategory = Arrays.stream(categoryMaps)
                    .filter(fastCampusCategoryMap ->
                            fastCampusCategoryMap.getOriginalMainCategory().equals(mainCategory) &&
                                    fastCampusCategoryMap.getOriginalSubCategory().equals(subCategory))
                    .findFirst();

            String source = getSource();

            if (convertedCategory.isEmpty()) {
                log.error("{} Category map conversion failed. Main Category: {}, Sub Category: {}", source, mainCategory, subCategory);
                throw new IllegalStateException(source + " Category map conversion failed. Main Category: " + mainCategory + " " + "Sub Category: " + subCategory);
            }

            String convertedMainCategory = convertedCategory.get().getConvertedMainCategory();
            String convertedSubCategory = convertedCategory.get().getConvertedSubCategory();

            Lecture lecture = new Lecture(title,
                    getSource(),
                    id.toString(),
                    url,
                    price,
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

    protected void printCategories(List<Category> categories) {
        for (Category category : categories) {
            List<Category> subCategories = category.getSubCategories();

            for (Category subCategory : subCategories) {
                log.info("{}\t{}\t{}", category.getTitle(), subCategory.getTitle(), subCategory.getUrl());
            }
        }
    }
}
