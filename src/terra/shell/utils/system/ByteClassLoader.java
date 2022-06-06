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

	/**
	 * Create a ByteClassLoader which can access Files located at the given URLs
	 * 
	 * @param url
	 * @param parent
	 */
	public ByteClassLoader(URL[] url, ClassLoader parent) {
		super(url, parent);
		loaded = new HashMap<String, ClassData>();
	}

	/**
	 * Create a ByteClassLoader which can access Files located at the given URLs
	 * 
	 * @param url
	 */
	public ByteClassLoader(URL[] url) {
		super(url);
		loaded = new HashMap<String, ClassData>();
	}

	/**
	 * Retrieve a Class object from the given byte array
	 * 
	 * @param b
	 * @return
	 */
	public Class<?> getClass(byte[] b) {
		Class<?> tmp = defineClass(b, 0, b.length);
		resolveClass(tmp);
		synchronized (loaded) {
			loaded.put(tmp.getName().replace('.', '/') + ".class", new ClassData(b, generateCRC(b), tmp));
		}
		return tmp;
	}

	/**
	 * Retrieve a Class object from the given byte array, using the given Name
	 * 
	 * @param name
	 * @param b
	 * @return
	 */
	public Class<?> getClass(String name, byte[] b) {
		synchronized (loaded) {
			if (loaded.containsKey(name)) {
				if ((loaded.get(name).getChk() != generateCRC(b))) {
					// FIXME Remove Replaceable annotation, cannot replace an already loaded class
					// without first destroying its classloader
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
	}

	/**
	 * Retrieve a Class object from the given byte array, using the given Name and
	 * Package
	 * 
	 * @param name
	 * @param pkg
	 * @param b
	 * @return
	 */
	public Class<?> getClass(String name, String pkg, byte[] b) {
		synchronized (loaded) {
			if (loaded.containsKey(name)) {
				if ((loaded.get(name).getChk() != generateCRC(b))) {
					LogManager.write("GOT DUPLICATE CLASS NAME BUT UNIQUE CHKSUM, RETURNING ALREADY LOADED VERSION");
					return loaded.get(name).getClassObject();
				} else {
					return loaded.get(name).getClassObject();
				}
			}
			try {
				definePackage(pkg, "", "", "", "", "", "", null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			try {
				Class<?> tmp = defineClass(name, b, 0, b.length);
				resolveClass(tmp);
				loaded.put(tmp.getName().replace('.', '/') + ".class", new ClassData(b, generateCRC(b), tmp));
				return tmp;
			} catch (LinkageError e) {
				LogManager.write("GOT LINKAGEERROR: " + e.getLocalizedMessage() + "\n");
				LogManager.write("ATTEMPTING TO LOAD PRE-LOADED VERSION\n");
				Class<?> preLoad = null;
				try {
					preLoad = loadClass(name);
				} catch (Exception e1) {
					e.printStackTrace();
					throw (e);
				}
				if (preLoad == null) {
					e.printStackTrace();
					LogManager.write("PRELOAD_NULL");
					throw (e);
				}
				return preLoad;
			}
		}
	}

	public Class<?> retrieveClass(String name) {
		synchronized (loaded) {
			if (loaded.containsKey(name))
				return loaded.get(name).getClass();
		}
		if (getParent() != null && getParent() instanceof ByteClassLoader) {
			return ((ByteClassLoader) getParent()).retrieveClass(name);
		}
		return null;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> cl = retrieveClass(name);
		if (cl != null)
			return cl;
		return super.loadClass(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		LogManager.out.println(name);
		synchronized (loaded) {
			if (loaded.containsKey(name)) {
				return new ByteArrayInputStream(loaded.get(name).getData());
			}
		}
		return super.getResourceAsStream(name);
	}

	/**
	 * Generate a CRC associated with incoming byte array
	 * 
	 * @param b
	 * @return
	 */
	protected final CRC32 generateCRC(byte[] b) {
		CRC32 chk = new CRC32();
		chk.update(b, 0, b.length);
		return chk;
	}

	/**
	 * Information Storage Object for each Class loaded by this ClassLoader
	 * 
	 * @author schirripad@moravian.edu
	 *
	 */
	protected final class ClassData {
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

	/**
	 * The Replaceable annotation states what to do in the case of a duplicate class
	 * load. If the annotation is marked 'true' the previously loaded instance will
	 * be flushed and replaced with the newly loaded one. Otherwise the new one will
	 * be disposed of and the loader will return the previously loaded instance.
	 * 
	 * @author schirripad@moravian.edu
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.LOCAL_VARIABLE, ElementType.TYPE })
	public @interface Replaceable {
		public boolean replaceable() default true;
	}
}
