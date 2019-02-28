package terra.shell.utils.streams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class OutputStreamMonitorBuffer extends OutputStream {
	private ArrayList<Integer> dat = new ArrayList<Integer>();
	private boolean store = false;
	private int t = -1;

	@Override
	public void write(int b) throws IOException {
		dat.add(b);
	}

	public void store(boolean b) {
		store = b;

	}

	public int read() {
		if (dat.size() < t) {
			return dat.get(dat.size() - 1);
		}
		t++;
		if (store) {
			return dat.get(t);
		}
		int tmp = dat.get(t);
		dat.remove(t);
		return tmp;
	}

}
