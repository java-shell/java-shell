package terra.shell.emulation.concurrency.helper;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import terra.shell.logging.LogManager;

/**
 * Creates an Image object that can be serialized, and de-serialized
 * 
 * @author schirripad@moravian.edu
 *
 */
public class SerializableImage extends BufferedImageNonserializableConstructor implements Serializable {
	private transient Image i;

	/**
	 * Create a SerializableImage object wrapped around a BufferedImage
	 * 
	 * @param i
	 */
	public SerializableImage(BufferedImage i) {
		super(i.getWidth(), i.getHeight(), i.getType());
		this.i = (Image) i;
	}

	@Override
	public int getWidth(ImageObserver observer) {
		return i.getHeight(null);
	}

	@Override
	public int getWidth() {
		return i.getWidth(null);
	}

	@Override
	public int getHeight() {
		return i.getHeight(null);
	}

	@Override
	public int getHeight(ImageObserver observer) {
		return i.getHeight(null);
	}

	@Override
	public ImageProducer getSource() {
		return i.getSource();
	}

	@Override
	public Graphics getGraphics() {
		return i.getGraphics();
	}

	@Override
	public Object getProperty(String name, ImageObserver observer) {
		return i.getProperty(name, observer);
	}

	private void writeObject(ObjectOutputStream s) throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		s.defaultWriteObject();
		int w = i.getWidth(null);
		int h = i.getHeight(null);

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DeflaterOutputStream dOut = new DeflaterOutputStream(bOut, new Deflater(Deflater.BEST_COMPRESSION));

		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(i, 0, 0, w, h, pixels, 0, w);
		pg.grabPixels();
		for (int p : pixels) {
			dOut.write(p);
		}

		s.writeInt(w);
		s.writeInt(h);
		s.writeObject(bOut.toByteArray());
		LogManager.write("Took " + (System.currentTimeMillis() - start) + "ms to serialize image");
	}

	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		long start = System.currentTimeMillis();
		in.defaultReadObject();
		int w = in.readInt();
		int h = in.readInt();
		byte[] compPixels = (byte[]) in.readObject();
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		InflaterOutputStream inf = new InflaterOutputStream(bOut);
		inf.write(compPixels);
		byte[] uncompPix = bOut.toByteArray();
		int[] pixels = new int[uncompPix.length];
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = (int) uncompPix[i];
		uncompPix = null;

		Toolkit tk = Toolkit.getDefaultToolkit();
		ColorModel cm = ColorModel.getRGBdefault();
		i = tk.createImage(new MemoryImageSource(w, h, cm, pixels, 0, w));
		LogManager.write("Took " + (System.currentTimeMillis() - start) + "ms to de-serialize image");
	}

}
