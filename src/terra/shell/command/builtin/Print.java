package terra.shell.command.builtin;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class Print extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -783307353146133920L;

	@Override
	public String getName() {
		return "print";
	}

	@Override
	public String getVersion() {
		return "0.2";
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
		if (args.length > 0) {
			getLogger().filter(false);
			if (args[0].startsWith("-")) {
				getLogger().log(args[0]);
				if (args[0].equals("-f") || args[0].equals("--filter")) {
					getLogger().filter(true);
					getLogger().log("Filtering on");
				}
				if (args.length > 1)
					args[0] = args[1];
				else {
					getLogger().log("Please specify file path");
					return false;
				}
			}
			java.io.File f = new java.io.File(args[0]);
			if (f.exists() && !f.isDirectory()) {
				try {
					final Scanner sc = new Scanner(new FileInputStream(f));
					int ln = 0;
					getLogger().log("Reading " + f.getAbsolutePath());
					getLogger().print(ln + ": ");
					while (sc.hasNextLine()) {
						getLogger().print(sc.nextLine());
						getLogger().endln();
						ln++;
						getLogger().print(ln + ": ");
					}
					getLogger().endln();
					sc.close();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			} else {
				getLogger().log("File unable to be read!");
				return false;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}

}
