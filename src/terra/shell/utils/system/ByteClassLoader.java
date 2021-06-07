package terra.shell.utils.system;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.zip.CRC32;

import terra.shell.logging.LogManager;

/**
 * Just a URLClassLoader wrapper class, used in loading Commands and Modules.
 * 
 * @author dan
 * 
 */
public class ByteClassLoader extends URLClassLoader {

	// TODO Find better way to reload dynamically loaded classes
	protected HashMap<String, ClassData> loaded;

	public ByteClassLoader(URL[] url) {
		super(url);
		loaded = new HashMap<String, ClassData>();
	}

	public Class<?> getClass(byte[] b) {
		Class<?> tmp = defineClass(b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", new ClassData(b, generateCRC(b), tmp));
		return tmp;
	}

	public Class<?> getClass(String name, byte[] b) {
		if (loaded.containsKey(name)) {
			if ((loaded.get(name).getChk() != generateCRC(b))) {
				// TODO Determine how to handle duplicate class
				Class<?> pl = loaded.get(name).getClassObject();
				if (pl.isAnnotationPresent(Replaceable.class)) {
					if (pl.getAnnotation(Replaceable.class).replaceable()) {
						// Replace the pre loaded class with a new one
					}
				}
				return pl;
			} else {
				return loaded.get(name).getClass();
			}
		}
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", new ClassData(b, generateCRC(b), tmp));
		return tmp;
	}

	public Class<?> getClass(String name, String pkg, byte[] b) {
		if (loaded.containsKey(name)) {
			if ((loaded.get(name).getChk() != generateCRC(b))) {
				// TODO Determine how to handle duplicate class
			} else {
				return loaded.get(name).getClass();
			}
		}
		try {
			definePackage(pkg, "", "", "", "", "", "", null);
		} catch (IllegalArgumentException e) {
		}
		Class<?> tmp = defineClass(name, b, 0, b.length);
		resolveClass(tmp);
		loaded.put(tmp.getName().replace('.', '/') + ".class", new ClassData(b, generateCRC(b), tmp));
		return tmp;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		LogManager.out.println(name);
		if (loaded.containsKey(name)) {
			return new ByteArrayInputStream(loaded.get(name).getData());
		}
		return super.getResourceAsStream(name);
	}

	protected CRC32 generateCRC(byte[] b) {
		CRC32 chk = new CRC32();
		chk.update(b, 0, b.length);
		return chk;
	}

	protected class ClassData {
		private byte[] b;
		private CRC32 chk;
		private Class<?> c;

		public ClassData(byte[] b, CRC32 chk, Class<?> c) {
			this.b = b;
			this.chk = chk;
			this.c = c;
		}

		public byte[] getData() {
			return b;
		}

		public CRC32 getChk() {
			return chk;
		}

		public Class<?> getClassObject() {
			return c;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.LOCAL_VARIABLE, ElementType.TYPE })
	public @interface Replaceable {
		public boolean replaceable() default true;
	}
}
