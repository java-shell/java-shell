package terra.shell.command.builtin;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class CurrentDir extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -342287897050580259L;

	@Override
	public String getName() {
		 
		return "crd";
	}

	@Override
	public String getVersion() {
		 
		return "0.1";
	}

	@Override
	public String getAuthor() {
		 
		return "D.S";
	}

	@Override
	public String getOrg() {
		 
		return "T3RRA";
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
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
		getLogger().log("Current Directory: " + term.currentDir());
		return true;
	}

}
