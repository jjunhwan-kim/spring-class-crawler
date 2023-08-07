package com.example.crawler.inflearn;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InflearnCrawler {
    private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";
    private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";
    private static final String WEB_DRIVER_PATH = "/Users/junhwan/Desktop/projects/crawler/chromedriver";
    private WebDriver driver;

    public InflearnCrawler() {

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
        //driver = new ChromeDriver(options);
    }

    public void activate() {

        String url = "https://www.inflearn.com/courses?types=ONLINE";

        List<String> categoryClassList = List.of(
                "accordion_5",
                "accordion_492",
                "accordion_9",
                "accordion_39306",
                "accordion_178677",
                "accordion_22",
                "accordion_33",
                "accordion_493",
                "accordion_494",
                "accordion_666");


        try {
            driver.get(url);
            Thread.sleep(2000); // 페이지 로딩 대기 시간
            String pageSource = driver.getPageSource();
            Document document = Jsoup.parse(pageSource);

            for (String categoryClass : categoryClassList) {

                Element categoryElement = document.getElementById(categoryClass);

                String category = categoryElement.getElementsByClass("accordion-header-text").first().text();

                for (Element element : categoryElement.getElementsByClass("accordion-content")) {
                    String subCategory = element.text();

                    System.out.println(category + " - " + subCategory);

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