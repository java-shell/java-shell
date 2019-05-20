package terra.shell.command.builtin;

import java.util.ArrayList;
import java.util.Set;

import terra.shell.command.Command;
import terra.shell.launch.Launch;
import terra.shell.utils.perms.Permissions;

public class LSCMD extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6401588913052646123L;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "lscmd";
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
		ArrayList<String> aliases = new ArrayList<String>();
		aliases.add("listcommands");
		aliases.add("listcmds");
		return aliases;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean start() {
		Set<String> keys = Launch.cmds.keySet();
		getLogger().log("Command : Version");
		for (String key : keys) {
			getLogger().log(key + ":  " + Launch.cmds.get(key).getVersion());
			if (Launch.cmds.containsKey(key)) {
				final ArrayList<String> ali = Launch.cmds.get(key).getAliases();
				if (ali != null) {
					for (String al : ali) {
						getLogger().log(" |-->" + al);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}

}
