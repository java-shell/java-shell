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
		// TODO Auto-generated method stub
		return "gc";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "D.S.";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return "TTD";
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getAliases() {
		// TODO Auto-generated method stubs
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
		long old = Runtime.getRuntime().freeMemory();
		System.runFinalization();
		System.gc();
		log.log("Cleared " + (Runtime.getRuntime().freeMemory() - old) + " bytes of memory");
		return true;
	}

}
