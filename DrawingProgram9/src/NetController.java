import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class NetController {
	LinkedList<Connection> connections = new LinkedList<Connection>();
	boolean serverRunning = false;
	boolean connected = false;
	int numConnections = 0;
	int myID = 0;
	ConnectionWaiter connectionWaiter;
	
	NetController netController;
	ConsolePanel consolePanel;
	ControlPanel controlPanel; 
	Drawing drawing;
	MainPanel.DrawingPanel drawingPanel;
	MainPanel.UserResponder userResponder;
	
	public NetController(ConsolePanel _consolePanel, 
							ControlPanel _controlPanel, 
							Drawing _drawing, 
							MainPanel.DrawingPanel _drawingPanel,
							MainPanel.UserResponder _userResponder) 
	{
		netController = this;
		controlPanel = _controlPanel;
		consolePanel = _consolePanel;
		drawing = _drawing;
		drawingPanel = _drawingPanel;
		userResponder = _userResponder;
	}
	
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
			userResponder.setControlsToNoConnection();
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
		int myWidth = drawing.width;
		int myHeight = drawing.height;
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
		consolePanel.tellUser("Correcting dimensions within correctDimensions");
		if (newHeight != myHeight || newWidth != myWidth) {
			Dimension newDim = new Dimension(newWidth, newHeight);
			drawing.resizeAllLayers(newDim);
			consolePanel.tellUser("Layers resized within correctDimensions");
			drawing.width = newDim.width;
			drawing.height = newDim.height;
			drawing.container.resizeDrawingPanel(newDim);
		}
	}

	public void sendLayersToServer(Connection connection) {
		for (Layer l: drawing.layers) {
			consolePanel.tellUser("Sending layer to server: " + l.name);
			connection.addLayerWithImage(myID, l.name, l.i);
		}
		connection.outStream.println("DONE");
		connection.outStream.flush();
	}

	public void correctLayerNames(Connection connection) {
		String in = "";
		try {
			in = connection.inStream.readLine();
		} catch(Exception e) {
			consolePanel.tellUser("Connection error while correcting layer names");
			consolePanel.tellUser("Error: " + e);
		}
		while (in.startsWith("RL")) {
			String[] processedIn = in.split(";");
			String oldName = processedIn[2];
			String newName = processedIn[3];
			consolePanel.tellUser("Renaming " + oldName + " to " + newName + ".");
			int oldLayerIndex = controlPanel.getCurrentLayerSelectIndex();
			for (Layer l: drawing.layers) {
				if (l.name.equals(oldName)) {
					l.name = newName;
					controlPanel.refigureLayers();
					controlPanel.setCurrentLayerSelectIndex(oldLayerIndex);
				}
			}
			try {
				in = connection.inStream.readLine();
			} catch(Exception e) {
				consolePanel.tellUser("Connection error while correcting layer names");
				consolePanel.tellUser("Error: " + e);
			}
		}
	}

	public void connectToServer(String ip, int port) {
		try {
			Socket socket = new Socket(ip, port);
			connected = true;
			Connection connection = new Connection(socket);
			consolePanel.tellUser("Connected to server at " + connection.remoteAddress);
			connections.add(connection);
			myID = Integer.parseInt(connection.inStream.readLine());
			connection.myID = myID;
			connection.otherID = myID;
			correctDimensions(connection);
			sendLayersToServer(connection);
			correctLayerNames(connection);
			connection.start();
		} catch (Exception e) {
			consolePanel.tellUser("connectToServer");
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
				serverRunning = false;
				userResponder.setControlsToNoConnection();
			}
			while(serverRunning) {
				try {
					System.out.println("Working 1");
					consolePanel.tellUser("Awaiting connections on port " + port + "...");
					newConnection = new Connection(listener.accept());
					System.out.println("Working 3");
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
					System.out.println("Working 2");
					while ((in = newConnection.inStream.readLine()).startsWith("LI;")) {
						System.out.println("Receiving layer from connector");
						processedIn = in.split(";");
						oldLayerName = processedIn[4];
						newLayerName = oldLayerName + "(" + processedIn[1] + ")";
						int width = Integer.parseInt(processedIn[2]);
						int height = Integer.parseInt(processedIn[3]);
						System.out.println("Working 5");
						newLayer = drawing.insertLayer(width, height, newLayerName, insertionPoint);
						System.out.println("Working 6");
						//menuBar.save.setEnabled(true);
						try {
							newLayer.i = getImageFromReader(newConnection.inStream);
						} catch (Exception e) {
							consolePanel.tellUser("Error receiving " + oldLayerName + " from connector");
						}
						System.out.println("Working 8");
						newLayers.add(newLayer);
						insertionPoint++;
						System.out.println("Working 9");
						System.out.println("CONTROLPANEL = " + controlPanel);
						controlPanel.refigureLayers();
						System.out.println("Working 7");
						drawingPanel.repaint();
						newConnection.renameLayer(0, oldLayerName, newLayerName);
					}
					System.out.println("Working 4");
					newConnection.outStream.println("DONE");
					newConnection.outStream.flush();
					System.out.println("Just sent DONE from server");
					for (Layer l: drawing.layers) {
						if (!newLayers.contains(l)) {
							newConnection.addLayerWithImage(0, l.name, l.i);
							System.out.println("Sending layer " + l.name + "from server.");
						} 
					}
					newConnection.start();
				} catch (Exception e) {
					consolePanel.tellUser("Server problem 2 in method run: " + e);
					serverRunning = false;
					userResponder.setControlsToNoConnection();
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
				/*The start of a new curve */
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
				activeRemoteCurves[receivedID] = drawing.startNewCurve(x, y, remoteLineWidth, remoteColor, erase, layer);
				if (serverRunning) {
					netController.sendNewCurve(receivedID, x, y, remoteLineWidth, remoteColor, erase, layer);
				}
			}
			else if (processedIn[0].equals("PT")) {
				/*A new point is being added to the connector's current curve*/
				receivedID = Integer.parseInt(processedIn[1]);
				x = Integer.parseInt(processedIn[2]);
				y = Integer.parseInt(processedIn[3]);
				drawing.addToCurve(x, y, activeRemoteCurves[receivedID]);
				drawingPanel.repaint();
				if (serverRunning) {
					netController.addToCurve(receivedID, x, y);
				}
			}
			else if (processedIn[0].equals("L")) {
				/*Connector added a new blank layer*/
				receivedID = Integer.parseInt(processedIn[1]);
				int width = Integer.parseInt(processedIn[2]);
				int height = Integer.parseInt(processedIn[3]);
				String name = processedIn[4];
				Layer newLayer = drawing.addLayer(width, height, name);
				System.out.println("Receiving layer " + name);
				newLayer.id = receivedID;
				controlPanel.refigureLayers();
			}
			else if (processedIn[0].equals("RL")) {
				/*Connector wants to rename layer*/
				receivedID = Integer.parseInt(processedIn[1]);
				String oldName = processedIn[2];
				String newName = processedIn[3];
				for (Layer l: drawing.layers) {
					if (l.name == oldName) {
						l.name = newName;
					}
				}
				controlPanel.refigureLayers();
			}
			else if (processedIn[0].equals("LI")) {
				/*Connector is sending a layer, along with image data to put in the layer*/
				receivedID = Integer.parseInt(processedIn[1]);
				int width = Integer.parseInt(processedIn[2]);
				int height = Integer.parseInt(processedIn[3]);
				String name = processedIn[4];
				Layer newLayer = drawing.addLayer(width, height, name);
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
				/*Connector is swapping layers*/
				receivedID = Integer.parseInt(processedIn[1]);
				int l1 = Integer.parseInt(processedIn[2]);
				int l2 = Integer.parseInt(processedIn[3]);
				drawing.swapLayers(l1, l2);
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