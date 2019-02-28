package terra.shell.command.builtin;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import terra.shell.command.BasicCommand;
import terra.shell.launch.Launch;
import terra.shell.utils.perms.Permissions;

public class ClusterManagement extends BasicCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7578289179058204962L;

	@Override
	public String getName() {
		return "clusterman";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.0.1";
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
		// Arguments
		// Add Node
		// Remove Node
		// Ping Node
		// Check Node Load
		if (hasArgument("add")) {
			String ip = getArg(getArgIndex("add", true) + 1);
			log.log("Adding Node: " + ip);
			try {
				InetAddress ip4 = InetAddress.getByName(ip);
				Launch.getConnectionMan().addNode((Inet4Address) ip4);
				log.log("Node Added");
				return true;
			} catch (UnknownHostException e) {
				log.log("Failed to add node: " + e.getMessage());
				return true;
			} catch (IOException e) {
				log.log("Failed to add node: " + e.getMessage());
				return true;
			}
		} else if (hasArgument("remove")) {
			log.log("Node removal not yet implemented!");
			return true;
			// Removal of nodes not yet implemented in ConnectionManager
		} else if (hasArgument("ping")) {
			String ip = getArg(getArgIndex("ping", true) + 1);
			try {
				long ping = Launch.getConnectionMan().ping(ip);
				log.log("Ping of " + ip + " is " + ping);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				log.log("Failed to ping: " + e.getMessage());
				log.err("Failed to ping");
				return true;
			}
		} else if (hasArgument("checkLoad")) {
			// Node work load check not yet implemented in ConnectionManager
			log.log("Node work load checking not yet implemented!");
			return true;
		}
		return true;
	}

}