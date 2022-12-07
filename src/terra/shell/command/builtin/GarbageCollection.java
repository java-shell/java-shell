package terra.shell.command.builtin;

import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class GarbageCollection extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6344163579262629327L;

	@Override
	public String getName() {
		 
		return "gc";
	}

	@Override
	public String getVersion() {
		 
		return "0.1";
	}

	@Override
	public String getAuthor() {
		 
		return "D.S.";
	}

	@Override
	public String getOrg() {
		 
		return "TTD";
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
		 
		long old = Runtime.getRuntime().freeMemory();
		System.runFinalization();
		System.gc();
		getLogger().log("Cleared " + (Runtime.getRuntime().freeMemory() - old) + " bytes of memory");
		return true;
	}

}
