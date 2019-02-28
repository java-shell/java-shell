package terra.shell.utils.system;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Just a URLClassLoader wrapper class, used in loading Commands and Modules.
 * 
 * @author dan
 * 
 */
public class ByteClassLoader extends URLClassLoader {
	public ByteClassLoader(URL[] url) {
		super(url);
	}

	public Class<?> getClass(byte[] b) {
		Class<?> tmp = defineClass(b, 0, b.length);
		resolveClass(tmp);
		return tmp;
	}

	public Class<?> getClass(String name, byte[] b) {
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		return tmp;
	}
}
