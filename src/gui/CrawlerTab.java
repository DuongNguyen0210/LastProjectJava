package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import crawler.codeforces.CodeforcesApiCrawler;
import crawler.codeforces.CodeforcesHtmlScraper;

public class CrawlerTab {
	private JPanel panel;
	private JTextArea logArea;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private JButton startBtn;
	private JButton stopBtn;
	private JComboBox<String> platformCombo;
	private JSpinner daysSpinner;
	private JTextArea usernamesArea;
	private JTextField botUserField;
	private JPasswordField botPassField;
	private volatile boolean isCrawling = false;

	public CrawlerTab() {
		createUI();

		// ========================================================
		// ĐỌC MẬT THƯ (THÔNG SỐ) TỪ FILE .BAT
		// ========================================================
		String botUser = System.getProperty("botUser");
		String botPass = System.getProperty("botPass");
		String crawlDaysStr = System.getProperty("crawlDays");
		String crawlUsersStr = System.getProperty("crawlUsers");

		if (botUser != null) {
			botUserField.setText(botUser);
		}
		if (botPass != null) {
			botPassField.setText(botPass);
		}

		if (crawlDaysStr != null) {
			try {
				int d = Integer.parseInt(crawlDaysStr.trim());
				daysSpinner.setValue(d);
			} catch (NumberFormatException ignored) {
			}
		}

		if (crawlUsersStr != null && !crawlUsersStr.trim().isEmpty()) {
			String formattedUsers = crawlUsersStr.replace(",", "\n").replace(";", "\n");
			usernamesArea.setText(formattedUsers);
		}

		SwingUtilities.invokeLater(() -> {
			checkInputFields();
			if (startBtn != null && startBtn.isEnabled()) {
				startBtn.doClick();
			} else {
				appendLog("Hệ thống phát hiện chưa đủ thông tin cài đặt tự động. Vui lòng nhập tay để tiếp tục!");
			}
		});
	}

	private void createUI() {
		panel = new JPanel(new BorderLayout(15, 15));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		panel.setBackground(UIUtils.LIGHT_GRAY);

		JLabel titleLabel = new JLabel("Cào Mã Nguồn");
		titleLabel.setFont(UIUtils.FONT_TITLE);
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		panel.add(titleLabel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.35);
		splitPane.setLeftComponent(createConfigPanel());
		splitPane.setRightComponent(createLogsPanel());
		panel.add(splitPane, BorderLayout.CENTER);
	}

	private JPanel createConfigPanel() {
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
		configPanel.setBackground(Color.WHITE);
		configPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"Cấu Hình", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));
		configPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel platformPanel = createConfigRow("Nền Tảng:", 100);
		String[] platforms = { "Codeforces" };
		platformCombo = new JComboBox<>(platforms);
		platformCombo.setFont(UIUtils.FONT_NORMAL);
		platformCombo.setPreferredSize(new Dimension(150, 30));
		platformPanel.add(platformCombo);
		configPanel.add(platformPanel);
		configPanel.add(Box.createVerticalStrut(12));

		JPanel daysPanel = createConfigRow("Số Ngày:", 100);
		daysSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
		daysSpinner.setFont(UIUtils.FONT_NORMAL);
		daysSpinner.setPreferredSize(new Dimension(80, 30));
		daysPanel.add(daysSpinner);
		configPanel.add(daysPanel);
		configPanel.add(Box.createVerticalStrut(12));

		JPanel userPanel = createConfigRow("Tài khoản Bot:", 100);
		botUserField = new JTextField();
		botUserField.setFont(UIUtils.FONT_NORMAL);
		botUserField.setPreferredSize(new Dimension(150, 30));
		userPanel.add(botUserField);
		configPanel.add(userPanel);
		configPanel.add(Box.createVerticalStrut(12));

		JPanel passPanel = createConfigRow("Mật khẩu Bot:", 100);
		botPassField = new JPasswordField();
		botPassField.setFont(UIUtils.FONT_NORMAL);
		botPassField.setPreferredSize(new Dimension(150, 30));
		passPanel.add(botPassField);
		configPanel.add(passPanel);
		configPanel.add(Box.createVerticalStrut(12));

		JLabel usernamesLabel = new JLabel("Tên Người Dùng (một dòng một nick):");
		usernamesLabel.setFont(UIUtils.FONT_NORMAL);
		configPanel.add(usernamesLabel);

		usernamesArea = new JTextArea(6, 20);
		usernamesArea.setFont(new Font("Courier New", Font.PLAIN, 11));
		usernamesArea.setLineWrap(true);
		usernamesArea.setWrapStyleWord(true);
		// Giá trị mặc định (nếu file .bat không truyền thì nó hiện cái này)
		usernamesArea.setText("tourist\necnerwala\njiangly");
		usernamesArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JScrollPane scrollPane = new JScrollPane(usernamesArea);
		configPanel.add(scrollPane);
		configPanel.add(Box.createVerticalStrut(20));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
		buttonPanel.setBackground(Color.WHITE);

		startBtn = UIHelper.createHeaderButton("Bắt Đầu Cào", UIUtils.SUCCESS_COLOR);
		startBtn.setFocusPainted(false);
		startBtn.setContentAreaFilled(true);
		startBtn.setOpaque(true);
		startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		startBtn.setEnabled(false);
		startBtn.addActionListener(e -> startCrawling());

		stopBtn = UIHelper.createHeaderButton("Dừng", UIUtils.ERROR_COLOR);
		stopBtn.setEnabled(false);
		stopBtn.setFocusPainted(false);
		stopBtn.setContentAreaFilled(true);
		stopBtn.setOpaque(true);
		stopBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		stopBtn.addActionListener(e -> stopCrawling());

		buttonPanel.add(startBtn);
		buttonPanel.add(stopBtn);
		configPanel.add(buttonPanel);

		configPanel.add(Box.createVerticalGlue());

		// THEO DÕI SỰ THAY ĐỔI CỦA TẤT CẢ CÁC Ô QUAN TRỌNG
		DocumentListener fieldListener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { checkInputFields(); }
			@Override public void removeUpdate(DocumentEvent e) { checkInputFields(); }
			@Override public void changedUpdate(DocumentEvent e) { checkInputFields(); }
		};
		botUserField.getDocument().addDocumentListener(fieldListener);
		botPassField.getDocument().addDocumentListener(fieldListener);
		usernamesArea.getDocument().addDocumentListener(fieldListener);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(new JScrollPane(configPanel), BorderLayout.CENTER);
		return wrapper;
	}

	private void checkInputFields() {
		if (startBtn == null || isCrawling) return;

		String user = botUserField.getText().trim();
		String pass = new String(botPassField.getPassword()).trim();
		String targetUsers = usernamesArea.getText().trim();

		// Điều kiện để nút SÁNG: User Bot có, Pass Bot có, và Ô danh sách người dùng cũng phải có chữ
		startBtn.setEnabled(!user.isEmpty() && !pass.isEmpty() && !targetUsers.isEmpty());
	}

	private JPanel createConfigRow(String label, int labelWidth) {
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		pnl.setBackground(Color.WHITE);
		JLabel lbl = new JLabel(label);
		lbl.setFont(UIUtils.FONT_NORMAL);
		lbl.setPreferredSize(new Dimension(labelWidth, 30));
		pnl.add(lbl);
		return pnl;
	}

	private JPanel createLogsPanel() {
		JPanel logsPanel = new JPanel(new BorderLayout(0, 10));
		logsPanel.setBackground(Color.WHITE);
		logsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR, 2),
				"Nhật Ký", TitledBorder.LEFT, TitledBorder.TOP, UIUtils.FONT_HEADER));

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
		logArea.setText("Hệ thống sẵn sàng.\n\n");

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

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.add(clearBtn);
		logsPanel.add(bottomPanel, BorderLayout.SOUTH);

		return logsPanel;
	}

	private void startCrawling() {
		String platform = (String) platformCombo.getSelectedItem();
		int days = (Integer) daysSpinner.getValue();
		String usernames = usernamesArea.getText();

		if (usernames.trim().isEmpty()) {
			JOptionPane.showMessageDialog(panel, "Vui lòng nhập ít nhất một tên người dùng!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String botUser = botUserField.getText();
		String botPass = new String(botPassField.getPassword());

		if (botUser.trim().isEmpty() || botPass.trim().isEmpty()) {
			JOptionPane.showMessageDialog(panel, "Vui lòng nhập thông tin tài khoản Bot!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		isCrawling = true;
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);

		new Thread(() -> {
			try {
				appendLog("==============================================");
				appendLog("CÀO MÃ ĐÃ BẮT ĐẦU");
				appendLog("==============================================");
				appendLog("Nền tảng: " + platform);
				appendLog("Số ngày: " + days);
				appendLog("");

				String[] users = usernames.split("\n");
				appendLog("Người dùng mục tiêu: " + users.length);
				appendLog("");

				progressBar.setVisible(true);
				progressBar.setValue(0);

				if ("Codeforces".equals(platform)) {
					crawlCodeforces(platform, days, users, botUser, botPass);
				}

				if (isCrawling) {
					appendLog("\n==============================================");
					appendLog("CÀO MÃ HOÀN TẤT THÀNH CÔNG!");
					appendLog("==============================================");
					updateStatus("Hoàn tất!", UIUtils.SUCCESS_COLOR);
					progressBar.setValue(0);
				}

			} catch (Exception e) {
				appendLog("\nLỖI: " + e.getMessage());
				updateStatus("Lỗi!", UIUtils.ERROR_COLOR);
				progressBar.setValue(0);
				e.printStackTrace();
			} finally {
				isCrawling = false;
				SwingUtilities.invokeLater(() -> checkInputFields());
				stopBtn.setEnabled(false);
				progressBar.setVisible(false);
			}
		}).start();
	}

	private void crawlCodeforces(String platform, int days, String[] users, String botUser, String botPass) {
		updateStatus("Đang mở trình duyệt...", UIUtils.WARNING_COLOR);
		appendLog("Đang mở trình duyệt...");

		CodeforcesHtmlScraper.initAndLogin();

		updateStatus("Đang tự động đăng nhập...", UIUtils.WARNING_COLOR);
		appendLog("Đang tự động đăng nhập với tài khoản: " + botUser);

		boolean loginSuccess = CodeforcesHtmlScraper.autoLogin(botUser, botPass);

		if (!loginSuccess) {
			CodeforcesHtmlScraper.quitDriver();
			appendLog("Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản hoặc kết nối mạng.");
			updateStatus("Lỗi đăng nhập", UIUtils.ERROR_COLOR);
			progressBar.setValue(0);
			isCrawling = false;
			return;
		}

		appendLog("Đăng nhập tự động thành công! Bắt đầu cào...\n");
		progressBar.setValue(0);

		for (int i = 0; i < users.length && isCrawling; i++) {
			String user = users[i].trim();
			if (user.isEmpty())
				continue;

			updateStatus("Đang xử lý: " + user + " (" + (i + 1) + "/" + users.length + ")", UIUtils.PRIMARY_COLOR);
			appendLog("Đang xử lý: " + user);

			try {
				int count = CodeforcesApiCrawler.fetchUserSubmissions(user, days);
				appendLog("Hoàn tất: " + user + " | Tìm thấy: " + count + " bài nộp");
			} catch (Exception e) {
				appendLog("Lỗi cho " + user + ": " + e.getMessage());
			}

			int progress = ((i + 1) * 100) / users.length;
			progressBar.setValue(progress);
		}

		CodeforcesHtmlScraper.quitDriver();
		progressBar.setValue(100);
	}

	private void stopCrawling() {
		isCrawling = false;
		appendLog("\nCào mã đã bị dừng bởi người dùng.");
		updateStatus("Đã dừng", UIUtils.ERROR_COLOR);
		progressBar.setValue(0);
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
			progressBar.setVisible(true);
		});
	}

	public JPanel getPanel() {
		return panel;
	}
}