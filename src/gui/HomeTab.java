package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class HomeTab {
	private JPanel panel;
	private JLabel[] statLabels = new JLabel[4];

	public HomeTab() {
		createUI();
		startAutoRefresh();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel titleLabel = new JLabel("Bảng Điều Khiển");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		titlePanel.add(titleLabel, BorderLayout.WEST);

		panel.add(titlePanel, BorderLayout.NORTH);

		JPanel statsGridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
		statsGridPanel.setBackground(UIUtils.LIGHT_GRAY);

		JPanel userCard = createStatCard("Tổng Người Dùng", UIUtils.PRIMARY_COLOR);
		JPanel submissionCard = createStatCard("Tổng Bài Nộp", UIUtils.SUCCESS_COLOR);
		JPanel analyzedCard = createStatCard("Đã Phân Tích", UIUtils.WARNING_COLOR);
		JPanel pendingCard = createStatCard("Chờ Xử Lý", UIUtils.ERROR_COLOR);

		statsGridPanel.add(userCard);
		statsGridPanel.add(submissionCard);
		statsGridPanel.add(analyzedCard);
		statsGridPanel.add(pendingCard);

		panel.add(statsGridPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 15, 0));
		bottomPanel.setBackground(UIUtils.LIGHT_GRAY);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		javax.swing.JButton crawlerBtn = UIHelper.createHeaderButton("Cào Mã", UIUtils.SUCCESS_COLOR);
		crawlerBtn.setFocusPainted(false);
		crawlerBtn.setContentAreaFilled(true);
		crawlerBtn.setOpaque(true);
		crawlerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		crawlerBtn.addActionListener(e -> MainWindow.getInstance().switchTab(1));

		javax.swing.JButton resultsBtn = UIHelper.createHeaderButton("Kết Quả", UIUtils.PRIMARY_COLOR);
		resultsBtn.setFocusPainted(false);
		resultsBtn.setContentAreaFilled(true);
		resultsBtn.setOpaque(true);
		resultsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		resultsBtn.addActionListener(e -> MainWindow.getInstance().switchTab(2));

		javax.swing.JButton aiBtn = UIHelper.createHeaderButton("Phân Tích AI", UIUtils.WARNING_COLOR);
		aiBtn.setFocusPainted(false);
		aiBtn.setContentAreaFilled(true);
		aiBtn.setOpaque(true);
		aiBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		aiBtn.addActionListener(e -> MainWindow.getInstance().switchTab(3));

		javax.swing.JButton evalBtn = UIHelper.createHeaderButton("Đánh Giá", UIUtils.PRIMARY_COLOR);
		evalBtn.setFocusPainted(false);
		evalBtn.setContentAreaFilled(true);
		evalBtn.setOpaque(true);
		evalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		evalBtn.addActionListener(e -> MainWindow.getInstance().switchTab(4));

		bottomPanel.add(crawlerBtn);
		bottomPanel.add(resultsBtn);
		bottomPanel.add(aiBtn);
		bottomPanel.add(evalBtn);

		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	private JPanel createStatCard(String title, Color color) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(color, 3, true));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(UIUtils.FONT_HEADER);
		titleLabel.setForeground(color);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

		JLabel valueLabel = new JLabel("0");
		valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
		valueLabel.setForeground(color);
		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		valueLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

		if (title.contains("Người")) {
			statLabels[0] = valueLabel;
		} else if (title.contains("Bài")) {
			statLabels[1] = valueLabel;
		} else if (title.contains("Phân")) {
			statLabels[2] = valueLabel;
		} else if (title.contains("Chờ")) {
			statLabels[3] = valueLabel;
		}

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(valueLabel, BorderLayout.CENTER);

		return card;
	}

	private void startAutoRefresh() {
		new Timer(3000, e -> refreshStats()).start();
	}

	private void refreshStats() {
		new SwingWorker<int[], Void>() {
			@Override
			protected int[] doInBackground() {
				return new int[] { getTotalUsers(), getTotalSubmissions(), getAnalyzedCount(), getPendingCount() };
			}

			@Override
			protected void done() {
				try {
					int[] stats = get();
					statLabels[0].setText(String.valueOf(stats[0]));
					statLabels[1].setText(String.valueOf(stats[1]));
					statLabels[2].setText(String.valueOf(stats[2]));
					statLabels[3].setText(String.valueOf(stats[3]));
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				}
			}
		}.execute();
	}

	private int getTotalUsers() {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT username) FROM target_account")) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	private int getTotalSubmissions() {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM submission")) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	private int getAnalyzedCount() {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(
						"SELECT COUNT(*) FROM submission s INNER JOIN ai_analysis ai ON s.id = ai.submission_id")) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	private int getPendingCount() {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(
						"SELECT COUNT(*) FROM submission s WHERE NOT EXISTS (SELECT 1 FROM ai_analysis ai WHERE ai.submission_id = s.id)")) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			System.err.println("Error: " + e.getMessage());
		}
		return 0;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
				"jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;integratedSecurity=true;encrypt=false;trustServerCertificate=true;");
	}

	public JPanel getPanel() {
		return panel;
	}
}