import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class InitPanel extends JPanel {
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
				DrawingProgram.newWindow();
			}
		});
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DrawingProgram.open();
			}
		});
		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DrawingProgram.showConnectDialog();
			}
		});
	}	
	public void addComponent(JComponent c) {
		c.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(c);
	}
}
