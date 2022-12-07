package terra.shell.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import terra.shell.utils.JProcess;
import terra.shell.utils.perms.Permissions;

/**
 * Terminal Executable Command Object
 * 
 * @author schirripad@moravian.edu
 *
 */
public abstract class Command extends JProcess {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7560604411365272984L;
	protected String[] args;
	protected List<String> largs;
	protected transient Terminal term;

	/**
	 * @author schirripad@morvian.edu
	 * @return String used to call command
	 */
	@Override
	public abstract String getName();

	/**
	 * Creates an unlinked instance of Command, which needs to be assigned to a Terminal
	 */
	public Command() {
		super();
	}

	/**
	 * Create a new instance of this Command that is linked to a specific Terminal
	 * object
	 * 
	 * @param t
	 *            Terminal which this command will interact with
	 */
	public Command(Terminal t) {
		super();
		this.term = t;
	}

	/**
	 * Provides version information provided by Command author
	 * 
	 * @return Version of this commands build
	 */
	public abstract String getVersion();

	/**
	 * Provides author name provided by command author
	 * 
	 * @return Command Author
	 */
	public abstract String getAuthor();

	/**
	 * Provides organization name provided by command author
	 * 
	 * @return Command Author's Organization
	 */
	public abstract String getOrg();

	/**
	 * Determines whether or not the terminal should wait for command completion
	 * before accepting new input
	 * 
	 * @return True if this command requires the terminal to stop accepting input.
	 *         For example, if the Command requires input independently of the
	 *         argument interface allotted, the Command should block to prevent the
	 *         Terminal from gaining input and take over the Terminals "in" stream
	 */
	public abstract boolean isBlocking();

	/**
	 * Provides an ArrayList of Strings representing different aliases of which this
	 * command can be called from
	 * 
	 * @return Command aliases, other strings that will call this command
	 */
	public abstract ArrayList<String> getAliases();

	/**
	 * Provides an ArrayList of Permission objects which determine this commands
	 * security level
	 * 
	 * @return List of required permissions
	 */
	public abstract ArrayList<Permissions> getPerms();

	/**
	 * Assign arguments to this Command object
	 * 
	 * @param args
	 *            Arguments passed to this command
	 * @param executor
	 *            Terminal which will execute this command
	 */
	public void addArgs(String[] args, Terminal executor) {
		this.args = args;
		this.largs = Arrays.asList(args);
		term = executor;
	}
}
