package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class LoginConfirmationDialog extends JDialog {
	private boolean confirmed = false;

	public LoginConfirmationDialog(JFrame parent, String platform) {
		super(parent, "Xac nhan dang nhap", true); // true = Modal (Chặn luồng chính)
		setLayout(new BorderLayout(15, 15));
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Bắt buộc dùng nút để đóng

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contentPanel.setBackground(Color.WHITE);

		JLabel titleLabel = new JLabel("Dang nhap " + platform);
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JTextArea infoText = new JTextArea("1. Trinh duyet Edge da duoc mo.\n" + "2. Vui long dang nhap vao " + platform
				+ ".\n" + "3. Sau khi hoan tat, nhan nut duoi day de tiep tuc.");
		infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		infoText.setEditable(false);
		infoText.setBackground(Color.WHITE);
		infoText.setMargin(new Insets(10, 10, 10, 10));

		contentPanel.add(titleLabel);
		contentPanel.add(Box.createVerticalStrut(15));
		contentPanel.add(infoText);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		btnPanel.setBackground(new Color(245, 245, 245));

		JButton confirmBtn = new JButton("DA DANG NHAP");
		confirmBtn.setBackground(new Color(46, 204, 113)); // Xanh lá
		confirmBtn.setForeground(Color.WHITE);
		confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		confirmBtn.setFocusPainted(false);
		confirmBtn.setContentAreaFilled(false);
		confirmBtn.setOpaque(true);
		confirmBtn.setPreferredSize(new Dimension(150, 40));
		confirmBtn.addActionListener(e -> {
			confirmed = true;
			dispose();
		});

		JButton cancelBtn = new JButton("HUY BO");
		cancelBtn.setBackground(new Color(231, 76, 60)); // Đỏ
		cancelBtn.setForeground(Color.WHITE);
		cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
		cancelBtn.setFocusPainted(false);
		cancelBtn.setContentAreaFilled(false);
		cancelBtn.setOpaque(true);
		cancelBtn.setPreferredSize(new Dimension(100, 40));
		cancelBtn.addActionListener(e -> {
			confirmed = false;
			dispose();
		});

		btnPanel.add(confirmBtn);
		btnPanel.add(cancelBtn);

		add(contentPanel, BorderLayout.CENTER);
		add(btnPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(parent);
	}

	public boolean showDialog() {
		setVisible(true);
		return confirmed; // Trả về true nếu ấn "ĐÃ ĐĂNG NHẬP", false nếu ấn "HỦY BỎ"
	}
}