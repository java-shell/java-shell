package terra.shell.command.builtin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class Copy extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4444739106326791702L;

	@Override
	public String getName() {
		 
		return "cp";
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
		if (args.length != 2) {
			getLogger().log("Not enough arguments! \nUsage cp <src> <dest>");
			return true;
		}
		File f = new File(args[0]);
		if (!f.exists()) {
			getLogger().log("File not found!");
			return false;
		}
		File f2 = new File(args[1]);
		try {
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(f));
			BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(f2, false));
			int h = 0;
			int ct = 0;
			int p = 0;
			getLogger().print("0%");
			while ((h = bin.read()) != -1) {
				ct++;
				p = (int) (ct / (f.length()));
				if (p >= 10) {
					getLogger().print("\010\010\010" + p + "%");
				} else {
					getLogger().print("\010\010" + p + "%");
				}
				bout.write(h);
			}
			bin.close();
			bout.flush();
			bout.close();
			bin = null;
			bout = null;
			f = null;
			f2 = null;
			getLogger().log("File copy complete!");
			return true;
		} catch (Exception e) {
			getLogger().log("Copy failed! " + e.getMessage());
		}
		return false;
	}

}
