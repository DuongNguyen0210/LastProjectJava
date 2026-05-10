package crawler.vjudge;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;

public class VjudgeHtmlScraper {

    private static WebDriver driver;

    private static final String DRIVER_PATH = "msedgedriver.exe";
    private static final String BASE_URL = "https://vjudge.net";
    private static final int TIMEOUT_SECONDS = 15;
    private static final String CODE_ELEMENT_TAG = "pre";

    public static void initAndLogin()
    {
        try
        {
            System.setProperty("webdriver.edge.driver", DRIVER_PATH);
            EdgeOptions options = new EdgeOptions();

            String userDataPath = Paths.get(System.getProperty("user.home"),
                    "AppData", "Local", "Microsoft", "Edge", "User Data - Copy").toString();

            options.addArguments("user-data-dir=" + userDataPath);
            options.addArguments("profile-directory=Default");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-debugging-port=9222");

            options.addArguments("--disable-notifications");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            driver = new EdgeDriver(options);
            driver.manage().window().maximize();
            driver.get(BASE_URL);

            printLoginInstructions();
            waitForUserConfirmation();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static String getSourceCode(String solutionId)
    {
        if (driver == null) {
            System.out.println(" Trình duyệt chưa được mở!");
            return null;
        }

        String url = String.format("%s/solution/%s", BASE_URL, solutionId);

        try {
            long delay = 2000 + (long) (Math.random() * 2000);
            Thread.sleep(delay);

            System.out.println("Đang chuyển hướng đến: " + url);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            WebElement codeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName(CODE_ELEMENT_TAG)));

            System.out.println("Accepted!");
            return codeElement.getText();

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void quitDriver() {
        if (driver != null) {
            System.out.println("Success");
            driver.quit();
            driver = null;
        }
    }

    public static WebDriver getDriver() {
        return driver;
    }

    private static void printLoginInstructions() {
        System.out.println("Trình duyệt mở lên và chuyển hướng tới trang chủ vjudge, bạn đăng nhập tài khoản vào nếu đăng nhập xong thì gõ 'OK' rồi nhấn Enter!");
    }

    private static void waitForUserConfirmation() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine().trim();
            if ("OK".equalsIgnoreCase(input)) break;
        }
    }
}