package terra.shell.command.builtin;

import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.utils.perms.Permissions;

public class PWD extends BasicCommand {

	@Override
	public String getName() {
		 
		return "pwd";
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
		 
		return false;
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
		getLogger().log(term.currentDir().getAbsolutePath());
		return true;
	}

}
