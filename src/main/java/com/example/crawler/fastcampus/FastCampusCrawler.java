package com.example.crawler.fastcampus;

import com.example.crawler.domain.OnlineClass;
import com.example.crawler.domain.OnlineClassService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class FastCampusCrawler {
    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "/Users/junhwan/Desktop/projects/crawler/chromedriver";
    private WebDriver driver;
    private final OnlineClassService onlineClassService;

    public FastCampusCrawler(OnlineClassService onlineClassService) {

        this.onlineClassService = onlineClassService;

        // WebDriver 경로 설정
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        // 2. WebDriver 옵션 설정
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--start-maximized");
        options.addArguments("--disable-popup-blocking");             // 팝업 안띄움
        options.addArguments("headless");                             // 브라우저 안띄움
        options.addArguments("--disable-gpu");                        // gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); // 이미지 다운 안받음

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2); // 브라우저 알림 차단
        options.setExperimentalOption("prefs", prefs);

        options.setBinary("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
        driver = new ChromeDriver(options);
    }

    public void activate() {

        try {
            for (FastCampusCategory category : FastCampusCategory.values()) {

                String mainCategory = category.getMainCategory();
                String subCategory = category.getSubCategory();
                String url = category.getUrl();

                try {
                    driver.get(url);

                    Thread.sleep(2000); // 페이지 로딩 대기 시간

                    // 자바스크립트 실행을 위한 JavascriptExecutor 인터페이스 사용
                    JavascriptExecutor js = (JavascriptExecutor) driver;

                    // 먼저 스크롤을 다 해서 모든 강의 리스트 출력
                    Long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");

                    // 페이지 끝까지 스크롤하는 자바스크립트 코드 실행
                    while (true) {

                        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                        Thread.sleep(2000); // 페이지 로딩 대기 시간

                        Long height = (Long) js.executeScript("return document.body.scrollHeight");

                        if (Objects.equals(height, lastHeight)) {
                            break;
                        }

                        lastHeight = height;
                    }

                    // 페이지 파싱
                    String pageSource = driver.getPageSource();
                    Document document = Jsoup.parse(pageSource);
                    Elements courses = document.getElementsByClass("infinity-course");

                    List<FastCampusClassInfo> classInfoList = new ArrayList<>();

                    for (Element course : courses) {
                        Elements containers = course.getElementsByClass("card__container");

                        for (Element container : containers) {
                            String title = container.getElementsByClass("card__title").first().text().replaceAll(LINE_SEPARATOR_PATTERN, "");
                            String content = container.getElementsByClass("card__content").first().text().replaceAll(LINE_SEPARATOR_PATTERN, "");
                            String imageUrl = container.getElementsByClass("card__image").first().attr("src").replaceAll(LINE_SEPARATOR_PATTERN, "");

                            Elements list = container.getElementsByClass("card__labels");
                            Elements labels = list.first().getElementsByTag("li");

                            StringJoiner stringJoiner = new StringJoiner(",");
                            for (Element label : labels) {
                                String tag = label.text().replaceAll(LINE_SEPARATOR_PATTERN, "");
                                stringJoiner.add(tag);
                            }
                            String tags = stringJoiner.toString();

                            classInfoList.add(new FastCampusClassInfo(title, content, imageUrl, tags));
                        }
                    }

                    // 강의 링크 추출
                    for (FastCampusClassInfo classInfo : classInfoList) {
                        String classTitle = classInfo.getTitle();

                        boolean found = false;
                        Long height;

                        while (true) {
                            WebElement course = driver.findElement(By.className("infinity-course"));
                            List<WebElement> containers = course.findElements(By.className("card__container"));

                            for (WebElement container : containers) {
                            String title = container.findElement(By.className("card__title")).getText().replaceAll(LINE_SEPARATOR_PATTERN, "");

                                if (title.equals(classTitle)) {

                                    js.executeScript("arguments[0].scrollIntoView(true);", container);
                                    Thread.sleep(2000);

                                    container.click();
                                    Thread.sleep(2000);

                                    String currentUrl = driver.getCurrentUrl();
                                    classInfo.updateUrl(currentUrl);
                                    System.out.println(currentUrl);

                                    driver.get(url);
                                    Thread.sleep(2000);
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                break;
                            }

                            height = (Long) js.executeScript("return document.body.scrollHeight");

                            if (Objects.equals(height, lastHeight)) {
                                log.error("Class Not Found, {}", classTitle);
                                break;
                            } else {
                                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                                Thread.sleep(2000); // 페이지 로딩 대기 시간
                            }
                        }
                    }

                    // DB 저장
                    for (FastCampusClassInfo classInfo : classInfoList) {
                        onlineClassService.saveOrUpdate(
                                new OnlineClass(
                                        "Fast Campus",
                                        classInfo.getTitle(),
                                        classInfo.getUrl(),
                                        classInfo.getImageUrl(),
                                        null,
                                        null,
                                        mainCategory,
                                        subCategory,
                                        classInfo.getContent(),
                                        classInfo.getTags(),
                                        null
                                ));
                    }
                    log.info("Category {}-{}({}) Success({} Classes)", mainCategory, subCategory, url, classInfoList.size());
                } catch (Exception e) {
                    log.error("Category {}-{}({}) Failed", mainCategory, subCategory, url, e);
                }
            }
        } catch (Exception e) {
            log.error("Crawling Failed", e);
        } finally {
            driver.close(); // 5. 브라우저 종료
            driver.quit();
        }

        log.info("Crawling Finished");
    }
}