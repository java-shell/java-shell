package terra.shell.command.builtin;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class CmdHistory extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1338806971071277977L;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "cmdhis";
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
		final String[][] hist = term.getHistory();
		getLogger().log("Command History: ");
		for (int i = 0; i < hist.length; i++) {
			getLogger().print(i + ": ");
			getLogger().log(hist[i]);
		}
		return true;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}
}
