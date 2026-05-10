package utils;

import okhttp3.*;
import com.google.gson.*;
import java.util.concurrent.TimeUnit;

public class GeminiAnalyzer {
	private static final String API_KEY = "KEY_API_AI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + API_KEY;
    private static final int MAX_RETRIES = 3;

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();
    private static final Gson GSON = new Gson();

    public static String analyzeCode(String sourceCode) {
        int retryCount = 0;
        
        while (retryCount <= MAX_RETRIES) {
            String response = performApiCall(sourceCode);
            
            // Nếu kết quả không phải là lỗi hệ thống thì trả về luôn
            if (!response.contains("RETRY_NEEDED")) {
                return response;
            }

            retryCount++;
            if (retryCount <= MAX_RETRIES) {
                handleRetryDelay(retryCount);
            }
        }
        return createErrorJson("Server Google không khả dụng sau " + MAX_RETRIES + " lần thử.");
    }

    /**
     * Hàm thực thi: Chỉ lo việc gửi Request và nhận Response
     */
    private static String performApiCall(String sourceCode) {
        try {
            String jsonBody = buildRequestBody(sourceCode);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(API_URL).post(body).build();

            try (Response response = CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return extractJsonFromResponse(response.body().string());
                }
                
                if (response.code() == 503 || response.code() == 429) {
                    return "RETRY_NEEDED";
                }
                
                System.err.println("DEBUG - Lỗi API: " + response.code());
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            return "RETRY_NEEDED";
        }
        return createErrorJson("Lỗi không xác định");
    }

    private static void handleRetryDelay(int attempt) {
        try {
            int delay = attempt * 5000; // Thử lần 1 đợi 5s, lần 2 đợi 10s...
            System.out.println("==> Đang đợi " + (delay/1000) + "s để thử lại lần " + attempt + "...");
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String buildRequestBody(String sourceCode) {
        String prompt = "Đóng vai chuyên gia lập trình thi đấu. Phân tích đoạn code sau và TRẢ VỀ DUY NHẤT một chuỗi JSON. "
                + "YÊU CẦU: Tất cả các giá trị (data_structure, algorithm, note) PHẢI VIẾT BẰNG TIẾNG VIỆT. "
                + "Mẫu: {\"data_structure\":\"...\", \"algorithm\":\"...\", \"ai_generated_probability\":0, \"note\":\"...\"}. "
                + "\n\nCode:\n" + sourceCode;

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject root = new JsonObject();
        root.add("contents", contents);

        return GSON.toJson(root);
    }

    private static String extractJsonFromResponse(String responseData) {
        try {
            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
            String rawResult = jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            //System.out.println("DEBUG - AI Trả về: " + rawResult);

            // Xử lý trường hợp AI bao bọc JSON trong Markdown ```json ... ```
            int firstBrace = rawResult.indexOf("{");
            int lastBrace = rawResult.lastIndexOf("}");
            
            if (firstBrace != -1 && lastBrace != -1) {
                return rawResult.substring(firstBrace, lastBrace + 1);
            }
        } catch (Exception e) {
            System.err.println("Lỗi bóc tách JSON: " + e.getMessage());
        }
        return createErrorJson("AI không phản hồi JSON đúng chuẩn");
    }

    private static String createErrorJson(String message) {
        return String.format("{\"data_structure\":\"Error\", \"algorithm\":\"Error\", \"ai_generated_probability\":0, \"note\":\"%s\"}", message);
    }
}