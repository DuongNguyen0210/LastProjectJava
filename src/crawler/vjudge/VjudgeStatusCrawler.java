package crawler.vjudge;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.LastProjectJava;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class VjudgeStatusCrawler {
    
    private static class SubmissionTask {
        String solutionId;
        String language;
        long submissionTimeMs;

        public SubmissionTask(String solutionId, String language, long submissionTimeMs) {
            this.solutionId = solutionId;
            this.language = language;
            this.submissionTimeMs = submissionTimeMs;
        }
    }

    public static int fetchUserSubmissions(String username, int daysLimit) {
        int successfulCrawls = 0;
        WebDriver driver = VjudgeHtmlScraper.getDriver();

        if (driver == null) {
            System.out.println("Trình duyệt chưa được mở!");
            return 0;
        }

        String url = String.format("https://vjudge.net/status#un=%s&OJId=All&probNum=&res=1", username);

        List<SubmissionTask> pendingTasks = new ArrayList<>();

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[contains(@class, 'table')]//tbody//tr//a[contains(text(), '" + username + "')]")));

            List<WebElement> rows = driver.findElements(By.cssSelector("table.table tbody tr"));
            long limitTimestampMs = System.currentTimeMillis() - (daysLimit * 24L * 60 * 60 * 1000L);

            for (WebElement row : rows)
            {
                try {
                    WebElement timeDiv = row.findElement(By.cssSelector("td.date div.localizedTime"));
                    long submissionTimeMs = Long.parseLong(timeDiv.getAttribute("data-time"));

                    if (submissionTimeMs < limitTimestampMs) {
                        System.out.println("-> Đã chạm mốc bài nộp cũ (" + daysLimit + " ngày). Dừng quét bảng!");
                        break;
                    }

                    String solutionId = row.getAttribute("id");
                    if (solutionId == null || solutionId.isEmpty()) continue;

                    String rowClass = row.getAttribute("class");
                    if (rowClass == null || !rowClass.contains("accepted")) {
                        continue;
                    }

                    String language = "Unknown";
                    try {
                        WebElement langElement = row.findElement(By.cssSelector("td.language div.view-solution"));
                        language = langElement.getText().trim();
                    } catch (Exception ignored) {}

                    pendingTasks.add(new SubmissionTask(solutionId, language, submissionTimeMs));

                } catch (Exception e) {
                    System.out.println("Lỗi đọc DOM một dòng: " + e.getMessage());
                }
            }

            for (SubmissionTask task : pendingTasks)
            {
                String code = VjudgeHtmlScraper.getSourceCode(task.solutionId);

                if (code != null && !code.isEmpty()) {
                    java.sql.Timestamp submittedAt = new java.sql.Timestamp(task.submissionTimeMs);
                    LastProjectJava.saveSubmission(task.solutionId, username, "Vjudge", code, task.language, submittedAt);
                    successfulCrawls++;
                } else {
                    System.out.println("that bai");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return successfulCrawls;
    }
}