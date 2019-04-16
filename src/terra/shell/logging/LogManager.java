package terra.shell.logging;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

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
			new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), true);
	private static boolean writing = false;
	private static int debug = -1;

	private LogManager() {
	}

	/**
	 * Creates a Logger with the specified name.
	 * 
	 * @param name
	 *            The Logger's name.
	 * @return A new Logger.
	 */
	public static Logger getLogger(String name) {
		final Logger l = new Logger(name, loggers, false);
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
	public static Logger getLogger() {
		final Logger l = new Logger(loggers, true);
		logs.put(loggers, l);
		loggers++;
		return l;
	}

	/**
	 * Removes the logger from LogManagers list of loggers.
	 * 
	 * @param l
	 *            Logger to be removed.
	 */
	public static void removeLogger(Logger l) {
		logs.remove(l.getId());
		logNames.remove(l.getName());
	}

	public static boolean debug() {
		if (debug == -1) {
			log.log("Value for \"debug\" in launch conf not found, temporarily assigning the value of true");
			debug = 1;
		}
		if (debug == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static void setDebug(boolean debugValue) {
		if (debugValue)
			debug = 1;
		else {
			debug = 0;
		}
	}

	public static void write(String s) {
		if (!writing) {
			writing = true;
			out.print(s);
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
