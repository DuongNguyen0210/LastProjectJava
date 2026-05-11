package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class MainWindow extends JFrame {
	private JTabbedPane tabbedPane;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	public MainWindow() {
		instance = this;
		initializeFrame();
		initializeComponents();
		setVisible(true);
	}

	private void initializeFrame() {
		setTitle("🤖 Code Analysis System - Codeforces & Vjudge");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 700);
		setLocationRelativeTo(null);
		setResizable(true);
		setIconImage(createImageIcon());
	}

	private void initializeComponents() {
		// Main panel
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Header
		mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

		// Tabbed pane
		tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

		// Add tabs
		tabbedPane.addTab("🏠 Home", null, createHomeTab(), "Dashboard & Statistics");
		tabbedPane.addTab("🕷️  Crawler", null, createCrawlerTab(), "Setup & Run Crawler");
		tabbedPane.addTab("📊 Results", null, createResultsTab(), "View Analysis Results");
		tabbedPane.addTab("⭐ Evaluation", null, createEvaluationTab(), "User Evaluation Reports");

		mainPanel.add(tabbedPane, BorderLayout.CENTER);

		// Footer (Status bar)
		mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

		add(mainPanel);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(25, 118, 210)); // Material blue
		panel.setPreferredSize(new Dimension(0, 60));

		JLabel titleLabel = new JLabel("🤖 Code Analysis System");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

		panel.add(titleLabel, BorderLayout.WEST);
		return panel;
	}

	private JPanel createStatusBar() {
		JPanel panel = new JPanel(new BorderLayout(10, 0));
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setPreferredSize(new Dimension(0, 35));

		statusLabel = new JLabel("Ready");
		statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		statusLabel.setForeground(new Color(0, 128, 0)); // Green

		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(200, 25));
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setVisible(false);

		panel.add(statusLabel, BorderLayout.WEST);
		panel.add(progressBar, BorderLayout.EAST);

		return panel;
	}

	// ========== TAB CONTENTS ==========

	private JPanel createHomeTab() {
		return new HomeTab().getPanel();
	}

	private JPanel createCrawlerTab() {
		return new CrawlerTab().getPanel();
	}

	private JPanel createResultsTab() {
		return new ResultsTab().getPanel();
	}

	private JPanel createEvaluationTab() {
		return new EvaluationTab().getPanel();
	}

	// ========== UTILITIES ==========

	private Image createImageIcon() {
		// Return null hoặc tự tạo icon 16x16
		return null;
	}

	public void setStatus(String message) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(message);
			statusLabel.setForeground(new Color(0, 128, 0));
		});
	}

	public void setError(String message) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText("❌ " + message);
			statusLabel.setForeground(Color.RED);
		});
	}

	public void showProgress(int percent) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setValue(percent);
			progressBar.setVisible(true);
		});
	}

	public void hideProgress() {
		SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
	}

	private static MainWindow instance;

	public static MainWindow getInstance() {
		return instance;
	}

	public void switchTab(int tabIndex) {
		tabbedPane.setSelectedIndex(tabIndex);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainWindow());
	}
}