package terra.shell.emulation.concurrency.helper;

import java.awt.image.BufferedImage;

/**
 * Parent class for SerializableImage, acts as skeleton for serialization and
 * de-serialization of SerializableImage
 * 
 * @author schirripad@moravian.edu
 *
 */
public class BufferedImageNonserializableConstructor extends BufferedImage {

	public BufferedImageNonserializableConstructor() {
		super(1, 1, BufferedImage.TYPE_INT_ARGB);
	}

	public BufferedImageNonserializableConstructor(int a, int b, int c) {
		super(a, b, c);
	}

}
