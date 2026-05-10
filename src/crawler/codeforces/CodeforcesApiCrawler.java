package crawler.codeforces;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utils.DatabaseHelper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CodeforcesApiCrawler {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static int fetchUserSubmissions(String handle, int daysLimit) {
        int successfulCrawls = 0;
        String apiUrl = "https://codeforces.com/api/user.status?handle=" + handle + "&from=1&count=500";
        long limitTimestamp = (System.currentTimeMillis() / 1000) - (daysLimit * 24L * 60 * 60);

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

                if (jsonObject.get("status").getAsString().equals("OK")) {
                    JsonArray results = jsonObject.getAsJsonArray("result");

                    for (JsonElement element : results) {
                        JsonObject submission = element.getAsJsonObject();
                        long creationTime = submission.get("creationTimeSeconds").getAsLong();

                        // CHUYỂN ĐỔI THỜI GIAN: API trả về giây, java.sql.Timestamp cần mili giây
                        java.sql.Timestamp submittedAt = new java.sql.Timestamp(creationTime * 1000L);

                        if (creationTime < limitTimestamp) break;

                        if (submission.has("verdict") && submission.get("verdict").getAsString().equals("OK")) {
                            String submitId = submission.get("id").getAsString();
                            String contestId = submission.get("contestId").getAsString();
                            String language = submission.get("programmingLanguage").getAsString();
                            String sourceCode = CodeforcesHtmlScraper.getSourceCode(contestId, submitId);

                            if (sourceCode != null && !sourceCode.isEmpty()) {
                                // Truyền thêm biến submittedAt vào đây
                                DatabaseHelper.saveSubmission(submitId, handle, "Codeforces", sourceCode, language, submittedAt);
                                successfulCrawls++;
                            }
                        }
                    }
                } else {
                    System.out.println("Lỗi từ API: " + jsonObject.get("comment").getAsString());
                }
            } else {
                System.out.println("Lỗi: " + statusCode);
            }

        } catch (Exception e) {
            System.out.println("Lỗi kết nối mạng: " + e.getMessage());
        }

        return successfulCrawls;
    }
}