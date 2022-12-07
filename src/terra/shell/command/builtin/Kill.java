package terra.shell.command.builtin;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class Kill extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9168996525849226651L;

	@Override
	public String getName() {
		 
		return "kt";
	}

	@Override
	public String getVersion() {
		 
		return "0.1";
	}

	@Override
	public String getAuthor() {
		 
		return "DS";
	}

	@Override
	public String getOrg() {
		 
		return "T3RRA";
	}

	@Override
	public ArrayList<String> getAliases() {
		 
		return null;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		 
		return null;
	}

	@Override
	public boolean start() {
		 
		term.halt();
		return true;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
