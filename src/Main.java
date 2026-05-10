import crawler.codeforces.CodeforcesApiCrawler;
import crawler.codeforces.CodeforcesHtmlScraper;
import crawler.vjudge.VjudgeHtmlScraper;
import crawler.vjudge.VjudgeStatusCrawler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("======================================");
        System.out.println("    HỆ THỐNG CÀO DỮ LIỆU TỰ ĐỘNG");
        System.out.println("======================================");
        System.out.println("   1. Cào dữ liệu Codeforces");
        System.out.println("   2. Cào dữ liệu Vjudge");
        System.out.println("======================================");
        System.out.print("👉 Nhập lựa chọn của bạn (1 hoặc 2): ");

        String choice = scanner.nextLine().trim();

        System.out.print("👉 Nhập số ngày gần nhất muốn cào dữ liệu (VD: 2): ");
        int daysLimit = 2;
        try {
            daysLimit = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Nhập sai định dạng số! Hệ thống tự động thiết lập mặc định là 2 ngày.");
        }

        System.out.print("👉 Nhập tên user muốn cào (nếu nhiều user thì cách nhau bằng dấu phẩy): ");
        String userInput = scanner.nextLine().trim();
        String[] usersToCrawl = userInput.split("\\s*,\\s*");

        if ("1".equals(choice)) {
            CodeforcesHtmlScraper.initAndLogin();
            for (String user : usersToCrawl) {
                if (user.isEmpty()) continue;
                System.out.println("\nProcessing Codeforces: " + user);

                int crawledCount = CodeforcesApiCrawler.fetchUserSubmissions(user, daysLimit);

                if (crawledCount == 0) {
                    System.out.println("📭 KHÔNG CÓ BÀI NỘP MỚI: User " + user + " không có bài Accepted nào trong " + daysLimit + " ngày qua.");
                } else {
                    System.out.println("✅ HOÀN TẤT: Đã cào thành công " + crawledCount + " bài nộp của " + user);
                }

                sleepRandom(3000);
            }
            CodeforcesHtmlScraper.quitDriver();

        } else if ("2".equals(choice)) {
            VjudgeHtmlScraper.initAndLogin();

            for (String user : usersToCrawl) {
                if (user.isEmpty()) continue;
                System.out.println("\nProcessing Vjudge: " + user);

                int crawledCount = VjudgeStatusCrawler.fetchUserSubmissions(user, daysLimit);

                if (crawledCount == 0) {
                    System.out.println("📭 KHÔNG CÓ BÀI NỘP MỚI: User " + user + " không có bài Accepted nào trong " + daysLimit + " ngày qua.");
                } else {
                    System.out.println("✅ HOÀN TẤT: Đã cào thành công " + crawledCount + " bài nộp của " + user);
                }

                sleepRandom(3000);
            }
            VjudgeHtmlScraper.quitDriver();
        } else {
            System.out.println("\nLựa chọn không hợp lệ. Vui lòng chạy lại chương trình!");
        }
        scanner.close();
    }

    private static void sleepRandom(long baseTime) {
        try {
            long restTime = baseTime + (long) (Math.random() * 3000);
            Thread.sleep(restTime);
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting: " + e.getMessage());
        }
    }
}