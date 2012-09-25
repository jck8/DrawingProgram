import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ConsolePanel extends JPanel {   
	JTextArea console;
	synchronized public void tellUser(String s) {
		console.append(s+"\n\n");
		console.setCaretPosition(console.getText().length());
	}
	public ConsolePanel() {
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setLayout(new BorderLayout());
		console = new JTextArea(5, 15);
		JScrollPane consoleScroller = new JScrollPane(console);
		console.setEditable(false);
		console.setLineWrap(true);
		add(consoleScroller, BorderLayout.CENTER);
		tellUser("*************");
		tellUser("  Welcome!  ");
		tellUser("*************");
	}
}
