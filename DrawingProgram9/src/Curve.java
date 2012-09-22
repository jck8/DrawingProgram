import java.awt.Color;

public class Curve {
	public Coord[] coords;
	public int lineWidth;
	public Color color = Color.BLACK;
	public int layer = 0;
	public boolean done = false;
	public boolean erase = false;

	public Curve(int x, int y) {
		lineWidth = 1;
		coords = new Coord[1];
		coords[0] = new Coord(x, y);
	}
	public Curve(int x, int y, int w, Color c, int l, boolean e) {
		lineWidth = w;
		coords = new Coord[1];
		coords[0] = new Coord(x, y);
		color = c;
		layer = l;
		erase = e;
	}

	public void addPoint(int x, int y) {
		Coord[] coordsNew = new Coord[coords.length+1];
		System.arraycopy(coords, 0, coordsNew, 0, coords.length);
		coordsNew[coords.length] = new Coord(x, y);
		coords = new Coord[coordsNew.length];
		System.arraycopy(coordsNew, 0, coords, 0, coordsNew.length);
	}
}