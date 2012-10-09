import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ActionListeners {
	
	ActionListener newHandler;
	ActionListener openHandler;
	ActionListener saveHandler;
	ActionListener saveAsHandler;
	ActionListener exportHandler;
	ActionListener connectHandler;
	ActionListener serverHandler;
	ActionListener colorHandler;
	ActionListener addLayerHandler;
	ActionListener brushHandler;
	ActionListener eraserHandler;
	ActionListener colorPickerHandler;
	ActionListener quitHandler;
	ActionListener advanceLayerHandler;
	ActionListener moveLayerBackHandler;
	ActionListener hideLayerHandler;
	ActionListener showLayerHandler;
	ActionListener layerVisibleHandler;
	ActionListener layerSelectHandler;
	DocumentListener changeWidthHandler;

	
	/*ActionListener newHandler;
	ActionListener openHandler;
	ActionListener saveHandler;
	ActionListener saveAsHandler;
	ActionListener exportHandler;
	ActionListener connectHandler;
	ActionListener serverHandler;
	ActionListener colorHandler;
	ActionListener addLayerHandler;
	ActionListener brushHandler;
	ActionListener eraserHandler;
	ActionListener colorPickerHandler;
	ActionListener quitHandler;
	ActionListener advanceLayerHandler;
	ActionListener moveLayerBackHandler;
	ActionListener hideLayerHandler;
	ActionListener showLayerHandler;
	ActionListener layerVisibleHandler;
	ActionListener layerSelectHandler;
	DocumentListener changeWidthHandler;

	ControlPanel controlPanel;
	Drawing drawing;
	ConsolePanel consolePanel;
	MainPanel.DrawingPanel drawingPanel;
	Menu menuBar;
	
	FileActions fileActions = new FileActions();
	DrawingActions drawingActions = new DrawingActions();

	final String DEFAULT_PORT = "21476";

	Boolean connectedToServer = false;
	
	public ActionListeners(ControlPanel cp, Drawing d, ConsolePanel con, MainPanel.DrawingPanel dp, Menu menuBar) {

		this.controlPanel = cp;
		this.drawing = d;
		this.consolePanel = con;
		this.drawingPanel = dp;

		newHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DrawingProgram.newWindow();
			}
		};
		exportHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileActions.export();
			}
		};
		saveHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileActions.save();
			}
		};
		saveAsHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileActions.saveAs();
			}
		};
		openHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DrawingProgram.open();
			}
		};
		connectHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				netActions.toggleConnect();
			}
		};
		serverHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				netActions.toggleServer();
			}
		};
		colorHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.changeColor(JColorChooser.showDialog(controlPanel, "New Color", drawing.currentColor));
			}
		};
		addLayerHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.addLayer();
			}
		};
		advanceLayerHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.moveLayerForward();
			}
		};
		moveLayerBackHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.moveLayerBack();
			}
		};
		showLayerHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.showCurrentLayer();
			}
		};
		hideLayerHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.hideCurrentLayer();
			}
		};
		layerVisibleHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layer currentLayer = drawing.layers[controlPanel.getCurrentLayer()];
				boolean checkBoxState = menuBar.layerVisible.isSelected();
				if (currentLayer.visible != checkBoxState) {
					currentLayer.setVisible(checkBoxState);
				}
				drawingPanel.repaint();
			}
		};
		layerSelectHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.reactToLayerSelection();
			}
		};
		brushHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.brushToggle();
			}
		};
		eraserHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.eraseToggle();
			}
		};
		colorPickerHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawingActions.colorPickerToggle();
			}
		};
		quitHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		changeWidthHandler = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {}
			public void removeUpdate(DocumentEvent e) {
				drawingPanel.setNewCursor();
			}
			public void insertUpdate(DocumentEvent e) {
				drawingPanel.setNewCursor();
			}
		};
	}

	private class DrawingActions {

		public void colorPickerToggle() {
			menuBar.brush.setSelected(false);
			menuBar.eraser.setSelected(false);
			if (!menuBar.colorPicker.isSelected()) {
				menuBar.colorPicker.setSelected(true);
			}
			drawingPanel.setNewCursor();
		}

		public void eraseToggle() {
			menuBar.brush.setSelected(false);
			menuBar.colorPicker.setSelected(false);
			if (!menuBar.eraser.isSelected()) {
				menuBar.eraser.setSelected(true);
			}
			drawingPanel.setNewCursor();
		}

		public void brushToggle() {
			menuBar.eraser.setSelected(false);
			menuBar.colorPicker.setSelected(false);
			if (!menuBar.brush.isSelected()) {
				menuBar.brush.setSelected(true);
			}
			drawingPanel.setNewCursor();
		}

		public void changeColor(Color c) {
			drawing.currentColor = c;
			//setBorder(BorderFactory.createLineBorder(c, 2));
			//consolePanel.setBorder(BorderFactory.createLineBorder(c, 2));
			drawingPanel.setNewCursor();
		}

		public void addLayer() {
			int numLayers = controlPanel.layerSelect.getItemCount();
			String defaultName = "Layer " + numLayers;
			String name = (String)JOptionPane.showInputDialog(
					SwingUtilities.getWindowAncestor(controlPanel),
					"Enter layer name: ",
					"New Layer",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					defaultName);
			if (name != null) {
				if (drawing.layerExists(name)) {
					JOptionPane.showMessageDialog(null, "Name already in use! Please choose another.", "Cannot create layer", JOptionPane.ERROR_MESSAGE);
					addLayer();
				} else {
					drawing.addLayer(drawing.width, drawing.height, name);
					netController.addLayer(-1, drawing.width, drawing.height, name);
					controlPanel.refigureLayers();
					controlPanel.layerSelect.setSelectedIndex(0);
				}
			}
		}

		public void hideCurrentLayer() {
			int currentLayer = controlPanel.getCurrentLayer();
			drawing.layers[currentLayer].visible = false;
			drawingPanel.repaint();
		}

		public void showCurrentLayer() {
			int currentLayer = controlPanel.getCurrentLayer();
			drawing.layers[currentLayer].visible = true;
			drawingPanel.repaint();
		}

		public void reactToLayerSelection() {
			Layer currentLayer = drawing.layers[controlPanel.getCurrentLayer()];
			if (menuBar.layerVisible.isSelected() != currentLayer.visible) {
				menuBar.layerVisible.setSelected(currentLayer.visible);
			}
			controlPanel.refigureLayerButtons(); 
		}

		public void moveLayerForward() {
			Object currentItem = controlPanel.layerSelect.getSelectedItem();
			int currentLayer = controlPanel.getCurrentLayer();
			int maxLayer = controlPanel.getMaxLayer();
			if (currentLayer < maxLayer) {
				drawing.swapLayers(currentLayer, currentLayer+1);
				controlPanel.refigureLayers();
				controlPanel.layerSelect.setSelectedItem(currentItem);
				drawingPanel.repaint();
				netController.swapLayers(-1, currentLayer, currentLayer+1);
			} else {
				consolePanel.tellUser("Layer is already at front!");
			}
		}

		public void moveLayerBack() {
			Object currentItem = controlPanel.layerSelect.getSelectedItem();
			int currentLayer = controlPanel.getCurrentLayer();
			if (currentLayer > 0) {
				drawing.swapLayers(currentLayer, currentLayer-1);
				controlPanel.refigureLayers();
				controlPanel.layerSelect.setSelectedItem(currentItem);
				drawingPanel.repaint();
				netController.swapLayers(-1, currentLayer, currentLayer-1);
			} else {
				consolePanel.tellUser("Layer is already at back!");
			}
		}
	}

	private class NetActions {
		public void showConnectDialog() {
			ConnectPanel connectPanel = new ConnectPanel();
			int result = JOptionPane.showConfirmDialog(null, connectPanel, "Enter connection details...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			if (result == JOptionPane.OK_OPTION) {
				String ip = connectPanel.getIP();
				int port = Integer.parseInt(connectPanel.getPort());
				beginConnection(ip, port);
			}
		}
		public void beginConnection(final String ip, final int port) {
			Thread connectionThread = new Thread() {
				public void run() {
					netController.connectToServer(ip, port);
				}
			};
			connectionThread.start();
			connectedToServer = true;
			menuBar.connect.setText("Disconnect...");
			menuBar.server.setEnabled(false);
			controlPanel.connectButton.setText("End Connection");
			controlPanel.serverButton.setEnabled(false);
		}

		public void toggleConnect() {
			if (!connectedToServer) {
				showConnectDialog();
			} else {
				netController.disconnectFromServer();
				connectedToServer = false;
				menuBar.connect.setText("Connect...");
				menuBar.server.setEnabled(true);
				controlPanel.connectButton.setText("Connect...");
				controlPanel.serverButton.setEnabled(true);
			}
		}

		public void toggleServer() {
			if (!serverRunning) {
				final String portInput = (String)JOptionPane.showInputDialog(
						SwingUtilities.getWindowAncestor(controlPanel),
						"Enter port:",
						"Start Server",
						JOptionPane.PLAIN_MESSAGE,
						null,
						null,
						DEFAULT_PORT); 
				if (portInput != null) {
					final int port = Integer.parseInt(portInput);
					menuBar.server.setText("Stop Server");
					menuBar.connect.setEnabled(false);
					setControlsToServerRunning();
					netController.startServer(port);
				}
			} else {
				netController.stopServer();
				menuBar.server.setText("Start Server...");
				menuBar.connect.setEnabled(true);
				controlPanel.serverButton.setText("Start Server...");
				controlPanel.connectButton.setEnabled(true);
				serverRunning = false;
			}
		}
	}

	private class FileActions {
		File currentFile = null;
		FileHandler fileHandler = new FileHandler();

		public void save() {
			if (currentFile != null) {
				tryToWriteDrawing(currentFile);
			} else {
				saveAs();
			}
		}

		public void tryToWriteDrawing(File file) {
			try {
				fileHandler.writeDrawingToFile(file, drawing);
				System.out.println("File saved");
				menuBar.save.setEnabled(false);
				currentFile = file;
				JFrame parentWindow = (JFrame)SwingUtilities.getWindowAncestor(controlPanel);
				parentWindow.setTitle(file.getName());
			} catch (IOException e) {
				System.out.println("IOException: " + e);
			}
		}

		public void saveAs() {
			JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						consolePanel.tellUser("Overwriting file...");
						tryToWriteDrawing(file);
					} else {
						consolePanel.tellUser("Cancelled...");
					}
				} else {
					tryToWriteDrawing(file);
				}
			}
		}

		public void export() {
			JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						consolePanel.tellUser("Overwriting file...");
						fileHandler.exportFile(file, drawing);
					} else {
						consolePanel.tellUser("Cancelled...");
					}
				} else {
					fileHandler.exportFile(file, drawing);
				}
			}
		}
	}*/
}

