package gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Launcher {
	public static void main(String[] args) {
		// Set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start GUI
		SwingUtilities.invokeLater(() -> new MainWindow());
	}
}