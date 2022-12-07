package terra.shell.command.builtin;

import java.util.ArrayList;
import java.util.Enumeration;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;
import terra.shell.utils.system.GeneralVariable;
import terra.shell.utils.system.Variable;
import terra.shell.utils.system.Variables;

public class Export extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4614522559985606222L;

	@Override
	public String getName() {
		 
		return "export";
	}

	@Override
	public String getVersion() {
		 
		return "0.1";
	}

	@Override
	public String getAuthor() {
		 
		return "D.S";
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
		 
		if (args.length > 0) {
			if (args[0].equals("set")) {
				if (args.length == 3) {
					Variables.setVar(new GeneralVariable(args[1], args[2]));
					return true;
				} else {
					getLogger().log("Invalid Args!");
					return false;
				}
			}
			if (args[0].equals("help")) {
				getLogger().log("Syntax: export <set,help> (key) (value)");
				return true;
			}
			getLogger().log("Invalid Arguments!");
			return false;
		}
		final Enumeration<Variable> e = Variables.getVars();
		while (e.hasMoreElements()) {
			final Variable tmp = e.nextElement();
			getLogger().log(tmp.getVarName() + ": " + tmp.getVarValue());
		}
		return true;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
