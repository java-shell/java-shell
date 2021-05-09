package terra.shell.utils.system;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import terra.shell.logging.LogManager;

/**
 * Just a URLClassLoader wrapper class, used in loading Commands and Modules.
 * 
 * @author dan
 * 
 */
public class ByteClassLoader extends URLClassLoader {

	// TODO Find better way to reload dynamically loaded classes
	private HashMap<String, byte[]> loaded;

	public ByteClassLoader(URL[] url) {
		super(url);
		loaded = new HashMap<String, byte[]>();
	}

	public Class<?> getClass(byte[] b) {
		Class<?> tmp = defineClass(b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", b);
		return tmp;
	}

	public Class<?> getClass(String name, byte[] b) {
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", b);
		return tmp;
	}

	public Class<?> getClass(String name, String pkg, byte[] b) {
		try {
			definePackage(pkg, "", "", "", "", "", "", null);
		} catch (IllegalArgumentException e) {
		}
		;
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", b);
		return tmp;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		LogManager.out.println(name);
		if (loaded.containsKey(name)) {
			return new ByteArrayInputStream(loaded.get(name));
		}
		return super.getResourceAsStream(name);
	}
}
