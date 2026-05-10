package crawler.vjudge;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DatabaseHelper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VjudgeStatusCrawler {
    public static int fetchUserSubmissions(String username, int daysLimit) {
        int successfulCrawls = 0;
        Map<String, String> submissions = new LinkedHashMap<>();
        WebDriver driver = VjudgeHtmlScraper.getDriver();

        if (driver == null) {
            System.out.println("Trình duyệt chưa được mở!");
            return 0;
        }

        String url = String.format("https://vjudge.net/status#un=%s&OJId=All&probNum=&res=1", username);

        try {
            System.out.println("Đang quét lịch sử nộp bài của: " + username);
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));

            List<WebElement> rows = driver.findElements(By.cssSelector("table.table tbody tr"));

            long limitTimestampMs = System.currentTimeMillis() - (daysLimit * 24L * 60 * 60 * 1000L);

            for (WebElement row : rows) {
                long submissionTimeMs = 0;

                try {
                    // 1. LẤY THỜI GIAN CHUẨN TỪ THUỘC TÍNH ẨN 'data-ts'
                    WebElement dateCell = row.findElement(By.cssSelector("td.date"));
                    submissionTimeMs = Long.parseLong(dateCell.getAttribute("data-ts"));

                    // Nếu thời gian bài nộp nhỏ hơn mốc giới hạn (tức là đã quá cũ) -> Dừng quét
                    if (submissionTimeMs < limitTimestampMs) {
                        System.out.println("   -> Đã chạm mốc bài nộp cũ. Dừng quét Vjudge!");
                        break;
                    }
                } catch (Exception e) {
                    continue; // Lỗi đọc thời gian thì bỏ qua dòng này
                }

                // 2. LẤY ID BÀI NỘP
                String solutionId = row.getAttribute("id");
                if (solutionId == null || solutionId.isEmpty()) {
                    continue;
                }

                // 3. LẤY NGÔN NGỮ
                String language = "Unknown";
                try {
                    WebElement langElement = row.findElement(By.cssSelector("td.language div"));
                    language = langElement.getAttribute("data-bs-original-title");
                } catch (Exception ignored) {}

                // 4. TẠO TIMESTAMP VÀ LƯU DATABASE LUÔN
                java.sql.Timestamp submittedAt = new java.sql.Timestamp(submissionTimeMs);

                System.out.println("Tìm thấy ID: " + solutionId + " | Ngôn ngữ: " + language);

                // Gọi Scraper đi bế code về
                String code = VjudgeHtmlScraper.getSourceCode(solutionId);
                if (code != null && !code.isEmpty()) {
                    System.out.println("Đã lấy được code ID: " + solutionId);

                    // Bơm thẳng vào CSDL với đầy đủ 6 tham số (có submittedAt)
                    DatabaseHelper.saveSubmission(solutionId, username, "Vjudge", code, language, submittedAt);
                    successfulCrawls++;
                }
            }

        } catch (Exception e) {
            System.out.println("Lỗi quét bảng: " + e.getMessage());
        }

        return successfulCrawls;
    }
}