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
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class ResultsTab {
	private JPanel panel;
	private JTable resultsTable;
	private DefaultTableModel tableModel;
	private JTextField filterField;

	public ResultsTab() {
		createUI();
		loadData();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		// Title
		JLabel titleLabel = new JLabel("📊 Analysis Results");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		panel.add(titleLabel, BorderLayout.NORTH);

		// Filter panel
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		filterPanel.setBackground(Color.WHITE);
		filterPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JLabel filterLabel = new JLabel("🔍 Filter by Username:");
		filterLabel.setFont(UIUtils.FONT_NORMAL);
		filterPanel.add(filterLabel);

		filterField = new JTextField(20);
		filterField.setFont(UIUtils.FONT_NORMAL);
		filterField.setPreferredSize(new Dimension(150, 30));
		filterPanel.add(filterField);

		JButton filterBtn = new JButton("🔎 Search");
		filterBtn.setFont(UIUtils.FONT_SMALL);
		filterBtn.setPreferredSize(new Dimension(100, 30));
		filterBtn.addActionListener(e -> filterResults());

		JButton clearFilterBtn = new JButton("✕ Clear");
		clearFilterBtn.setFont(UIUtils.FONT_SMALL);
		clearFilterBtn.setPreferredSize(new Dimension(80, 30));
		clearFilterBtn.addActionListener(e -> {
			filterField.setText("");
			loadData();
		});

		filterPanel.add(filterBtn);
		filterPanel.add(clearFilterBtn);

		panel.add(filterPanel, BorderLayout.NORTH);

		// Main content with table
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(UIUtils.LIGHT_GRAY);

		// Table
		String[] columns = { "ID", "Username", "Platform", "Submit ID", "Language", "Data Structure", "Algorithm",
				"AI %", "Status" };
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

		// Bottom panel with action buttons
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JButton refreshBtn = UIUtils.createButton("🔄 Refresh", UIUtils.PRIMARY_COLOR);
		refreshBtn.addActionListener(e -> loadData());

		JButton exportBtn = UIUtils.createButton("💾 Export CSV", UIUtils.SUCCESS_COLOR);
		exportBtn.addActionListener(e -> exportToCSV());

		JButton exportExcelBtn = UIUtils.createButton("📊 Export Excel", UIUtils.WARNING_COLOR);
		exportExcelBtn.addActionListener(e -> exportToExcel());

		JButton detailsBtn = UIUtils.createButton("👁️  View Details", UIUtils.PRIMARY_COLOR);
		detailsBtn.addActionListener(e -> viewDetails());

		bottomPanel.add(refreshBtn);
		bottomPanel.add(exportBtn);
		bottomPanel.add(exportExcelBtn);
		bottomPanel.add(detailsBtn);

		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	private void loadData() {
		tableModel.setRowCount(0);

		String sql = "SELECT TOP 100 s.id, t.username, t.platform, s.submit_id, s.language, "
				+ "s.data_structure, s.algorithm, s.ai_generated_probability, "
				+ "CASE WHEN s.data_structure IS NULL THEN 'Pending' ELSE 'Analyzed' END as status "
				+ "FROM submission s JOIN target_account t ON s.account_id = t.id " + "ORDER BY s.id DESC";

		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Object[] row = { rs.getInt("id"), rs.getString("username"), rs.getString("platform"),
						rs.getString("submit_id"), rs.getString("language"),
						rs.getString("data_structure") != null ? rs.getString("data_structure") : "N/A",
						rs.getString("algorithm") != null ? rs.getString("algorithm") : "N/A",
						String.format("%.0f", rs.getDouble("ai_generated_probability")), rs.getString("status") };
				tableModel.addRow(row);
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(panel, "❌ Error: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void filterResults() {
		String filter = filterField.getText().trim();
		if (filter.isEmpty()) {
			loadData();
			return;
		}

		tableModel.setRowCount(0);

		String sql = "SELECT TOP 100 s.id, t.username, t.platform, s.submit_id, s.language, "
				+ "s.data_structure, s.algorithm, s.ai_generated_probability, "
				+ "CASE WHEN s.data_structure IS NULL THEN 'Pending' ELSE 'Analyzed' END as status "
				+ "FROM submission s JOIN target_account t ON s.account_id = t.id " + "WHERE t.username LIKE ? "
				+ "ORDER BY s.id DESC";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, "%" + filter + "%");
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				Object[] row = { rs.getInt("id"), rs.getString("username"), rs.getString("platform"),
						rs.getString("submit_id"), rs.getString("language"),
						rs.getString("data_structure") != null ? rs.getString("data_structure") : "N/A",
						rs.getString("algorithm") != null ? rs.getString("algorithm") : "N/A",
						String.format("%.0f", rs.getDouble("ai_generated_probability")), rs.getString("status") };
				tableModel.addRow(row);
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(panel, "❌ Error: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewDetails() {
		int selectedRow = resultsTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(panel, "⚠️  Vui lòng chọn một dòng", "Info", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Object id = tableModel.getValueAt(selectedRow, 0);
		String details = "ID: " + id + "\n" + "Username: " + tableModel.getValueAt(selectedRow, 1) + "\n" + "Platform: "
				+ tableModel.getValueAt(selectedRow, 2) + "\n" + "Submit ID: " + tableModel.getValueAt(selectedRow, 3)
				+ "\n" + "Language: " + tableModel.getValueAt(selectedRow, 4) + "\n" + "Data Structure: "
				+ tableModel.getValueAt(selectedRow, 5) + "\n" + "Algorithm: " + tableModel.getValueAt(selectedRow, 6)
				+ "\n" + "AI Score: " + tableModel.getValueAt(selectedRow, 7) + "%\n" + "Status: "
				+ tableModel.getValueAt(selectedRow, 8);

		JOptionPane.showMessageDialog(panel, details, "📋 Chi tiết", JOptionPane.INFORMATION_MESSAGE);
	}

	private void exportToCSV() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

		if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.getName().endsWith(".csv")) {
				file = new File(file.getAbsolutePath() + ".csv");
			}

			try (FileWriter fw = new FileWriter(file); BufferedWriter bw = new BufferedWriter(fw)) {

				// Header
				for (int i = 0; i < tableModel.getColumnCount(); i++) {
					bw.write(tableModel.getColumnName(i));
					if (i < tableModel.getColumnCount() - 1)
						bw.write(",");
				}
				bw.newLine();

				// Data
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					for (int j = 0; j < tableModel.getColumnCount(); j++) {
						Object value = tableModel.getValueAt(i, j);
						bw.write(value != null ? value.toString() : "");
						if (j < tableModel.getColumnCount() - 1)
							bw.write(",");
					}
					bw.newLine();
				}

				JOptionPane.showMessageDialog(panel, "✅ Exported to:\n" + file.getAbsolutePath(), "Success",
						JOptionPane.INFORMATION_MESSAGE);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(panel, "❌ Error: " + e.getMessage(), "Export Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void exportToExcel() {
		JOptionPane.showMessageDialog(panel, "📊 Excel export coming soon!\n\nFor now, use CSV export.", "Info",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
				"jdbc:sqlserver://localhost:1433;databaseName=LastProjectJava;integratedSecurity=true;encrypt=false;trustServerCertificate=true;");
	}

	public JPanel getPanel() {
		return panel;
	}
}