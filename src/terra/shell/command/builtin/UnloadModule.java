package terra.shell.command.builtin;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.modules.ModuleManagement;
import terra.shell.utils.perms.Permissions;

public class UnloadModule extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1527944078976196757L;

	@Override
	public String getName() {
		 
		return "ulm";
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
		getLogger().log(args.length + "");
		if (args.length != 0) {
			getLogger().log("Unloading " + args[0]);
			ModuleManagement.disable(args[0]);
			return true;
		} else {
			getLogger().log("Not Enough Arguments!");
			getLogger().log("USAGE: ulm <module>");
			return true;
		}
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
