package terra.shell.command.builtin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class Out extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6905309766064468831L;

	@Override
	public String getName() {
		 
		return "po";
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
			final File f = new File(args[0]);
			Scanner sc = new Scanner(System.in);
			PrintWriter out = null;
			try {
				if (f.exists()) {
					getLogger().log("File Exists! Overwrite? (Y,N)");
					String answer = sc.nextLine();
					if (answer.equalsIgnoreCase("n")) {
						return true;
					}
				} else
					f.createNewFile();
				out = new PrintWriter(new FileOutputStream(f), true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			getLogger().log("To end file, type /:quit");
			if (args.length > 1) {
				for (int i = 1; i < args.length; i++) {
					out.println(args[i]);
					return true;
				}
			} else {
				int ln = 0;
				getLogger().print(ln + ": ");
				while (sc.hasNextLine()) {
					final String tmp = sc.nextLine();
					if (tmp.equals("/:quit")) {
						break;
					}
					out.println(tmp);
					ln++;
					getLogger().print(ln + ": ");
				}
				return true;
			}
		} else {
			getLogger().log("Not Enough Arguments! What do you think I am? A mind reader?");
		}
		return false;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
	}

}
