package terra.shell.emulation.concurrency.math.cluster.data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.utils.JProcess;

/**
 * DataDispersionInterface acts as a solution for passing small amounts of data
 * (512 bytes) between nodes.
 * 
 * @author schirripad@moravian.edu
 *
 */
public class DataDispersionInterface extends JProcess {
	private static final long serialVersionUID = -7165179139963776006L;
	private String mCastAddress;
	private int port, dscp = 0;
	private NetworkInterface nic;
	private HashSet<DataDispersionListener> listeners;
	private DatagramSocket dSock;

	/**
	 * Create a new DataDispersionInterface which sends/receives data on a specific
	 * address/port, and network interface. If the network interface is left null,
	 * all interfaces will be used.
	 * 
	 * @param mCastAddress Multicast Group to join
	 * @param port         Multicast port
	 * @param nic          Network Interface to use, or null for all
	 * @throws SocketException
	 */
	public DataDispersionInterface(String mCastAddress, int port, NetworkInterface nic) throws SocketException {
		this.mCastAddress = mCastAddress;
		this.port = port;
		listeners = new HashSet<DataDispersionListener>();
		this.dSock = new DatagramSocket();
	}

	/**
	 * Set the Differentiated Services Code Point (DSCP) value for packets sent from
	 * this DataDispersionInterface
	 * 
	 * @param value DSCP Value to assign to packets sent
	 */
	public void setDSCPValue(int value) {
		if (value < 0) {
			value = 0;
		}
		if (value > 63) {
			value = 63;
		}
		this.dscp = value;
	}

	@Override
	public String getName() {
		return "DataDispersionInterface-" + mCastAddress;
	}

	/**
	 * Register a DataDispersionListener which will be triggered when data is
	 * received
	 * 
	 * @param l DataDispersionListener to register
	 */
	public void registerListener(DataDispersionListener l) {
		listeners.add(l);
	}

	/**
	 * Unregister a DataDispersionListener
	 * 
	 * @param l DataDispersionListener to unregister
	 */
	public void unregisterListener(DataDispersionListener l) {
		listeners.remove(l);
	}

	@Override
	public boolean start() {
		boolean failed = true;
		int retryCount = 0;

		try {
			InetSocketAddress multiAddress = new InetSocketAddress(mCastAddress, port);
			final MulticastSocket mCast = new MulticastSocket(port);
			mCast.setTrafficClass(dscp);
			getLogger().debug("Attempting to register DataDispersionInterface - " + mCastAddress + ":" + port);
			if (nic == null) {
				while (failed) {
					try {
						mCast.joinGroup(multiAddress, nic);
					} catch (Exception e) {
						if (e.getMessage().equals("Already a member of group")) {
							failed = false;
							break;
						}

						if (retryCount == 20) {
							e.printStackTrace();
							getLogger().err("Failed to register DispersionInterface:" + e.getMessage());
							return false;
						}
						try {
							getLogger().debug("Registration failure, trying again in 1 second...  - " + e.getMessage());
							Thread.sleep(1000);
							retryCount++;
						} catch (Exception e1) {
							e1.printStackTrace();
							return false;
						}
					}
				}

			} else {
				mCast.joinGroup(multiAddress, nic);
			}
			while (!mCast.isClosed()) {
				byte[] buf = new byte[512];
				DatagramPacket packet = new DatagramPacket(buf, 512);
				mCast.receive(packet);
				distributeToListeners((InetAddress) packet.getAddress(), packet.getData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Package and send the data. If the InetAddress within the BasicImmutableData
	 * object does not match that associated with this DataDispersionInterface, or
	 * if the length of data stored is not equal to 512 bytes, an IOException will
	 * be thrown.
	 * 
	 * @param data Data to send, 512 bytes
	 * @throws IOException
	 */
	public void disperseData(BasicImmutableData<byte[]> data) throws IOException {
		if (data.data().length != 512) {
			throw new IOException("Bad data size, got " + data.data().length + " should be 512");
		}
		InetAddress inetMCastAddress = InetAddress.getByName(mCastAddress);
		if (data.ip().getAddress().toString().equals(inetMCastAddress.getAddress().toString())) {
			throw new IOException("Wrong address for DataDispersionInterface, got " + data.ip().getHostAddress()
					+ " expected " + inetMCastAddress.getHostAddress());
		}
		DatagramPacket packet = new DatagramPacket(data.data(), 512, InetAddress.getByName(mCastAddress), port);
		dSock.setTrafficClass(dscp);
		dSock.send(packet);
	}

	/**
	 * Distributes received information to registered listeners
	 * 
	 * @param address IP Address of sender
	 * @param data    Data received from sender
	 */
	protected void distributeToListeners(InetAddress address, byte[] data) {
		for (DataDispersionListener l : listeners) {
			l.dataReceived(new BasicImmutableData<byte[]>(address, data));
		}
	}

}
