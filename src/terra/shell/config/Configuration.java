package terra.shell.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.Variables;

public class Configuration {
	private int type = 3; // 0 - File; 1 - InputStream; 2 - URL; 3 - No Physical
	protected File f;
	protected InputStream in;
	protected URL u;
	private Logger log = LogManager.getLogger("Configuration");

	protected Hashtable<String, Object> vlist = new Hashtable<String, Object>();

	protected Configuration() {
	}

	/**
	 * Create a new configuration object
	 * 
	 * @param f File to use as the configuration file (Please use
	 *          Launch.getConfD()'s directory to keep all of your conf files)
	 */
	public Configuration(File f) {
		type = 0;
		this.f = f;
		parse();
	}

	/**
	 * Create a virtual read-only configuration object using an InputStream
	 * 
	 * @param f An InputStream which contains configuration information.
	 */
	public Configuration(InputStream f) {
		type = 1;
		in = f;
		parse();
	}

	/**
	 * Create a configuration object using a URL
	 * 
	 * @param f The URL to look at for configuration information
	 */
	public Configuration(URL f) {
		type = 2;
		u = f;
		parse();
	}

	/**
	 * Parse the configuration files contents
	 */
	protected void parse() {
		// Check the configuration file exists
		if (!f.exists())
			return;
		InputStream tmp = null;
		// Check the conf type, and deal with it accordingly
		if (type == 0) {
			// Conf is a local file
			try {
				tmp = new FileInputStream(f);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		if (type == 1) {
			// Conf is being served through a stream
			tmp = in;
		}
		if (type == 2) {
			// Conf is being served through a URL
			try {
				tmp = u.openStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (tmp == null) {
			log.log("Failed to get InputStream for Configuration!");
			return;
		}
		// Begin reading the conf information
		Scanner sc = new Scanner(tmp);
		String st;
		while (sc.hasNext() && (st = sc.nextLine()) != null) {
			// Check if the line is a comment, if not, parse
			if (!st.startsWith("#")) {
				final String[] bk = st.split(":");
				if (bk.length == 2) {
					// Check for variable declarations in the conf, and substitute
					if (bk[1].contains("%")) {
						final String tm = Variables.getVarValue(bk[2]);
						if (tm != null) {
							bk[1] = tm;
						}
					}
					// Save configuration declarations in memory
					vlist.put(bk[0], bk[1]);
				} else {
					if (bk.length == 1)
						vlist.put(bk[0], "null");
				}
			}
		}
		sc.close();
		sc = null;
		st = null;
		tmp = null;
	}

	/**
	 * Get a configuration value as an object
	 * 
	 * @param id The key used to identify the value required
	 * @return The value requested
	 */
	public Object getValue(String id) {
		if (vlist.containsKey(id)) {
			return vlist.get(id);
		}
		return null;
	}

	/**
	 * Attempts to convert the value assigned to 'id' to an int
	 * 
	 * @param id The key used to identify the value required
	 * @return The value requested as an int
	 */
	public int getValueAsInt(String id) {
		int ret;
		if (vlist.containsKey(id)) {
			try {
				ret = Integer.parseInt((String) vlist.get(id));
				return ret;
			} catch (Exception e) {
				log.err("Failed to load conf value as int, " + f.getName() + ":" + id);
				log.debug("Conf value conversion failed, message \"" + e.getMessage() + "\"");
			}
			try {
				ret = (int) vlist.get(id);
				return ret;
			} catch (Exception e) {
				log.err("Failed to load conf value as int, " + f.getName() + ":" + id);
				log.debug("Conf value conversion failed, message \"" + e.getMessage() + "\"");
			}
		}
		return 0;
	}

	public int getType() {
		return type;
	}

	/**
	 * Checks if a value has been assigned to this id
	 * 
	 * @param id The id who's value is being asserted
	 * @return True if the id has a value, or exists
	 */
	public boolean hasID(String id) {
		return vlist.containsKey(id);
	}

	/**
	 * Sets a configuration value (Only works if the configuration was constructed
	 * with a File object, otherwise does nothing
	 * 
	 * @param id    Key used to identify this value
	 * @param value Value to placed in the configuration
	 */
	public void setValue(String id, Object value) {
		vlist.put(id, value);
		_write();
	}

	/**
	 * Removes a key from a configuration entirely
	 * 
	 * @param id Key to be removed
	 */
	public void removeKey(String id) {
		vlist.remove(id);
		_write();
	}

	/**
	 * Save the configuration values back to the local file
	 */
	protected void _write() {
		// Writes the configuration
		Set<String> keys = vlist.keySet();
		try {
			if (type == 0) {
				final PrintStream fout = new PrintStream(new FileOutputStream(f), true);
				for (int i = 0; i < vlist.size(); i++) {
					fout.println(keys.toArray()[i] + ":" + vlist.get(keys.toArray()[i]));
				}
				fout.flush();
				fout.close();
			}
			return;
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(log.getOutputStream()));
		}
	}
}
