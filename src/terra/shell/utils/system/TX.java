package terra.shell.utils.system;

import terra.shell.utils.JProcess;

/**
 * <p>
 * General class for creation of TX files. A TX file is compiled with the
 * compile command, and run with the run command. A TX is an implementation of a
 * JProcess, and is run similarly. TX files are similar to modules that can be
 * moved and executed independently. This allows for TX over remote connections
 * as all that is needed is a stable byte stream.<br>
 * TX: T3RRA Executable
 * </p>
 * 
 * @author dan
 * 
 */
public abstract class TX extends JProcess {
	/**
	 * Get the name of this TX as specified by its code.
	 * 
	 * @return The TX's name.
	 */
	@Override
	public abstract String getName();

	/**
	 * Start this TX.
	 * 
	 * @return Whether or not this TX was executed successfully.
	 */
	@Override
	public abstract boolean start();

}
