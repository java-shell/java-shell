package terra.shell.utils.system;

import java.net.URL;

public class JSHClassLoader extends ByteClassLoader {

	public JSHClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public JSHClassLoader(URL[] urls) {
		super(urls);
	}

}
