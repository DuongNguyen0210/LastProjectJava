package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class UIUtils {

	public static final Color PRIMARY_COLOR = new Color(25, 118, 210);
	public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
	public static final Color WARNING_COLOR = new Color(255, 152, 0);
	public static final Color ERROR_COLOR = new Color(244, 67, 54);
	public static final Color LIGHT_GRAY = new Color(245, 245, 245);

	public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
	public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
	public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
	public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

	public static JButton createButton(String text, Color bgColor) {
		JButton btn = new JButton(text);
		btn.setFont(FONT_NORMAL);
		btn.setPreferredSize(new Dimension(120, 35));
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);
		btn.setContentAreaFilled(false);
		btn.setOpaque(true);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
		btn.setFocusPainted(false);
		return btn;
	}

	public static JPanel createPanel(Color bgColor, int hgap, int vgap) {
		JPanel panel = new JPanel();
		panel.setBackground(bgColor);
		if (hgap > 0 || vgap > 0) {
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		}
		return panel;
	}

	public static JSeparator createSeparator() {
		return new JSeparator(SwingConstants.HORIZONTAL);
	}
}