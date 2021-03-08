package terra.shell.utils.streams;

import java.io.IOException;
import java.io.InputStream;

public class StreamSink {
	private InputStream in;

	public StreamSink(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	public boolean hasNext() throws IOException {
		return (in.available() == 0 ? false : true);
	}

}
