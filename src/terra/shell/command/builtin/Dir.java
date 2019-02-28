package terra.shell.command.builtin;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;

public class Dir extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1939174397175311552L;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "dir";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.4";
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
	public ArrayList<Permissions> getPerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean start() {
		final File[] f = term.currentDir().listFiles();
		int len = 0;
		for (File d : f) {
			if (d.getName().length() > len)
				len = d.getName().length();
		}
		boolean full = false;
		if (args.length > 0) {
			if (largs.contains("-full"))
				full = true;
			else {
				File ftmp = new File(args[0]);
				if (!ftmp.exists())
					return false;
				File[] atmp = ftmp.listFiles();
				int le = 0;
				for (File d : atmp) {
					if (d.getName().length() > len)
						len = d.getName().length();
				}
				print(atmp, full, true, le);
			}
			if (largs.contains("-iterate")) {
				print(f, false, true, len);
			}
		} else {
			print(f, full, false, len);
		}
		log.endln();
		log.log("Finished");
		return true;
	}

	@Override
	public ArrayList<String> getAliases() {
		ArrayList<String> al = new ArrayList<String>();
		al.add("ls");
		return al;
	}

	private void print(File[] f, boolean full, boolean iterate, int longest) {
		if (longest > 15) {
			longest = 15;
		}
		if (f != null) {

			if (f.length > 9 && iterate) {
				int ct = 0;
				Scanner sc = new Scanner(term.getGInputStream());
				log.log("Type \"q\" in order to stop");
				while (true) {
					if (sc.nextLine().equals("q"))
						break;
					if (full) {
						if (ct < f.length) {
							log.log(f[ct].getAbsolutePath());
						} else
							break;
					} else {
						if (ct < f.length) {
							log.log(f[ct].getName());
						} else
							break;
					}
					ct++;
				}
				sc.close();
				sc = null;
				return;
			}

			int index = 0;

			if (full) {
				for (File d : f) {
					char[] c = d.getAbsolutePath().toCharArray();
					for (int i = 0; i < longest; i++) {
						if (i >= c.length) {
							log.print(" ");
						} else {
							log.print("" + c[i]);
						}
					}
					log.print(" | ");
					if (index == 3) {
						log.print("\n");
						index = 0;
					}
					index++;
				}
			} else
				for (File d : f) {
					char[] c = d.getName().toCharArray();
					for (int i = 0; i < longest; i++) {
						if (i >= c.length) {
							log.print(" ");
						} else {
							log.print("" + c[i]);
						}
					}
					log.print(" | ");
					if (index == 3) {
						log.print("\n");
						index = 0;
					}
					index++;
				}

			/*
			 * for (int i = 0; i < f.length; i++) { /* if (index != 3) {
			 * index++; if (full) { if (f[i].isDirectory())
			 * log.print(f[i].getAbsolutePath() + ":/d "); else
			 * log.print(f[i].getAbsolutePath() + " "); } else { if
			 * (f[i].isDirectory()) log.print(f[i].getName() + ":/d "); else
			 * log.print(f[i].getName() + " "); } } else { index = 1;
			 * log.endln(); if (full) { if (f[i].isDirectory())
			 * log.print(f[i].getAbsolutePath() + ":/d "); else
			 * log.print(f[i].getAbsolutePath() + " "); } else { if
			 * (f[i].isDirectory()) log.print(f[i].getName() + ":/d "); else
			 * log.print(f[i].getName() + " "); } } }
			 */
		}
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}

}
