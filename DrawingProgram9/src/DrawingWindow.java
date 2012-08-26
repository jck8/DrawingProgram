import javax.swing.*;

import java.io.File;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.*;
import java.awt.*;
public class DrawingWindow extends JFrame {

	public final int defaultWindowWidth = 1200;
	public final int defaultWindowHeight = 800;
	public static WindowTracker wt = new WindowTracker();
	static JPanel keyPanel;
	MainPanel mainPanel = null;
	public static void main(String[] args) {
		//JFrame window = new DrawingWindow();
		wt.setInitFrame(displayInitialWindow());
		keyPanel = new JPanel();
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
						//new DrawingWindow();
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
								dw.mainPanel.userResponder.save();
							}
						}
						break;		
					}						
				}
			}
		});
		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher(new KeyDispatcher(keyPanel, kfm));
	}

	public static boolean open() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			new DrawingWindow(null, file);
			return true;
		} else {
			return false;
		}
	}

	public static JFrame displayInitialWindow() {
		JFrame initFrame = new JFrame();
		initFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		InitPanel initPanel = new InitPanel();
		initFrame.setContentPane(initPanel);
		initFrame.pack();
		initFrame.setLocationRelativeTo(null);
		System.out.println("Height: " + initPanel.getHeight() + ". Width: " + initPanel.getWidth());
		initFrame.setVisible(true);
		initFrame.addWindowListener(wt);
		return initFrame;
	}

	public static void newWindow() {
		NewPanel newPanel = new NewPanel();
		int result = JOptionPane.showConfirmDialog(null, newPanel, "New Drawing", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (result == JOptionPane.OK_OPTION) {
			Dimension drawingSize = new Dimension(newPanel.getWidthInput(), newPanel.getHeightInput());
			new DrawingWindow(drawingSize, null);
		}
	}

	public static class InitPanel extends JPanel {
		public InitPanel() {
			JLabel message = new JLabel("Select an option:");
			JButton newDrawingButton = new JButton("New Drawing...");
			JButton openButton = new JButton("Open file...");
			JButton connectButton = new JButton("Connect to server...");
			JButton quitButton = new JButton("Quit...");
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setAlignmentX(Component.CENTER_ALIGNMENT);
			addComponent(message);
			addComponent(newDrawingButton);
			addComponent(openButton);
			addComponent(connectButton);
			addComponent(quitButton);
			newDrawingButton.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) {
					newWindow();
				}
			});
			openButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					open();
				}
			});
			quitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			connectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
				}
			});
		}	
		public void addComponent(JComponent c) {
			c.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(c);
		}
	}

	public static class NewPanel extends JPanel {
		JTextField widthField = new JTextField("1000");
		JTextField heightField = new JTextField("1000");
		public NewPanel() {
			JLabel message = new JLabel("Enter width and height...");
			JLabel widthLabel = new JLabel("Width:");
			JLabel heightLabel = new JLabel("Height:");
			JPanel widthPanel = new JPanel();
			JPanel heightPanel = new JPanel();
			widthPanel.add(widthLabel);
			widthPanel.add(widthField);
			heightPanel.add(heightLabel);
			heightPanel.add(heightField);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(message);
			add(widthPanel);
			add(heightPanel);
		}
		public int getWidthInput() {
			return Integer.parseInt(widthField.getText());
		}
		public int getHeightInput() {
			return Integer.parseInt(heightField.getText());
		}
	}

	public DrawingWindow() {
		addWindowListener(wt);
		mainPanel = new MainPanel(this, null, null, null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(mainPanel);
		setSize(defaultWindowWidth, defaultWindowHeight);
		setLocation(100, 100);
		setJMenuBar(mainPanel.menuBar);
		setTitle("New drawing");
		setVisible(true);
		addWindowListener(wt);
	}

	public DrawingWindow(Dimension drawingSize, File file) {
		addWindowListener(wt);
		mainPanel = new MainPanel(this, file, drawingSize, null);
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

	static class KeyDispatcher implements KeyEventDispatcher {
		JPanel keyPanel;
		KeyboardFocusManager kfm;
		public KeyDispatcher(JPanel kp, KeyboardFocusManager k) {
			keyPanel = kp;
			kfm = k;
		}
		public boolean dispatchKeyEvent (KeyEvent e) {
			int mods = e.getModifiersEx();
			if ((mods & KeyEvent.META_DOWN_MASK) == KeyEvent.META_DOWN_MASK) {
				kfm.redispatchEvent(keyPanel, e);
				return true;
			}
			return false;
		}
	}

}