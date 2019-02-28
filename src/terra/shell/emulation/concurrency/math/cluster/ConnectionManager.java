package terra.shell.emulation.concurrency.math.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.JProcess;

public final class ConnectionManager {

	private LinkedList<Node> nodes = new LinkedList<>();
	private Logger log = LogManager.getLogger("ClusterManager");
	private LocalServer ls;
	private String ipFormat = "192.168.1.1.";
	private int port = 2100, activeProcessLimit = 20, passiveProcessLimit = 10, connectionLimit = 5, ipScanRangeMin = 1,
			ipScanRangeMax = 253;

	// TODO
	// Finish setting up defaults
	// Finish LocalServer clientHandler
	// Create service scan *
	// TODO Add detailed description of ConnectionManager
	/**
	 * Configure ConnectionManager, INIT LocalServer, run serviceScan
	 */
	public ConnectionManager() {
		log.log("Starting Connection Manager...");
		// Try to open conf file
		Configuration conf = Launch.getConfig("ClusterService");
		// If conf nonexistent, create one with defaults
		log.log("Loading  config...");
		if (conf == null) {
			log.log("No existing config, generating defaults...");
			conf = new Configuration(new File(Launch.getConfD() + "/ClusterService"));
			conf.setValue("port", 2100);
			conf.setValue("ipformat", "192.168.1.X");
			conf.setValue("passiveProcessLimit", 10);
			conf.setValue("activeProcessLimit", 20);
			conf.setValue("connectionLimit", 5);
			conf.setValue("ipScanRangeMin", 1);
			conf.setValue("ipScanRangeMax", 253);
			// TODO Finish setting up Default values
		} else {
			// If conf exists, gather configuration options
			port = conf.getValueAsInt("port");
			ipFormat = (String) conf.getValue("ipformat");
			ipFormat.substring(0, ipFormat.length() - 2);
			activeProcessLimit = conf.getValueAsInt("activeProcessLimit");
			passiveProcessLimit = conf.getValueAsInt("passiveProcessLimit");
			connectionLimit = conf.getValueAsInt("connectionLimit");

			int ipScanMin = conf.getValueAsInt("ipScanRangeMin");
			int ipScanMax = conf.getValueAsInt("ipScanRangeMax");
			// Check if Minimum is higher than Maximum, if so use defaults
			if (!(ipScanMin > ipScanMax)) {
				ipScanRangeMin = ipScanMin;
				ipScanRangeMax = ipScanMax;
			}
		}
		log.log("Successfully loaded config");
		// Start LocalServer
		ls = new LocalServer();
		if (ls.ss == null) {
			log.err("Failed to start server on localhost:" + port);
			log.log("Stopping Connection Manager...");
			return;
		}
		ls.start();

		// Scan for other servers on the LAN
		try {
			serviceScan();
		} catch (Exception e) {
			e.printStackTrace();
			log.err("Failed to run service scan: " + e.getMessage());
		}
	}

	/**
	 * Queue a JProcess to be serialized and sent to another Node for processing
	 * 
	 * @param p
	 *            JProcess to be sent
	 * @param out
	 * @param in
	 * @return True if process is succesfully queued, false otherwise
	 */
	public boolean queueProcess(JProcess p, OutputStream out, InputStream in) {

		// TODO Add node selection
		try {
			ls.sendProcess(nodes.getFirst().ip, p, ProcessPriority.MEDIUM, out, in);
		} catch (Exception e) {

		}
		return true;
	}

	/**
	 * Ping Node at "ip"
	 * 
	 * @param ip
	 *            IP to ping
	 * @return Latency, in MS
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public long ping(Inet4Address ip) throws UnknownHostException, IOException {
		return ping(ip.getHostAddress());
	}

	/**
	 * Ping node at "ip"
	 * 
	 * @param ip
	 * 
	 * @return Latency, in MS
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public long ping(String ip) throws UnknownHostException, IOException {
		if (ip == null)
			return -1;
		Socket s = new Socket();
		s.connect(new InetSocketAddress(ip, port), 1000);
		log.log("Pinging: " + s.getInetAddress().getHostAddress());
		PrintStream out = new PrintStream(s.getOutputStream());
		Scanner sc = new Scanner(s.getInputStream());

		long startTimeStamp = System.currentTimeMillis();
		out.println("PING");
		if (sc.nextLine().equals("CCSERVER")) {
			s.close();
			sc.close();
			return System.currentTimeMillis() - startTimeStamp;
		}
		s.close();
		sc.close();
		return -1;
	}

	/**
	 * Scan for Node on the LAN
	 * 
	 * @throws IOException
	 */
	public void serviceScan() throws IOException {
		log.log("Running service scan...");

		Socket s = new Socket();
		String ip = ipFormat;
		PrintStream out;
		Scanner sc;
		// Scan all IP's from range 1-253
		for (int i = ipScanRangeMin; i <= ipScanRangeMax; i++) {
			try {
				s.connect(new InetSocketAddress(ip + i, port), 10);
				out = new PrintStream(s.getOutputStream());
				sc = new Scanner(s.getInputStream());

				out.println("PING");
				String in = sc.nextLine();
				if (in.equals("CCSERVER")) {
					// This is indeed a server, client server handshake is complete
					Node n = new Node((Inet4Address) s.getInetAddress());
					nodes.add(n);
				}
			} catch (Exception e) {
			}
		}
		s.close();
		log.log("Found: " + nodes.size() + " nodes");
	}

	/**
	 * Add a Node to use for Clustering
	 * 
	 * @param ip
	 *            IP of Node
	 * @return True if the node was succesfully added, false otherwise
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public boolean addNode(Inet4Address ip) throws UnknownHostException, IOException {
		if (checkNodeAt(ip)) {
			nodes.add(new Node(ip));
			return true;
		}
		return false;
	}

	/**
	 * Check to see if a Node exists at a given IP
	 * 
	 * @param ip
	 *            IP of node
	 * @return True if a node is found, false otherwise
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public boolean checkNodeAt(Inet4Address ip) throws UnknownHostException, IOException {
		return (ping(ip) == 0);
	}

	/**
	 * Local Cluster Node server implementation
	 * 
	 * @author schirripad@moravian.edu
	 *
	 */
	private class LocalServer extends JProcess {
		private static final long serialVersionUID = 1414706641471181855L;
		private final ServerSocket ss = createServer();
		private int connections = 0;
		private ArrayList<JProcess> processes = new ArrayList<JProcess>();

		public void startServer() throws IOException {
			log.log("Starting Local Server");
			Thread t = new Thread(new Runnable() {
				public void run() {
					while (!ss.isClosed()) {
						try {
							Socket s = ss.accept();
							handleClient(s);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});

			t.setName("ClusterServer");
			t.start();
		}

		private ServerSocket createServer() {
			try {
				return new ServerSocket(port);
			} catch (IOException e) {
				return null;
			}
		}

		// CODEAT Client Handling
		private void handleClient(final Socket s) throws IOException {
			final Scanner sc = new Scanner(s.getInputStream());
			final PrintStream out = new PrintStream(s.getOutputStream());
			// TODO Add client handling
			// - Ping
			// - Passive Cluster
			// - Active Cluster
			String in = sc.nextLine();
			// Check if PING is sent
			if (in.equals("PING")) {
				out.println("CCSERVER");
				sc.close();
				out.close();
				s.close();
				return;
			} else {
				// Check connection limit, if exceeded refuse connection
				if (connections >= connectionLimit) {
					s.close();
					sc.close();
					return;
				}
				if (in.equals("PASSIVE")) {
					// Passive connection handshake completed, process passive data transference
					// accordingly
					// - Receive serialized data
					// - De-serialize
					// - Execute
					// - Return
					connections++;
					out.println("RECEIVEDAT");
					// Receive size of serialized process
					int size = sc.nextInt();
					// Receive process priority of process
					// TODO Implement prioritizing
					int priority = sc.nextInt();
					// Allocate space for, and read in serialized process
					byte[] ser = new byte[size];
					for (int i = 0; i < size; i++) {
						ser[i] = sc.nextByte();
					}
					// De-serialize and instantiate process
					ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(ser));
					Object processObj;
					try {
						processObj = objIn.readObject();
					} catch (ClassNotFoundException e) {
						// If de-serialization fails, throw error to client, cleanup
						out.println("FAIL:" + e.getMessage());
						s.close();
						objIn.close();
						ser = null;
						connections--;
						return;
					}
					objIn.close();
					// Convert from object to process
					JProcess process;
					try {
						process = (JProcess) processObj;
						// Set process to use I/O from Socket
						process.reInitialize();
						process.setOutputStream(out);
						process.redirectIn(s.getInputStream());
					} catch (ClassCastException e) {
						out.println("FAIL:" + e.getMessage());
						s.close();
						return;
					}
					final JProcess procMon = process;
					process = null;
					out.println("RUNNING");
					processes.add(procMon);
					// Create process monitor to remove from running processes when completed, and
					// cleanup
					Thread processMonitor = new Thread(new Runnable() {
						public void run() {
							// Check if process is active every .5 seconds
							while (procMon.isRunning()) {
								try {
									Thread.sleep(500);
								} catch (Exception e) {
									// Alert client process failed to run, and cleanup
									e.printStackTrace();
									procMon.halt();
									processes.remove(procMon);
									out.println("PROCESSCOMPLETION:FAILURE");
									try {
										s.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									connections--;
									return;
								}
							}
							// When process is no longer active, tell client that process is done
							out.println("PROCESSCOMPLETION:SUCCESS");
							// Remove process from list of processess
							processes.remove(procMon);
							try {
								// Cleanup
								out.flush();
								out.close();
								sc.close();
								s.close();
								connections--;
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
					processMonitor.setName("ProcessMonitor:" + process.getName() + "-" + process.getUUID().toString());
					process.run(false);
					processMonitor.run();
				}
				if (in.equals("ACTIVE")) {
					// Active connection handshake completed, process active data transference
					// accordingly
					// - Receive basic instruction
					// - Receive arguments
					// - Execute methods
					// - Return
					connections++;
					out.println("RECEIVE");

					// Receive data, interpret whether it is Math based, or method based

				}
			}
			sc.close();

		}

		// Add I/O redirection
		private boolean sendProcess(Inet4Address ip, JProcess p, ProcessPriority priority, OutputStream out,
				InputStream in) throws UnknownHostException, IOException {
			// Setup server connection
			Socket s = new Socket(ip.getHostAddress(), port);
			Scanner sc = new Scanner(s.getInputStream());
			PrintStream pOut = new PrintStream(s.getOutputStream());

			// Send type of process to server
			pOut.println("PASSIVE");
			// If Server doesn't respond correctly, close and cleanup
			if (!sc.nextLine().equals("RECEIVEDAT")) {
				// TODO add failure reason
				s.close();
				sc.close();
				return false;
			}

			// Serialize process
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(bout);
			objOut.writeObject(p);
			objOut.flush();
			objOut.close();
			objOut = null;

			// Turn serialized process into byte[]
			byte[] dat = bout.toByteArray();
			// Tell server [] length, and process priority
			pOut.println(dat.length + ":" + priority.asInt());
			// Send serialized process to server
			pOut.write(dat);
			// Wait for a response
			String response = sc.nextLine();
			// If response is FAILURE, close, cleanup
			if (response.startsWith("FAIL")) {
				log.err(response);
				s.close();
				sc.close();
				return false;
			}
			// TODO Add reception of process output
			// Use I/O redirect buffers? Need to read Socket input in order to determine
			// process completion
			while (sc.hasNextLine()) {
				String next = sc.nextLine();
				if (next.startsWith("PROCESSCOMPLETION")) {
					// Close socket, cleanup
					s.close();
					out.close();
					sc.close();
					return true;
				}

			}
			sc.close();
			return true;
		}

		@Override
		public String getName() {
			return "ClusterDaemon";
		}

		@Override
		public boolean start() {
			try {
				startServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	private class Node implements Comparable<Node> {
		private long ping;
		private Inet4Address ip;
		private Socket s;

		public Node(Inet4Address ip) throws UnknownHostException, IOException {
			this.ip = ip;
			s = new Socket(ip.getHostAddress(), port);
			updatePing();
		}

		@SuppressWarnings("unused")
		public Node(Socket s) throws IOException {
			this.s = s;
			updatePing();
		}

		@SuppressWarnings("unused")
		public Node(Inet4Address ip, int ping) throws UnknownHostException, IOException {
			this.ping = ping;
			this.ip = ip;
			s = new Socket(ip.getHostAddress(), port);
		}

		public long updatePing() throws IOException {
			ping = ping(ip);
			return ping;
		}

		@Override
		public int compareTo(Node n) {
			if (ping > n.ping)
				return -1;
			if (ping < n.ping)
				return 1;
			return 0;
		}

		@SuppressWarnings("unused")
		public long getPing() {
			return ping;
		}

		@SuppressWarnings("unused")
		public Inet4Address getIPv4() {
			return ip;
		}
	}
}
