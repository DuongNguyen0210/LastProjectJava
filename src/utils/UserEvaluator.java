package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UserEvaluator {
	private static final int SLEEP_TIME = 25000;

	private static final String SELECT_QUERY = "SELECT s.id, s.source_code " + "FROM submission s "
			+ "LEFT JOIN ai_analysis a ON s.id = a.submission_id " + "WHERE a.id IS NULL";

	private static final String INSERT_QUERY = "INSERT INTO ai_analysis (submission_id, data_structure, algorithm, ai_generated_probability, ai_evaluation_note) "
			+ "VALUES (?, ?, ?, ?, ?)";

	public interface AnalysisCallback {
		void onAnalysisComplete();
	}

	public static void evaluateUnanalyzedSubmissions(AnalysisCallback callback) {
		System.out.println("Starting AI analysis...");
		int processedCount = 0;

		try (Connection conn = LastProjectJava.getConnection();
				PreparedStatement selectStmt = conn.prepareStatement(SELECT_QUERY);
				ResultSet rs = selectStmt.executeQuery();
				PreparedStatement insertStmt = conn.prepareStatement(INSERT_QUERY)) {

			while (rs.next()) {
				int id = rs.getInt("id");
				String code = rs.getString("source_code");
				if (code != null && code.length() > 3000) {
					code = code.substring(code.length() - 3000);
				}
				System.out.println("Processing ID: " + id);

				String jsonResponse = GeminiAnalyzer.analyzeCode(code);

				if (jsonResponse.contains("Error")) {
					System.out.println("API overloaded. Waiting 1 minute...");
					Thread.sleep(60000);
					continue;
				}

				if (saveResultToDb(insertStmt, id, jsonResponse)) {
					processedCount++;
					System.out.println("Done ID: " + id);
					Thread.sleep(SLEEP_TIME);
				}
			}
			System.out.println("Analysis complete! Processed: " + processedCount);
			if (callback != null) {
				callback.onAnalysisComplete();
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static boolean saveResultToDb(PreparedStatement pstmt, int submissionId, String jsonStr) {
		try {
			JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();
			pstmt.setInt(1, submissionId);
			pstmt.setString(2, getString(obj, "data_structure"));
			pstmt.setString(3, getString(obj, "algorithm"));
			pstmt.setDouble(4,
					obj.has("ai_generated_probability") ? obj.get("ai_generated_probability").getAsDouble() : 0.0);
			pstmt.setString(5, getString(obj, "note"));

			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			System.out.println("Error processing ID " + submissionId);
			return false;
		}
	}

	private static String getString(JsonObject obj, String key) {
		return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : "N/A";
	}

	public static void main(String[] args) {
		evaluateUnanalyzedSubmissions(() -> {
			System.out.println("Analysis finished!");
		});
	}
}