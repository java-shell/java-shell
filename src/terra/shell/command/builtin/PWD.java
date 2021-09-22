package terra.shell.command.builtin;

import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.utils.perms.Permissions;

public class PWD extends BasicCommand {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "pwd";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "D.S";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return "T3RRA";
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
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
		getLogger().log(term.currentDir().getAbsolutePath());
		return true;
	}

}
