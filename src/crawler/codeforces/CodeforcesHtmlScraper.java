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
	public static volatile boolean shouldStop = false;

	private static final String DRIVER_PATH = "msedgedriver.exe";
	private static final String BASE_URL = "https://codeforces.com";
	private static final int TIMEOUT_SECONDS = 15;
	private static final String CODE_ELEMENT_ID = "program-source-text";

	public static void initAndLogin() {
		try {
			ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "msedge.exe", "--remote-debugging-port=9222",
					"--user-data-dir=C:\\CodeforcesProfile", "https://codeforces.com/enter");
			pb.start();

			// Đợi vài giây để trình duyệt kịp khởi động trước khi Selenium attach
			Thread.sleep(3000);

			System.setProperty("webdriver.edge.driver", DRIVER_PATH);
			EdgeOptions options = new EdgeOptions();

			// Kết nối Selenium vào trình duyệt đang mở thông qua debugger address
			options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

			driver = new EdgeDriver(options);

			// Giới hạn thời gian tải trang tối đa là 15 giây
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
		} catch (Exception e) {
			System.err.println("Lỗi: " + e.getMessage());
		}
	}

	public static boolean autoLogin(String username, String password) {
		if (driver == null) {
			System.out.println("Trình duyệt chưa được mở!");
			return false;
		}

		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

			// 1. Chờ cho ô nhập tài khoản xuất hiện
			WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("handleOrEmail")));

			// 2. Dùng tổ hợp phím Ctrl + A và Delete để xóa triệt để nội dung
			usernameInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
			Thread.sleep(500);
			usernameInput.sendKeys(username);

			// 3. Tìm, xóa sạch và điền mật khẩu
			WebElement passwordInput = driver.findElement(By.id("password"));
			passwordInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
			Thread.sleep(500);
			passwordInput.sendKeys(password);

			// 4. Tìm nút đăng nhập và click
			WebElement loginButton = driver.findElement(By.className("submit"));
			loginButton.click();

			// 5. Chờ quá trình đăng nhập hoàn tất
			wait.until(ExpectedConditions.urlContains("codeforces.com"));
			Thread.sleep(3000);

			System.out.println("Đăng nhập tự động thành công!");
			return true;

		} catch (Exception e) {
			System.err.println("Lỗi tự động đăng nhập: " + e.getMessage());
			if (driver.getCurrentUrl().equals("https://codeforces.com/")
					|| driver.getCurrentUrl().contains("codeforces.com")) {
				System.out.println("Trình duyệt đã ghi nhớ đăng nhập từ trước!");
				return true;
			}
			return false;
		}
	}

	public static String getSourceCode(String contestId, String submitId) {
		if (driver == null) {
			System.out.println("Trình duyệt chưa được mở!");
			return null;
		}

		if (shouldStop) {
			System.out.println("Người dùng đã yêu cầu dừng cào mã");
			return null;
		}

		String url = String.format("%s/contest/%s/submission/%s", BASE_URL, contestId, submitId);

		try {
			long delay = 5000 + (long) (Math.random() * 5000);
			Thread.sleep(delay);

			if (shouldStop) {
				System.out.println("Dừng trước khi tải trang");
				return null;
			}

			// ======================================================
			// BỘ MÁY RETRY CHỐNG KẸT TRANG (SELENIUM HANG)
			// ======================================================
			int maxRetries = 3;
			boolean pageLoaded = false;

			for (int attempt = 1; attempt <= maxRetries; attempt++) {
				// ✅ KIỂM TRA TRONG MỖI RETRY
				if (shouldStop) {
					return null;
				}

				try {
					System.out.println("Đang chuyển hướng đến: " + url + " (Thử lần " + attempt + ")");
					driver.get(url);
					pageLoaded = true;
					break; // Tải mượt mà thì phá vòng lặp đi tiếp
				} catch (org.openqa.selenium.TimeoutException e) {
					System.out.println("   [!] Lần " + attempt + " load quá lâu! Đang ép dừng và thử lại...");
					// Thần chú ngắt kết nối rác đang bị kẹt
					((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.stop();");
					Thread.sleep(2000); // Nghỉ 2s trước khi get lại

					// ✅ KIỂM TRA SAU TIMEOUT
					if (shouldStop) {
						return null;
					}
				}
			}

			if (!pageLoaded) {
				System.out.println("   [!] Thua! Đã thử " + maxRetries + " lần nhưng trang CF đang nghẽn.");
				return null; // Bỏ bài này, nhảy sang bài khác chứ không treo app
			}
			// ======================================================

			// ✅ KIỂM TRA TRƯỚC KHI CHỜ ELEMENT
			if (shouldStop) {
				return null;
			}

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
			WebElement codeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(CODE_ELEMENT_ID)));

			return codeElement.getText();

		} catch (Exception e) {
			System.err.println("Lỗi khi lấy code: " + e.getMessage());
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

	// ========== 2 METHOD MỚI (BẮT BUỘC) ==========

	/**
	 * ✅ Dừng cào bằng cách set flag + đóng trình duyệt
	 */
	public static void stopCrawler() {
		shouldStop = true;
		System.out.println("[DỪNG] Đã thiết lập cờ dừng");

		if (driver != null) {
			try {
				// Ép dừng bất kỳ quá trình tải nào
				((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.stop();");
				Thread.sleep(500);
				driver.quit();
				driver = null;
				System.out.println("[DỪNG] Đã đóng trình duyệt thành công");
			} catch (Exception e) {
				System.err.println("[DỪNG] Lỗi khi dừng: " + e.getMessage());
			}
		}
	}

	public static void resetStop() {
		shouldStop = false;
		System.out.println("[KHÔI PHỤC] Cờ dừng đã được đặt lại");
	}
}