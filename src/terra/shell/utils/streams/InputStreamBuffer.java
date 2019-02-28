package terra.shell.utils.streams;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamBuffer extends InputStream {
	private OutputStreamMonitorBuffer inb;

	public InputStreamBuffer(OutputStreamMonitorBuffer in) {
		inb = in;
	}

	@Override
	public int read() throws IOException {
		return inb.read();
	}

}
