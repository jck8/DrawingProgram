import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ControlPanel2 extends JPanel {
	/*Dependencies that should be worked on*/
	Menu2 menuBar;
	Drawing drawing;
	MainPanel.UserResponder userResponder;
	
	public JTextField lineWidthField;
	boolean serverRunning = false;
	boolean connectedToServer = false;
	JButton serverButton = new JButton("Start server");
	JButton connectButton = new JButton("Connect...");
	JButton colorButton = new JButton("Color...");
	JComboBox layerSelect = new JComboBox();
	JButton moveLayerBackButton = new JButton("Move layer back");
	JButton layerAddButton = new JButton("Add Layer");
	JButton moveLayerForwardButton = new JButton("Move layer forward");
	
	
	public int getCurrentLayer() {
		return (layerSelect.getItemCount()-1)-layerSelect.getSelectedIndex();
	}
	public int getMaxLayer() {
		return layerSelect.getItemCount()-1;
	}
	public Object getCurrentLayerName() {
		return layerSelect.getSelectedItem();
	}
	public void setCurrentLayer(Object layerName) {
		layerSelect.setSelectedItem(layerName);
	}
	public void setCurrentLayerSelectIndex(int i) {
		layerSelect.setSelectedIndex(i);
	}
	public int getCurrentLayerSelectIndex() {
		return layerSelect.getSelectedIndex();
	}
	public void refigureLayers() {
		Object oldItem = layerSelect.getSelectedItem();
		layerSelect.removeAllItems();
		for (Layer layer: drawing.layers) {
			layerSelect.insertItemAt(layer.name, 0);
		}
		if (oldItem == null) {
			layerSelect.setSelectedIndex(0);
		} else {
			layerSelect.setSelectedItem(oldItem);
		}
		refigureLayerButtons();
	}
	public void refigureLayerButtons() {
		if (getCurrentLayer() >= getMaxLayer()) {
			moveLayerForwardButton.setEnabled(false);
			menuBar.moveLayerForward.setEnabled(false);
		} else {
			moveLayerForwardButton.setEnabled(true);
			menuBar.moveLayerForward.setEnabled(true);
		}
		if (getCurrentLayer() <= 0) {
			moveLayerBackButton.setEnabled(false);
			menuBar.moveLayerBack.setEnabled(false);
		} else {
			moveLayerBackButton.setEnabled(true);
			menuBar.moveLayerBack.setEnabled(true);
		}
	}
	public int getLineWidth() {
		try { 
			return Integer.parseInt(lineWidthField.getText());
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public ControlPanel2(Menu2 m, Drawing d, ActionListeners u) {
		menuBar = m;
		drawing = d;
		userResponder = (MainPanel.UserResponder)u;
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setLayout(new FlowLayout());
		setBackground(Color.WHITE);

		lineWidthField = new JTextField("1", 3);
		JLabel lineWidthLabel = new JLabel("Width:");
		layerSelect.addItem("Layer 0");
		refigureLayers();


		layerAddButton.addActionListener(userResponder.addLayerHandler);
		moveLayerForwardButton.addActionListener(userResponder.advanceLayerHandler);
		moveLayerBackButton.addActionListener(userResponder.moveLayerBackHandler);
		serverButton.addActionListener(userResponder.serverHandler);
		connectButton.addActionListener(userResponder.connectHandler);
		colorButton.addActionListener(userResponder.colorHandler);
		layerSelect.addActionListener(userResponder.layerSelectHandler);
		lineWidthField.getDocument().addDocumentListener(userResponder.changeWidthHandler);
		add(moveLayerBackButton);
		add(moveLayerForwardButton);
		add(layerSelect);
		add(layerAddButton);
		add(lineWidthLabel);
		add(lineWidthField);
		add(colorButton);
		add(serverButton);
		add(connectButton);
		requestFocusInWindow();
	}

}