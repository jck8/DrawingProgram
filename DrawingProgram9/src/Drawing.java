import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.LinkedList;

public class Drawing {
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
	Curve currentCurve = new Curve(-1, -1);
	LinkedList<Curve> curves = new LinkedList<Curve>();
	LinkedList<Curve> newCurves = new LinkedList<Curve>();
	Layer[] layers = new Layer[1];
	int nextLayerID = 0;

	public Drawing() {
		layers[0] = new Layer(1, 1, "Layer 0");
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
		Layer temp = layers[b];
		layers[b] = layers[a];
		layers[a] = temp;
	}

	public Layer insertLayer(int w, int h, String n, int insertionPoint) {
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
		Layer[] newLayers = new Layer[layers.length+1];
		System.arraycopy(layers, 0, newLayers, 0, layers.length);
		layers = new Layer[newLayers.length];
		System.arraycopy(newLayers, 0, layers, 0, newLayers.length);
		layers[newLayers.length-1] = new Layer(w, h, n);
		layers[newLayers.length-1].id = nextLayerID;
		nextLayerID++;
		return layers[newLayers.length-1];
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

	public void resizeAllLayers(Dimension newSize) {
		for (Layer l: layers) {
			l.resize(newSize);
		}
	}
}