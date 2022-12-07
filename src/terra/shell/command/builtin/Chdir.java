package terra.shell.command.builtin;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import terra.shell.command.Command;
import terra.shell.command.Terminal;
import terra.shell.utils.perms.Permissions;
import terra.shell.command.Terminal;

public class Chdir extends Command {

	private static final long serialVersionUID = 8417652681651520855L;

	@Override
	public String getName() {
		 
		return "cd";
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
		final File curr = term.currentDir();
		final File[] poss = curr.listFiles();
		if (args.length > 0) {
			File f = new File(curr.getPath() + "/" + args[0]);
			if (f.exists() && f.isDirectory()) {
				term.setCurrentDir(f);
				return true;
			} else {
				for (int i = 0; i < poss.length; i++) {
					if (f.getName() == poss[i].getName())
						term.setCurrentDir(f);
				}
			}
		}
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
