package terra.shell.logging;

import java.io.OutputStream;
import java.io.PrintStream;

import terra.shell.utils.system.Variables;

/**
 * Resource class which provides writing to the default Output, or a redirected
 * OutputStream. Loggers should be obtained through the LogManager.
 * 
 * @author dan
 * 
 */
public class Logger {
	final private String name;
	final private int id;
	private PrintStream out = LogManager.out;
	private boolean filter = true;
	private boolean useOut = false;

	/**
	 * Create a Logger with this numerical ID and Display Name
	 * 
	 * @param name
	 *            Display Name
	 * @param id
	 *            Numerical ID
	 */
	public Logger(String name, int id, boolean useOut) {
		this.useOut = useOut;
		this.id = id;
		this.name = name + ":" + id;
		try {
			filter = Boolean.parseBoolean(Variables.getVarValue("Log.filter"));
		} catch (Exception e) {
			filter = true;
		}
	}

	/**
	 * Specify whether or not to use an OutputStream or to use the
	 * LogManager.write function
	 * 
	 * @param useOut
	 *            Whether or not to use an OutputStream (True) or
	 *            LogManager.write (False) Default: False
	 */
	public void useOut(boolean useOut) {
		this.useOut = useOut;
	}

	/**
	 * Create a Logger with this numerical ID, the ID will be used as the
	 * Display Name
	 * 
	 * @param id
	 *            Numerical ID
	 */
	public Logger(int id, boolean useOut) {
		this.useOut = useOut;
		this.name = "ID:" + id;
		this.id = id;
	}

	/**
	 * Write a String out to the current OutputStream. This String will be
	 * checked for variables within the String unless otherwise specified.
	 * 
	 * @param s
	 *            String to write.
	 */
	public void log(String s) {
		final String[] tmp = chkVar(s);
		// out.print("[" + name + "] " + s + "\n");
		actual(tmp);
	}

	/**
	 * Write an array of Strings out to the current OutputStream. This Array
	 * will be checked for variables within itself unless otherwise specified.
	 * 
	 * @param s
	 *            Array Of String to be written.
	 */
	public void log(String[] s) {
		// print("[" + name + "]");
		// for (int i = 0; i < s.length; i++) {
		// print(s[i] + " ");
		// }
		// endln();
		chkVar(s);
		actual(s);
	}

	/**
	 * Whether or not to find and replace variables within Strings being
	 * written.
	 * 
	 * @param filter
	 */
	public void filter(boolean filter) {
		this.filter = filter;
	}

	private void actual(String[] s) {
		print("[" + name + "]");
		for (int i = 0; i < s.length; i++) {
			print(s[i] + " ");
		}
		endln();
	}

	/**
	 * End the current line.
	 */
	public void endln() {
		if (useOut) {
			out.print("\n");
			return;
		}
		LogManager.write("\n");
	}

	/**
	 * Write out to this Stream temporarily. Only one line will be written here
	 * 
	 * @param s
	 *            String to be written.
	 * @param out
	 *            PrintStream to write out to.
	 */
	public void log(String s, PrintStream out) {
		out.print("[" + name + "] " + s + "\n");
	}

	/**
	 * Print this String without ending the line, and without any Display Name.
	 * 
	 * @param s
	 *            String to be written.
	 */
	public void print(String s) {
		if (useOut) {
			out.print(s);
			return;
		}
		LogManager.write(s);
	}

	/**
	 * Write an Error, this goes to System.err.
	 * 
	 * @param s
	 *            String to be written.
	 */
	public void err(String s) {
		System.err.print("[" + name + "] " + s + "\n");
	}

	/**
	 * Obtain the Display Name of this Logger.
	 * 
	 * @return The Logger's Display Name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Obtain the numerical ID of this Logger.
	 * 
	 * @return The Numerical ID of this Logger.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Clear the console
	 * 
	 */
	public void clear() {
		try {
			int col = Integer.parseInt(System.getenv("COLUMNS"));
			for (int i = 0; i < col; i++) {
				out.println();
			}
		} catch (Exception e) {
			final int col = 80;
			for (int i = 0; i < col; i++) {
				out.println();
			}
		}
	}

	/**
	 * Obtain the currently used OutputStream to write to.
	 * 
	 * @return The current OutputStream.
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * Change the current OutputStream.
	 * 
	 * @param out
	 *            The OutputStream to be written to.
	 */
	public void setOutputStream(OutputStream out) {
		this.out = new PrintStream(out);
	}

	private String[] chkVar(String in) {
		if (filter) {
			String[] args = in.split("\\ ");
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("%")) {
					final String tmp = Variables.getVarValue(args[i].substring(1));
					if (tmp != null) {
						args[i] = tmp;
					}
				}
			}
			return args;
		}
		return in.split("\\ ");
	}

	private String[] chkVar(String[] args) {
		if (filter) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("%")) {
					final String tmp = Variables.getVarValue(args[i].substring(1));
					if (tmp != null) {
						args[i] = tmp;
					}
				}
			}
		}
		return args;
	}

}
