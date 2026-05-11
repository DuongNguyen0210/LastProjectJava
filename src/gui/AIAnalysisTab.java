package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import utils.GeminiAnalyzer;

public class AIAnalysisTab {
	private JPanel panel;
	private JTextArea logArea;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private JButton startBtn;
	private JButton stopBtn;
	private JSpinner batchSizeSpinner;
	private JLabel pendingLabel;
	private JLabel analyzedLabel;
	private volatile boolean isAnalyzing = false;

	public AIAnalysisTab() {
		createUI();
		startAutoRefresh();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel titleLabel = new JLabel("Phân Tích Mã Bằng AI");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		panel.add(titleLabel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.25);
		splitPane.setLeftComponent(createControlPanel());
		splitPane.setRightComponent(createLogsPanel());
		panel.add(splitPane, BorderLayout.CENTER);
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBackground(Color.WHITE);
		controlPanel
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
						"Điều Khiển Phân Tích", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JLabel infoLabel = new JLabel("Trạng Thái Phân Tích");
		infoLabel.setFont(UIUtils.FONT_HEADER);
		controlPanel.add(infoLabel);
		controlPanel.add(Box.createVerticalStrut(15));

		JPanel statsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
		statsPanel.setBackground(Color.WHITE);

		JPanel pendingPanel = createStatBox("Chờ Phân Tích", "0");
		JPanel analyzedPanel = createStatBox("Đã Phân Tích", "0");

		pendingLabel = (JLabel) pendingPanel.getComponent(1);
		analyzedLabel = (JLabel) analyzedPanel.getComponent(1);

		statsPanel.add(pendingPanel);
		statsPanel.add(analyzedPanel);
		controlPanel.add(statsPanel);
		controlPanel.add(Box.createVerticalStrut(20));

//		JLabel batchLabel = new JLabel("Kích Thước Batch:");
//		batchLabel.setFont(UIUtils.FONT_NORMAL);
//		controlPanel.add(batchLabel);

//		JPanel batchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
//		batchPanel.setBackground(Color.WHITE);
//		batchSizeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
//		batchSizeSpinner.setFont(UIUtils.FONT_NORMAL);
//		batchSizeSpinner.setPreferredSize(new Dimension(80, 30));
//		batchPanel.add(batchSizeSpinner);
//		controlPanel.add(batchPanel);
//		controlPanel.add(Box.createVerticalStrut(20));

		JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
		buttonPanel.setBackground(Color.WHITE);

		startBtn = UIHelper.createHeaderButton("Bắt Đầu", UIUtils.SUCCESS_COLOR);
		startBtn.setFocusPainted(false);
		startBtn.setContentAreaFilled(true);
		startBtn.setOpaque(true);
		startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		startBtn.addActionListener(e -> startAnalysis());

		stopBtn = UIHelper.createHeaderButton("Dừng", UIUtils.ERROR_COLOR);
		stopBtn.setEnabled(false);
		stopBtn.setFocusPainted(false);
		stopBtn.setContentAreaFilled(true);
		stopBtn.setOpaque(true);
		stopBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		stopBtn.addActionListener(e -> stopAnalysis());

		buttonPanel.add(startBtn);
		buttonPanel.add(stopBtn);
		controlPanel.add(buttonPanel);
		controlPanel.add(Box.createVerticalStrut(20));

		JLabel noteLabel = new JLabel("Sử dụng Google Gemini API");
		noteLabel.setFont(UIUtils.FONT_SMALL);
		noteLabel.setForeground(new Color(100, 100, 100));
		controlPanel.add(noteLabel);

		controlPanel.add(Box.createVerticalGlue());

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(new JScrollPane(controlPanel), BorderLayout.CENTER);
		return wrapper;
	}

	private JPanel createStatBox(String label, String value) {
		JPanel box = new JPanel(new BorderLayout());
		box.setBackground(new Color(240, 240, 240));
		box.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

		JLabel labelComp = new JLabel(label);
		labelComp.setFont(UIUtils.FONT_SMALL);
		labelComp.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

		JLabel valueComp = new JLabel(value);
		valueComp.setFont(new Font("Segoe UI", Font.BOLD, 24));
		valueComp.setHorizontalAlignment(SwingConstants.CENTER);
		valueComp.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

		box.add(labelComp, BorderLayout.NORTH);
		box.add(valueComp, BorderLayout.CENTER);

		return box;
	}

	private JPanel createLogsPanel() {
		JPanel logsPanel = new JPanel(new BorderLayout(0, 10));
		logsPanel.setBackground(Color.WHITE);
		logsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"Nhật Ký Phân Tích", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.setBackground(new Color(245, 245, 245));
		statusPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		statusLabel = new JLabel("Sẵn Sàng");
		statusLabel.setFont(UIUtils.FONT_SMALL);
		statusLabel.setForeground(UIUtils.SUCCESS_COLOR);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		progressBar.setPreferredSize(new Dimension(150, 20));

		statusPanel.add(statusLabel, BorderLayout.WEST);
		statusPanel.add(progressBar, BorderLayout.EAST);
		logsPanel.add(statusPanel, BorderLayout.NORTH);

		logArea = new JTextArea();
		logArea.setFont(new Font("Courier New", Font.PLAIN, 11));
		logArea.setEditable(false);
		logArea.setBackground(new Color(20, 20, 20));
		logArea.setForeground(new Color(0, 255, 0));
		logArea.setText("Hệ thống AI sẵn sàng\n\n");

		JScrollPane scrollPane = new JScrollPane(logArea);
		logsPanel.add(scrollPane, BorderLayout.CENTER);

		JButton clearBtn = UIHelper.createSmallButton("Xóa Nhật Ký", UIUtils.ERROR_COLOR);
		clearBtn.setFocusPainted(false);
		clearBtn.setContentAreaFilled(true);
		clearBtn.setOpaque(true);
		clearBtn.addActionListener(e -> {
			logArea.setText("Nhật ký đã xóa.\n\n");
			statusLabel.setText("Sẵn Sàng");
			statusLabel.setForeground(UIUtils.SUCCESS_COLOR);
		});

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.add(clearBtn);
		logsPanel.add(bottomPanel, BorderLayout.SOUTH);

		return logsPanel;
	}

	private void startAutoRefresh() {
		new javax.swing.Timer(5000, e -> refreshStats()).start();
	}

	private void refreshStats() {
		new SwingWorker<int[], Void>() {
			@Override
			protected int[] doInBackground() {
				return new int[] { getPendingCount(), getAnalyzedCount() };
			}

			@Override
			protected void done() {
				try {
					int[] stats = get();
					pendingLabel.setText(String.valueOf(stats[0]));
					analyzedLabel.setText(String.valueOf(stats[1]));
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				}
			}
		}.execute();
	}

	private void startAnalysis() {
		int pendingCount = getPendingCount();
		if (pendingCount == 0) {
			JOptionPane.showMessageDialog(panel, "Không có bài nộp chờ phân tích!", "Thông Tin",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(panel,
				"Phân tích " + pendingCount + " bài nộp chờ xử lý?\n\n"
						+ "Sẽ sử dụng Google Gemini API và mất nhiều thời gian.\n" + "Thời gian ước tính: "
						+ (pendingCount * 30 / 60) + " phút",
				"Xác Nhận Phân Tích", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		isAnalyzing = true;
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);

		new Thread(() -> {
			try {
				appendLog("==============================================");
				appendLog("PHÂN TÍCH AI ĐÃ BẮTĐẦU");
				appendLog("==============================================");
				appendLog("Bài nộp chờ xử lý: " + pendingCount);
				// appendLog("Kích thước Batch: " + batchSizeSpinner.getValue());
				appendLog("");

				updateStatus("Đang phân tích bằng Gemini API...", UIUtils.WARNING_COLOR);
				progressBar.setVisible(true);
				progressBar.setValue(0);

				analyzeWithProgressTracking(pendingCount);

				if (isAnalyzing) {
					appendLog("\n==============================================");
					appendLog("PHÂN TÍCH HOÀN TẤT THÀNH CÔNG!");
					appendLog("==============================================");
					updateStatus("Hoàn tất!", UIUtils.SUCCESS_COLOR);
					refreshStats();
				}

			} catch (Exception e) {
				appendLog("LỖI: " + e.getMessage());
				updateStatus("Lỗi!", UIUtils.ERROR_COLOR);
				e.printStackTrace();
			} finally {
				isAnalyzing = false;
				startBtn.setEnabled(true);
				stopBtn.setEnabled(false);
				progressBar.setVisible(false);
			}
		}).start();
	}

	private void stopAnalysis() {
		isAnalyzing = false;
		appendLog("\nPhân tích đã bị dừng bởi người dùng.");
		updateStatus("Đã dừng", UIUtils.ERROR_COLOR);
	}

	private void analyzeWithProgressTracking(int totalPending) {
		String SELECT_QUERY = "SELECT s.id, s.source_code " + "FROM submission s "
				+ "LEFT JOIN ai_analysis a ON s.id = a.submission_id " + "WHERE a.id IS NULL";

		String INSERT_QUERY = "INSERT INTO ai_analysis (submission_id, data_structure, algorithm, ai_generated_probability, ai_evaluation_note) "
				+ "VALUES (?, ?, ?, ?, ?)";

		int processedCount = 0;
		int totalCount = 0;

		try (Connection conn = getConnection();
				PreparedStatement selectStmt = conn.prepareStatement(SELECT_QUERY);
				ResultSet rs = selectStmt.executeQuery();
				PreparedStatement insertStmt = conn.prepareStatement(INSERT_QUERY)) {

			while (rs.next() && isAnalyzing) {
				int id = rs.getInt("id");
				String code = rs.getString("source_code");

				if (code != null && code.length() > 3000) {
					code = code.substring(code.length() - 3000);
				}

				appendLog("Đang xử lý ID: " + id);
				totalCount++;

				try {
					String jsonResponse = GeminiAnalyzer.analyzeCode(code);

					if (jsonResponse.contains("Error") || jsonResponse.contains("error")) {
						appendLog("API quá tải. Chờ...");
						Thread.sleep(60000);
						continue;
					}

					if (saveResultToDb(insertStmt, id, jsonResponse)) {
						processedCount++;
						appendLog("Hoàn tất ID: " + id);

						int progress = (processedCount * 100) / totalPending;
						updateProgress(progress);

						Thread.sleep(25000);
					}
				} catch (Exception e) {
					appendLog("Lỗi xử lý ID " + id + ": " + e.getMessage());
				}
			}

			appendLog("\nĐã xử lý: " + processedCount + " / " + totalCount);

		} catch (Exception e) {
			appendLog("Lỗi Cơ Sở Dữ Liệu: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean saveResultToDb(PreparedStatement pstmt, int submissionId, String jsonStr) {
		try {
			com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(jsonStr).getAsJsonObject();
			pstmt.setInt(1, submissionId);
			pstmt.setString(2, getString(obj, "data_structure"));
			pstmt.setString(3, getString(obj, "algorithm"));
			pstmt.setDouble(4,
					obj.has("ai_generated_probability") ? obj.get("ai_generated_probability").getAsDouble() : 0.0);
			pstmt.setString(5, getString(obj, "note"));

			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			appendLog("Lỗi phân tích ID " + submissionId);
			return false;
		}
	}

	private String getString(com.google.gson.JsonObject obj, String key) {
		return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : "N/A";
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

	private void appendLog(String message) {
		SwingUtilities.invokeLater(() -> {
			logArea.append(message + "\n");
			logArea.setCaretPosition(logArea.getDocument().getLength());
		});
	}

	private void updateStatus(String message, Color color) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(message);
			statusLabel.setForeground(color);
		});
	}

	private void updateProgress(int percent) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(percent);
			progressBar.repaint();
		});
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;"
				+ "integratedSecurity=true;encrypt=false;trustServerCertificate=true;");
	}

	public JPanel getPanel() {
		return panel;
	}
}