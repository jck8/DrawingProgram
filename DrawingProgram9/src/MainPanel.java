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
	ActionListeners userResponder;
	FileHandler fileHandler = new FileHandler();
	NetController netController;
	Controls controls; 
	Menu menuBar;

	public MainPanel(File file, Dimension d, String ip, int port) {

		setLayout(new BorderLayout());
		drawing = new Drawing();
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

		controls = new Controls(consolePanel, netController, drawingPanel, drawing);

		userResponder = controls.getActionListener();
		menuBar = new Menu(userResponder);
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
			controls.netActions.beginConnection(ip, port);
		}

		netController = new NetController(consolePanel, controlPanel, drawing, drawingPanel, userResponder);
		controls.netController = netController;

		drawingPanel.initCursor();
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
					controls.drawingActions.changeColor(drawing.getColorAt(x, y));
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
					controls.drawingActions.changeColor(drawing.getColorAt(x, y));
			}
		}
	}

	public class DrawingPanel extends JPanel {

		DPCursor dpCursor;
		Curve currentCurve;

		public void initCursor() {
			dpCursor = new DPCursor(drawing, this, controls, Color.black, DPCursor.DEFAULTSHAPE, 1);
		}

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
