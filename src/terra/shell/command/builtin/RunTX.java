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
		// TODO Auto-generated method stub
		return "tx";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "2.0";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "D.S.";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
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
		// TODO Auto-generated method stub
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
