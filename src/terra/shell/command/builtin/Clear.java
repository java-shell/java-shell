package terra.shell.command.builtin;

import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.utils.perms.Permissions;

public class Clear extends BasicCommand {

	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getAuthor() {
		return "D.S.";
	}

	@Override
	public String getOrg() {
		return "java-shell";
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
		getLogger().clear();
		getLogger().print(">");
		return true;
	}

}
