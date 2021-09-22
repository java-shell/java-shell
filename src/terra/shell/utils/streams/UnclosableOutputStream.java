package terra.shell.utils.streams;

import java.io.IOException;
import java.io.OutputStream;

public class UnclosableOutputStream extends OutputStream {
	private final OutputStream out;

	public UnclosableOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public synchronized void flush() throws IOException {
		out.flush();
	}

	@Override
	public synchronized void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public synchronized void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void close() {
		return;
	}
}
