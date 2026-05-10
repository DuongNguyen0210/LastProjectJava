package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UserEvaluator {
    private static final int SLEEP_TIME = 45000;
    private static final String SELECT_QUERY = "SELECT id, source_code FROM submission WHERE data_structure IS NULL";
    private static final String UPDATE_QUERY = "UPDATE submission SET data_structure = ?, algorithm = ?, ai_generated_probability = ?, ai_evaluation_note = ? WHERE id = ?";

    public static void evaluateUnanalyzedSubmissions() {
        System.out.println("===> Bắt đầu quét các bài nộp chưa phân tích...");
        int processedCount = 0;

        try (Connection conn = LastProjectJava.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(SELECT_QUERY);
             ResultSet rs = selectStmt.executeQuery();
             PreparedStatement updateStmt = conn.prepareStatement(UPDATE_QUERY)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String code = rs.getString("source_code");
                if (code != null && code.length() > 3000) {
                    code = code.substring(code.length() - 3000); // Lấy 3000 ký tự cuối (thường chứa hàm main)
                }
                System.out.println("Processing ID: " + id + "...");
                
                String jsonResponse = GeminiAnalyzer.analyzeCode(code);
                
                if (jsonResponse.contains("\"Error\"")) {
                	//Hehe:)))
                    System.out.println("\u001B[31m" + "Warning: Irreversible data loss detected." + "\u001B[0m");
                    Thread.sleep(60000); 
                    continue; 
                }
                
                if (saveResultToDb(updateStmt, id, jsonResponse)) {
                    processedCount++;
                    System.out.println("Done ID: " + id);
                    Thread.sleep(SLEEP_TIME);
                }
            }
            System.out.println("===> Hoàn tất! Đã cập nhật: " + processedCount + " bài.");

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private static boolean saveResultToDb(PreparedStatement pstmt, int id, String jsonStr) {
        try {
            JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();
            pstmt.setString(1, getString(obj, "data_structure"));
            pstmt.setString(2, getString(obj, "algorithm"));
            pstmt.setDouble(3, obj.has("ai_generated_probability") ? obj.get("ai_generated_probability").getAsDouble() : 0.0);
            pstmt.setString(4, getString(obj, "note"));
            pstmt.setInt(5, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("-> Bỏ qua ID " + id + " do lỗi dữ liệu AI.");
            return false;
        }
    }

    private static String getString(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : "N/A";
    }

    public static void main(String[] args) {
        evaluateUnanalyzedSubmissions();
    }
}