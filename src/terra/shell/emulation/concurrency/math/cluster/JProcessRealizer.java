package terra.shell.emulation.concurrency.math.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import terra.shell.logging.LogManager;

/**
 * Realize classes sent from remote Nodes via ConnectionManager. Decodes and
 * De-Serializes quantized class information
 * 
 * @author schirripad@moravian.edu
 *
 */
public class JProcessRealizer extends ObjectInputStream {
	private ClassLoader cl;

	/**
	 * Realize a class from an InputStream
	 * 
	 * @param in Stream containing serialized class.
	 * @throws IOException
	 */
	public JProcessRealizer(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Create a default JProcessRealizer
	 * 
	 * @throws SecurityException
	 * @throws IOException
	 */
	public JProcessRealizer() throws SecurityException, IOException {
		super();
	}

	/**
	 * Set the ClassLoader object which this JProcessRealizer will utilize to search
	 * for its class skeleton upon de-serialization
	 * 
	 * @param cl ClassLoader to use
	 */
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	@Override
	public Class<?> resolveClass(ObjectStreamClass desc) {
		String name = desc.getName();

		try {
			return Class.forName(name, false, cl);
		} catch (Exception e) {
			try {
				return cl.loadClass(name);
			} catch (Exception e1) {
				e1.printStackTrace();
				LogManager.out.println();
			}
			e.printStackTrace();
			return null;
		}
	}

}
