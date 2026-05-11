package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class ResultsTab {
	private JPanel panel;
	private JTable resultsTable;
	private DefaultTableModel tableModel;
	private JTextField filterField;
	private boolean isFiltering = false;

	public ResultsTab() {
		createUI();
		loadData();
		startAutoRefresh();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel titleLabel = new JLabel("Kết Quả Phân Tích");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		panel.add(titleLabel, BorderLayout.NORTH);

		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		filterPanel.setBackground(Color.WHITE);
		filterPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JLabel filterLabel = new JLabel("Lọc theo Tên Người Dùng:");
		filterLabel.setFont(UIUtils.FONT_NORMAL);
		filterPanel.add(filterLabel);

		filterField = new JTextField(20);
		filterField.setFont(UIUtils.FONT_NORMAL);
		filterField.setPreferredSize(new Dimension(150, 30));
		filterPanel.add(filterField);

		JButton filterBtn = UIHelper.createButton("Tìm Kiếm", UIUtils.PRIMARY_COLOR);
		filterBtn.setFocusPainted(false);
		filterBtn.setContentAreaFilled(true);
		filterBtn.setOpaque(true);
		filterBtn.addActionListener(e -> filterResults());

		JButton clearFilterBtn = UIHelper.createButton("Xóa Lọc", UIUtils.ERROR_COLOR);
		clearFilterBtn.setFocusPainted(false);
		clearFilterBtn.setContentAreaFilled(true);
		clearFilterBtn.setOpaque(true);
		clearFilterBtn.addActionListener(e -> clearFilter());

		filterPanel.add(filterBtn);
		filterPanel.add(clearFilterBtn);

		panel.add(filterPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(UIUtils.LIGHT_GRAY);

		String[] columns = { "ID", "Người Dùng", "Nền Tảng", "ID Bài", "Ngôn Ngữ", "Cấu Trúc Dữ Liệu", "Thuật Toán",
				"AI %", "Trạng Thái" };
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		resultsTable = new JTable(tableModel);
		resultsTable.setFont(UIUtils.FONT_SMALL);
		resultsTable.setRowHeight(25);
		resultsTable.getTableHeader().setFont(UIUtils.FONT_NORMAL);
		resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		resultsTable.setBackground(Color.WHITE);
		resultsTable.setSelectionBackground(UIUtils.PRIMARY_COLOR);

		JScrollPane scrollPane = new JScrollPane(resultsTable);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		panel.add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JButton exportBtn = UIHelper.createButton("Xuất CSV", UIUtils.SUCCESS_COLOR);
		exportBtn.setFocusPainted(false);
		exportBtn.setContentAreaFilled(true);
		exportBtn.setOpaque(true);
		exportBtn.addActionListener(e -> exportToCSV());

		JButton deleteBtn = UIHelper.createButton("Xóa Bài", UIUtils.ERROR_COLOR);
		deleteBtn.setFocusPainted(false);
		deleteBtn.setContentAreaFilled(true);
		deleteBtn.setOpaque(true);
		deleteBtn.addActionListener(e -> deleteSelected());

		JButton detailsBtn = UIHelper.createButton("Chi Tiết", UIUtils.PRIMARY_COLOR);
		detailsBtn.setFocusPainted(false);
		detailsBtn.setContentAreaFilled(true);
		detailsBtn.setOpaque(true);
		detailsBtn.addActionListener(e -> viewDetails());

		bottomPanel.add(exportBtn);
		bottomPanel.add(deleteBtn);
		bottomPanel.add(detailsBtn);

		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	private void startAutoRefresh() {
		new javax.swing.Timer(8000, e -> {
			if (!isFiltering) {
				loadData();
			}
		}).start();
	}

	private void loadData() {
		tableModel.setRowCount(0);

		String sql = "SELECT TOP 100 s.id, t.username, t.platform, s.submit_id, s.language, "
				+ "COALESCE(ai.data_structure, N'N/A') as data_structure, "
				+ "COALESCE(ai.algorithm, N'N/A') as algorithm, "
				+ "COALESCE(ai.ai_generated_probability, 0) as ai_generated_probability, "
				+ "CASE WHEN ai.id IS NULL THEN N'Chờ Xử Lý' ELSE N'Đã Phân Tích' END as status " + "FROM submission s "
				+ "JOIN target_account t ON s.account_id = t.id "
				+ "LEFT JOIN ai_analysis ai ON s.id = ai.submission_id " + "ORDER BY s.id DESC";

		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Object[] row = { rs.getInt("id"), rs.getString("username"), rs.getString("platform"),
						rs.getString("submit_id"), rs.getString("language"), rs.getString("data_structure"),
						rs.getString("algorithm"), String.format("%.0f", rs.getDouble("ai_generated_probability")),
						rs.getString("status") };
				tableModel.addRow(row);
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(panel, "Lỗi: " + e.getMessage(), "Lỗi Cơ Sở Dữ Liệu",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void filterResults() {
		String filter = filterField.getText().trim();
		if (filter.isEmpty()) {
			isFiltering = false;
			loadData();
			return;
		}

		isFiltering = true;

		tableModel.setRowCount(0);

		String sql = "SELECT TOP 100 s.id, t.username, t.platform, s.submit_id, s.language, "
				+ "COALESCE(ai.data_structure, N'N/A') as data_structure, "
				+ "COALESCE(ai.algorithm, N'N/A') as algorithm, "
				+ "COALESCE(ai.ai_generated_probability, 0) as ai_generated_probability, "
				+ "CASE WHEN ai.id IS NULL THEN N'Chờ Xử Lý' ELSE N'Đã Phân Tích' END as status " + "FROM submission s "
				+ "JOIN target_account t ON s.account_id = t.id "
				+ "LEFT JOIN ai_analysis ai ON s.id = ai.submission_id " + "WHERE t.username LIKE ? "
				+ "ORDER BY s.id DESC";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, "%" + filter + "%");
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("id"), rs.getString("username"), rs.getString("platform"),
						rs.getString("submit_id"), rs.getString("language"), rs.getString("data_structure"),
						rs.getString("algorithm"), String.format("%.0f", rs.getDouble("ai_generated_probability")),
						rs.getString("status") };
				tableModel.addRow(row);
			}

			isFiltering = false;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(panel, "Lỗi: " + e.getMessage(), "Lỗi Cơ Sở Dữ Liệu",
					JOptionPane.ERROR_MESSAGE);
			isFiltering = false;
		}
	}

	private void clearFilter() {
		filterField.setText("");
		isFiltering = false;
		loadData();
	}

	private void deleteSelected() {
		int selectedRow = resultsTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(panel, "Vui lòng chọn một bài nộp để xóa", "Thông Báo",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(panel,
				"Xác nhận xóa bài nộp này?\n\nHành động này không thể hoàn tác!", "Xóa Bài Nộp",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		Object submissionId = tableModel.getValueAt(selectedRow, 0);

		String sql = "DELETE FROM submission WHERE id = ?";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, Integer.parseInt(submissionId.toString()));
			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				tableModel.removeRow(selectedRow);
				JOptionPane.showMessageDialog(panel, "Đã xóa bài nộp thành công!", "Thành Công",
						JOptionPane.INFORMATION_MESSAGE);
				isFiltering = false;
				loadData();
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(panel, "Lỗi: " + e.getMessage(), "Lỗi Cơ Sở Dữ Liệu",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewDetails() {
		int selectedRow = resultsTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(panel, "Vui lòng chọn một dòng", "Thông Tin",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Object id = tableModel.getValueAt(selectedRow, 0);
		String username = tableModel.getValueAt(selectedRow, 1).toString();
		String platform = tableModel.getValueAt(selectedRow, 2).toString();
		String submitId = tableModel.getValueAt(selectedRow, 3).toString();
		String language = tableModel.getValueAt(selectedRow, 4).toString();
		String dsa = tableModel.getValueAt(selectedRow, 5).toString();
		String algorithm = tableModel.getValueAt(selectedRow, 6).toString();
		String aiScore = tableModel.getValueAt(selectedRow, 7).toString();
		String status = tableModel.getValueAt(selectedRow, 8).toString();

		String details = String.format(
				"═════════════════════════════════════════\n" + "CHI TIẾT BÀI NỘP\n"
						+ "═════════════════════════════════════════\n\n" + "ID:                    %s\n"
						+ "Người Dùng:            %s\n" + "Nền Tảng:              %s\n" + "ID Bài:                %s\n"
						+ "Ngôn Ngữ:              %s\n\n" + "CẤU TRÚC DỮ LIỆU:\n" + "%s\n\n" + "THUẬT TOÁN:\n"
						+ "%s\n\n" + "PHÂN TÍCH AI:\n" + "Điểm AI:               %s%%\n" + "Trạng Thái:            %s\n"
						+ "═════════════════════════════════════════",
				id, username, platform, submitId, language, dsa, algorithm, aiScore, status);

		JTextArea textArea = new JTextArea(details);
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
		textArea.setBackground(new java.awt.Color(250, 250, 250));
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(500, 400));

		JOptionPane.showMessageDialog(panel, scrollPane, "Chi Tiết Bài Nộp", JOptionPane.INFORMATION_MESSAGE);
	}

	private void exportToCSV() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

		if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.getName().endsWith(".csv")) {
				file = new File(file.getAbsolutePath() + ".csv");
			}

			try (FileWriter fw = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8);
					BufferedWriter bw = new BufferedWriter(fw)) {

				for (int i = 0; i < tableModel.getColumnCount(); i++) {
					bw.write(tableModel.getColumnName(i));
					if (i < tableModel.getColumnCount() - 1)
						bw.write(",");
				}
				bw.newLine();

				for (int i = 0; i < tableModel.getRowCount(); i++) {
					for (int j = 0; j < tableModel.getColumnCount(); j++) {
						Object value = tableModel.getValueAt(i, j);
						bw.write(value != null ? value.toString() : "");
						if (j < tableModel.getColumnCount() - 1)
							bw.write(",");
					}
					bw.newLine();
				}

				JOptionPane.showMessageDialog(panel, "Đã xuất tới: " + file.getAbsolutePath(), "Thành Công",
						JOptionPane.INFORMATION_MESSAGE);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(panel, "Lỗi: " + e.getMessage(), "Lỗi Xuất", JOptionPane.ERROR_MESSAGE);
			}
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