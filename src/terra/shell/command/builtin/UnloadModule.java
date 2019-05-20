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
		// TODO Auto-generated method stub
		return "ulm";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "DS";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return "T3RRA";
	}

	@Override
	public ArrayList<String> getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return true;
	}

}
