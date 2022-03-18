package terra.shell.command.builtin;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.launch.Launch;
import terra.shell.utils.perms.Permissions;

public class ReloadCommands extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -52423136821917082L;

	@Override
	public String getName() {
		 
		return "rlcmds";
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
	public ArrayList<Permissions> getPerms() {
		 
		return null;
	}

	@Override
	public boolean start() {
		Launch.rlCmds();
		stop();
		return true;
	}

	@Override
	public ArrayList<String> getAliases() {
		 
		return null;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
