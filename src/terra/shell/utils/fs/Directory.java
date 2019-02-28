package terra.shell.utils.fs;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class Directory extends File {

	public Directory(String file) {
		super(file, true);
	}

	public Directory(URL file) {
		super(file, true);
	}

	public Directory(URI file) {
		super(file, true);
	}
}
