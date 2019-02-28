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
		// TODO Auto-generated method stub
		return "po";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "D.S";
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
		if (args.length > 0) {
			final File f = new File(args[0]);
			Scanner sc = new Scanner(System.in);
			PrintWriter out = null;
			try {
				if (f.exists()) {
					log.log("File Exists! Overwrite? (Y,N)");
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
			log.log("To end file, type /:quit");
			if (args.length > 1) {
				for (int i = 1; i < args.length; i++) {
					out.println(args[i]);
					return true;
				}
			} else {
				int ln = 0;
				log.print(ln + ": ");
				while (sc.hasNextLine()) {
					final String tmp = sc.nextLine();
					if (tmp.equals("/:quit")) {
						break;
					}
					out.println(tmp);
					ln++;
					log.print(ln + ": ");
				}
				return true;
			}
		} else {
			log.log("Not Enough Arguments! What do you think I am? A mind reader?");
		}
		return false;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}

}
