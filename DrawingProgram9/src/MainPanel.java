import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.LinkedList;
import java.net.*;
import java.io.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.File;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*; 

public class MainPanel extends JPanel {

	public final String DEFAULT_PORT = "21476";

	JFrame parentWindow;
	Dimension drawingSize;
	File currentFile = null;
	DrawingData drawingData;
	DrawingPanel drawingPanel;
	ControlPanel controlPanel;
	ConsolePanel consolePanel;
	JScrollPane scroller;
	UserResponder userResponder = new UserResponder();
	NetController netController = new NetController();
	Thread connectionThread = null;
	Thread serverThread = null;
	Menu menuBar = new Menu();
	
	public MainPanel(JFrame pw, File file, Dimension d) {
		parentWindow = pw;
		setLayout(new BorderLayout());
		drawingData = new DrawingData();
		if (d != null) {
			drawingSize = d;
			drawingPanel = new DrawingPanel();
			drawingData.container = this;
			drawingPanel.setPreferredSize(drawingSize);
		} else {
			drawingSize = new Dimension(1000, 1000);
			drawingPanel = new DrawingPanel();
			drawingPanel.setPreferredSize(drawingSize);
		}
		scroller = new JScrollPane(drawingPanel);
		controlPanel = new ControlPanel();
		consolePanel = new ConsolePanel();
		add(scroller, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
		add(consolePanel, BorderLayout.EAST);
		if (file != null) {
			userResponder.openFile(file);
			currentFile = file;
		}
		drawingData.setNewCursor();
		menuBar.save.setEnabled(false);
		validate();
	}
	
	public void resizeDrawingPanel(Dimension d) {
		scroller.remove(drawingPanel);
		remove(scroller);
		drawingPanel = new DrawingPanel();
		drawingPanel.setPreferredSize(d);
		scroller = new JScrollPane(drawingPanel);
		add(scroller);
		validate();
	}

	public class ConnectPanel extends JPanel {
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

	public class UserResponder {
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
		boolean connectedToServer = false;
		boolean serverRunning = false;
		
		public UserResponder () {
			newHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DrawingWindow.newWindow();
				}
			};
			exportHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					export();
				}
			};
			saveHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					save();
				}
			};
			saveAsHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAs();
				}
			};
			openHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DrawingWindow.open();
				}
			};
			connectHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					toggleConnect();
				}
			};
			serverHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					toggleServer();
				}
			};
			colorHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					changeColor(JColorChooser.showDialog(controlPanel, "New Color", drawingData.currentColor));
				}
			};
			addLayerHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addLayer();
				}
			};
			advanceLayerHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					moveLayerForward();
				}
			};
			moveLayerBackHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					moveLayerBack();
				}
			};
			showLayerHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showCurrentLayer();
				}
			};
			hideLayerHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					hideCurrentLayer();
				}
			};
			layerVisibleHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Layer currentLayer = drawingData.layers[controlPanel.getCurrentLayer()];
					boolean checkBoxState = menuBar.layerVisible.isSelected();
					if (currentLayer.visible != checkBoxState) {
						currentLayer.setVisible(checkBoxState);
					}
				}
			};
			layerSelectHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					reactToLayerSelection();
				}
			};
			brushHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					brushToggle();
				}
			};
			eraserHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					eraseToggle();
				}
			};
			colorPickerHandler = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					colorPickerToggle();
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
					drawingData.setNewCursor();
				}
				public void insertUpdate(DocumentEvent e) {
					drawingData.setNewCursor();
				}
			};
		}

		public String generateLayerName(String init) {
			int num = 0;
			String name = init;
			while (drawingData.layerExists(name)) {
				num = num + 1;
				name = init + "_" + num;
			}
			return name;
		}
		
		public void insertImage(BufferedImage img, String name) {
			int rgb;
			int w;
			int h;
			Layer newLayer;
			name = generateLayerName(name);
			w = img.getWidth();
			h = img.getHeight();
			newLayer = drawingData.addLayer(w, h, name);
			System.out.println("w="+w);
			System.out.println("h="+h);
			newLayer.i = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					rgb = img.getRGB(x, y);
					newLayer.i.setRGB(x, y, rgb);
				}
			}
			controlPanel.refigureLayers();
			drawingPanel.repaint();
		}
		
		public void openFile (File file) {
			String currentLayerName = "";
			String chunk;
			FileReader fr;
			String[] chunkSplit;
			BufferedReader br;
			BufferedImage img;
			String imageText = "";
			drawingData.layers = new Layer[0];
			drawingData.firstLayerAdded = true; //Necessary so we don't overwrite our new layer when we repaint drawingPanel 
			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				while ((chunk = getChunk(br)) != null) {
					if (chunk.startsWith("NAMEIS")) {
						if (!imageText.equals("!")) {
							InputStream is = new ByteArrayInputStream(imageText.getBytes());
							img = ImageIO.read(is);
							insertImage(img, currentLayerName);
						} 
						imageText = "";
						chunkSplit = chunk.split(":");
						int len = chunkSplit[1].length();
						currentLayerName = chunkSplit[1].substring(0, len-1);

					} else {
						imageText = imageText + chunk;
					}
				}
				System.out.println(currentLayerName);
				if (currentLayerName.equals("")) {
					currentLayerName = "Imported Image";
				}
				System.out.println(currentLayerName);
				InputStream is = new ByteArrayInputStream(imageText.getBytes());
				img = ImageIO.read(is);
				System.out.println("Width:" + img.getWidth() + ". Height: " + img.getHeight());
				insertImage(img, currentLayerName);
				System.out.println("Inserted image");
				drawingPanel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
				controlPanel.setCurrentLayer(currentLayerName);
			} catch (Exception e) {
				consolePanel.tellUser("Error reading file: " + e);
			}
		}

		public String getChunk(BufferedReader br) {
			int i = -1;
			try {
				i = br.read();
				if (i == -1) {
					System.out.println("getChunk returning null");
					return null;
				}
			} catch (Exception e) {
				System.out.println("Error reading file 1: " + e);
			}
			char c = (char)i;
			String s = c + "";
			while (i != -1 && c != '!') {
				try {
					br.mark(1);
					i = br.read();
				} catch (Exception e) {
					System.out.println("Error reading file 2: " + e);
				}
				c = (char)i;
				s = s + c;
			}
			if (i == -1) {
				try {
					br.reset();
				} catch (Exception e) {
					System.out.println("Error reading file 3: " + e);
				}
				return s;
			} else return (s);
		}
		
		public void save() {
			if (currentFile != null) {
				writeDrawingToFile(currentFile);
				consolePanel.tellUser("File saved");
			} else {
				saveAs();
			}
		}
		
		public void saveAs() {
			JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(controlPanel)==JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						consolePanel.tellUser("Overwriting file...");
						writeDrawingToFile(file);
					} else {
						consolePanel.tellUser("Cancelled...");
					}
				} else {
					writeDrawingToFile(file);
					currentFile = file;
					parentWindow.setTitle(file.getName());
				}
			}
		}

		
		public void writeDrawingToFile(File file) {
			FileWriter fw;
			PrintWriter pw;
			Iterator<ImageWriter> iter;
			ImageWriter writer;
			ImageWriteParam iwp;
			FileImageOutputStream output;
			IIOImage img;
			try {
				iter = ImageIO.getImageWritersByFormatName("png");
				writer = (ImageWriter)iter.next();
				iwp = writer.getDefaultWriteParam();
				output = new FileImageOutputStream(file);
				writer.setOutput(output);
				fw = new FileWriter(file);
				pw = new PrintWriter(fw);
				synchronized(file) {
					for (Layer layer: drawingData.layers) {
						pw.print("!NAMEIS:" + layer.name + "!");
						pw.close();
						output.seek(file.length());
						writer.setOutput(output);
						img = new IIOImage(layer.i, null, null);
						writer.write(null, img, iwp);
						fw = new FileWriter(file, true);
						pw = new PrintWriter(fw);
					}
				}
				menuBar.save.setEnabled(false);
				writer.dispose();
			} catch (Exception e){
				consolePanel.tellUser("Error writing file: " + e);
			}
		}
		
		public void export() {
			JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(controlPanel)==JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						consolePanel.tellUser("Overwriting file...");
						writeDrawingToFile(file);
					} else {
						consolePanel.tellUser("Cancelled...");
					}
				} else {
					BufferedImage toSave = new BufferedImage(drawingData.panelWidth, drawingData.panelHeight, BufferedImage.TYPE_INT_RGB);
					for (int x = 0; x < drawingData.panelWidth; x++) {
						for (int y = 0; y < drawingData.panelHeight; y++) {
							toSave.setRGB(x, y, drawingData.getColorAt(x, y).getRGB());
						}
					}
					try {
						ImageIO.write(toSave, "png", file);
						consolePanel.tellUser("File " + file.getName() + " has been successfully written.");
					} catch (Exception e) {
						consolePanel.tellUser("Error writing file: " + e);
					}
				}
			}
		}
		
		public void colorPickerToggle() {
			menuBar.brush.setSelected(false);
			menuBar.eraser.setSelected(false);
			if (!menuBar.colorPicker.isSelected()) {
				menuBar.colorPicker.setSelected(true);
			}
			drawingData.setNewCursor();
		}

		public void eraseToggle() {
			menuBar.brush.setSelected(false);
			menuBar.colorPicker.setSelected(false);
			if (!menuBar.eraser.isSelected()) {
				menuBar.eraser.setSelected(true);
			}
			drawingData.setNewCursor();
		}

		public void brushToggle() {
			menuBar.eraser.setSelected(false);
			menuBar.colorPicker.setSelected(false);
			if (!menuBar.brush.isSelected()) {
				menuBar.brush.setSelected(true);
			}
			drawingData.setNewCursor();
		}

		public void changeColor(Color c) {
			drawingData.currentColor = c;
			setBorder(BorderFactory.createLineBorder(c, 2));
			consolePanel.setBorder(BorderFactory.createLineBorder(c, 2));
			drawingData.setNewCursor();
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
				if (drawingData.layerExists(name)) {
					JOptionPane.showMessageDialog(null, "Name already in use! Please choose another.", "Cannot create layer", JOptionPane.ERROR_MESSAGE);
					addLayer();
				} else {
					drawingData.addLayer(drawingData.panelWidth, drawingData.panelHeight, name);
					netController.addLayer(-1, drawingData.panelWidth, drawingData.panelHeight, name);
					controlPanel.refigureLayers();
					controlPanel.layerSelect.setSelectedIndex(0);
				}
			}
		}

		public void hideCurrentLayer() {
			int currentLayer = controlPanel.getCurrentLayer();
			drawingData.layers[currentLayer].visible = false;
			drawingPanel.repaint();
		}

		public void showCurrentLayer() {
			int currentLayer = controlPanel.getCurrentLayer();
			drawingData.layers[currentLayer].visible = true;
			drawingPanel.repaint();
		}

		public void reactToLayerSelection() {
			Layer currentLayer = drawingData.layers[controlPanel.getCurrentLayer()];
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
				drawingData.swapLayers(currentLayer, currentLayer+1);
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
			int maxLayer = controlPanel.getMaxLayer();
			if (currentLayer > 0) {
				drawingData.swapLayers(currentLayer, currentLayer-1);
				controlPanel.refigureLayers();
				controlPanel.layerSelect.setSelectedItem(currentItem);
				drawingPanel.repaint();
				netController.swapLayers(-1, currentLayer, currentLayer-1);
			} else {
				consolePanel.tellUser("Layer is already at back!");
			}
		}

		public void showConnectDialog() {
			ConnectPanel connectPanel = new ConnectPanel();
			int result = JOptionPane.showConfirmDialog(null, connectPanel, "Enter connection details...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			if (result == JOptionPane.OK_OPTION) {
				final String ip = connectPanel.getIP();
				final int port = Integer.parseInt(connectPanel.getPort());
				connectionThread = new Thread() {
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
					serverThread = new Thread() {
						public void run() {
							netController.startServer(port);
						}
					};
					serverThread.start();
					menuBar.server.setText("Stop Server");
					menuBar.connect.setEnabled(false);
					controlPanel.serverButton.setText("Stop Server");
					controlPanel.connectButton.setEnabled(false);
					serverRunning = true;
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


	public class Menu extends JMenuBar {

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
		public Menu() {
			newDoc.addActionListener(userResponder.newHandler);
			open.addActionListener(userResponder.openHandler);
			save.addActionListener(userResponder.saveHandler);
			saveAs.addActionListener(userResponder.saveAsHandler);
			export.addActionListener(userResponder.exportHandler);
			server.addActionListener(userResponder.serverHandler);
			connect.addActionListener(userResponder.connectHandler);
			quit.addActionListener(userResponder.quitHandler);
			changeColor.addActionListener(userResponder.colorHandler);
			brush.setSelected(true);
			brush.addActionListener(userResponder.brushHandler);
			eraser.addActionListener(userResponder.eraserHandler);
			colorPicker.addActionListener(userResponder.colorPickerHandler);
			addLayer.addActionListener(userResponder.addLayerHandler);
			moveLayerForward.addActionListener(userResponder.advanceLayerHandler);
			moveLayerBack.addActionListener(userResponder.moveLayerBackHandler);
			layerVisible.setSelected(true);
			layerVisible.addActionListener(userResponder.layerVisibleHandler);
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

	public class ControlPanel extends JPanel {
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
			for (Layer layer: drawingData.layers) {
				layerSelect.insertItemAt(layer.name, 0);
			}
			layerSelect.setSelectedItem(oldItem);
			refigureLayerButtons();
		}
		public void refigureLayerButtons() {
			/*if (drawingPanel.layers[getCurrentLayer()].visible) {
        menuBar.layerVisible.setSelected(true);
      } else {
        menuBar.layerVisible.setSelected(false);
      }*/
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
		/*public void moveLayerForward() {
      int currentIndex = layerSelect.getSelectedIndex();
      Object currentItem = layerSelect.getSelectedItem();
      layerSelect.removeItem(currentItem);
      layerSelect.insertItemAt(currentItem, currentIndex-1);
      layerSelect.setSelectedIndex(currentIndex-1);
    }
    public void moveLayerBack() {
      int currentIndex = layerSelect.getSelectedIndex();
      Object currentItem = layerSelect.getSelectedItem();
      layerSelect.removeItem(currentItem);
      layerSelect.insertItemAt(currentItem, currentIndex+1);
      layerSelect.setSelectedIndex(currentIndex+1);
    }*/
		public ControlPanel() {

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

		public int getLineWidth() {
			try { 
				return Integer.parseInt(lineWidthField.getText());
			} catch (NumberFormatException e) {
				return 1;
			}
		}
	}

	public class NetController {
		LinkedList<Connection> connections = new LinkedList<Connection>();
		boolean serverRunning = false;
		boolean connected = false;
		int numConnections = 0;
		int myID = 0;
		ConnectionWaiter connectionWaiter;

		public void addLayer(int ident, int width, int height, String name) {
			for (Connection connection: connections) {
				if (ident != connection.otherID) {
					connection.addLayer(ident, width, height, name);
				}
			}
		}

		public void addToCurve(int ident, int x, int y) {
			for (Connection connection: connections) {
				if (ident != connection.otherID) {
					connection.addToCurve(ident, x, y);
				}
			}
		}

		public void sendNewCurve(int ident, int startX, int startY, int lineWidth, Color color, boolean erase, int layer) {
			for (Connection connection: connections) {
				if (ident != connection.otherID) {
					connection.sendNewCurve(ident, startX, startY, lineWidth, color, erase, layer);
				}
			}
		}

		public void swapLayers(int ident, int l1, int l2) {
			for (Connection connection: connections) {
				if (ident != connection.otherID) {
					connection.swapLayers(ident, l1, l2);
				}
			}
		}
		
		public void handleRemoteDisconnect(Connection c) {
			try {
				c.connectionSocket.close();
			} catch (Exception e) {
				consolePanel.tellUser("Error in handling remote disconnection: " + e);
			}
			c.active = false;
			if (serverRunning) {
				consolePanel.tellUser("User number " + c.otherID + " disconnected.");
				connections.remove(c);
			} else {
				consolePanel.tellUser("Server ended connection.");
				controlPanel.connectButton.setText("Connect to server...");
				controlPanel.serverButton.setEnabled(true);
			}
		}

		public void disconnectFromServer() {
			connected = false;
			connections.peek().end();
			numConnections = 0;
			connections = new LinkedList<Connection>();
		}

		public void stopServer() {
			serverRunning = false;
			connectionWaiter.stopWaiting();
			for (Connection connection: connections) {
				connection.end();
			}
			numConnections = 0;
			connected = false;
			connections = new LinkedList<Connection>();
		}

		public void startServer(int port) {
			try {
				connectionWaiter = new ConnectionWaiter(port);
				serverRunning = true;
				connected = true;
				connectionWaiter.start();
			} catch (Exception e) {
				consolePanel.tellUser("A connection error occurred: " + e);
			}
		}

		public BufferedImage string2Image(String s) {
			InputStream is = new ByteArrayInputStream(s.getBytes());
			BufferedImage img = null;
			try {
				img = ImageIO.read(is);
			} catch (Exception e) {
				System.out.println("Error reading image");
			}
			return img;
		}
		
		public String getImageStringFromReader(BufferedReader br) {
			StringBuffer imageString = new StringBuffer("");
			String in;
			System.out.println("Trying to return image from a reader");
			while (!(in = getChunk(br, '\n')).equals("ENDIMAGE\n")) {
				imageString.append(in);
			}
			System.out.println("Returning an image from a reader");
			return imageString.toString();
		}
		
		public BufferedImage getImageFromReader(BufferedReader br) {
			return string2Image(getImageStringFromReader(br));
		}
		
		public String getChunk(BufferedReader br, char delim) {
			int i = -1;
			try {
				i = br.read();
				if (i == -1) {
					System.out.println("getChunk returning null");
					return null;
				}
			} catch (Exception e) {
				System.out.println("Error reading file 1: " + e);
			}
			char c = (char)i;
			String s = c + "";
			while (i != -1 && c != delim) {
				try {
					br.mark(1);
					i = br.read();
				} catch (Exception e) {
					System.out.println("Error reading file 2: " + e);
				}
				c = (char)i;
				s = s + c;
			}
			if (i == -1) {
				try {
					br.reset();
				} catch (Exception e) {
					System.out.println("Error reading file 3: " + e);
				}
				return s;
			} else return (s);
		}
		
		public void correctDimensions(Connection connection) {
			int myWidth = drawingData.panelWidth;
			int myHeight = drawingData.panelHeight;
			connection.outStream.println(myWidth);
			connection.outStream.println(myHeight);
			connection.outStream.flush();
			int otherWidth = 0;
			int otherHeight = 0;
			try {
				otherWidth = Integer.parseInt(connection.inStream.readLine());
				otherHeight = Integer.parseInt(connection.inStream.readLine());
			} catch(Exception e) {
				consolePanel.tellUser("correctDimensions");
				consolePanel.tellUser("A connection error occurred.");
				consolePanel.tellUser("Error: " + e);
			}
 			int newWidth = myWidth;
			int newHeight = myHeight;
			if (otherWidth > myWidth) {
				newWidth = otherWidth;
			}
			if (otherHeight > myHeight) {
				newHeight = otherHeight;
			}
			if (newHeight != myHeight || newWidth != myWidth) {
				Dimension newDim = new Dimension(newWidth, newHeight);
				drawingData.resizeAllLayers(newDim);
				drawingData.panelWidth = newDim.width;
				drawingData.panelHeight = newDim.height;
				drawingData.container.resizeDrawingPanel(newDim);
			}
		}
		
		public void connectToServer(String ip, int port) {
			try {
				String in; 
				Socket socket = new Socket(ip, port);
				connected = true;
				Connection connection = new Connection(socket);
				consolePanel.tellUser("Connected to server at " + connection.remoteAddress);
				connections.add(connection);
				myID = Integer.parseInt(connection.inStream.readLine());
				connection.myID = myID;
				connection.otherID = myID;
				consolePanel.tellUser("We are connection number: " + myID);
				correctDimensions(connection);
				for (Layer l: drawingData.layers) {
					consolePanel.tellUser("Sending layer to server: " + l.name);
					connection.addLayerWithImage(myID, l.name, l.i);
				}
				connection.outStream.println("DONE");
				connection.outStream.flush();
				while ((in = connection.inStream.readLine()).startsWith("RL")) {
					Object currentlySelectedLayer = controlPanel.getCurrentLayer();
					String[] processedIn = in.split(";");
					int receivedID = Integer.parseInt(processedIn[1]);
					String oldName = processedIn[2];
					String newName = processedIn[3];
					consolePanel.tellUser("Renaming " + oldName + " to " + newName + ".");
					int oldLayerIndex = controlPanel.getCurrentLayerSelectIndex();
					for (Layer l: drawingData.layers) {
						if (l.name.equals(oldName)) {
							l.name = newName;
							controlPanel.refigureLayers();
							controlPanel.setCurrentLayerSelectIndex(oldLayerIndex);
						}
					}
				}
				connection.start();
			} catch (Exception e) {
				consolePanel.tellUser("A connection error occurred.");
				consolePanel.tellUser("Error: " + e);
			}
		}

		public class ConnectionWaiter extends Thread {
			ServerSocket listener;
			Connection newConnection;
			int port;
			public ConnectionWaiter(int p) {
				port = p;
			}
			public void stopWaiting() {
				try {
					listener.close();
				} catch (Exception e) {
					consolePanel.tellUser("Server ending error? " + e);
				}
			}
			public void run() {
				try {
					listener = new ServerSocket(port);
				} catch (Exception e) {
					consolePanel.tellUser("Server problem 1 in method run: " + e);
				}
				while(serverRunning) {
					try {
						consolePanel.tellUser("Awaiting connections on port " + port + "...");
						newConnection = new Connection(listener.accept());
						consolePanel.tellUser("Connection established with " + newConnection.remoteAddress);
						connections.add(newConnection);
						numConnections += 1;
						newConnection.outStream.println(numConnections);
						newConnection.outStream.flush();
						newConnection.otherID = numConnections;
						LinkedList<Layer> newLayers = new LinkedList<Layer>();
						String in;
						String[] processedIn;
						String newLayerName;
						String oldLayerName;
						Layer newLayer;
						int insertionPoint = 0;
						correctDimensions(newConnection);
						while ((in = newConnection.inStream.readLine()).startsWith("LI;")) {
							System.out.println("Receiving layer from connector");
							processedIn = in.split(";");
							oldLayerName = processedIn[4];
							newLayerName = oldLayerName + "(" + processedIn[1] + ")";
							int width = Integer.parseInt(processedIn[2]);
							int height = Integer.parseInt(processedIn[3]);
							newLayer = drawingData.insertLayer(width, height, newLayerName, insertionPoint);
							try {
								newLayer.i = getImageFromReader(newConnection.inStream);
							} catch (Exception e) {
								consolePanel.tellUser("Error receiving " + oldLayerName + " from connector");
							}
							newLayers.add(newLayer);
							insertionPoint++;
							controlPanel.refigureLayers();
							drawingPanel.repaint();
							newConnection.renameLayer(0, oldLayerName, newLayerName);
						}
						newConnection.outStream.println("DONE");
						newConnection.outStream.flush();
						System.out.println("Just sent DONE from server");
						for (Layer l: drawingData.layers) {
							if (!newLayers.contains(l)) {
								newConnection.addLayerWithImage(0, l.name, l.i);
								System.out.println("Sending layer " + l.name + "from server.");
							} 
						}
						/*for (Layer l: drawingPanel.layers) {
							consolePanel.tellUser("Sending layer and image for " + l.name);
							newConnection.addLayerWithImage(0, l.name, l.i);
						}*/
						/*for (Curve c: drawingPanel.curves) {
							consolePanel.tellUser("Sending a curve from server...");
							newConnection.sendCurve(c);
						}*/
						newConnection.start();
					} catch (Exception e) {
						consolePanel.tellUser("Server problem 2 in method run: " + e);
					}
				}
			}
		}

		public class Connection extends Thread {
			PrintWriter outStream;
			OutputStream socketOutStream;
			InputStream socketInStream;
			BufferedReader inStream;
			Curve currentRemoteCurve;
			Curve[] activeRemoteCurves = new Curve[1];
			Socket connectionSocket;
			int remoteLineWidth;
			int otherID = 0;
			int myID = 0;
			String remoteAddress;
			Color remoteColor;
			volatile boolean active;

			public void end() {
				active = false;
				outStream.println("END");
				outStream.flush();
				try {
					connectionSocket.close();
				} catch (Exception e) {
					consolePanel.tellUser("Error: " + e);
				}
			}

			public void run() {
				while(active) {
					handleRemoteAction();
				}
			}
			
			public void swapLayers(int ident, int l1, int l2) {
				if (ident == -1) {
					ident = myID;
				}
				outStream.println("SL"+";"+ident+";"+l1+";"+l2);
				outStream.flush();
			}
			
			public void sendNewCurve(int ident, int startX, int startY, int lineWidth, Color color, boolean erase, int layer) {
				if (ident == -1) {
					ident = myID;
				}
				outStream.println("NEW"+";"+ident+";"+lineWidth+";"+String.valueOf(color.getRGB())+";"+startX+";"+startY+";"+erase+";"+layer);
				outStream.flush();
			}

			public void addToCurve(int ident, int x, int y) {
				if (ident == -1) {
					ident = myID;
				}
				outStream.println("PT"+";"+ident+";"+x+";"+y);
				outStream.flush();
			}

			public void sendCurve(Curve c) {
				outStream.println("NEW"+";"+otherID+";"+c.lineWidth+";"+String.valueOf(c.color.getRGB())+";"+c.coords[0].x+";"+c.coords[0].y+";"+c.erase+";"+c.layer);
				for (Coord coord: c.coords) {
					outStream.println("PT"+";"+otherID+";"+coord.x+";"+coord.y);
				}
				outStream.flush();
			}

			public void renameLayer(int ident, String oldName, String newName) {
				if (ident == -1) {
					ident = myID;
				}
				outStream.println("RL"+";"+ident+";"+oldName+";"+newName);
				outStream.flush();
			}
			
			public void addLayer(int ident, int width, int height, String name) {
				if (ident == -1) {
					ident = myID;
				}
				outStream.println("L"+";"+ident+";"+width+";"+height+";"+name);
				outStream.flush();
			}
			
			public void addLayerWithImage(int ident, String name, BufferedImage img) {
				if (ident == -1) {
					ident = myID;
				}
				int width = img.getWidth();
				int height = img.getHeight();
				outStream.println("LI"+";"+ident+";"+width+";"+height+";"+name);
				outStream.flush();
				try {
					ImageIO.write(img, "png", socketOutStream);
				} catch (Exception e) {
					consolePanel.tellUser("Could not send the image for layer named " + name + ".");
				}
				outStream.println();
				outStream.println("ENDIMAGE");
				outStream.flush();
			}
			
			public void handleRemoteAction() {
				int x;
				int y;
				int layer;
				String in;
				String[] processedIn;
				String out;
				int receivedID;
				boolean erase;
				try {
					in = inStream.readLine();
				} catch(Exception e) {
					System.out.println("Connection possibly interrupted: " + e);
					in = "";
				}
				processedIn = in.split(";");
				if (processedIn[0].equals("NEW")) {
					consolePanel.tellUser("Received 'NEW'");
					receivedID = Integer.parseInt(processedIn[1]);
					remoteLineWidth = Integer.parseInt(processedIn[2]);
					remoteColor = new Color(Integer.parseInt(processedIn[3]));
					x = Integer.parseInt(processedIn[4]);
					y = Integer.parseInt(processedIn[5]);
					erase = new Boolean(processedIn[6]);
					layer = Integer.parseInt(processedIn[7]);
					if (receivedID > activeRemoteCurves.length-1) {
						Curve[] temp = new Curve[receivedID+1];
						System.arraycopy(activeRemoteCurves, 0, temp, 0, activeRemoteCurves.length);
						activeRemoteCurves = new Curve[receivedID+1];
						System.arraycopy(temp, 0, activeRemoteCurves, 0, activeRemoteCurves.length);
					}
					activeRemoteCurves[receivedID] = drawingData.startNewCurve(x, y, remoteLineWidth, remoteColor, erase, layer);
					if (serverRunning) {
						netController.sendNewCurve(receivedID, x, y, remoteLineWidth, remoteColor, erase, layer);
					}
				}
				else if (processedIn[0].equals("PT")) {
					receivedID = Integer.parseInt(processedIn[1]);
					x = Integer.parseInt(processedIn[2]);
					y = Integer.parseInt(processedIn[3]);
					drawingData.addToCurve(x, y, activeRemoteCurves[receivedID]);
					if (serverRunning) {
						netController.addToCurve(receivedID, x, y);
					}
				}
				else if (processedIn[0].equals("L")) {
					receivedID = Integer.parseInt(processedIn[1]);
					int width = Integer.parseInt(processedIn[2]);
					int height = Integer.parseInt(processedIn[3]);
					String name = processedIn[4];
					Layer newLayer = drawingData.addLayer(width, height, name);
					System.out.println("Receiving layer " + name);
					newLayer.id = receivedID;
					controlPanel.refigureLayers();
				}
				else if (processedIn[0].equals("RL")) {
					receivedID = Integer.parseInt(processedIn[1]);
					String oldName = processedIn[2];
					String newName = processedIn[3];
					for (Layer l: drawingData.layers) {
						if (l.name == oldName) {
							l.name = newName;
						}
					}
					controlPanel.refigureLayers();
				}
				else if (processedIn[0].equals("LI")) {
					receivedID = Integer.parseInt(processedIn[1]);
					int width = Integer.parseInt(processedIn[2]);
					int height = Integer.parseInt(processedIn[3]);
					String name = processedIn[4];
					Layer newLayer = drawingData.addLayer(width, height, name);
					System.out.println("Receiving layer with image " + name);
					try {
						newLayer.i = getImageFromReader(inStream);
					} catch (Exception e) {
						consolePanel.tellUser("Error reading layer image for layer named " + newLayer.name + ".");
					}
					controlPanel.refigureLayers();
					drawingPanel.repaint();
				}
				else if (processedIn[0].equals("SL")) {
					receivedID = Integer.parseInt(processedIn[1]);
					int l1 = Integer.parseInt(processedIn[2]);
					int l2 = Integer.parseInt(processedIn[3]);
					drawingData.swapLayers(l1, l2);
					controlPanel.refigureLayers();
					drawingPanel.repaint();
				}
				else {
					if (processedIn[0].equals("END")) {
						netController.handleRemoteDisconnect(this);
					}
				}
			}


			public Connection(Socket s) {
				connectionSocket = s;
				active = true;
				try {
					socketOutStream = connectionSocket.getOutputStream();
					socketInStream = connectionSocket.getInputStream();
					inStream = new BufferedReader(new InputStreamReader(socketInStream));
					outStream = new PrintWriter(socketOutStream);
					remoteAddress = connectionSocket.getInetAddress().getHostName();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}

	public class DrawingPanel extends JPanel {
		public DrawingPanel() {
			setBackground(Color.WHITE);

			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent evt) {
					setSize(new Dimension(2000, 2000));
					validate();
					drawingData.handleMouseDown(evt);
				}
				public void mouseReleased(MouseEvent evt) {
					drawingData.handleMouseUp();
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent evt) {
					drawingData.handleMouseDragged(evt);
				}
			});
		}
		public void paintComponent(Graphics g) {
			if (!drawingData.firstLayerAdded) {
				drawingData.panelWidth = getWidth();
				drawingData.panelHeight = getHeight();
				System.out.println("Init: " + drawingData.panelWidth);
				drawingSize = new Dimension(drawingData.panelWidth, drawingData.panelHeight);
				drawingData.layers[0] = new Layer(drawingData.panelWidth, drawingData.panelHeight, "Layer 0");
				drawingData.layers[0].id = drawingData.nextLayerID;
				drawingData.nextLayerID++;
				drawingData.firstLayerAdded = true;
				controlPanel.refigureLayers();
			}
			super.paintComponent(g);
			Graphics2D layerG;

			synchronized(drawingData.curves) {
				for (Curve c: drawingData.curves) {
					if (!c.done) {
						layerG = (Graphics2D)drawingData.layers[c.layer].i.getGraphics();
						layerG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
						layerG.setColor(c.color);
						drawingData.drawCurve(c, layerG);
					}
				}
			}
			for (Layer layer: drawingData.layers) {
				if (layer.visible) g.drawImage(layer.i, 0, 0, this);
			}
		}
	}
	
	public class DrawingData extends JPanel {
		MainPanel container = null;
		public int panelWidth;
		public int panelHeight;
		BufferedReader incoming = null;
		PrintWriter outgoing = null;
		Color currentColor = Color.BLACK;
		boolean firstLayerAdded = false;
		boolean mouseDown = false;
		int lastX;
		int lastY;
		int lineWidth;
		boolean coordsInitialized = false;
		Curve currentCurve = new Curve(-1, -1);
		LinkedList<Curve> curves = new LinkedList<Curve>();
		LinkedList<Curve> newCurves = new LinkedList<Curve>();
		//BufferedImage displayedImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
		Layer[] layers = new Layer[1];
		int nextLayerID = 0;
		
		public DrawingData() {
			layers[0] = new Layer(1, 1, "Layer 0");
		}

		public void setNewCursor() {
			if (!menuBar.colorPicker.isSelected()) {
				int w = controlPanel.getLineWidth();
				if (w>0) {
					if (w>100) w=100;
					int cw;
					if (w<9) {
						cw = 11;
					} else {
						cw = w+2;
					}
					BufferedImage i = new BufferedImage(cw, cw, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2 = (Graphics2D)i.getGraphics();
					g2.setColor(currentColor);
					if (w < 3 && !menuBar.eraser.isSelected()) {
						g2.drawLine(cw/2-5, cw/2, cw/2+5, cw/2);
						g2.drawLine(cw/2, cw/2-5, cw/2, cw/2+5);
					} else {
						if (menuBar.eraser.isSelected()) {
							g2.drawRect(cw/2-w/2, cw/2-w/2, w, w);
						} else {
							g2.drawOval(cw/2-w/2, cw/2-w/2, w, w);
						}
					}
					g2.dispose();
					Point hs = new Point(cw/2, cw/2);
					Toolkit tk = Toolkit.getDefaultToolkit();
					Cursor cursor = tk.createCustomCursor(i, hs, "cursor");
					setCursor(cursor);
				}
			} else {
				Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
				setCursor(cursor);
			}
		}

		public boolean layerExists(String name) {
			for (Layer layer: layers) {
				if (name.equals(layer.name)) {
					return true;
				}
			}
			return false;
		}

		public void swapLayers(int a, int b) {
			menuBar.save.setEnabled(true);
			Layer temp = layers[b];
			layers[b] = layers[a];
			layers[a] = temp;
		}
		
		public Layer insertLayer(int w, int h, String n, int insertionPoint) {
			menuBar.save.setEnabled(true);
			Layer[] newLayers = new Layer[layers.length+1];
			newLayers[insertionPoint] = new Layer(w, h, n);
			for (int i = 0; i < insertionPoint; i++) {
				newLayers[i] = layers[i];
			}
			for (int i = insertionPoint + 1; i < newLayers.length; i++) {
				newLayers[i] = layers[i-1]; 
			}
			layers = new Layer[newLayers.length];
			System.arraycopy(newLayers, 0, layers, 0, newLayers.length);
			nextLayerID++;
			return layers[insertionPoint];
		}

		public Layer addLayer(int w, int h, String n) {
			menuBar.save.setEnabled(true);
			Layer[] newLayers = new Layer[layers.length+1];
			System.arraycopy(layers, 0, newLayers, 0, layers.length);
			layers = new Layer[newLayers.length];
			System.arraycopy(newLayers, 0, layers, 0, newLayers.length);
			layers[newLayers.length-1] = new Layer(w, h, n);
			layers[newLayers.length-1].id = nextLayerID;
			nextLayerID++;
			return layers[newLayers.length-1];
		}

		public void drawCurve(Curve c, Graphics2D g) {
			/*GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, c.coords.length);
       float[] points;
       polyline.moveTo(c.coords[0].x, c.coords[0].y);
       for (Coord coord : c.coords) {
       System.out.println("x = " + coord.x +"; y = " + coord.y);
       polyline.lineTo(coord.x, coord.y);
       }*/
			Coord lastCoord = c.coords[0];
			g.setBackground(new Color(0, 0, 0, 0));
			for (Coord coord : c.coords) {
				if (!coord.drawn) {
					for (Coord lineCoord: lineBetween(lastCoord, coord)) {
						if (c.lineWidth == 1 && !c.erase) {
							g.drawRect(lineCoord.x, lineCoord.y, 0, 0);
						} else if (!c.erase) {
							g.fillOval(lineCoord.x-c.lineWidth/2, lineCoord.y-c.lineWidth/2, c.lineWidth, c.lineWidth);
						} else {
							g.setBackground(new Color(0, 0, 0, 0));
							g.clearRect(lineCoord.x-c.lineWidth/2, lineCoord.y-c.lineWidth/2, c.lineWidth, c.lineWidth);
						}
					}
					coord.drawn = true;
				}
				lastCoord = coord;
			}
		}

		public Curve startNewCurve(int x, int y, int lineWidth, Color color, boolean erase, int layer) {
			Curve newCurve = new Curve(x, y, lineWidth, color, layer, erase);
			synchronized(curves) {
				curves.add(newCurve);
			}
			//newCurves.add(newCurve);
			return newCurve;
		}

		public void addToCurve(int x, int y, Curve c) {
			c.addPoint(x, y);
			drawingPanel.repaint();
		}
		public Color getColorAt(int x, int y) {
			Color newColor = Color.WHITE;
			Color layerColor;
			int alpha;
			for (Layer layer: layers) {
				alpha = (layer.i.getRGB(x,y)>>24) & 0xff;
				layerColor = new Color(layer.i.getRGB(x, y));
				if (layer.visible && alpha != 0) {
					newColor = layerColor;
				}
			}
			return newColor;
		}
		public void handleMouseDown(MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			if (!layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
				JOptionPane.showMessageDialog(null, "The current layer is invisible.", "Can't draw there", JOptionPane.ERROR_MESSAGE);
			} else if (menuBar.colorPicker.isSelected()) {
				userResponder.changeColor(getColorAt(x, y));
			} else {
				lineWidth = controlPanel.getLineWidth();
				lastX = x;
				lastY = y;
				mouseDown = true;
				boolean erase = menuBar.eraser.isSelected();
				currentCurve = new Curve(lastX, lastY, lineWidth, currentColor, controlPanel.getCurrentLayer(), erase);
				synchronized(curves) {
					curves.add(currentCurve);
				}
				//newCurves.add(currentCurve);
				if (netController!=null && netController.connected == true) {
					netController.sendNewCurve(-1, lastX, lastY, lineWidth, currentColor, erase, controlPanel.getCurrentLayer());
				}
				drawingPanel.repaint();
			}
		}
		public void handleMouseUp() {
			mouseDown = false;
			coordsInitialized = false;
			currentCurve.done = true;
		}
		public void handleMouseDragged(MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			if (layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
				currentCurve.addPoint(x, y);
				if (netController != null && netController.connected == true) {
					netController.addToCurve(-1, x, y);
				}
				drawingPanel.repaint();
			} else if (menuBar.colorPicker.isSelected()) {
				userResponder.changeColor(getColorAt(x, y));
			}
		}
		public void resizeAllLayers(Dimension newSize) {
			for (Layer l: layers) {
				System.out.println("Resizing layer "+ l.name);
				l.resize(newSize);
			}
		}
		public LinkedList<Coord> lineBetween(Coord pointA, Coord pointB) {
			Coord pt1;
			Coord pt2;
			if (pointA.x<pointB.x) {
				pt1 = pointA;
				pt2 = pointB;
			} else {
				pt1 = pointB;
				pt2 = pointA;
			}

			LinkedList<Coord> coords = new LinkedList<Coord>();
			coords.add(pt1);

			if (pt1.x == pt2.x) {
				if (pt1.y < pt2.y) for (int y = pt1.y+1; y <= pt2.y; y++) coords.add(new Coord(pt1.x, y));
				else for (int y = pt1.y-1; y >= pt2.y; y--) coords.add(new Coord(pt1.x, y)); 
				return coords;
			}

			double slope = (double)(pt2.y-pt1.y)/(double)(pt2.x-pt1.x);

			double oldY = pt1.y;
			double newY;

			for (int x = pt1.x+1; x <= pt2.x; x++) {
				if (x == pt2.x) newY = pt2.y;
				else newY = oldY + slope;
				if (Math.abs(slope)<1) {
					coords.add(new Coord(x, (int)newY));
				}
				else {
					if (slope > 0) {
						for (int y = (int)oldY+1; y <= (int)newY; y++) {
							coords.add(new Coord(x, y));
						}
					} else {
						for (int y = (int)oldY-1; y >= (int)newY; y--) {
							coords.add(new Coord(x, y));
						}
					}
				}
				oldY = newY;
			}
			return coords;
		}
	}

	public class Coord { 
		public int x; 
		public int y; 
		public boolean drawn = false;
		public Coord(int x, int y) { 
			this.x = x; 
			this.y = y; 
		}
	}

	public class Layer {
		BufferedImage i;
		String name;
		int creator = -1;
		int id;
		boolean visible = true;

		public Layer(int width, int height, String n) {
			menuBar.save.setEnabled(true);
			i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			name = n;
		}
		public Layer(BufferedImage img, String n) {
			menuBar.save.setEnabled(true);
			i = img;
			name = n;
		}
		public Layer copy() {
			return (new Layer(this.i, this.name));
		}
		public void setVisible(Boolean v) {
			visible = v;
			drawingPanel.repaint();
		}
		public void resize(Dimension newSize) {
			BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_ARGB);
			System.out.println("Entering resize loop");
			for (int x = 0; x<i.getWidth(); x++) {
				for (int y = 0; y < i.getHeight(); y++) {
					int rgb = i.getRGB(x, y);
					resizedImage.setRGB(x, y, rgb);
				}
			}
			System.out.println("Exiting resize loop");
			i = resizedImage;
		}
	}

	public class Curve {
		public Coord[] coords;
		public int lineWidth;
		public Color color = Color.BLACK;
		public int layer = 0;
		public boolean done = false;
		public boolean erase = false;

		public Curve() {
			menuBar.save.setEnabled(true);
			coords = new Coord[0];
		}
		public Curve(int x, int y) {
			menuBar.save.setEnabled(true);
			lineWidth = 1;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
		}
		public Curve(int x, int y, int t) {
			menuBar.save.setEnabled(true);
			lineWidth = t;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
		}
		public Curve(int x, int y, int w, Color c) {
			menuBar.save.setEnabled(true);
			lineWidth = w;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
			color = c;
		}         
		public Curve(int x, int y, int w, Color c, int l) {
			menuBar.save.setEnabled(true);
			lineWidth = w;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
			color = c;
			layer = l;
		}
		public Curve(int x, int y, int w, Color c, int l, boolean e) {
			menuBar.save.setEnabled(true);
			lineWidth = w;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
			color = c;
			layer = l;
			erase = e;
		}

		public Curve(int x, int y, int w, Color c, boolean e) {
			menuBar.save.setEnabled(true);
			lineWidth = w;
			coords = new Coord[1];
			coords[0] = new Coord(x, y);
			color = c;
			erase = e;
		}
		/*public void addPoint(int x, int y) {
     Coord lastPt = coords[coords.length-1];
     Coord newPt = new Coord(x, y);
     for (Coord coord: makeLine(lastPt, newPt)) {
     myAddPoint(coord.x, coord.y);
     }
     }*/
		public void addPoint(int x, int y) {
			menuBar.save.setEnabled(true);
			Coord[] coordsNew = new Coord[coords.length+1];
			System.arraycopy(coords, 0, coordsNew, 0, coords.length);
			coordsNew[coords.length] = new Coord(x, y);
			coords = new Coord[coordsNew.length];
			System.arraycopy(coordsNew, 0, coords, 0, coordsNew.length);
		}
	}
}
