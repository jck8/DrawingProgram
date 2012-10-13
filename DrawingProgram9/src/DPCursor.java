import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class DPCursor {
	/* Describes the cursor we'll use for the DrawingPanel. 
	 * Contains a method to convert itself to a Java "Cursor"
	 * object.
	 */
	
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

	MainPanel.DrawingPanel dp;
	ControlPanel controlPanel;
	Drawing drawing;
	Menu2 menuBar;
	Controls controls;
	
	public DPCursor(Drawing d, 
					MainPanel.DrawingPanel dp, 
					Controls cont, 
					Color c, 
					int s, 
					int w) {
		controls = cont;
		controlPanel = cont.getControlPanel();
		menuBar = cont.getMenu();
		drawing = d;
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
		return (new DPCursor(drawing, dp, controls, newColor, newShape, newWidth));
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
