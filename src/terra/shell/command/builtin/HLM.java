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
		// TODO Auto-generated method stub
		return "hlm";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "DS";
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
		// TODO Auto-generated method stub
		return true;
	}

}
