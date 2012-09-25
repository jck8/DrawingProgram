import javax.swing.*;
import java.io.File;
import java.awt.*;

public class DrawingWindow extends JFrame {
	public final int defaultWindowWidth = 1200;
	public final int defaultWindowHeight = 800;
	MainPanel mainPanel = null;

	public DrawingWindow(Dimension drawingSize, File file) {
		mainPanel = new MainPanel(file, drawingSize, null, -1);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(mainPanel);
		setSize(defaultWindowWidth, defaultWindowHeight);
		setLocation(100, 100);
		setJMenuBar(mainPanel.menuBar);
		if (file != null) {
			setTitle(file.getName());
		}
		setVisible(true);
	}
	
	public DrawingWindow(String ip, int port) {
		mainPanel = new MainPanel(null, null, ip, port);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(mainPanel);
		setSize(defaultWindowWidth, defaultWindowHeight);
		setLocation(100, 100);
		setJMenuBar(mainPanel.menuBar);
		setVisible(true);
	}
}