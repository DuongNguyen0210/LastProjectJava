package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class EvaluationTab {
	private JPanel panel;
	private JTextArea reportArea;

	public EvaluationTab() {
		createUI();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Title
		JLabel titleLabel = new JLabel("⭐ User Evaluation Reports");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		panel.add(titleLabel, BorderLayout.NORTH);

		// Top panel - User selection
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		topPanel.setBackground(new Color(240, 240, 240));

		JLabel userLabel = new JLabel("Select User:");
		userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		JComboBox<String> userCombo = new JComboBox<>();
		userCombo.setPreferredSize(new Dimension(200, 30));
		loadUsers(userCombo);

		JButton generateBtn = new JButton("📊 Generate Report");
		generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		generateBtn.setPreferredSize(new Dimension(150, 30));
		generateBtn.setBackground(new Color(33, 150, 243));
		generateBtn.setForeground(Color.WHITE);
		generateBtn.addActionListener(e -> {
			String selectedUser = (String) userCombo.getSelectedItem();
			if (selectedUser != null) {
				generateReport(selectedUser);
			}
		});

		topPanel.add(userLabel);
		topPanel.add(userCombo);
		topPanel.add(generateBtn);

		panel.add(topPanel, BorderLayout.NORTH);

		// Report area
		reportArea = new JTextArea();
		reportArea.setFont(new Font("Courier New", Font.PLAIN, 11));
		reportArea.setEditable(false);
		reportArea.setBackground(new Color(250, 250, 250));
		reportArea.setLineWrap(true);
		reportArea.setWrapStyleWord(true);
		reportArea.setText("Select a user and click 'Generate Report' to view their evaluation.\n\n"
				+ "The report will include:\n" + "- Total submissions analyzed\n" + "- Data structures usage\n"
				+ "- Algorithm usage\n" + "- AI usage detection score\n" + "- Overall evaluation level");

		JScrollPane scrollPane = new JScrollPane(reportArea);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private void loadUsers(JComboBox<String> combo) {
		String sql = "SELECT DISTINCT username FROM target_account ORDER BY username";

		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				combo.addItem(rs.getString("username"));
			}

		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private void generateReport(String username) {
		String sql = "SELECT " + "COUNT(*) as total_submissions, "
				+ "AVG(CAST(ai_generated_probability AS FLOAT)) as avg_ai, "
				+ "COUNT(DISTINCT data_structure) as unique_dsa, " + "COUNT(DISTINCT algorithm) as unique_algo "
				+ "FROM submission s " + "JOIN target_account t ON s.account_id = t.id " + "WHERE t.username = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int totalSubs = rs.getInt("total_submissions");
				double avgAi = rs.getDouble("avg_ai");
				int uniqueDsa = rs.getInt("unique_dsa");
				int uniqueAlgo = rs.getInt("unique_algo");

				StringBuilder report = new StringBuilder();
				report.append("╔════════════════════════════════════════════════════╗\n");
				report.append("║           USER EVALUATION REPORT                   ║\n");
				report.append("╠════════════════════════════════════════════════════╣\n");
				report.append("║ Username: ").append(String.format("%-39s", username)).append("║\n");
				report.append("╚════════════════════════════════════════════════════╝\n\n");

				report.append("📊 STATISTICS:\n");
				report.append("─────────────────────────────────────────────────────\n");
				report.append(String.format("  Total Submissions:        %d\n", totalSubs));
				report.append(String.format("  Unique Data Structures:   %d\n", uniqueDsa));
				report.append(String.format("  Unique Algorithms:        %d\n", uniqueAlgo));
				report.append(String.format("  Average AI Score:         %.1f%%\n\n", avgAi));

				report.append("🤖 AI USAGE EVALUATION:\n");
				report.append("─────────────────────────────────────────────────────\n");
				if (avgAi > 70) {
					report.append("  Level: ⚠️⚠️⚠️ VERY HIGH - Likely using AI heavily\n");
				} else if (avgAi > 50) {
					report.append("  Level: ⚠️⚠️ HIGH - Noticeable AI usage\n");
				} else if (avgAi > 30) {
					report.append("  Level: ⚠️ MEDIUM - Some AI assistance detected\n");
				} else {
					report.append("  Level: ✅ LOW - Mostly self-written code\n");
				}

				report.append("\n⭐ OVERALL EVALUATION:\n");
				report.append("─────────────────────────────────────────────────────\n");
				double skillScore = (uniqueDsa * 5) + (uniqueAlgo * 3) + (100 - avgAi) / 2.0;
				skillScore = Math.min(100, skillScore);

				if (skillScore >= 80) {
					report.append("  Rating: ⭐⭐⭐ EXCELLENT\n");
				} else if (skillScore >= 65) {
					report.append("  Rating: ⭐⭐ GOOD\n");
				} else if (skillScore >= 50) {
					report.append("  Rating: ⭐ AVERAGE\n");
				} else {
					report.append("  Rating: ⚠️ BELOW AVERAGE\n");
				}

				report.append("\n═════════════════════════════════════════════════════\n");

				reportArea.setText(report.toString());
			}

		} catch (SQLException e) {
			reportArea.setText("Error: " + e.getMessage());
		}
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
				"jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;integratedSecurity=true;encrypt=false;trustServerCertificate=true;");
	}

	public JPanel getPanel() {
		return panel;
	}
}