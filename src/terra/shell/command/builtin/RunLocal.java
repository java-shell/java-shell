package terra.shell.command.builtin;

import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.utils.perms.Permissions;
import terra.shell.utils.streams.OutputStreamMonitorBuffer;

public class RunLocal extends BasicCommand {

	@Override
	public String getName() {
		return "run";
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
		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(term.currentDir());
		pb.command(args);
		try {
			pb.inheritIO();
			pb.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
