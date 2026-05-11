package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiAnalyzer {
	private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final int MAX_RETRIES = 3;

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();
    private static final Gson GSON = new Gson();

    //Đọc API
    private static String getApiKey() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            return prop.getProperty("gemini.api.key");
        } catch (IOException ex) {
            System.err.println("Lỗi: Không tìm thấy file config.properties hoặc lỗi đọc file!");
            return null;
        }
    }
    
    public static String analyzeCode(String sourceCode) {
    	String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return createErrorJson("Thiếu API Key trong file config.properties");
        }
        String fullUrl = BASE_URL + "?key=" + apiKey.trim();
        int retryCount = 0;        
        while (retryCount <= MAX_RETRIES) {
            String response = performApiCall(fullUrl, sourceCode);
            
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

    
    private static String performApiCall(String url, String sourceCode) {
        try {
            String jsonBody = buildRequestBody(sourceCode);
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();

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
        String prompt = "Bạn là một chuyên gia Lập trình thi đấu (Competitive Programming) kiêm chuyên gia phát hiện mã nguồn do AI sinh ra. "
                + "Nhiệm vụ của bạn là phân tích đoạn code được cung cấp và TRẢ VỀ DUY NHẤT một chuỗi JSON hợp lệ (không chứa markdown, không có text bao quanh). "
                + "Tất cả các giá trị chuỗi phải được viết bằng TIẾNG VIỆT.\n\n"
                + "Định dạng JSON yêu cầu:\n"
                + "{\n"
                + "  \"data_structure\": \"Mô tả chi tiết các cấu trúc dữ liệu nổi bật (VD: Mảng tĩnh toàn cục, Segment Tree, Disjoint Set...)\",\n"
                + "  \"algorithm\": \"Mô tả các thuật toán và kỹ thuật (VD: Z-function, Sweep-line, Quy hoạch động...)\",\n"
                + "  \"ai_generated_probability\": [Một số nguyên từ 0 đến 100 thể hiện phần trăm code do AI viết], \n"
                + "  \"note\": \"Nhận xét tổng quan và GHI RÕ LÝ DO tại sao đưa ra mức tỷ lệ AI này dựa trên bộ tiêu chí bên dưới.\"\n"
                + "}\n\n"
                + "TIÊU CHÍ BẮT BUỘC ĐỂ ĐÁNH GIÁ TỶ LỆ AI CHO CODE LẬP TRÌNH THI ĐẤU (RẤT QUAN TRỌNG):\n"
                + "- Dấu hiệu CHẮC CHẮN của AI (Đánh giá 80% - 100%):\n"
                + "  1. Có comment tiếng Việt giải thích mang tính 'sư phạm' (VD: giải thích tại sao dùng mảng tĩnh, phân tích độ phức tạp O(N), giải thích kỹ thuật Fast I/O hay 0-based index). Coder thật KHÔNG BAO GIỜ viết comment giải thích template I/O cho chính họ.\n"
                + "  2. Code quá sạch sẽ, cấu trúc hoàn hảo, không có biến thừa hay code debug (cout << 'test').\n"
                + "  3. Hoàn toàn thiếu vắng các Macro 'luộm thuộm' đặc trưng của dân CP (như #define ll long long, #define pb push_back, #define FOR(i,a,b), typedef vector<int> vi).\n"
                + "  4. Đặt tên biến dài rõ nghĩa một cách bất thường trong ngữ cảnh CP (VD: current_dp, event_cnt, max_v thay vì cur, ec, mx).\n\n"
                + "- Dấu hiệu của NGƯỜI THẬT (Đánh giá 0% - 30%):\n"
                + "  1. Lạm dụng template Macro và Typedef dày đặc ở đầu file.\n"
                + "  2. Gần như không có comment, hoặc comment rất ngắn/tiếng Anh bồi (VD: // lazy, // wtf, // bug here).\n"
                + "  3. Nhồi nhét nhiều câu lệnh trên cùng một dòng, cấu trúc thiếu thẩm mỹ, ưu tiên tốc độ gõ phím.\n\n"
                + "Mã nguồn cần phân tích:\n"
                + sourceCode;

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