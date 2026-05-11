package gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class UIHelper {

	public static JButton createButton(String text, Color bgColor) {
		JButton btn = new JButton(text);
		btn.setFont(UIUtils.FONT_NORMAL);
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setContentAreaFilled(true);
		btn.setOpaque(true);
		btn.setBorderPainted(true);
		btn.setPreferredSize(new Dimension(120, 35));

		applyButtonFix(btn, bgColor);
		return btn;
	}

	public static JButton createHeaderButton(String text, Color bgColor) {
		JButton btn = new JButton(text);
		btn.setFont(UIUtils.FONT_HEADER);
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setContentAreaFilled(true);
		btn.setOpaque(true);
		btn.setBorderPainted(true);
		btn.setPreferredSize(new Dimension(150, 45));

		applyButtonFix(btn, bgColor);
		return btn;
	}

	public static JButton createSmallButton(String text, Color bgColor) {
		JButton btn = new JButton(text);
		btn.setFont(UIUtils.FONT_SMALL);
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setContentAreaFilled(true);
		btn.setOpaque(true);
		btn.setBorderPainted(true);
		btn.setPreferredSize(new Dimension(100, 25));

		applyButtonFix(btn, bgColor);
		return btn;
	}

	private static void applyButtonFix(JButton btn, Color bgColor) {
		btn.setUI(new BasicButtonUI());
		btn.setBackground(bgColor);
		btn.setForeground(Color.WHITE);
		btn.setOpaque(true);
		btn.setContentAreaFilled(true);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
	}
}