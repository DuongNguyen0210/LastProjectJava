package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicButtonUI;

public class LoginDialog extends JDialog {
	private boolean confirmed = false;
	private CountDownLatch latch;

	public LoginDialog(JFrame parent, String platform) {
		super(parent, "Xác Nhận Đăng Nhập - " + platform, true);
		latch = new CountDownLatch(1);
		createUI(platform);
	}

	private void createUI(String platform) {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setSize(850, 700);
		setLocationRelativeTo(getParent());
		setResizable(true);

		JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		mainPanel.setBackground(new Color(245, 245, 245));

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBackground(new Color(245, 245, 245));

		JLabel titleLabel = new JLabel("Xác Nhận Đăng Nhập");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
		titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		headerPanel.add(titleLabel);

		JLabel platformLabel = new JLabel(platform);
		platformLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		platformLabel.setForeground(new Color(100, 100, 100));
		platformLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		headerPanel.add(Box.createVerticalStrut(5));
		headerPanel.add(platformLabel);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		JTextArea instructionsArea = new JTextArea();
		instructionsArea.setText("HƯỚNG DẪN ĐĂNG NHẬP:\n\n" + "1. Một cửa sổ trình duyệt Edge sẽ tự động mở\n\n"
				+ "2. Nhập thông tin đăng nhập của bạn:\n" + "   - Email hoặc username của " + platform + "\n"
				+ "   - Mật khẩu\n\n" + "3. Hoàn thành xác minh (nếu có):\n" + "   - 2FA (Two-Factor Authentication)\n"
				+ "   - CAPTCHA hoặc các bước xác minh khác\n\n" + "4. Chờ cho đến khi bạn thấy trang chính của "
				+ platform + "\n\n" + "5. Sau khi đăng nhập xong, quay lại và nhấp:\n"
				+ "   - NÚT XANH: Đã xác nhận (bắt đầu cào)\n" + "   - NÚT ĐỎ: Hủy bỏ (dừng quá trình)\n\n"
				+ "LƯU Ý: Đừng đóng cửa sổ hộp thoại này trước khi hoàn tất!\n"
				+ "Nếu trình duyệt không mở được, hãy check:\n" + "   - Edge browser có được cài không\n"
				+ "   - msedgedriver.exe có trong thư mục project\n" + "   - Kết nối Internet ổn định không");

		instructionsArea.setFont(UIUtils.FONT_NORMAL);
		instructionsArea.setEditable(false);
		instructionsArea.setLineWrap(true);
		instructionsArea.setWrapStyleWord(true);
		instructionsArea.setBackground(new Color(255, 250, 205));
		instructionsArea
				.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 180, 0), 2),
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JScrollPane scrollPane = new JScrollPane(instructionsArea);
		scrollPane.setBorder(null);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
		buttonPanel.setBackground(new Color(245, 245, 245));

		JButton confirmBtn = createCustomButton("Đã Xác Nhận - Bắt Đầu Cào", UIUtils.SUCCESS_COLOR);
		confirmBtn.setPreferredSize(new Dimension(250, 55));
		confirmBtn.addActionListener(e -> {
			confirmed = true;
			latch.countDown();
			dispose();
		});

		JButton cancelBtn = createCustomButton("Hủy Bỏ", UIUtils.ERROR_COLOR);
		cancelBtn.setPreferredSize(new Dimension(150, 55));
		cancelBtn.addActionListener(e -> {
			confirmed = false;
			latch.countDown();
			dispose();
		});

		buttonPanel.add(confirmBtn);
		buttonPanel.add(cancelBtn);

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(mainPanel);
	}

	private JButton createCustomButton(String text, Color bgColor) {
		JButton btn = new JButton(text);

		// FIX: Override Look & Feel
		btn.setUI(new BasicButtonUI());

		// FIX: Force color
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);

		// FIX: Make visible
		btn.setContentAreaFilled(true);
		btn.setOpaque(true);
		btn.setFocusPainted(false);
		btn.setBorderPainted(true);

		// FIX: Border color
		btn.setBorder(BorderFactory.createRaisedBevelBorder());

		// Font
		btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

		// Mouse hover effect
		btn.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e) {
				btn.setBackground(bgColor.brighter());
				btn.repaint();
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e) {
				btn.setBackground(bgColor);
				btn.repaint();
			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				btn.setBackground(bgColor.darker());
				btn.repaint();
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				btn.setBackground(bgColor.brighter());
				btn.repaint();
			}
		});

		return btn;
	}

	public boolean waitForConfirmation() {
		setVisible(true);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return confirmed;
	}
}