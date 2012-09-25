import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class ConnectPanel extends JPanel {
	public static final String DEFAULT_PORT = "21476";

	JTextField ipField = new JTextField("127.0.0.1");
	JTextField portField = new JTextField(DEFAULT_PORT);
	
	public ConnectPanel() {
		JLabel message = new JLabel("Enter connection details...");
		JLabel ipLabel = new JLabel("IP:");
		JLabel portLabel = new JLabel("Port:");
		JPanel ipPanel = new JPanel();
		JPanel portPanel = new JPanel();
		ipPanel.add(ipLabel);
		ipPanel.add(ipField);
		portPanel.add(portLabel);
		portPanel.add(portField);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(message);
		add(ipPanel);
		add(portPanel);
	}
	public String getIP() {
		return ipField.getText();
	}
	public String getPort() {
		return portField.getText();
	}

}
