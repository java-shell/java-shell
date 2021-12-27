package terra.shell.logging;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

import terra.shell.utils.streams.UnclosableOutputStream;
import terra.shell.utils.system.GeneralVariable;
import terra.shell.utils.system.Variables;

/**
 * Resource class used to manage all Loggers registered with it, including all
 * Loggers affiliated with the JProcess class. Generally LogManager should not
 * be used directly as it may interfere with outputs of other processes.
 * 
 * @author dan
 * 
 */
public class LogManager {
	private static int loggers;
	private static Hashtable<Integer, Logger> logs = new Hashtable<Integer, Logger>();
	private static ArrayList<String> logNames = new ArrayList<String>();
	private static Logger log = new Logger("LogManager", loggers, false);
	public final static PrintStream out = new PrintStream(
			new UnclosableOutputStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out))), true);
	private static boolean writing = false;
	private static int debug = -1;

	private LogManager() {
	}

	/**
	 * Creates a Logger with the specified name.
	 * 
	 * @param name The Logger's name.
	 * @return A new Logger.
	 */
	public synchronized static Logger getLogger(String name) {
		final Logger l = new Logger(name, loggers, true);
		logNames.add(name);
		logs.put(loggers, l);
		loggers++;
		return l;
	}

	/**
	 * Creates a new logger with a random UUID as a name, that write to default
	 * output.
	 * 
	 * @return A new logger.
	 */
	public synchronized static Logger getLogger() {
		final Logger l = new Logger(loggers, true);
		logs.put(loggers, l);
		loggers++;
		return l;
	}

	/**
	 * Removes the logger from LogManagers list of loggers.
	 * 
	 * @param l Logger to be removed.
	 */
	public synchronized static void removeLogger(Logger l) {
		logs.remove(l.getId());
		logNames.remove(l.getName());
	}

	/**
	 * Gets whether or not debugging is enabled
	 * 
	 * @return True if debugging is enabled
	 */
	public static boolean doDebug() {

		// Check for session assigned value using Variables
		String doDbg;
		if ((doDbg = Variables.getVarValue("debug")) != null) {
			if (Boolean.parseBoolean(doDbg)) {
				return true;
			} else
				return false;
		}

		// If a Variable is not found, use the default from the configuration file, or 1
		// if a default wasn't defined
		if (debug == -1) {
			log.log("Value for \"debug\" in launch conf not found, temporarily assigning the value of true");
			setDebug(true);
		}
		if (debug == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets whether or not debugging is enabled
	 * 
	 * @param debugValue True to enable debugging
	 */
	public synchronized static void setDebug(boolean debugValue) {
		log.log("Debugging set to " + debugValue);
		Variables.setVar(new GeneralVariable("debug", "" + debugValue));
		if (debugValue)
			debug = 1;
		else {
			debug = 0;
		}
	}

	public synchronized static void write(String s) {
		if (!writing) {
			writing = true;
			synchronized (out) {
				out.print(s);
				out.flush();
			}
			writing = false;
		} else {
			try {
				Thread.sleep(1);
				write(s);
			} catch (Exception e) {
				write(s);
			}
		}
	}

}
