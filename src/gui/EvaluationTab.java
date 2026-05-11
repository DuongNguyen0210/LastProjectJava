package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import utils.ProfileEvaluator;

public class EvaluationTab {
	private JPanel panel;
	private JTextArea reportArea;
	private JTable statsTable;
	private DefaultTableModel tableModel;
	private JComboBox<String> userCombo;

	public EvaluationTab() {
		createUI();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel titleLabel = new JLabel("Đánh Giá Người Dùng");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);

		JPanel userSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		userSelectPanel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel userLabel = new JLabel("Chọn Người Dùng:");
		userLabel.setFont(UIUtils.FONT_NORMAL);

		userCombo = new JComboBox<>();
		userCombo.setFont(UIUtils.FONT_NORMAL);
		userCombo.setPreferredSize(new Dimension(180, 30));
		loadUsers(userCombo);

		JButton reportBtn = UIHelper.createButton("Tạo Báo Cáo", UIUtils.SUCCESS_COLOR);
		reportBtn.setFocusPainted(false);
		reportBtn.setContentAreaFilled(true);
		reportBtn.setOpaque(true);
		reportBtn.addActionListener(e -> {
			String user = (String) userCombo.getSelectedItem();
			if (user != null)
				generateReport(user);
		});

		JButton exportBtn = UIHelper.createButton("Xuất Báo Cáo", UIUtils.PRIMARY_COLOR);
		exportBtn.setFocusPainted(false);
		exportBtn.setContentAreaFilled(true);
		exportBtn.setOpaque(true);
		exportBtn.addActionListener(e -> {
			String user = (String) userCombo.getSelectedItem();
			if (user != null)
				exportReport(user);
		});

		userSelectPanel.add(userLabel);
		userSelectPanel.add(userCombo);
		userSelectPanel.add(reportBtn);
		userSelectPanel.add(exportBtn);

		topPanel.add(titleLabel, BorderLayout.WEST);
		topPanel.add(userSelectPanel, BorderLayout.EAST);
		panel.add(topPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.4);
		splitPane.setLeftComponent(createStatsPanel());
		splitPane.setRightComponent(createReportPanel());
		panel.add(splitPane, BorderLayout.CENTER);
	}

	private JPanel createStatsPanel() {
		JPanel statsPanel = new JPanel(new BorderLayout());
		statsPanel.setBackground(Color.WHITE);
		statsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"Thống Kê", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

		String[] columns = { "Chỉ Số", "Giá Trị" };
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		statsTable = new JTable(tableModel);
		statsTable.setFont(UIUtils.FONT_NORMAL);
		statsTable.setRowHeight(25);
		statsTable.getTableHeader().setFont(UIUtils.FONT_NORMAL);
		statsTable.setBackground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(statsTable);
		statsPanel.add(scrollPane, BorderLayout.CENTER);

		return statsPanel;
	}

	private JPanel createReportPanel() {
		JPanel reportPanel = new JPanel(new BorderLayout());
		reportPanel.setBackground(Color.WHITE);
		reportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"Báo Cáo Chi Tiết", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

		reportArea = new JTextArea();
		reportArea.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 11));
		reportArea.setEditable(false);
		reportArea.setBackground(new java.awt.Color(250, 250, 250));
		reportArea.setLineWrap(true);
		reportArea.setWrapStyleWord(true);
		reportArea.setText("Chọn người dùng và nhấp vào 'Tạo Báo Cáo' để xem đánh giá chi tiết.\n\n"
				+ "Báo cáo bao gồm:\n" + "• Tổng bài nộp đã phân tích\n" + "• Tỷ lệ sử dụng AI\n"
				+ "• Mức độ sử dụng AI\n" + "• Thuật toán nổi bật\n" + "• Xếp hạng kỹ năng tổng thể");

		JScrollPane scrollPane = new JScrollPane(reportArea);
		reportPanel.add(scrollPane, BorderLayout.CENTER);

		return reportPanel;
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
			System.err.println("Error loading users: " + e.getMessage());
		}
	}

	private void generateReport(String username) {
		tableModel.setRowCount(0);

		try {
			// Gọi ProfileEvaluator để lấy dữ liệu
			String profileReport = ProfileEvaluator.generateUserReport(username);

			// Lấy thống kê chi tiết
			ProfileEvaluator.UserStats stats = ProfileEvaluator.fetchUserStats(username);

			// Populate Stats Table
			tableModel.addRow(new Object[] { "Tổng Bài Nộp", stats.totalSubmissions });
			tableModel.addRow(new Object[] { "Đã Phân Tích", stats.analyzedCount });
			tableModel.addRow(new Object[] { "Chờ Xử Lý", stats.totalSubmissions - stats.analyzedCount });
			tableModel.addRow(new Object[] { "Tỷ Lệ Phân Tích", String.format("%.1f%%",
					stats.totalSubmissions > 0 ? (stats.analyzedCount * 100.0 / stats.totalSubmissions) : 0) });
			tableModel.addRow(new Object[] { "Tỷ Lệ AI Trung Bình", String.format("%.1f%%", stats.avgAi) });

			// Build detailed report
			StringBuilder report = new StringBuilder();
			report.append("═════════════════════════════════════════════════════\n");
			report.append("BÁO CÁO ĐÁNH GIÁ NGƯỜI DÙNG\n");
			report.append("═════════════════════════════════════════════════════\n\n");
			report.append("Tên Người Dùng: ").append(username).append("\n\n");

			report.append("KẾT QUẢ PHÂN TÍCH:\n");
			report.append("─────────────────────────────────────────────────────\n");
			report.append(String.format("Tổng Bài Nộp:          %d\n", stats.totalSubmissions));
			report.append(String.format("Đã Phân Tích:          %d\n", stats.analyzedCount));
			report.append(String.format("Chờ Xử Lý:             %d\n\n", stats.totalSubmissions - stats.analyzedCount));

			report.append("PHÁT HIỆN AI:\n");
			report.append("─────────────────────────────────────────────────────\n");
			report.append(String.format("Tỷ Lệ AI Trung Bình:   %.1f%%\n", stats.avgAi));

			if (stats.avgAi > 70) {
				report.append("Mức Độ Sử Dụng:        LẠM DỤNG CAO\n");
			} else if (stats.avgAi > 30) {
				report.append("Mức Độ Sử Dụng:        CÓ THAM KHẢO AI\n");
			} else {
				report.append("Mức Độ Sử Dụng:        TỰ LỰC TỐT\n");
			}

			report.append("\nTHUẬT TOÁN NỔI BẬT:\n");
			report.append("─────────────────────────────────────────────────────\n");
			for (String algo : stats.topAlgorithms) {
				report.append("• ").append(algo).append("\n");
			}

			report.append("\nXẾP HẠNG TỔNG THỂ:\n");
			report.append("─────────────────────────────────────────────────────\n");
			double skillScore = calculateSkillScore(stats);
			report.append(String.format("Điểm Kỹ Năng:          %.1f/100\n", skillScore));

			if (skillScore >= 80) {
				report.append("Xếp Hạng:              XUẤT SẮC\n");
			} else if (skillScore >= 65) {
				report.append("Xếp Hạng:              TỐT\n");
			} else if (skillScore >= 50) {
				report.append("Xếp Hạng:              TRUNG BÌNH\n");
			} else {
				report.append("Xếp Hạng:              DƯỚI TRUNG BÌNH\n");
			}

			report.append("\n═════════════════════════════════════════════════════\n");

			reportArea.setText(report.toString());

		} catch (Exception e) {
			reportArea.setText("Lỗi: " + e.getMessage());
		}
	}

	private double calculateSkillScore(ProfileEvaluator.UserStats stats) {
		int algoCount = stats.topAlgorithms.size();
		double skillScore = (algoCount * 5) + (100 - stats.avgAi) / 2.0;
		return Math.min(100, skillScore);
	}

	private void exportReport(String username) {
		try {
			String report = ProfileEvaluator.generateUserReport(username);

			javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
			fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));

			if (fileChooser.showSaveDialog(panel) == javax.swing.JFileChooser.APPROVE_OPTION) {
				java.io.File file = fileChooser.getSelectedFile();
				if (!file.getName().endsWith(".txt")) {
					file = new java.io.File(file.getAbsolutePath() + ".txt");
				}

				try (java.io.FileWriter fw = new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
					fw.write(reportArea.getText());
					JOptionPane.showMessageDialog(panel, "Đã xuất báo cáo tới: " + file.getAbsolutePath(), "Thành Công",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(panel, "Lỗi: " + e.getMessage(), "Lỗi Xuất", JOptionPane.ERROR_MESSAGE);
		}
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;"
				+ "integratedSecurity=true;encrypt=false;trustServerCertificate=true;"
				+ "characterEncoding=UTF-8;useUnicode=true;");
	}

	public JPanel getPanel() {
		return panel;
	}
}