import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class NewPanel extends JPanel {
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


