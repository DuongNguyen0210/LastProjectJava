package crawler.codeforces;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CodeforcesHtmlScraper {

	private static WebDriver driver;
	private static final String DRIVER_PATH = "msedgedriver.exe";
	private static final String BASE_URL = "https://codeforces.com";
	private static final int TIMEOUT_SECONDS = 15;
	private static final String CODE_ELEMENT_ID = "program-source-text";

	public static void initAndLogin() {
		try {
			// Mở trình duyệt bằng cmd với remote debugging port
			ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "msedge.exe",
					"--remote-debugging-port=9222",
					"--user-data-dir=C:\\CodeforcesProfile",
					"https://codeforces.com/enter");
			pb.start();

			// Đợi vài giây để trình duyệt kịp khởi động trước khi Selenium attach
			Thread.sleep(3000);

			System.setProperty("webdriver.edge.driver", DRIVER_PATH);
			EdgeOptions options = new EdgeOptions();

			// Kết nối Selenium vào trình duyệt đang mở thông qua debugger address
			options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

			driver = new EdgeDriver(options);
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static String getSourceCode(String contestId, String submitId) {
		if (driver == null) {
			System.out.println("Browser not opened!");
			return null;
		}

		String url = String.format("%s/contest/%s/submission/%s", BASE_URL, contestId, submitId);

		try {
			long delay = 2000 + (long) (Math.random() * 2000);
			Thread.sleep(delay);

			driver.get(url);

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
			WebElement codeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(CODE_ELEMENT_ID)));

			return codeElement.getText();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			return null;
		}
	}

	public static void quitDriver() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	public static WebDriver getDriver() {
		return driver;
	}
}
