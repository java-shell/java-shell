package terra.shell.command.builtin;

import java.util.ArrayList;
import java.util.Enumeration;

import terra.shell.command.Command;
import terra.shell.modules.ModuleManagement;
import terra.shell.utils.perms.Permissions;

public class ModuleList extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2003393452888137479L;

	@Override
	public String getName() {
		return "ml";
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
		Enumeration<String> e = ModuleManagement.getModules();
		int num = 0;
		while (e.hasMoreElements()) {
			getLogger().log(e.nextElement());
			num++;
		}
		getLogger().log("Total modules: " + num);
		stop();
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
