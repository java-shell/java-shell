package terra.shell.utils.streams;

import java.io.IOException;
import java.io.OutputStream;

public class DualOutputStream extends OutputStream {
	private OutputStream o1, o2;

	public DualOutputStream(OutputStream out1, OutputStream out2) throws IOException {
		if (out1 == null || out2 == null) {
			throw new IOException();
		}

		out1 = o1;
		out2 = o2;
	}

	@Override
	public void write(int b) throws IOException {
		o1.write(b);
		o2.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		o1.write(b, off, len);
		o2.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		o1.write(b);
		o2.write(b);
	}

	@Override
	public void flush() throws IOException {
		o1.flush();
		o2.flush();
	}

	@Override
	public void close() throws IOException {
		o1.close();
		o2.close();
	}
}
