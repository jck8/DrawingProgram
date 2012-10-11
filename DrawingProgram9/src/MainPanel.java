import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.LinkedList;
import java.net.*;
import java.io.*;
import java.awt.image.*;
import java.io.File;
import javax.imageio.*;

public class MainPanel extends JPanel {

	public final String DEFAULT_PORT = "21476";

	Dimension drawingSize;
	File currentFile = null;
	Drawing drawing;
	DrawingPanel drawingPanel;
	ControlPanel2 controlPanel;
	ConsolePanel consolePanel;
	JScrollPane scroller;
	UserResponder userResponder = new UserResponder();
	FileHandler fileHandler = new FileHandler();
	Menu menuBar = new Menu(userResponder);
	NetController netController;
	
	public MainPanel(File file, Dimension d, String ip, int port) {

		setLayout(new BorderLayout());
		drawing = new Drawing();
		drawing.registerListener(userResponder);
		drawing.container = this;

		if (d != null) {
			drawingSize = d;
			drawingPanel = new DrawingPanel();
			drawingPanel.setPreferredSize(drawingSize);
		} else {
			drawingSize = new Dimension(1000, 1000);
			drawingPanel = new DrawingPanel();
			drawingPanel.setPreferredSize(drawingSize);
			drawingPanel.setMaximumSize(drawingSize);
			drawingPanel.setMinimumSize(drawingSize);
		}
		
		BGBox bgBox = new BGBox(drawingPanel);

		scroller = new JScrollPane(bgBox);
		
		consolePanel = new ConsolePanel();

		Controls controls = new Controls(consolePanel, netController, drawingPanel, drawing);

		controlPanel = controls.getControlPanel();
		add(scroller, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
		add(consolePanel, BorderLayout.EAST);
		if (file != null) {
			try {
				drawing = fileHandler.openFile(file);
				controlPanel.drawing = drawing;
			} catch (FileNotFoundException e) {
				System.out.println("File not found: " + e);
			} catch (IOException e) {
				System.out.println("IOException: " + e);
			}
			for (Layer l: drawing.layers) {
				System.out.println("Layer named: " + l.name);
			}
			drawingPanel.setPreferredSize(new Dimension(drawing.width, drawing.height));
			controlPanel.refigureLayers();
			controlPanel.setCurrentLayer(drawing.layers[0].name);
			drawingPanel.repaint();
			currentFile = file;
		}
		if (ip != null) {
			userResponder.beginConnection(ip, port);
		}
		
		netController = new NetController(consolePanel, controlPanel, drawing, drawingPanel, userResponder);
		controls.netController = netController;
		
		drawingPanel.setNewCursor();
		menuBar.save.setEnabled(false);
		validate();
	}

	public void resizeDrawingPanel(Dimension d) {
		scroller.remove(drawingPanel);
		remove(scroller);
		drawingPanel = new DrawingPanel();
		drawingPanel.setPreferredSize(d);
		JPanel bgBox = new JPanel();
		bgBox.setLayout(new BoxLayout(bgBox, BoxLayout.Y_AXIS));
		bgBox.setBackground(Color.lightGray);
		bgBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		bgBox.add(Box.createVerticalGlue());
		bgBox.add(drawingPanel);
		bgBox.add(Box.createVerticalGlue());
		scroller = new JScrollPane(bgBox);
		add(scroller);
		validate();
	}

	public class UserResponder extends ActionListeners implements DrawingListener {
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
					DrawingProgram.newWindow();
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
					DrawingProgram.open();
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
					changeColor(JColorChooser.showDialog(controlPanel, "New Color", drawing.currentColor));
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
					drawingPanel.setNewCursor();
				}
				public void insertUpdate(DocumentEvent e) {
					drawingPanel.setNewCursor();
				}
			};
		}

		public void drawingChanged() {
			menuBar.save.setEnabled(true);
		}
		
		public void setControlsToNoConnection() {
			controlPanel.serverButton.setEnabled(true);
			controlPanel.connectButton.setEnabled(true);
			controlPanel.serverButton.setText("Start server");
			controlPanel.connectButton.setText("Connect...");
			serverRunning = false;
			connectedToServer = false;
			System.out.println("Set controls to no connection");
		}
		public void setControlsToServerRunning() {
			controlPanel.serverButton.setEnabled(true);
			controlPanel.connectButton.setEnabled(false);
			controlPanel.serverButton.setText("Stop server");
			controlPanel.connectButton.setText("Connect...");
			serverRunning = true;
			connectedToServer = false;
		}
		public void setControlsToConnectedToServer() {
			controlPanel.serverButton.setEnabled(false);
			controlPanel.connectButton.setEnabled(true);
			controlPanel.serverButton.setText("Start server");
			controlPanel.connectButton.setText("End connection");
			serverRunning = false;
			connectedToServer = true;
		}

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
				JFrame parentWindow = (JFrame)SwingUtilities.getWindowAncestor(drawingPanel);
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
			setBorder(BorderFactory.createLineBorder(c, 2));
			consolePanel.setBorder(BorderFactory.createLineBorder(c, 2));
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
			Thread connectionThread;
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
					userResponder.setControlsToServerRunning();
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

	public class DPCursor {
		/* Describes the cursor we'll use for the DrawingPanel. 
		 * Contains a method to convert itself to a Java "Cursor"
		 * object.
		 */

		DrawingPanel dp;
		ControlPanel cp;
		
		public static final int DEFAULTSHAPE = 0;
		public static final int CROSSHAIR = 1;
		public static final int CIRCLE = 2;
		public static final int SQUARE = 3;

		final int COLORCHANGETHRESHOLD = 400; 
		/*How bright/dim the area under the cursor has to be to justify changing 
		 * the cursor's color to make it more visible. The value of 400 was chosen to
		 * ensure that, e.g., the cursor becomes white when most of the underlying area
		 * is black. */

		final int CURSORIMAGEPADDING = 2;
		/*When we draw a circle-shaped cursor, the image needs to be a bit wider than the diameter of the
		 * circle in order to contain the whole circle
		 */

		final int CROSSHAIRTHRESHOLD = 3;
		/*When the cursor is small, we want to change it to a crosshair, rather than a circle the width
		 * of the brush size. This gives the cursor width (in pixels) at which we make the change.*/  

		Color color;
		int shape;
		int width;

		public DPCursor(DrawingPanel dp, Color c, int s, int w) {
			this.dp = dp;
			shape = s;
			color = c;
			width = w;
		}

		public Boolean equals(DPCursor c) {
			if (this.width == c.width && this.shape == c.shape && this.color.equals(c.color)) {
				return true;
			} else {
				return false;
			}
		}

		public Cursor convertToCursor() {
			Cursor cursor;
			int w = width;
			if (shape == DEFAULTSHAPE) {
				cursor = new Cursor(Cursor.DEFAULT_CURSOR);
			} else {
				BufferedImage img; //The buffered image we'll draw the new cursor image into, in order to pass to createCustomCursor
				int cursorImageWidth; //The width and height of that buffered image. Now just set it wide enough...

				if (shape == CROSSHAIR) { 
					cursorImageWidth = 11; 
				} else {
					cursorImageWidth = w+CURSORIMAGEPADDING; 
				}

				img = new BufferedImage(cursorImageWidth, cursorImageWidth, BufferedImage.TYPE_INT_ARGB);
				Graphics g = img.getGraphics();
				g.setColor(color);
				if (shape == CROSSHAIR) {
					g.drawLine(cursorImageWidth/2-5, cursorImageWidth/2, cursorImageWidth/2+5, cursorImageWidth/2);
					g.drawLine(cursorImageWidth/2, cursorImageWidth/2-5, cursorImageWidth/2, cursorImageWidth/2+5);
				}
				if (shape == CIRCLE) {
					g.drawOval(cursorImageWidth/2-w/2, cursorImageWidth/2-w/2, w, w);
				}
				if (shape == SQUARE) {
					g.drawRect(cursorImageWidth/2-w/2, cursorImageWidth/2-w/2, w, w);
				}
				g.dispose();
				Point hotspot = new Point(cursorImageWidth/2, cursorImageWidth/2);
				Toolkit tk = Toolkit.getDefaultToolkit();
				cursor = tk.createCustomCursor(img, hotspot, "cursor");
			}
			return cursor;
		}

		private int clipCursorSize(int preferredSize) {
			/* We want our cursor to take up an area that's preferredSize x preferredSize, 
			 * but the system we're on might not allow cursors that size. Return the closest 
			 * width/height we can actually use, assuming a square-shaped cursor.
			 */
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension closestAllowedDimension = 
					tk.getBestCursorSize(preferredSize + CURSORIMAGEPADDING, preferredSize + CURSORIMAGEPADDING);
			int closestW = closestAllowedDimension.width - CURSORIMAGEPADDING;
			int closestH = closestAllowedDimension.height - CURSORIMAGEPADDING;
			if (closestW != preferredSize) {
				System.out.println("W: " + closestW);
				System.out.println("H: " + closestH);
			}
			return Math.min(closestW, closestH);
		}

		public void makeCursorVisibleOn(Color c) {
			//Change our cursor color in order to be visible on a background of the given color.
			if (dp.getBrightness(c) > COLORCHANGETHRESHOLD) {
				System.out.println("changing to black");
				color = Color.black;
			} else {
				System.out.println("changing to white");
				color = Color.white;
			}
		}

		public DPCursor getNew(int x, int y) {
			/* When passed the current mouse coordinates, return a cursor appropriate to the mouse's
			 * current location: white if it's on a dark background, black if on a light background. */
			int newWidth;
			int newShape;
			Color newColor;
			newWidth = clipCursorSize(controlPanel.getLineWidth());

			System.out.println(dp.fractionOfPixelsBrighterThan(getPointsCircle(x, y, newWidth), 0.5));

			if (dp.averagePixelBrightness(getPointsCircle(x, y, newWidth)) > COLORCHANGETHRESHOLD) { 
				newColor = Color.BLACK;
			} else {
				newColor = Color.WHITE;
			}
			if (menuBar.colorPicker.isSelected()) {
				newShape = DEFAULTSHAPE;
			} else if (menuBar.eraser.isSelected()) {
				newShape = SQUARE;
			} else {
				if (newWidth < CROSSHAIRTHRESHOLD) {
					newShape = CROSSHAIR;
				} else {
					newShape = CIRCLE;
				}
			}
			return (new DPCursor(dp, newColor, newShape, newWidth));
		}

		public LinkedList<Coord> getPointsCircle(int x, int y, int w) {
			int r = w/2; //Make the radius of the "circle" we're testing equal to the width/2
			int pointX; //The x coordinate of the pixel currently being counted
			int pointY; //The y coordinate of the pixel currently being counted

			LinkedList<Coord> points = new LinkedList<Coord>();
			for (int xi = - r; xi <= r; xi++) {
				pointX = xi + x;
				pointY = y - (int)Math.sqrt(r*r - xi*xi);
				points.add(new Coord(pointX, pointY));
				pointY = y + (int)Math.sqrt(r*r - xi*xi);
				points.add(new Coord(pointX, pointY));
			}
			return points;
		}
	}

	public class DPMouseMotionListener extends MouseMotionAdapter {
		private int timesMouseMoved = 0;
		public void mouseDragged(MouseEvent evt) {
			handleMouseDragged(evt);
		}
		public void mouseMoved(MouseEvent evt) {
			if (timesMouseMoved < 15) {
				timesMouseMoved++;
			} else {
				timesMouseMoved = 0;
				DPCursor oldCursor = drawingPanel.dpCursor;
				DPCursor newCursor = oldCursor.getNew(evt.getX(), evt.getY());
				if (!newCursor.equals(oldCursor)) {
					drawingPanel.dpCursor = newCursor;
					drawingPanel.setCursor(drawingPanel.dpCursor.convertToCursor());
				}
			}
		}
		public void handleMouseDragged(MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			if (drawing.layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
				//drawing.currentCurve.addPoint(x, y);
				drawing.addToCurve(x, y, drawingPanel.currentCurve);
				//menuBar.save.setEnabled(true);
				if (netController != null && netController.connected == true) {
					netController.addToCurve(-1, x, y);
				}
				drawingPanel.repaint();
			} else if (menuBar.colorPicker.isSelected()) {
				if (x<drawingPanel.getWidth() && x>0 && y<drawingPanel.getHeight() && y>0)
					userResponder.changeColor(drawing.getColorAt(x, y));
			}
		}
	}
	
	public class DPMouseListener extends MouseAdapter {
		boolean mouseDown = false;
		int lastX;
		int lastY;
		int lineWidth;

		public void mousePressed(MouseEvent evt) {
			drawingPanel.validate();
			handleMouseDown(evt);
			//When we start drawing, the cursor will have the current brush color behind it.
			//Change the cursor color so that it stays visible against that background.
			if (drawingPanel.dpCursor.shape == DPCursor.CIRCLE) {
				drawingPanel.dpCursor.makeCursorVisibleOn(drawing.currentColor);
				drawingPanel.makeCursorCurrentDPCursor();
			}
		}
		public void mouseReleased(MouseEvent evt) {
			handleMouseUp();
		}

		public void handleMouseDown(MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			if (!drawing.layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
				JOptionPane.showMessageDialog(null, "The current layer is invisible.", "Can't draw there", JOptionPane.ERROR_MESSAGE);
			} else if (menuBar.colorPicker.isSelected()) {
				if (x<drawingPanel.getWidth() && x>0 && y<drawingPanel.getHeight() && y>0)
					userResponder.changeColor(drawing.getColorAt(x, y));
			} else {
				lineWidth = controlPanel.getLineWidth();
				lastX = x;
				lastY = y;
				mouseDown = true;
				boolean erase = menuBar.eraser.isSelected();
				synchronized(drawing.curves) {
					drawingPanel.currentCurve = drawing.startNewCurve(lastX, lastY, lineWidth, drawing.currentColor, erase, controlPanel.getCurrentLayer());
				}
				if (netController!=null && netController.connected == true) {
					netController.sendNewCurve(-1, lastX, lastY, lineWidth, drawing.currentColor, erase, controlPanel.getCurrentLayer());
				}
				drawingPanel.repaint();
			}
		}
		public void handleMouseUp() {
			mouseDown = false;
			drawingPanel.currentCurve.done = true;
		}
	}

	public class DrawingPanel extends JPanel {

		DPCursor dpCursor = new DPCursor(this, Color.black, DPCursor.DEFAULTSHAPE, 1);
		Curve currentCurve;
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		public DrawingPanel() {
			setBackground(Color.WHITE);
			addMouseListener(new DPMouseListener());
			addMouseMotionListener(new DPMouseMotionListener());
		}

		public void paintComponent(Graphics g) {
			if (!drawing.firstLayerAdded) {
				drawing.width = getWidth();
				drawing.height = getHeight();
				drawingSize = new Dimension(drawing.width, drawing.height);
				drawing.layers[0] = new Layer(drawing.width, drawing.height, "Layer 0");
				drawing.layers[0].id = drawing.nextLayerID;
				drawing.nextLayerID++;
				drawing.firstLayerAdded = true;
				controlPanel.refigureLayers();
			}
			super.paintComponent(g);
			Graphics2D layerG;

			synchronized(drawing.curves) {
				for (Curve c: drawing.curves) {
					if (!c.done) {
						layerG = (Graphics2D)drawing.layers[c.layer].i.getGraphics();
						layerG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
						layerG.setColor(c.color);
						drawCurve(c, layerG);
					}
				}
			}
			for (Layer layer: drawing.layers) {
				if (layer.visible) g.drawImage(layer.i, 0, 0, this);
			}
		}

		public float getBrightness2(Color c) {
			float[] hsb;
			hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
			return hsb[2];
		}

		public int getBrightness(Color c) {
			return c.getRed() + c.getGreen() + c.getBlue();
		}

		public float fractionOfPixelsBrighterThan(LinkedList<Coord> pixels, double threshold) {
			float fraction = 0;
			float brightness;
			int numPixelsCounted = 0;
			int numPixelsBrighter = 0;
			for (Coord c: pixels) {
				try {
					brightness = getBrightness(drawing.getColorAt(c.x, c.y));
					if (brightness > threshold) {
						numPixelsBrighter++;
					}
					numPixelsCounted++;
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			if (numPixelsCounted > 0) { //Avoid division by zero.
				fraction = (float)numPixelsBrighter / (float)numPixelsCounted;
			}
			return fraction;
		}

		public int averagePixelBrightness(LinkedList<Coord> pixels) {
			/* takes a list of Coords and gives the average "brightness" of the pixels
			 * that they represent. "Brightness" means the sum of the R, G, and B */
			int totalPixelBrightness = 0;
			Color pixelColor; //The color of the pixel currently being counted
			int n = 0;
			for (Coord c: pixels) {
				try {
					pixelColor = drawing.getColorAt(c.x, c.y);
					totalPixelBrightness += getBrightness(pixelColor);
					n++;
				} catch (Exception e) {}
			}
			if (n == 0) return 0;
			else {
				return totalPixelBrightness / n;
			}
		}


		public void makeCursorCurrentDPCursor() {
			setCursor(dpCursor.convertToCursor());
		}

		public void setNewCursor() {
			//Function to be called when we need to change the cursor, but it isn't 
			//necessarily in the drawingPanel, so we can't test the pixels underneath it.
			//Just set to black.
			DPCursor newDPCursor = dpCursor.getNew(0, 0);
			newDPCursor.color = Color.black;
			dpCursor = newDPCursor;
			setCursor(newDPCursor.convertToCursor());
		}
		
		private LinkedList<Coord> lineBetween(Coord pointA, Coord pointB) {
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
	}
}
