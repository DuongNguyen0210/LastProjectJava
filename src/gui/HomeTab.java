package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class HomeTab {
	private JPanel panel;
	private JLabel[] statLabels = new JLabel[4];

	public HomeTab() {
		createUI();
		refreshStats();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		// Top section
		JPanel topSection = new JPanel(new BorderLayout());
		topSection.setBackground(UIUtils.LIGHT_GRAY);

		// Title
		JLabel titleLabel = new JLabel("📊 Dashboard & System Statistics");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		topSection.add(titleLabel, BorderLayout.WEST);

		// Refresh button (top right)
		JButton refreshBtn = new JButton("🔄");
		refreshBtn.setPreferredSize(new Dimension(40, 40));
		refreshBtn.setFont(new Font("Arial", Font.PLAIN, 16));
		refreshBtn.addActionListener(e -> refreshStats());
		topSection.add(refreshBtn, BorderLayout.EAST);

		panel.add(topSection, BorderLayout.NORTH);

		// Center: Stats cards in 2x2 grid
		JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
		centerPanel.setBackground(UIUtils.LIGHT_GRAY);

		JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
		statsPanel.setBackground(UIUtils.LIGHT_GRAY);

		statLabels[0] = createStatCard("👤\nTotal Users", "0", UIUtils.PRIMARY_COLOR, statsPanel);
		statLabels[1] = createStatCard("📝\nSubmissions", "0", UIUtils.SUCCESS_COLOR, statsPanel);
		statLabels[2] = createStatCard("✅\nAnalyzed", "0", UIUtils.WARNING_COLOR, statsPanel);
		statLabels[3] = createStatCard("⏳\nPending", "0", UIUtils.ERROR_COLOR, statsPanel);

		centerPanel.add(statsPanel, BorderLayout.CENTER);
		panel.add(centerPanel, BorderLayout.CENTER);

		// Bottom: Action buttons
		JPanel bottomPanel = new JPanel(new GridLayout(2, 2, 15, 15));
		bottomPanel.setBackground(UIUtils.LIGHT_GRAY);

		// Row 1: Crawler & Results
		JButton crawlerBtn = createActionButton("🕷️  Start Crawling", UIUtils.SUCCESS_COLOR,
				"Bắt đầu cào dữ liệu từ Codeforces hoặc Vjudge");
		crawlerBtn.addActionListener(e -> {
			MainWindow.getInstance().switchTab(1); // Switch to Crawler tab
		});

		JButton resultsBtn = createActionButton("📊 View Results", UIUtils.PRIMARY_COLOR,
				"Xem tất cả kết quả phân tích");
		resultsBtn.addActionListener(e -> {
			MainWindow.getInstance().switchTab(2); // Switch to Results tab
		});

		// Row 2: Evaluation & Settings
		JButton evalBtn = createActionButton("⭐ User Evaluation", UIUtils.WARNING_COLOR,
				"Xem báo cáo đánh giá người dùng");
		evalBtn.addActionListener(e -> {
			MainWindow.getInstance().switchTab(3); // Switch to Evaluation tab
		});

		JButton settingsBtn = createActionButton("⚙️  Settings", UIUtils.PRIMARY_COLOR, "Cấu hình hệ thống");
		settingsBtn.addActionListener(e -> {
			showSettings();
		});

		bottomPanel.add(crawlerBtn);
		bottomPanel.add(resultsBtn);
		bottomPanel.add(evalBtn);
		bottomPanel.add(settingsBtn);

		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	private JLabel createStatCard(String title, String value, Color color, JPanel parent) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createLineBorder(color, 3, true));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(UIUtils.FONT_HEADER);
		titleLabel.setForeground(color);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
		valueLabel.setForeground(color);
		valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(valueLabel, BorderLayout.CENTER);

		parent.add(card);
		return valueLabel;
	}

	private JButton createActionButton(String title, Color color, String tooltip) {
		JButton btn = new JButton(title);
		btn.setFont(UIUtils.FONT_HEADER);
		btn.setBackground(color);
		btn.setForeground(Color.WHITE);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
		btn.setFocusPainted(false);
		btn.setToolTipText(tooltip);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Hover effect
		btn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				btn.setBackground(color.brighter());
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				btn.setBackground(color);
			}
		});

		return btn;
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

	private void showSettings() {
		JOptionPane.showMessageDialog(panel,
				"Settings:\n\n" + "• Database: SQL Server (localhost:1433)\n" + "• API Keys: config.properties\n"
						+ "• Crawler Delay: 2-3 seconds\n\n" + "Để thay đổi, chỉnh sửa config files.",
				"⚙️  Settings", JOptionPane.INFORMATION_MESSAGE);
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
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM submission WHERE data_structure IS NOT NULL")) {
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
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM submission WHERE data_structure IS NULL")) {
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