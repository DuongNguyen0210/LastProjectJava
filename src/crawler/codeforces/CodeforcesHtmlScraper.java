package crawler.codeforces;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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

	public static boolean autoLogin(String username, String password) {
		if (driver == null) {
			System.out.println("Browser not opened!");
			return false;
		}

		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

			// 1. Chờ cho ô nhập tài khoản xuất hiện
			WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("handleOrEmail")));

			// 2. Dùng tổ hợp phím Ctrl + A và Delete để xóa triệt để nội dung do tính năng tự điền của trình duyệt
			usernameInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
			Thread.sleep(500); // Đợi 1 chút sau khi xóa
			usernameInput.sendKeys(username);

			// 3. Tìm, xóa sạch bằng tổ hợp phím và điền mật khẩu
			WebElement passwordInput = driver.findElement(By.id("password"));
			passwordInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
			Thread.sleep(500); // Đợi 1 chút sau khi xóa
			passwordInput.sendKeys(password);

			// 4. Tìm nút "Bấm để đăng nhập" (thường là class submit hoặc value Login)
			WebElement loginButton = driver.findElement(By.className("submit"));
			loginButton.click();

			// 5. Chờ quá trình đăng nhập hoàn tất (URL có thay đổi hoặc ẩn form)
			wait.until(ExpectedConditions.urlContains("codeforces.com"));
			Thread.sleep(3000);

			System.out.println("Đăng nhập tự động thành công!");
			return true;

		} catch (Exception e) {
			System.err.println("Lỗi tự động đăng nhập: " + e.getMessage());
			// Nếu đã đăng nhập từ trước và không tìm thấy form đăng nhập (tức là đã ở trang chủ)
			if (driver.getCurrentUrl().equals("https://codeforces.com/") || driver.getCurrentUrl().contains("codeforces.com")) {
				System.out.println("Trình duyệt đã ghi nhớ đăng nhập từ trước!");
				return true;
			}
			return false;
		}
	}

	public static String getSourceCode(String contestId, String submitId) {
		if (driver == null) {
			System.out.println("Browser not opened!");
			return null;
		}

		String url = String.format("%s/contest/%s/submission/%s", BASE_URL, contestId, submitId);

		try {
			// Tăng thời gian delay ngẫu nhiên lên 5-10 giây để tránh bị ban
			long delay = 5000 + (long) (Math.random() * 5000);
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
