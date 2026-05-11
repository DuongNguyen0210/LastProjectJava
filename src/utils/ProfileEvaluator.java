package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfileEvaluator {

    // --- CẤU HÌNH NGƯỠNG ĐÁNH GIÁ ---
    private static final double AI_HIGH_THRESHOLD = 70.0;
    private static final double AI_MEDIUM_THRESHOLD = 30.0;

    // Cập nhật câu truy vấn để lấy dữ liệu từ bảng ai_analysis thay vì submission
    private static final String SQL_AVG_AI = 
        "SELECT COUNT(s.id) as total, AVG(CAST(a.ai_generated_probability AS FLOAT)) as avg_ai " +
        "FROM submission s " +
        "JOIN target_account t ON s.account_id = t.id " +
        "JOIN ai_analysis a ON s.id = a.submission_id " +
        "WHERE t.username = ?";

    // Cập nhật câu truy vấn để lấy dữ liệu từ bảng ai_analysis thay vì submission
    private static final String SQL_TOP_ALGO = 
        "SELECT TOP 3 a.algorithm, COUNT(*) AS usage_count " +
        "FROM submission s " +
        "JOIN target_account t ON s.account_id = t.id " +
        "JOIN ai_analysis a ON s.id = a.submission_id " +
        "WHERE t.username = ? AND a.algorithm IS NOT NULL AND a.algorithm NOT IN ('N/A', 'Error') " +
        "GROUP BY a.algorithm ORDER BY usage_count DESC";
    
    // Đánh giá 1 nick
    public static String generateUserReport(String username) {
        try (Connection conn = LastProjectJava.getConnection()) {
            
            AiStats aiStats = fetchAiStats(conn, username);
            if (aiStats.totalSubmissions == 0) {
                return "Tài khoản '" + username + "' chưa có dữ liệu bài nộp (hoặc chưa được phân tích AI).";
            }
            
            List<String> algorithms = fetchTopAlgorithms(conn, username);

            return buildReportString(aiStats, algorithms);

        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    // Đánh giá tất cả nick
    public static void evaluateAllUsers() {
        String sql = "SELECT username FROM target_account";
        
        try (Connection conn = LastProjectJava.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("==================================================");
            System.out.println("      BẮT ĐẦU ĐÁNH GIÁ TẤT CẢ CÁC NICK");
            System.out.println("==================================================");
            
            int count = 0;
            while (rs.next()) {
                String username = rs.getString("username");
                System.out.println("\n[ TÀI KHOẢN: " + username + " ]");
                
                // Gọi hàm lõi để lấy nhận xét cho nick này và in ra Console
                String report = generateUserReport(username);
                System.out.println(report);
                
                count++;
            }
            
            System.out.println("==================================================");
            System.out.println("=> Hoàn tất đánh giá cho " + count + " nick trong hệ thống.");
            
        } catch (Exception e) {
            System.err.println("Lỗi khi quét danh sách nick: " + e.getMessage());
        }
    }


    private static AiStats fetchAiStats(Connection conn, String username) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_AVG_AI)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Nếu AVG(NULL) trong SQL, kết quả trả về mặc định có thể là 0, 
                // ta nên cẩn thận check thêm total
                int total = rs.getInt("total");
                double avgAi = rs.getDouble("avg_ai");
                if (rs.wasNull()) avgAi = 0.0;
                return new AiStats(total, avgAi);
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

    private static String buildReportString(AiStats ai, List<String> algos) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("- Tổng bài đã phân tích: %d | Tỷ lệ AI trung bình: %.1f%%\n", ai.totalSubmissions, ai.avgAi));
        
        sb.append("=> Mức độ sử dụng AI: ");
        if (ai.avgAi > AI_HIGH_THRESHOLD) sb.append("Lạm dụng cao.\n");
        else if (ai.avgAi > AI_MEDIUM_THRESHOLD) sb.append("Có tham khảo AI.\n");
        else sb.append("Tự lực tốt.\n");

        sb.append("=> Thuật toán nổi bật:\n");
        if (algos.isEmpty()) {
            sb.append("   + Không có dữ liệu cụ thể.\n");
        } else {
            for (String a : algos) sb.append("   + ").append(a).append("\n");
        }
        
        return sb.toString();
    }

    private static class AiStats {
        int totalSubmissions;
        double avgAi;
        AiStats(int total, double avg) { this.totalSubmissions = total; this.avgAi = avg; }
    }

    public static void main(String[] args) {
        evaluateAllUsers(); 
    }
}