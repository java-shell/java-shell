package terra.shell.emulation.concurrency.math.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.JProcess;
import terra.shell.utils.ReturnValue;
import terra.shell.utils.system.JSHClassLoader;

public final class ConnectionManager {

	private JSHClassLoader loader;

	private LinkedList<Node> nodes = new LinkedList<>();
	private Logger log = LogManager.getLogger("ClusterManager");
	private LocalServer ls;
	private String ipFormat = "192.168.1.X";
	private int port = 2100, activeProcessLimit = 20, passiveProcessLimit = 10, connectionLimit = 5, ipScanRangeMin = 1,
			ipScanRangeMax = 253, handshakeTimeout = 1000;

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
			conf.setValue("handshakeTimeout", 1000);
			// TODO Finish setting up Default values
		} else {
			// If conf exists, gather configuration options
			port = conf.getValueAsInt("port");
			ipFormat = (String) conf.getValue("ipformat");
			ipFormat.substring(0, ipFormat.length() - 2);
			activeProcessLimit = conf.getValueAsInt("activeProcessLimit");
			passiveProcessLimit = conf.getValueAsInt("passiveProcessLimit");
			connectionLimit = conf.getValueAsInt("connectionLimit");
			handshakeTimeout = conf.getValueAsInt("handshakeTimeout");

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
		try {
			loader = new JSHClassLoader(new URL[] { new URL("file:///modules") });
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
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
	 * @param p   JProcess to be sent
	 * @param out
	 * @param in
	 * @return True if process is succesfully queued, false otherwise
	 */
	public boolean queueProcess(JProcess p, OutputStream out, InputStream in) {
		// TODO Add node selection
		try {
			ls.sendProcess(nodes.getFirst().ip, p, ProcessPriority.MEDIUM, out, in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Ping Node at "ip"
	 * 
	 * @param ip IP to ping
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
		// Check for valid IP
		if (ip == null)
			return -1;
		log.debug("Sending PING request", 2);
		Socket s = new Socket();
		// Connect to Node host
		s.connect(new InetSocketAddress(ip, port), 1000);
		log.debug("Pinging: " + s.getInetAddress().getHostAddress());
		// Initialize Socket IO
		PrintStream out = new PrintStream(s.getOutputStream());
		Scanner sc = new Scanner(s.getInputStream());
		// Attempt to handshake with Node
		if (!completeHandshake(sc, out)) {
			// If handshake fails, return exit code
			sc.close();
			out.close();
			s.close();
			return -1;
		}
		// Log start time of ping
		long startTimeStamp = System.currentTimeMillis();
		// Send PING query
		out.println("PING");
		log.debug("Sent PING\nAwaiting Response..", 2);
		// Wait for server response, if server responds correctly...
		if (sc.nextLine().equals("CCSERVER")) {
			// Log ping finish time
			long pingTime = System.currentTimeMillis() - startTimeStamp;
			// Inform Terminal that PING was successful
			log.debug("Got CCSERVER response, ping is " + pingTime + "ms", 2);
			// Close Socket and IO
			s.close();
			sc.close();
			return pingTime;
		}
		// Cleanup
		s.close();
		sc.close();
		// Return exit code, Ping failed
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
		InetSocketAddress rolling;
		for (int i = ipScanRangeMin; i <= ipScanRangeMax; i++) {
			try {
				// Adjust IP to scan next device on network
				rolling = new InetSocketAddress(ip.replace("X", "" + i), port);
				// Attempt a connection to a possible Node
				s.connect(rolling, 10);
				// If connection completes, get Socket IO
				out = new PrintStream(s.getOutputStream());
				sc = new Scanner(s.getInputStream());
				// Ping the possible Node
				out.println("PING");
				String in = sc.nextLine();
				if (in.equals("CCSERVER")) {
					// This is indeed a server, client server handshake is complete
					Node n = new Node((Inet4Address) s.getInetAddress(), false);
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
	 * @param ip IP of Node
	 * @return True if the node was succesfully added, false otherwise
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public boolean addNode(Inet4Address ip) throws UnknownHostException, IOException {
		// Ping the server to check if it exists
		long ping = ping(ip);
		if (ping != -1) {
			// Add the server as Node object
			nodes.add(new Node(ip, ping));
			return true;
		}
		return false;
	}

	/**
	 * Check to see if a Node exists at a given IP
	 * 
	 * @param ip IP of node
	 * @return True if a node is found, false otherwise
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public boolean checkNodeAt(Inet4Address ip) throws UnknownHostException, IOException {
		return (ping(ip) != -1);
	}

	/**
	 * Complete handshake between a pair in order to sync the two
	 * 
	 * @param sc  Scanner of the remote socket
	 * @param out PrintStream of the remote socket
	 */
	private boolean completeHandshake(Scanner sc, PrintStream out) {
		log.debug("Starting Handshake");
		// Send query message to server
		out.println("READY");
		// Receive query response
		String s = sc.nextLine();
		log.debug("Handshake complete: " + s);
		return true;
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

		public LocalServer() {
			try {
				// Acquire a classloader for loaded objects in Active Processing, use the same
				// class scope as Commands and Modules to allow for interaction between both
				// loader = (JSHClassLoader)
				// this.getClass().getClassLoader().getSystemClassLoader();
				// loader = new JSHClassLoader(new URL[] { new URL("file:///modules") });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void startServer() throws IOException {
			log.log("Starting Local Server");
			Thread t = new Thread(new Runnable() {
				public void run() {
					while (!ss.isClosed()) {
						try {
							final Socket s = ss.accept();
							Thread t = new Thread(new Runnable() {
								public void run() {
									try {
										handleClient(s);
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
							t.setName(s.toString());
							t.start();
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

			log.debug("Got connection from " + s.getInetAddress());

			final Scanner sc = new Scanner(s.getInputStream());
			final PrintStream out = new PrintStream(s.getOutputStream());
			// Client handling
			// - Ping
			// - Passive Cluster
			// - Active Cluster
			completeHandshake(sc, out);

			String in = sc.nextLine();
			// Check if PING is sent
			if (in.equals("PING")) {
				log.debug("Got PING request", 2);
				out.println("CCSERVER");
				log.debug("Sending CCSERVER", 3);
				sc.close();
				out.close();
				s.close();
				return;
			} else {
				// Check connection limit, if exceeded refuse connection
				if (connections >= connectionLimit) {
					log.debug("Too Many connections, refusing");
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
					log.debug("Passive connection received");
					connections++;
					out.println("RECEIVEDAT");

					log.log("Realizing Quantized class...");
					// Load class from bytes
					// Save 'c' for a bit so GC doesn't remove the class from mem
					// DONE Add reception for dependency classes (Untested)
					int numDeps = Integer.parseInt(sc.nextLine());
					log.debug("Receiving Dependencies...");
					Class<?>[] classDeps = new Class<?>[numDeps];
					log.debug("Says there are " + numDeps + " dependencies"); // DONE sendProcess and reception not
																				// lining up
					for (int i = 0; i < numDeps; i++) {
						classDeps[i] = receiveClass(out, sc);
					}
					Class<?> c = receiveClass(out, sc); // CODEAT Receive Main class object for JProcess
					try {
						JProcess p = (JProcess) c.newInstance(); // DONE Class not being realized properly, need deps
					} catch (Exception e) {
						e.printStackTrace();
						out.println("FAIL:" + e.getMessage());
					}
					log.log("Realized " + c.getName());
					// Receive size of serialized process
					int size = sc.nextInt();
					// Receive process priority of process
					// FIXME Implement prioritizing
					int priority = sc.nextInt();
					// Allocate space for, and read in serialized process
					log.debug("Got process of size " + size + " and priority " + priority);
					byte[] ser = new byte[size];
					for (int i = 0; i < size; i++) {
						ser[i] = (byte) sc.nextInt();
					}
					log.debug("Done receiving process");
					// De-serialize and instantiate process
					JProcessRealizer objIn;
					try {
						objIn = new JProcessRealizer(new ByteArrayInputStream(ser));
						objIn.setClassLoader(loader);

						// TODO Try to instantiate objIn as JSHClassLoader
					} catch (IllegalArgumentException | SecurityException e) {
						e.printStackTrace();
						objIn = new JProcessRealizer(new ByteArrayInputStream(ser));
					}
					JProcess processObj;
					log.debug("Converting to Object");
					try {
						processObj = (JProcess) objIn.readObject(); // FIXME Not finding class that has been already
																	// loaded
						// successfully,
						// maybe out of scope? How do I set the scope of objIn to be
						// the same as the URLClassLoader loading the class into
						// modules:///??
					} catch (ClassNotFoundException e) {
						// If de-serialization fails, throw error to client, cleanup
						e.printStackTrace();
						out.println("FAIL:" + e.getMessage());
						s.close();
						objIn.close();
						ser = null;
						connections--;
						return;
					}
					log.debug("Object converted sucecssfully");
					objIn.close();
					// Convert from object to process
					log.debug("Creating JProcess");
					JProcess process;
					try {
						process = (JProcess) processObj;
						// Set process to use I/O from Socket
						process.reInitialize(classDeps);
						process.setOutputStream(out);
						process.redirectIn(s.getInputStream());
					} catch (ClassCastException e) {
						e.printStackTrace();
						out.println("FAIL:" + e.getMessage());
						s.close();
						return;
					}
					// Free up space, at this point 'c' is not needed
					c = null;
					final JProcess procMon = process;
					out.println("RUNNING");
					processes.add(procMon);
					// Create process monitor to remove from running processes when completed, and
					// cleanup
					Thread processMonitor = new Thread(new Runnable() { // FIXME Update procMonitor to not have to
																		// update every .5 seconds, instead include
																		// trigger inside of JProcess, use form similar
																		// to EventDriven to signal when a proc is done
						public void run() {
							if (!procMon.isRunning())
								procMon.run();
							// Check if process is active every .5 seconds
							terra.shell.utils.system.ReturnType ret = terra.shell.utils.system.ReturnType.VOID;
							if (procMon.getClass().isAnnotationPresent(JProcess.ReturnType.class)) {
								ret = procMon.getClass().getAnnotation(JProcess.ReturnType.class).getReturnType();
							}
							if (ret == terra.shell.utils.system.ReturnType.VOID
									|| ret == terra.shell.utils.system.ReturnType.ASYNCHRONOUS) {
								// Disconnect IO streams to save bandwidth
								// TODO
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
							// Process Return
							if (ret == terra.shell.utils.system.ReturnType.SYNCHRONOUS) {
								// TODO Return
								if (procMon.getReturn() == null) {
									// TODO Deal with no return
								} else {
									Object[] returnValue = procMon.getReturn().getReturnValue();
									ReturnObjectWrapper[] retObj = new ReturnObjectWrapper[returnValue.length];
									out.println("ret:" + returnValue.length);
									log.debug("Wrapping " + returnValue.length + " returns");
									for (int i = 0; i < retObj.length; i++) {
										retObj[i] = new ReturnObjectWrapper(returnValue[i]);
									}
									log.debug("Wrapped returns...");
								}
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
							} else if (ret == terra.shell.utils.system.ReturnType.ASYNCHRONOUS) {
								// TODO Schedule a return
								ReturnValue rv = procMon.getReturn();
								try {
									sendReturn(procMon.getOrigin(), rv);
								} catch (Exception e) {
									e.printStackTrace();
									log.err("UNABLE TO SEND RETURN TO ORIGIN: " + procMon.getOrigin().toString());
									for (int ie = 0; ie < 5; ie++)
										log.err("SERIOUS ERROR");
								}
							}
							// When process is no longer active, tell client that process is done
							out.println("PROCESSCOMPLETION:SUCCESS");
							// Remove process from list of processess
							processes.remove(procMon);

						}
					});
					processMonitor.setName("ProcessMonitor:" + process.getName());
					process = null;
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
				if (in.equals("RET")) {
					// TODO Handle ReturnValue reception
					// Scanner sc
					// PrintStream out
					out.println("RECEIVEDAT");
					Class<?> rvClass = receiveClass(out, sc);
					try {
						ReturnValue rv = (ReturnValue) rvClass.newInstance();
						Object[] value = rv.getReturnValue();
						// TODO Determine where to place returnvalue
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			sc.close();
		}

		@SuppressWarnings("resource")
		private boolean sendReturn(Inet4Address ip, ReturnValue rv) throws IOException {
			log.debug("Sending return to : " + ip.toString());
			Socket s = new Socket(ip.getHostAddress(), port);
			Scanner sc = new Scanner(s.getInputStream());
			PrintStream out = new PrintStream(s.getOutputStream());

			completeHandshake(sc, out);

			out.println("RET");
			log.debug("Sent RET");

			final String nextLine = sc.nextLine();
			if (!nextLine.equals("RECEIVEDAT")) {
				log.debug("Server incorrectly responded, expected\"RECEIVEDAT\" got \"" + nextLine + "\"");
				s.close();
				sc.close();
				out.close();
				return false;
			}

			sendClass(rv.getClass(), out, sc);
			// TODO Send return
			return true;
		}

		// Add I/O redirection
		@SuppressWarnings("resource")
		private boolean sendProcess(Inet4Address ip, JProcess p, ProcessPriority priority, OutputStream out,
				InputStream in) throws UnknownHostException, IOException {
			// Setup server connection
			log.debug("Sending process: " + p.getName() + ", to " + ip);
			Socket s = new Socket(ip.getHostAddress(), port);
			Scanner sc = new Scanner(s.getInputStream());
			PrintStream pOut = new PrintStream(s.getOutputStream());

			completeHandshake(sc, pOut);

			// Send type of process to server
			pOut.println("PASSIVE");
			log.debug("Sent PASSIVE");
			// If Server doesn't respond correctly, close and cleanup
			final String nextLine = sc.nextLine();
			if (!nextLine.equals("RECEIVEDAT")) {
				log.debug("Server incorrectly responded, expected \"RECEIVEDAT\" got \"" + nextLine + "\"");
				s.close();
				sc.close();
				return false;
			}

			p.prepSerialization();
			// Send class as stream to other JSH, load class in at other JSH and then
			// allow for this one to spawn
			log.debug("Sending class by name of: " + p.getClass().getName() + " : "
					+ p.getClass().getPackage().getName());
			log.debug("Quantizing Process...");
			String classPath = p.getClass().getName().replace('.', '/') + ".class";
			// Get classes actual bytes in order to reinitialize correctly on host
			InputStream cin = p.getClass().getClassLoader().getResourceAsStream(classPath);
			LinkedList<Byte> cBytes = new LinkedList<Byte>();
			int b;
			while ((b = cin.read()) != -1) {
				cBytes.add((byte) b);
			}
			log.debug("Quantization Complete");
			// Serialize process
			log.debug("Serializing Process: " + p.getClass().toString());
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(bout);
			objOut.writeObject(p);
			objOut.flush();
			objOut.close();
			objOut = null;
			log.debug("Serialization complete!");
			log.debug("Running dependency check..."); // DONE Annotations not returning correct value??
			// CODEAT Check for dependencies
			if (p.getClass().isAnnotationPresent(JProcess.Depends.class)) {
				Class<?>[] deps = p.getClass().getAnnotation(JProcess.Depends.class).dependencies();
				pOut.println(deps.length);
				for (Class<?> d : deps)
					if (!sendClass(d, pOut, sc)) {
						log.err("FAILED TO SEND DEPENDENCY CLASS: " + d.getName());
					}
			} else
				pOut.println(0);

			// TODO Check if annotations are sent to node as well, need to check for
			// returntype at non-source node

			log.debug("Finished Dependency Check");

			log.debug("Sending size of quantized data: " + cBytes.size());
			pOut.println(cBytes.size());
			log.debug("Sending class name: " + p.getName());
			pOut.println(p.getClass().getName());

			log.debug("Sending package name: " + p.getClass().getPackage().getName());
			pOut.println(p.getClass().getPackage().getName());

			log.debug("Sending quantized data...");
			for (Byte by : cBytes) {
				pOut.println((int) by);
			}

			// Turn serialized process into byte[]
			byte[] dat = bout.toByteArray();
			// Tell server [] length, and process priority
			log.debug("Sending size of data " + dat.length);
			pOut.println(dat.length);
			pOut.println(priority.asInt());
			// Send serialized process to server
			for (int i = 0; i < dat.length; i++) {
				pOut.println((int) dat[i]);
			}
			// Wait for a response
			String response = sc.nextLine();
			// If response is FAILURE, close, cleanup
			if (response.startsWith("FAIL")) {
				log.err(response);
				s.close();
				sc.close();
				return false;
			}
			log.debug("Process sent");
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

		public Class<?> receiveClass(PrintStream out, Scanner sc) {
			log.debug("Recieving Quantized class size...");
			// Get Class size
			int cSize = Integer.parseInt(sc.nextLine());
			log.debug("Got size " + cSize);
			log.debug("Recieving Quantized class data...");
			log.debug("Getting name...");

			String cName = sc.nextLine();
			log.debug(cName);
			String cNamePart[] = cName.split("\\.");
			log.debug(cNamePart.toString());
			cName = cNamePart[cNamePart.length - 1];
			log.debug("Got name: " + cName);

			log.debug("Getting package...");
			String packageName = sc.nextLine();
			log.debug("Got package: " + packageName);

			loader.setPackageAssertionStatus(packageName, true);

			byte[] cBytes = new byte[cSize];
			// Receive Class
			for (int i = 0; i < cSize; i++) {
				cBytes[i] = (byte) sc.nextInt();
			}
			sc.nextLine();
			log.log("Realizing Quantized class...");
			// Load class from bytes
			// Save 'c' for a bit so GC doesn't remove the class from mem

			// FIXME Possibly not loading class into correct package hierarchy??
			Class<?> c = loader.getClass(packageName + "." + cName, cBytes);
			log.debug("Loaded class: " + c.getName()); // Package returning NULL
			loader.setPackageAssertionStatus("", false);
			return c;
		}

		public boolean sendClass(Class<?> c, PrintStream out, Scanner sc) throws IOException {
			String classPath = c.getName().replace('/', '.');
			String packageName = c.getPackage().getName();
			log.debug("Sending class by name of: " + c.getCanonicalName() + " : " + c.getPackage().getName());
			InputStream cin = c.getClassLoader().getResourceAsStream(c.getName().replace('.', '/') + ".class");
			LinkedList<Byte> dat = new LinkedList<Byte>();
			int b;
			while ((b = cin.read()) != -1) {
				dat.add((byte) b);
			}
			log.debug("Sending size of data " + dat.size());
			out.println(dat.size());

			log.debug("Sending class name " + classPath);
			out.println(classPath);

			log.debug("Sending package name " + packageName);
			out.println(packageName);

			// Send serialized process to server
			for (int i = 0; i < dat.size(); i++) {
				out.println((int) dat.get(i));
			}
			log.debug("Process sent");
			return true;
		}
	}

	private class Node implements Comparable<Node> {
		private long ping;
		private Inet4Address ip;
		private Socket s;

		@SuppressWarnings("unused")
		public Node(Inet4Address ip) throws UnknownHostException, IOException {
			this.ip = ip;
			s = new Socket(ip.getHostAddress(), port);
			updatePing();
		}

		public Node(Inet4Address ip, boolean doPing) throws UnknownHostException, IOException {
			this.ip = ip;
			s = new Socket(ip.getHostAddress(), port);
			if (doPing)
				updatePing();
		}

		public Node(Inet4Address ip, long ping) throws UnknownHostException, IOException {
			this.ip = ip;
			s = new Socket(ip.getHostAddress(), port);
			this.ping = ping;
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
