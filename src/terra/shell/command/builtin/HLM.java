package terra.shell.command.builtin;

import java.io.File;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.modules.ModuleManagement;
import terra.shell.utils.perms.Permissions;

public class HLM extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8032840951260016958L;

	@Override
	public String getName() {
		 
		return "hlm";
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
	public ArrayList<String> getAliases() {
		 
		return null;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		 
		return null;
	}

	@Override
	public boolean start() {
		if (args.length == 0) {
			getLogger().log("Not Enough Arguments");
			return false;
		}
		File f;
		if (args[0].startsWith("/")) {
			f = new File(args[0]);
		} // else if(args[0].startsWith("http://")){

		// }
		f = new File(term.currentDir(), args[0]);
		ModuleManagement.hotload(f);
		return true;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
