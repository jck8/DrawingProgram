import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class Layer {
	BufferedImage i;
	String name;
	int creator = -1;
	int id;
	boolean visible = true;

	public Layer(int width, int height, String n) {
		i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		name = n;
	}
	public Layer(BufferedImage img, String n) {
		i = img;
		name = n;
	}
	public Layer copy() {
		return (new Layer(this.i, this.name));
	}
	public void setVisible(Boolean v) {
		visible = v;
	}
	public void resize(Dimension newSize) {
		BufferedImage resizedImage = new BufferedImage(newSize.width, newSize.height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x<i.getWidth(); x++) {
			for (int y = 0; y < i.getHeight(); y++) {
				int rgb = i.getRGB(x, y);
				resizedImage.setRGB(x, y, rgb);
			}
		}
		i = resizedImage;
	}
}