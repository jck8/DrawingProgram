import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
class WindowTracker extends WindowAdapter {
	static int numWindows = 0;
	JFrame initFrame;
	
	public void setInitFrame(JFrame initFrame) {
		this.initFrame = initFrame;
	}
	public void windowOpened(WindowEvent e) {
		if (initFrame.isVisible() && (e.getSource() instanceof DrawingWindow)) {
			initFrame.setVisible(false);
		}
		numWindows++;
	}
	public void windowClosed(WindowEvent e) {
		numWindows--;
		if (numWindows == 1) {
			initFrame.setVisible(true);
		}
		if (numWindows <= 0) {
			System.exit(0);
		}
	}
}