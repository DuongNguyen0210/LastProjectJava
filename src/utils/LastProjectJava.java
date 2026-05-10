package utils;

import java.sql.*;

public class LastProjectJava {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;integratedSecurity=true;encrypt=false;trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void saveSubmission(String submitId, String username, String platform, String sourceCode, String language, Timestamp submittedAt) {
        String checkAccountSql = "SELECT id FROM target_account WHERE username = ? AND platform = ?";
        String insertAccountSql = "INSERT INTO target_account (username, platform) VALUES (?, ?)";
        // Thêm cột submitted_at vào câu lệnh INSERT
        String insertSubmissionSql = "INSERT INTO submission (submit_id, account_id, language, source_code, submitted_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            int accountId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(checkAccountSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, platform);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) accountId = rs.getInt("id");
            }

            if (accountId == -1) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, platform);
                    pstmt.executeUpdate();
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) accountId = rs.getInt(1);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSubmissionSql)) {
                pstmt.setString(1, submitId);
                pstmt.setInt(2, accountId);
                pstmt.setString(3, language);
                pstmt.setString(4, sourceCode);
                pstmt.setTimestamp(5, submittedAt); // Lưu thời gian nộp bài
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() != 2627) System.out.println("Lỗi DB: " + e.getMessage());
        }
    }
}