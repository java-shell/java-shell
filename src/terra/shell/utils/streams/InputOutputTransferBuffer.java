package terra.shell.utils.streams;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class, writes info from InputStream directly to OutputStream,
 * asynchronously
 * 
 * @author schirripad@moravian.edu
 *
 */
public class InputOutputTransferBuffer {
	private boolean bw = true;

	public InputOutputTransferBuffer(final InputStream in, final OutputStream out) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					int b;
					while ((b = in.read()) != -1) {
						out.write(b);
					}
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				bw = false;
			}
		});
		t.setName("RA TransferBuffer");
		t.start();
	}

	public boolean isTransferring() {
		return bw;
	}

}
