package terra.shell.command;

import java.util.ArrayList;

/**
 * BasicCommand is a wrapper class for Command which adds more functionality and
 * utilitarian functions
 */
public abstract class BasicCommand extends Command {

	private static final long serialVersionUID = -5393600630319994216L;
	private ArrayList<String> arguments = new ArrayList<String>();

	@Override
	public void addArgs(String[] args, Terminal executor) {
		arguments.clear();
		super.addArgs(args, executor);
		for (String arg : args)
			arguments.add(arg);
	}

	/**
	 * Checks to see if this Command has been assigned a specific argument
	 * 
	 * @param arg
	 *            String argument to check
	 * @param caseSensitive
	 * @return True if Command has been assigned the argument in question
	 */
	public boolean hasArgument(String arg, boolean caseSensitive) {
		if (caseSensitive)
			return arguments.contains(arg);

		for (String arg0 : arguments)
			if (arg0.equalsIgnoreCase(arg))
				return true;

		return false;
	}

	/**
	 * Checks to see if this Command has been assigned a specific argument
	 * 
	 * @param arg
	 *            String argument to check (Not Case Sensitive)
	 * @return True if Command has been assigned the argument in question
	 */
	public boolean hasArgument(String arg) {
		return hasArgument(arg, false);
	}

	/**
	 * Get the number of arguments assigned to this Command
	 * 
	 * @return Number of arguments assigned to this Command
	 */
	public int getNumArgs() {
		return arguments.size();
	}

	/**
	 * Retrieve the index at which a specific argument is located within the
	 * "arguments" List
	 * 
	 * @param arg
	 *            String argument to find
	 * @param caseSensitive
	 * @return Index at which the argument is located, or -1 if the argument does
	 *         not exist
	 */
	public int getArgIndex(String arg, boolean caseSensitive) {
		if (caseSensitive)
			return arguments.indexOf(arg);

		for (int i = 0; i < arguments.size(); i++)
			if (arguments.get(i).equalsIgnoreCase(arg))
				return i;

		return -1;
	}

	/**
	 * Retrieve an argument based off its index
	 * 
	 * @param index
	 *            Index of argument to retrieve
	 * @return First iteration of String argument located at the specified index
	 */
	public String getArg(int index) {
		if (index < 0 || index >= arguments.size())
			return null;
		return arguments.get(index);
	}

	/**
	 * Retrieve all indices at which a specific argument is located
	 * 
	 * @param arg
	 *            String argument to locate
	 * @param caseSensitive
	 * @return An array of int which contains all indices of the specified argument
	 *         within the "arguments" List
	 */
	public int[] getAllIndicesOfArg(String arg, boolean caseSensitive) {
		if (!hasArgument(arg, caseSensitive)) {
			return null;
		}
		// TODO
		// Figure out best way to instantiate an array so that it doesn't allocate more
		// space than necessary
		for (String curArg : arguments) {
			if (caseSensitive) {
				if (curArg.equals(arg))
					;
			} else {
				if (curArg.equalsIgnoreCase(arg))
					;
			}

		}
		// TODO
		return null;
	}

}
