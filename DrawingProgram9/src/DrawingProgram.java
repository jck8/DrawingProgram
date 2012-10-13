import javax.swing.*;
import java.io.File;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.*;

public class DrawingProgram {
	public static final String DEFAULT_PORT = "21476";
	public final int defaultWindowWidth = 1200;
	public final int defaultWindowHeight = 800;

	public static WindowTracker wt = new WindowTracker();

	public static void main(String[] args) {
		wt.setInitFrame(displayInitialWindow());
		JPanel keyPanel = new JPanel();
		keyPanel.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Window wind;
				DrawingWindow dw;
				int key = e.getKeyCode();
				int mods = e.getModifiersEx();
				if ((mods & KeyEvent.META_DOWN_MASK) == KeyEvent.META_DOWN_MASK && e.getID() == KeyEvent.KEY_PRESSED) { //If the command key is being held down
					switch (key) {
					case KeyEvent.VK_N: 
						newWindow();
						break;
					case KeyEvent.VK_O: 
						open();
						break;
					case KeyEvent.VK_W:
						wind = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
						wind.setVisible(false);
						wind.dispose();
						break;
					case KeyEvent.VK_S:
						wind = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
						if (wind instanceof DrawingWindow) {
							dw = (DrawingWindow)wind;
							if (dw.mainPanel != null) {
								dw.mainPanel.controls.fileActions.save();
							}
						}
						break;		
					}						
				}
			}
		});
		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher(new MainKeyDispatcher(keyPanel, kfm));
	}
	
	public static JFrame displayInitialWindow() {
		JFrame initFrame = new JFrame();
		initFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		InitPanel initPanel = new InitPanel();
		initFrame.setContentPane(initPanel);
		initFrame.pack();
		initFrame.setLocationRelativeTo(null);
		initFrame.setVisible(true);
		initFrame.addWindowListener(wt);
		return initFrame;
	}
	
	public static void newWindow() {
		NewPanel newPanel = new NewPanel();
		int result = JOptionPane.showConfirmDialog(null, newPanel, "New Drawing", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (result == JOptionPane.OK_OPTION) {
			Dimension drawingSize = new Dimension(newPanel.getWidthInput(), newPanel.getHeightInput());
			DrawingWindow newWindow = new DrawingWindow(drawingSize, null);
			newWindow.addWindowListener(wt);
		}
	}
	
	public static boolean open() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			DrawingWindow newWindow = new DrawingWindow(null, file);
			newWindow.addWindowListener(wt);
			return true;
		} else {
			return false;
		}
	}
	
	public static void showConnectDialog() {
		ConnectPanel connectPanel = new ConnectPanel();
		int result = JOptionPane.showConfirmDialog(null, connectPanel, "Enter connection details...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (result == JOptionPane.OK_OPTION) {
			final String ip = connectPanel.getIP();
			final int port = Integer.parseInt(connectPanel.getPort());
			DrawingWindow newWindow = new DrawingWindow(ip, port);
			newWindow.addWindowListener(wt);
		}
	}
}
