package terra.shell.emulation.concurrency.math.cluster;

import java.net.InetAddress;

public interface ConnectionListener {

	public void connectionEvent(InetAddress ip);

	public void disconnectionEvent(InetAddress ip);
}
