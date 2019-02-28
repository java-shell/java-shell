package terra.shell.utils.fs;

import java.net.URI;
import java.net.URL;

import terra.shell.logging.LogManager;

public class File {
	private byte[] b;
	private String type;
	private String sf;
	private URL uf;
	private URI uif;
	private boolean dir;

	public File(String file, boolean dir) {
		sf = file;
		this.dir = dir;
		type = "SF";
	}

	public File(URL file, boolean dir) {
		uf = file;
		this.dir = dir;
		type = "UF";
	}

	public File(URI file, boolean dir) {
		uif = file;
		this.dir = dir;
		sf = file.toString();
		type = "UIF";
	}

	public boolean isDirectory() {
		return dir;
	}

	public File[] listFiles() {
		return ZipFileSystem.listFiles(this);
	}

	public String getName() {
		final String[] np = sf.split("/");
		if (np.length > 1)
			return np[np.length - 1];
		else
			return sf;
	}

	public File getParentFile() {
		final String[] np = sf.split("/");
		if (np.length > 0) {
			final File tmp = ZipFileSystem.getFile(sf.substring(0, sf.length()
					- (np[np.length - 1].length())));
			LogManager.out.println(sf.substring(0, sf.length()
					- (np[np.length - 1].length())));
			if (tmp != null)
				return tmp;
		}
		return this;
	}

	public String getPath() {
		return sf;
	}

	@Override
	public String toString() {
		return sf;
	}

	public URL getURL() {
		return uf;
	}

	public URI getURI() {
		return uif;
	}

	public boolean exists() {
		return ZipFileSystem.findFile(this);
	}

}
