package terra.shell.emulation.concurrency.math.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class JProcessRealizer extends ObjectInputStream {
	private ClassLoader cl;

	public JProcessRealizer(InputStream in) throws IOException {
		super(in);
	}

	public JProcessRealizer() throws SecurityException, IOException {
		super();
	}

	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	@Override
	public Class<?> resolveClass(ObjectStreamClass desc) {
		String name = desc.getName();
		try {
			return Class.forName(name, false, cl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
