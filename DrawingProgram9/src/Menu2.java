import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menu2 extends JMenuBar {

	JMenu fileMenu = new JMenu("File");
	JMenu drawingMenu = new JMenu("Drawing");
	JMenu networkMenu = new JMenu("Network");
	JMenuItem quit = new JMenuItem("Quit");
	JMenuItem newDoc = new JMenuItem("New...");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem saveAs = new JMenuItem("Save as...");
	JMenuItem open = new JMenuItem("Open...");
	JMenuItem export = new JMenuItem("Export as PNG...");
	JCheckBoxMenuItem brush = new JCheckBoxMenuItem("Brush");
	JCheckBoxMenuItem eraser = new JCheckBoxMenuItem("Eraser");
	JCheckBoxMenuItem colorPicker = new JCheckBoxMenuItem("Color picker");
	JMenuItem changeColor = new JMenuItem("Change color...");
	JMenuItem addLayer = new JMenuItem("Add layer...");
	JMenuItem moveLayerForward = new JMenuItem("Move layer forward");
	JMenuItem moveLayerBack = new JMenuItem("Move layer back");
	JMenuItem layerVisible = new JCheckBoxMenuItem("Layer visible");
	JMenuItem connect = new JMenuItem("Connect...");
	JMenuItem server = new JMenuItem("Start server...");
	public Menu2(ActionListeners actionListener) {
		newDoc.addActionListener(actionListener.newHandler);
		open.addActionListener(actionListener.openHandler);
		save.addActionListener(actionListener.saveHandler);
		saveAs.addActionListener(actionListener.saveAsHandler);
		export.addActionListener(actionListener.exportHandler);
		server.addActionListener(actionListener.serverHandler);
		connect.addActionListener(actionListener.connectHandler);
		quit.addActionListener(actionListener.quitHandler);
		changeColor.addActionListener(actionListener.colorHandler);
		brush.setSelected(true);
		brush.addActionListener(actionListener.brushHandler);
		eraser.addActionListener(actionListener.eraserHandler);
		colorPicker.addActionListener(actionListener.colorPickerHandler);
		addLayer.addActionListener(actionListener.addLayerHandler);
		moveLayerForward.addActionListener(actionListener.advanceLayerHandler);
		moveLayerBack.addActionListener(actionListener.moveLayerBackHandler);
		layerVisible.setSelected(true);
		layerVisible.addActionListener(actionListener.layerVisibleHandler);
		fileMenu.add(newDoc);
		fileMenu.add(open);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		fileMenu.add(export);
		fileMenu.add(quit);
		drawingMenu.add(brush);
		drawingMenu.add(eraser);
		drawingMenu.add(colorPicker);
		drawingMenu.addSeparator();
		drawingMenu.add(changeColor);
		drawingMenu.addSeparator();
		drawingMenu.add(addLayer);
		drawingMenu.add(moveLayerForward);
		drawingMenu.add(moveLayerBack);
		drawingMenu.add(layerVisible);
		networkMenu.add(connect);
		networkMenu.add(server);
		add(fileMenu);
		add(drawingMenu);
		add(networkMenu);
	}
}