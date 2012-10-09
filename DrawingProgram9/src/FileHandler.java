import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

	public class FileHandler {

		private String generateLayerName(String init, Drawing newDrawing) {
			int num = 0;
			String name = init;
			while (newDrawing.layerExists(name)) {
				num = num + 1;
				name = init + "_" + num;
			}
			return name;
		}

		private void insertImage(BufferedImage img, String name, Drawing newDrawing) {
			int rgb;
			int w;
			int h;
			Layer newLayer;
			name = generateLayerName(name, newDrawing);
			w = img.getWidth();
			h = img.getHeight();
			newLayer = newDrawing.addLayer(w, h, name);
			for (Layer l: newDrawing.layers) {
				System.out.println("Layer in newDrawing1 named: " + l.name);
			}
			newLayer.i = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					rgb = img.getRGB(x, y);
					newLayer.i.setRGB(x, y, rgb);
				}
			}
		}

		public Drawing openFile (File file) throws FileNotFoundException, IOException {
			String currentLayerName = "";
			String chunk;
			FileReader fr;
			String[] chunkSplit;
			BufferedReader br;
			BufferedImage img;
			String imageText = "";
			Drawing newDrawing = new Drawing();
			newDrawing.layers = new Layer[0];
			newDrawing.firstLayerAdded = true; //Necessary so we don't overwrite our new layer the first time we repaint drawingPanel 
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			while ((chunk = getChunk(br)) != null) {
				if (chunk.startsWith("NAMEIS")) {
					if (!imageText.equals("!")) {
						InputStream is = new ByteArrayInputStream(imageText.getBytes());
						img = ImageIO.read(is);
						insertImage(img, currentLayerName, newDrawing);
					} 
					imageText = "";
					chunkSplit = chunk.split(":");
					int len = chunkSplit[1].length();
					currentLayerName = chunkSplit[1].substring(0, len-1);

				} else {
					imageText = imageText + chunk;
				}
			}
			if (currentLayerName.equals("")) {
				currentLayerName = "Imported Image";
			}
			InputStream is = new ByteArrayInputStream(imageText.getBytes());
			img = ImageIO.read(is);
			insertImage(img, currentLayerName, newDrawing);

			newDrawing.width = img.getWidth();
			newDrawing.height = img.getHeight();

			return newDrawing;
		}

		private String getChunk(BufferedReader br) {
			//Customized function for getting the next "chunk" of text from a BufferedReader. 
			//Similar to readLine(), but uses '!' as a delimiter between chunks. readLine has 
			//multiple delimiters (linefeeds, carriage returns, linefeeds+carriage returns), 
			//and there doesn't seem to be any way to tell which one ended a particular line. 
			//Using readLine would thus lead to data loss if used to read image files.
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


		public void writeDrawingToFile(File file, Drawing drawingToSave) throws IOException {
			FileWriter fw;
			PrintWriter pw;
			Iterator<ImageWriter> iter;
			ImageWriter writer;
			ImageWriteParam iwp;
			FileImageOutputStream output;
			IIOImage img;
			iter = ImageIO.getImageWritersByFormatName("png");
			writer = (ImageWriter)iter.next();
			iwp = writer.getDefaultWriteParam();
			output = new FileImageOutputStream(file);
			writer.setOutput(output);
			fw = new FileWriter(file);
			pw = new PrintWriter(fw);
			synchronized(file) {
				for (Layer layer: drawingToSave.layers) {
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
			pw.close();
			writer.dispose();

		}

		public void exportFile(File file, Drawing d) {
			BufferedImage toSave = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < d.width; x++) {
				for (int y = 0; y < d.height; y++) {
					toSave.setRGB(x, y, d.getColorAt(x, y).getRGB());
				}
			}
			try {
				ImageIO.write(toSave, "png", file);
				System.out.println("File " + file.getName() + " has been successfully written.");
			} catch (Exception e) {
				System.out.println("Error writing file: " + e);
			}
		}

	}