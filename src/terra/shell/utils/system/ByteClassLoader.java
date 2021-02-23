package terra.shell.utils.system;

import java.net.URL;
import java.net.URLClassLoader;

import terra.shell.logging.LogManager;

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
		LogManager.out.println("Resolved class with name: "+tmp.getName());
		return tmp;
	}

	public Class<?> getClass(String name, byte[] b) {
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		LogManager.out.println("Resolved class with name: "+tmp.getName());
		return tmp;
	}
}
