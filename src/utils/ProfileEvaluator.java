package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfileEvaluator {

    private static final double AI_HIGH_THRESHOLD = 70.0;
    private static final double AI_MEDIUM_THRESHOLD = 30.0;

    private static final String SQL_AVG_AI = 
        "SELECT COUNT(*) as total, AVG(CAST(ai_generated_probability AS FLOAT)) as avg_ai " +
        "FROM submission s JOIN target_account t ON s.account_id = t.id WHERE t.username = ?";

    private static final String SQL_TOP_ALGO = 
        "SELECT TOP 3 s.algorithm, COUNT(*) AS usage_count " +
        "FROM submission s JOIN target_account t ON s.account_id = t.id " +
        "WHERE t.username = ? AND s.algorithm IS NOT NULL AND s.algorithm NOT IN ('N/A', 'Error') " +
        "GROUP BY s.algorithm ORDER BY usage_count DESC";

    /**
     * Hàm chính để tạo báo cáo (Đã được làm sạch)
     */
    public static String generateUserReport(String username) {
        try (Connection conn = LastProjectJava.getConnection()) {
            
            // 1. Lấy dữ liệu thô từ DB
            AiStats aiStats = fetchAiStats(conn, username);
            if (aiStats.totalSubmissions == 0) {
                return "Tài khoản '" + username + "' chưa có dữ liệu bài nộp.";
            }
            
            List<String> algorithms = fetchTopAlgorithms(conn, username);

            // 2. Trình bày báo cáo (Presentation Layer)
            return buildReportString(username, aiStats, algorithms);

        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    // --- CÁC HÀM BỔ TRỢ (HELPER METHODS) ---

    private static AiStats fetchAiStats(Connection conn, String username) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_AVG_AI)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new AiStats(rs.getInt("total"), rs.getDouble("avg_ai"));
            }
        }
        return new AiStats(0, 0);
    }

    private static List<String> fetchTopAlgorithms(Connection conn, String username) throws Exception {
        List<String> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_TOP_ALGO)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("algorithm") + " (" + rs.getInt("usage_count") + " lần)");
            }
        }
        return list;
    }

    private static String buildReportString(String user, AiStats ai, List<String> algos) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("  BÁO CÁO: ").append(user).append("\n");
        sb.append("==================================================\n");
        sb.append(String.format("- Tổng bài: %d | Tỷ lệ AI: %.1f%%\n", ai.totalSubmissions, ai.avgAi));
        
        // Nhận xét AI
        sb.append("=> AI: ");
        if (ai.avgAi > AI_HIGH_THRESHOLD) sb.append("Lạm dụng cao. Cần kiểm tra kỹ.\n");
        else if (ai.avgAi > AI_MEDIUM_THRESHOLD) sb.append("Có tham khảo AI.\n");
        else sb.append("Tự lực tốt.\n");

        // Nhận xét Thuật toán
        sb.append("=> Thuật toán nổi bật:\n");
        if (algos.isEmpty()) {
            sb.append("   + Không có dữ liệu cụ thể.\n");
        } else {
            for (String a : algos) sb.append("   + ").append(a).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Lớp nội bộ để chứa dữ liệu AI tạm thời
     */
    private static class AiStats {
        int totalSubmissions;
        double avgAi;
        AiStats(int total, double avg) { this.totalSubmissions = total; this.avgAi = avg; }
    }

    // --- HÀM VẬN HÀNH TỔNG QUÁT ---
    public static void evaluateAllUsers() {
        String sql = "SELECT username FROM target_account";
        try (Connection conn = LastProjectJava.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                System.out.println(generateUserReport(rs.getString("username")));
            }
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        evaluateAllUsers();
    }
}