package terra.shell.launch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import com.hk.lua.LuaLibrary;

import terra.shell.command.Command;
import terra.shell.command.Terminal;
import terra.shell.command.builtin.Chdir;
import terra.shell.command.builtin.Clear;
import terra.shell.command.builtin.ClusterManagement;
import terra.shell.command.builtin.CmdHistory;
import terra.shell.command.builtin.Copy;
import terra.shell.command.builtin.Dir;
import terra.shell.command.builtin.ExecLua;
import terra.shell.command.builtin.Export;
import terra.shell.command.builtin.GarbageCollection;
import terra.shell.command.builtin.HLM;
import terra.shell.command.builtin.Kill;
import terra.shell.command.builtin.LSCMD;
import terra.shell.command.builtin.ModuleList;
import terra.shell.command.builtin.Out;
import terra.shell.command.builtin.PWD;
import terra.shell.command.builtin.Print;
import terra.shell.command.builtin.ReloadCommands;
import terra.shell.command.builtin.RunLocal;
import terra.shell.command.builtin.UnloadModule;
import terra.shell.config.Configuration;
import terra.shell.emulation.concurrency.math.cluster.ConnectionManager;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.modules.ModuleManagement;
import terra.shell.utils.keys.DummyListener;
import terra.shell.utils.keys.DummyType;
import terra.shell.utils.keys.Event;
import terra.shell.utils.keys.EventInterpreter;
import terra.shell.utils.keys.EventType;
import terra.shell.utils.keys._Listener;
import terra.shell.utils.lua.LuaHookManager;
import terra.shell.utils.lua.builtin.JavashellLuaLibrary;
import terra.shell.utils.lua.exceptions.LuaLibraryLoadException;
import terra.shell.utils.streams.DualPrintStream;
import terra.shell.utils.streams.UnclosableInStream;
import terra.shell.utils.system.ByteClassLoader;
import terra.shell.utils.system.EventManager;
import terra.shell.utils.system.GeneralVariable;
import terra.shell.utils.system.JSHClassLoader;
import terra.shell.utils.system.Variables;
import terra.shell.utils.system.user.UserManagement;
import terra.shell.utils.system.user.UserManagement.UserValidation;

/**
 * Launches JSH, Launch Initialization is completed in Stages, and at each
 * individual stage, an InitEvent is fired signaling the completion of that
 * stage
 * 
 * @author dan
 * 
 */
public class Launch {
	private static Logger log = LogManager.getLogger("KERNEL");
	private static boolean doHib = false;
	private static boolean doTerm = true;
	private static JSHClassLoader loader;
	private static _Listener list;
	public static boolean modularizedCmds;
	private static File confD = new File("/config");
	private static Hashtable<String, Configuration> confs = new Hashtable<String, Configuration>();
	private static boolean keepergone = true;
	private static boolean isRoot = true;
	private static String fPrefix = "";
	private static Configuration launchConf;
	private static ConnectionManager clusterManager;
	private static String[] ignoredConfigExtensions = { ".jar", ".lib", ".so", ".dylib", ".png", ".jpg", ".jpeg",
			".gif", ".fst", ".dubm", ".mdl", ".carpa" };

	static {
		try {
			// Create ClassLoader for Commands and Modules
			loader = new JSHClassLoader(new URL[] { new URL("file:///modules") });
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Failed to create classloader, stopping...");
			throw new ExceptionInInitializerError(e);
		}
	}
	public static Hashtable<String, Command> cmds = new Hashtable<String, Command>();

	private Launch(String[] args) {
		// Check arguments
		if (args.length > 0) {
			if (args[0].equals("-n") || args[0].equals("--no-terminal")) {
				doTerm = false;
			}
		}
		// If the application is being run without SU privilege, modify paths to
		// accomadate permission level
		if (System.getProperty("user.name") != "root" && fPrefix.equals("")) {
			isRoot = false;
			fPrefix = System.getProperty("user.home") + "/JSH/";
			File tmp = new File(fPrefix);
			if (!tmp.exists()) {
				tmp.mkdir();
			}
			tmp = null;
		}
		confD = new File(fPrefix + "/config");

		// Begin Shell init
		log.log("Starting shell!");
		// Redirect standard I/O
		try {
			if (!isRoot) {
				File tmp = new File(fPrefix + "/tmp/");
				if (!tmp.exists()) {
					tmp.mkdir();
				}
				tmp = null;
			}
			PrintStream out = new PrintStream(new FileOutputStream(new File(fPrefix + "/tmp/out")));
			System.setOut(out);
			System.setIn(new UnclosableInStream(System.in));
			log.log("Output Redirected!");
			System.out.println("Test for output Redirection! This SHOULD NOT SHOW UP in terminal!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to redirect output! Canceling launch!");
			System.exit(-1);
		}

		// CODEAT Redirect I/O Streams

		// Send ERR to error log
		try {
			File f = new File(fPrefix + "/var/log/JSH");
			if (!f.exists()) {
				f.mkdirs();
			}
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss");
			PrintStream out = new PrintStream(
					new FileOutputStream(new File(fPrefix + "/var/log/JSH/" + dtf.format(LocalDateTime.now()))));
			DualPrintStream dps = new DualPrintStream(out, new PrintStream(new FileOutputStream(FileDescriptor.err)));
			System.setErr(dps);
			System.err.println("ERROR STREAM TEST");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to redirect error stream, Err Logging disabled");
		}
		// CODEAT Get Command locations
		File cmds = new File(fPrefix + "/modules/Commands");
		if (cmds.exists()) {
			modularizedCmds = true;
		}
		// CODEAT Load configurations
		loadConfigs();
		launchConf = getConfig("launch");
		if (launchConf == null) {
			launchConf = new Configuration(new File(confD.getPath(), "launch"));
			launchConf.setValue("loadEmbeddedCmds", "true");
			launchConf.setValue("launchTerminal", "true");
			launchConf.setValue("debug", "false");
		}
		if (launchConf.hasID("debug")) {
			log.log("Checking debug settings...");
			LogManager.setDebug(Boolean.parseBoolean((String) launchConf.getValue("debug")));
		}
		// Check for System configuration, if not found create one and add default
		// values
		if (getConfig("system") == null) {
			log.log(fPrefix + "config/system not found, creating!");
			final File sysFile = new File(fPrefix + "config/system");
			try {
				if (!sysFile.createNewFile()) {
					throw new IOException();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Configuration sysConf = new Configuration(sysFile);
			sysConf.setValue("moduleload", "true");
		}

		// CODEAT Set variables
		Variables.setVar(new GeneralVariable("log.filter", "true"));
		EventManager.invokeEvent(new InitEvent(InitStage.LOAD_MODULES));
		ModuleManagement mm = new ModuleManagement();
		mm.start();
		try {
			// list = new _Listener(); //Old code for setting up EVDEV hook listener,
			// deprecated but likely to be updated
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		UserManagement.init();

		// Register event types
		EventManager.registerEvType("Dummy");
		EventManager.registerListener(new DummyListener(), "Dummy");
		EventInterpreter.addType(new DummyType());
		EventManager.registerEvType("module-access");

		EventManager.invokeEvent(new InitEvent(InitStage.CLUSTER_INIT));

		// CODEAT Launch Cluster Management server
		log.log("Starting Cluster Management");
		try {
			clusterManager = new ConnectionManager();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// INIT Lua
		log.log("Initializing Lua management interface");
		try {
			LuaHookManager.registerHook("logger", JavashellLuaLibrary.LOGGER);
			LuaHookManager.registerHook("Socket", JavashellLuaLibrary.SOCKET);
			LuaHookManager.registerHook("events", JavashellLuaLibrary.EVENTS);
			LuaHookManager.registerHook("Timer", JavashellLuaLibrary.TIMER);
		} catch (LuaLibraryLoadException e) {
			e.printStackTrace();
			log.err("Exception in LuaLibrary loading");
			log.err("Lua interpretation may not function as expected!");
		}

		// Check terminal flags, if none are detected launch terminal, otherwise run as
		// daemon
		if (doTerm && Boolean.parseBoolean((String) launchConf.getValue("launchTerminal"))) {
			log.log("Accessing " + fPrefix + "/bin/jsh");
			// readBin, read commands from <prefix>/bin/JSH
			readBin();
			// Get commands from terra.shell.command

			// Check whether or not embedded commands are enabled
			if (Boolean.parseBoolean((String) launchConf.getValue("loadEmbeddedCmds"))) {
				log.log("Loading embedded commands");
				try {
					// Load embedded commands
					getLocalCommands();
				} catch (IOException e) {
					e.printStackTrace();
					log.err("Failed to access local embedded commands");
				}
			}

			// Init Terminal
			EventManager.invokeEvent(new InitEvent(InitStage.INIT_COMPLETION));
			Terminal t = new Terminal();
			log.log(t.getGOutputStream().toString());
			t.run();
		} else {
			log.debug("Terminal disabled in settings, invoking INIT_COMPLETION");
			EventManager.invokeEvent(new InitEvent(InitStage.INIT_COMPLETION));
		}
		// Keep Main thread alive
		while (keepergone)
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				log.log("Cannot Sleep!");
			}
	}

	// Load embedded commands located in terra.shell.command.builtin
	private void getLocalCommands() throws IOException {
		GarbageCollection gc = new GarbageCollection();
		cmds.put(gc.getName(), gc);
		log.log("Loaded embedded command: gc");
		Chdir cd = new Chdir();
		cmds.put(cd.getName(), cd);
		log.log("Loaded embedded command: cd");
		CmdHistory cmdhis = new CmdHistory();
		cmds.put(cmdhis.getName(), cmdhis);
		log.log("Loaded embedded command: cmdhis");
		// Compile com = new Compile();
		// cmds.put(com.getName(), com);
		log.log("Loaded embedded command: compile");
		Copy cp = new Copy();
		cmds.put(cp.getName(), cp);
		log.log("Loaded embedded command: cp");
		Dir dir = new Dir();
		cmds.put(dir.getName(), dir);
		cmds.put("ls", dir);
		log.log("Loaded embedded command: ls");
		Export ex = new Export();
		cmds.put(ex.getName(), ex);
		log.log("Loaded embedded command: export");
		Kill k = new Kill();
		cmds.put(k.getName(), k);
		log.log("Loaded embedded command: kt");
		ModuleList ml = new ModuleList();
		cmds.put(ml.getName(), ml);
		log.log("Loaded embedded command: ml");
		Out out = new Out();
		cmds.put(out.getName(), out);
		log.log("Loaded embedded command: " + out.getName());
		Print p = new Print();
		cmds.put(p.getName(), p);
		log.log("Loaded embedded command: " + p.getName());
		ReloadCommands rlcmd = new ReloadCommands();
		cmds.put(rlcmd.getName(), rlcmd);
		log.log("Loaded embedded command: " + rlcmd.getName());
		UnloadModule uml = new UnloadModule();
		cmds.put(uml.getName(), uml);
		log.log("Loaded embedded command: " + uml.getName());
		HLM hlm = new HLM();
		cmds.put(hlm.getName(), hlm);
		log.log("Loaded embedded command: " + hlm.getName());
		LSCMD lscmd = new LSCMD();
		cmds.put(lscmd.getName(), lscmd);
		log.log("Loaded embedded command: " + lscmd.getName());
		// RunTX rtx = new RunTX();
		// cmds.put(rtx.getName(), rtx);
		// log.log("Loaded embedded command: " + rtx.getName());
		ClusterManagement cm = new ClusterManagement();
		cmds.put(cm.getName(), cm);
		log.log("Loaded embedded command: " + cm.getName());
		RunLocal run = new RunLocal();
		cmds.put(run.getName(), run);
		log.log("Loaded embedded command: " + run.getName());
		PWD pwd = new PWD();
		cmds.put(pwd.getName(), pwd);
		log.log("Loaded embedded command: " + pwd.getName());
		Clear clear = new Clear();
		cmds.put(clear.getName(), clear);
		log.log("Loaded embedded command: " + clear.getName());
		ExecLua lua = new ExecLua();
		cmds.put(lua.getName(), lua);
		log.log("Loaded embedded command: " + lua.getName());
	}

	// Halt JSH
	/**
	 * Signal JSH main thread to return
	 */
	public static void stop() {
		keepergone = false;
	}

	// Returns configuration directory, <prefix>/config
	/**
	 * Returns the main confguration directory for JSH
	 * 
	 * @return Directory containing all configuration files
	 */
	public static File getConfD() {
		return confD;
	}

	// Load configurations from confD
	/**
	 * Load configuration files from the assigned configuration directory
	 */
	public static void loadConfigs() {
		log.log("Loading Configurations from " + fPrefix + "/config...");
		// Check if dir exists, if false create new dir
		if (!confD.exists()) {
			try {
				confD.mkdir();
			} catch (Exception e) {
				if (e instanceof SecurityException)
					log.log("Root Permissions Required");
				e.printStackTrace();
				return;
			}
		}
		loadConfigsRecursively(confD);
	}

	private static void loadConfigsRecursively(File parent) {
		// Check that confD is a dir, if not, do nothing
		if (!parent.isDirectory())
			return;
		// Load files from confD
		File[] cf = parent.listFiles();
		for (int i = 0; i < cf.length; i++) {
			// Create Configuration object wrappers for conf files
			if (!cf[i].isDirectory()) {
				if (cf[i].getName().lastIndexOf(".") - 1 > 0) {
					String fileExtension = cf[i].getName().substring(cf[i].getName().lastIndexOf(".") - 1);
					for (String badExtension : ignoredConfigExtensions) {
						if (fileExtension.equalsIgnoreCase(badExtension))
							continue;
					}
				}
				try {
					String confName = cf[i].getAbsolutePath().substring(cf[i].getAbsolutePath().indexOf("config/") + 7);
					confs.put(confName, new Configuration(cf[i]));
					log.debug("Added configuration: " + confName);
				} catch (Exception e) {
					log.err("Not a configuration file: " + cf[i].getName());
				}
			} else {
				loadConfigsRecursively(cf[i]);
			}
		}
	}

	// Get a config
	/**
	 * Get a configuration file
	 * 
	 * @param conf String key representing Configuration Name
	 * @return Assigned configuration, or NULL if not found
	 */
	public static Configuration getConfig(String conf) {
		if (confs.containsKey(conf)) {
			return confs.get(conf);
		}
		return null;
	}

	// Get all loaded commands
	/**
	 * Returns all loaded commands
	 * 
	 * @return All loaded commands
	 */
	public static Hashtable<String, Command> getCmds() {
		return cmds;
	}

	// Get the cluster ConnectionManager
	/**
	 * Returns the current ClusterManager object
	 * 
	 * @return
	 */
	public static ConnectionManager getConnectionMan() {
		return clusterManager;
	}

	// TODO enable remote repositories for commands
	// Load in commands
	/**
	 * Reads the source for Command Loading, be it a Repository, or local directory
	 */
	public static void readBin() {
		final File bin = new File(fPrefix + "/bin/jsh");
		// Check if dir exists
		if (!bin.exists()) {
			// If dir is non existent, tell the user and ask to use a remote repo
			log.log(fPrefix + "/bin/jsh is not existent!");
			while (true) {
				log.log("Use a remote repository? [Y/N]");
				log.print("> ");
				Scanner sc = new Scanner(System.in);
				String answer = sc.nextLine();
				if (answer.equalsIgnoreCase("Y")) {
					// TODO Load commands from repo, break from loop
					// Compare commands MD5 sums to expected
					// Define default repo location
					log.log("Repository Address:");
					log.print(">");
					String address = sc.nextLine();
					try {
						loadCmdsRepo(address);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

					break;

				} else if (answer.equalsIgnoreCase("N")) {
					break;
				}

			}
		} else {
			// If dir exists, load commands normally
			log.log("Loading Commands in " + fPrefix + "/bin/jsh...");
			final File[] binFiles = bin.listFiles();
			for (int i = 0; i < binFiles.length; i++) {
				final File tmp = binFiles[i];
				if (!tmp.getName().endsWith(".class")) {
					continue;
				}
				try {
					// TODO Check if a new classloader needs to be created each load cycle
					loader = new JSHClassLoader(new URL[] { new URL("file:///modules") });
				} catch (Exception e) {
					e.printStackTrace();
					log.log("Failed to create classloader, stopping");
					return;
				}
				try {
					final BufferedInputStream b = new BufferedInputStream(new FileInputStream(tmp));
					byte[] bytes = new byte[(int) tmp.length()];
					b.read(bytes);
					final Class<?> classtmp = loader.getClass(bytes);
					bytes = null;
					loader = null;
					Command cmd = null;
					try {
						cmd = (Command) classtmp.newInstance();
						registerCommand(cmd.getName(), cmd);
						log.log("Command loaded: " + cmd.getName());
						final ArrayList<String> al = cmd.getAliases();
						if (al != null) {
							for (int a = 0; a < al.size(); a++) {
								cmds.put(al.get(a), cmd);
								log.log("Command Loaded: " + al.get(a));
							}
						}
					} catch (Error e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
					log.log("Unable to load command: " + tmp.getAbsolutePath());
				}
			}
			log.log("All commands in " + fPrefix + "/bin/jsh loaded!");
		}

	}

	private static void loadCmdsRepo(String address) throws UnknownHostException, IOException {
		// Connect to repo
		Socket s = new Socket();
		// s.setReceiveBufferSize(512);
		s.connect(new InetSocketAddress(address, 2101), 100);
		Scanner sc = new Scanner(s.getInputStream());
		PrintStream out = new PrintStream(s.getOutputStream());
		// Ask for all CMDS
		out.println("CMDS");
		out.println("READY");
		while (true) {
			// Tell server ready to receive
			out.println("READY");
			// Receive header, parse
			String[] fileInf = sc.nextLine().split(":");
			// If header is bad, throw error
			if (fileInf.length < 3) {
				log.err("Bad command header from server, failed to load command");
				// If server says complete, break loop
				if (fileInf[0].equals("COMPLETE")) {
					break;
				}
			}

			// Begin loading file
			int fileSize = Integer.parseInt(fileInf[2]);
			byte[] file = new byte[fileSize];

			ArrayList<Byte> fileS = new ArrayList<Byte>();

			// log.log("File size: " + fileSize);

			out.println();

			// Get File from server
			int i;
			for (i = 0; i < (fileSize); i++) {
				file[i] = (byte) sc.nextInt();
				// log.debug(i + "");
			}
			sc.nextLine();

			// log.log("File received, size: " + i);

			// Instantiate command, add to Command list
			try {
				Command c = (Command) loader.getClass(file).newInstance();
				registerCommand(c.getName(), c);
				log.log("Command Loaded: " + c.getName());
				// Catch exceptions, and continue to next iteration
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				continue;
			} catch (InstantiationException e) {
				e.printStackTrace();
				continue;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			}

			String next = sc.nextLine();

			if (next.equals("COMPLETE")) {
				log.log("Completed loading commands from " + address);
				break;
			}
		}
		sc.close();
		s.close();
	}

	/**
	 * UNIMPLEMENTED
	 */
	public static void doHibernate() {
		doHib = true; // TODO Add handler to anticipate hibernate attack, (Add
						// cooldown timer)
	}

	/**
	 * UNIMPLEMENTED
	 */
	public static void noHibernate() {
		doHib = false;
	}

	/**
	 * UNIMPLEMENTED
	 * 
	 * @return
	 */
	public static boolean willHibernate() {
		return doHib;
	}

	public static void main(String[] args) {
		try {
			new Launch(args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Reload commands
	/**
	 * Reload the commands, utilizes the readBin() method
	 */
	public static void rlCmds() {
		cmds.clear();
		cmds = new Hashtable<String, Command>();
		loader = null;
		System.gc();
		System.runFinalization();
		System.gc();
		try {
			loader = new JSHClassLoader(new URL[] { new URL("file:///modules") });
		} catch (Exception e) {
			e.printStackTrace();
		}
		readBin();
	}

	public static ByteClassLoader getClassLoader() {
		return loader;
	}

	// Register a device, EVDEV
	/**
	 * Register an evdev event listener
	 * 
	 * @param inter
	 * @param dev
	 * @deprecated
	 */
	public static void regDev(String inter, String dev) {
		final EventType tmp = EventInterpreter.getType(inter);
		if (tmp != null) {
			EventInterpreter.registerType(dev, tmp);
			list.registerDevice(dev);
			log.log(dev + " registered with " + inter);
		} else {
			log.log("Failed to register device with interpreter! NULL!");
		}
	}

	/**
	 * Register a command if one is not already registered under that alias
	 * 
	 * @param cmd String used to call command
	 * @param c   Command to be executed
	 * @return True if the command is properly registered, False if the command has
	 *         been previously registered
	 */
	public static boolean registerCommand(String cmd, Command c) {
		if (cmds.containsKey(cmd)) {
			return false;
		}
		cmds.put(cmd, c);
		log.debug("Registered command " + cmd);
		return true;
	}

	// TODO Allow cmd replacement via User account lock
	public static boolean registerCommand(String cmd, Command c, UserValidation token) {
		cmds.put(cmd, c);
		return true;
	}

	/**
	 * Initialization Event wrapper
	 * 
	 * @author schirripad@moravian.edu
	 *
	 */
	public final class InitEvent implements Event {
		private final InitStage stage;

		@EventPriority(id = INIT_TYPE, value = -1)
		public InitEvent(InitStage stage) {
			this.stage = stage;
		}

		@Override
		public String getCreator() {
			return "INIT";
		}

		public InitStage getStage() {
			return stage;
		}

		@Override
		public Object[] getArgs() {
			Object[] args = { "INIT", stage };
			return args;
		}

	}

	/**
	 * Initialization Stages
	 * 
	 * @author schirripad@moravian.edu
	 *
	 */
	public static enum InitStage {
		EARLY_INIT, LOAD_CONFIG, LOAD_COMMANDS, LOAD_MODULES, CLUSTER_INIT, INIT_COMPLETION
	}
}
