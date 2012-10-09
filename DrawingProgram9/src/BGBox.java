import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BGBox extends JPanel {
	//A gray-colored background for when the drawing area is smaller than the window.
	public BGBox(MainPanel.DrawingPanel drawingPanel) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(JComponent.CENTER_ALIGNMENT);
		add(Box.createVerticalGlue());
		add(drawingPanel);
		add(Box.createVerticalGlue());
		setBackground(Color.lightGray);
	}
}
