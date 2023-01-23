package terra.shell.command.builtin;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.utils.perms.Permissions;
import terra.shell.utils.system.executable.Executor;

public class RunTX extends BasicCommand {

	@Override
	public String getName() {
		 
		return "tx";
	}

	@Override
	public String getVersion() {
		 
		return "2.0";
	}

	@Override
	public String getAuthor() {
		 
		return "D.S.";
	}

	@Override
	public String getOrg() {
		 
		return null;
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
		 
		if (getNumArgs() < 1) {
			getLogger().log("Not enough arguments");
			return false;
		}

		boolean checkDigest = true;

		if (this.hasArgument("-skipDigest"))
			checkDigest = false;

		try {
			URL url = new URL(this.getArg(0));
			try {
				Executor.execute(url.openStream(), checkDigest).start();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} catch (MalformedURLException e) {
			File f = new File(args[0]);
			if (!f.exists()) {
				getLogger().log("Failed to access resource: " + args[0]);
				return false;
			}
			try {
				Executor.execute(new FileInputStream(f), checkDigest).start();
			} catch (Exception e1) {
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}

}
