package terra.shell.utils.streams;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource class, this is quite literally an unclosable stream!
 * 
 * @author dan
 * 
 */
public class UnclosableInStream extends InputStream {
	private InputStream inputStream;

	public UnclosableInStream(InputStream in) {
		this.inputStream = in;
	}

	@Override
	public int read() throws IOException {
		synchronized (this) {
			return inputStream.read();
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		synchronized (this) {
			return inputStream.read(b);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (this) {
			return inputStream.read(b, off, len);
		}
	}

	@Override
	public long skip(long n) throws IOException {
		synchronized (this) {
			return inputStream.skip(n);
		}
	}

	@Override
	public int available() throws IOException {
		synchronized (this) {
			return inputStream.available();
		}
	}

	@Override
	public synchronized void mark(int readlimit) {
		inputStream.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		inputStream.reset();
	}

	@Override
	public boolean markSupported() {
		return inputStream.markSupported();
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

}
