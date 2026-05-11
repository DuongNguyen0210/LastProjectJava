package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import crawler.codeforces.CodeforcesApiCrawler;
import crawler.codeforces.CodeforcesHtmlScraper;
import crawler.vjudge.VjudgeHtmlScraper;
import crawler.vjudge.VjudgeStatusCrawler;

public class CrawlerTab {
	private JPanel panel;
	private JTextArea logArea;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private JButton startBtn;
	private JButton stopBtn;
	private JCheckBox autoLoginCheckbox;
	private volatile boolean isCrawling = false;

	public CrawlerTab() {
		createUI();
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		// Title
		JLabel titleLabel = new JLabel("🕷️  Crawler Configuration");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		panel.add(titleLabel, BorderLayout.NORTH);

		// Main content split panel
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(400);
		splitPane.setResizeWeight(0.4);

		// Left panel - Configuration
		splitPane.setLeftComponent(createConfigPanel());

		// Right panel - Logs
		splitPane.setRightComponent(createLogsPanel());

		panel.add(splitPane, BorderLayout.CENTER);
	}

	private JPanel createConfigPanel() {
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		configPanel.setBackground(Color.WHITE);
		configPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"⚙️  Settings", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

		// 1. Platform Selection
		JPanel platformPanel = createLabeledPanel("Platform:");
		String[] platforms = { "Codeforces", "Vjudge" };
		JComboBox<String> platformCombo = new JComboBox<>(platforms);
		platformCombo.setPreferredSize(new Dimension(200, 30));
		platformPanel.add(platformCombo);
		configPanel.add(platformPanel);
		configPanel.add(Box.createVerticalStrut(10));

		// 2. Days Limit
		JPanel daysPanel = createLabeledPanel("Days Limit:");
		JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
		((JSpinner.DefaultEditor) daysSpinner.getEditor()).getTextField().setFont(UIUtils.FONT_NORMAL);
		((JSpinner.DefaultEditor) daysSpinner.getEditor()).getTextField().setPreferredSize(new Dimension(80, 30));
		daysPanel.add(daysSpinner);
		configPanel.add(daysPanel);
		configPanel.add(Box.createVerticalStrut(10));

		// 3. Usernames Input
		JLabel usernamesLabel = new JLabel("Usernames (một dòng một nick):");
		usernamesLabel.setFont(UIUtils.FONT_NORMAL);
		configPanel.add(usernamesLabel);

		JTextArea usernamesArea = new JTextArea(6, 20);
		usernamesArea.setFont(UIUtils.FONT_SMALL);
		usernamesArea.setLineWrap(true);
		usernamesArea.setWrapStyleWord(true);
		usernamesArea.setText("tourist\necnerwala\njiangly\ncolorful");
		usernamesArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JScrollPane scrollPane = new JScrollPane(usernamesArea);
		configPanel.add(scrollPane);
		configPanel.add(Box.createVerticalStrut(15));

		// 4. Auto Login Checkbox
		autoLoginCheckbox = new JCheckBox("Auto-login (sử dụng saved session)", true);
		autoLoginCheckbox.setFont(UIUtils.FONT_SMALL);
		configPanel.add(autoLoginCheckbox);
		configPanel.add(Box.createVerticalStrut(15));

		// 5. Action Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		startBtn = UIUtils.createButton("▶️  Start Crawling", UIUtils.SUCCESS_COLOR);
		startBtn.setPreferredSize(new Dimension(160, 40));
		startBtn.setFont(UIUtils.FONT_HEADER);
		startBtn.addActionListener(e -> startCrawling((String) platformCombo.getSelectedItem(),
				(Integer) daysSpinner.getValue(), usernamesArea.getText(), autoLoginCheckbox.isSelected()));

		stopBtn = UIUtils.createButton("⏹️  Stop", UIUtils.ERROR_COLOR);
		stopBtn.setPreferredSize(new Dimension(120, 40));
		stopBtn.setFont(UIUtils.FONT_HEADER);
		stopBtn.setEnabled(false);
		stopBtn.addActionListener(e -> stopCrawling());

		buttonPanel.add(startBtn);
		buttonPanel.add(stopBtn);
		configPanel.add(buttonPanel);

		// Add stretch space
		configPanel.add(Box.createVerticalGlue());

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(new JScrollPane(configPanel), BorderLayout.CENTER);
		return wrapper;
	}

	private JPanel createLogsPanel() {
		JPanel logsPanel = new JPanel(new BorderLayout(0, 10));
		logsPanel.setBackground(Color.WHITE);
		logsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"📋 Logs", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

		// Status bar
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.setBackground(new Color(245, 245, 245));
		statusPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		statusLabel = new JLabel("🟢 Ready");
		statusLabel.setFont(UIUtils.FONT_SMALL);
		statusLabel.setForeground(UIUtils.SUCCESS_COLOR);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(200, 20));
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);

		statusPanel.add(statusLabel, BorderLayout.WEST);
		statusPanel.add(progressBar, BorderLayout.EAST);
		logsPanel.add(statusPanel, BorderLayout.NORTH);

		// Log text area
		logArea = new JTextArea();
		logArea.setFont(new Font("Courier New", Font.PLAIN, 10));
		logArea.setEditable(false);
		logArea.setBackground(new Color(20, 20, 20));
		logArea.setForeground(new Color(0, 255, 0));
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		logArea.setText("➤ System initialized. Ready for crawling.\n\n");

		JScrollPane scrollPane = new JScrollPane(logArea);
		logsPanel.add(scrollPane, BorderLayout.CENTER);

		// Clear button
		JButton clearBtn = new JButton("🗑️  Clear Logs");
		clearBtn.setFont(UIUtils.FONT_SMALL);
		clearBtn.setPreferredSize(new Dimension(100, 25));
		clearBtn.addActionListener(e -> logArea.setText("➤ Logs cleared.\n\n"));

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.add(clearBtn);
		logsPanel.add(bottomPanel, BorderLayout.SOUTH);

		return logsPanel;
	}

	private JPanel createLabeledPanel(String label) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		panel.setBackground(Color.WHITE);
		JLabel lbl = new JLabel(label);
		lbl.setFont(UIUtils.FONT_NORMAL);
		lbl.setPreferredSize(new Dimension(100, 30));
		panel.add(lbl);
		return panel;
	}

	private void startCrawling(String platform, int days, String usernames, boolean autoLogin) {
		if (usernames.trim().isEmpty()) {
			JOptionPane.showMessageDialog(panel, "❌ Vui lòng nhập ít nhất một username!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		isCrawling = true;
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);

		// Run in background thread
		new Thread(() -> {
			try {
				appendLog("═══════════════════════════════════════");
				appendLog("🕷️  CRAWLING STARTED");
				appendLog("═══════════════════════════════════════");
				appendLog("Platform: " + platform);
				appendLog("Days: " + days);
				appendLog("Auto-login: " + (autoLogin ? "YES" : "NO"));
				appendLog("");

				String[] users = usernames.split("\n");
				int totalUsers = users.length;

				appendLog("Processing " + totalUsers + " user(s)...\n");

				if ("Codeforces".equals(platform)) {
					updateStatus("Opening Codeforces...", UIUtils.WARNING_COLOR);
					CodeforcesHtmlScraper.initAndLogin();

					for (int i = 0; i < users.length; i++) {
						if (!isCrawling)
							break;

						String user = users[i].trim();
						if (user.isEmpty())
							continue;

						updateStatus("Processing: " + user + " (" + (i + 1) + "/" + totalUsers + ")",
								UIUtils.PRIMARY_COLOR);
						appendLog("\n▶️  Crawling: " + user);

						int crawledCount = CodeforcesApiCrawler.fetchUserSubmissions(user, days);

						appendLog("✅ Completed: " + user + " | Crawled: " + crawledCount + " submissions");
						progressBar.setValue((i + 1) * 100 / totalUsers);
					}

					CodeforcesHtmlScraper.quitDriver();

				} else if ("Vjudge".equals(platform)) {
					updateStatus("Opening Vjudge...", UIUtils.WARNING_COLOR);
					VjudgeHtmlScraper.initAndLogin();

					for (int i = 0; i < users.length; i++) {
						if (!isCrawling)
							break;

						String user = users[i].trim();
						if (user.isEmpty())
							continue;

						updateStatus("Processing: " + user + " (" + (i + 1) + "/" + totalUsers + ")",
								UIUtils.PRIMARY_COLOR);
						appendLog("\n▶️  Crawling: " + user);

						int crawledCount = VjudgeStatusCrawler.fetchUserSubmissions(user, days);

						appendLog("✅ Completed: " + user + " | Crawled: " + crawledCount + " submissions");
						progressBar.setValue((i + 1) * 100 / totalUsers);
					}

					VjudgeHtmlScraper.quitDriver();
				}

				if (isCrawling) {
					appendLog("\n═══════════════════════════════════════");
					appendLog("✅ CRAWLING FINISHED SUCCESSFULLY!");
					appendLog("═══════════════════════════════════════");
					updateStatus("✅ Completed!", UIUtils.SUCCESS_COLOR);
				}

			} catch (Exception e) {
				appendLog("\n❌ ERROR: " + e.getMessage());
				updateStatus("❌ Error: " + e.getMessage(), UIUtils.ERROR_COLOR);
			} finally {
				isCrawling = false;
				startBtn.setEnabled(true);
				stopBtn.setEnabled(false);
				progressBar.setVisible(false);
			}
		}).start();
	}

	private void stopCrawling() {
		isCrawling = false;
		appendLog("\n⚠️  Crawling stopped by user.");
		updateStatus("Stopped", UIUtils.ERROR_COLOR);
	}

	private void appendLog(String message) {
		SwingUtilities.invokeLater(() -> {
			logArea.append(message + "\n");
			// Auto-scroll to bottom
			logArea.setCaretPosition(logArea.getDocument().getLength());
		});
	}

	private void updateStatus(String message, Color color) {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText(message);
			statusLabel.setForeground(color);
			progressBar.setVisible(true);
		});
	}

	public JPanel getPanel() {
		return panel;
	}
}