package terra.shell.launch;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import terra.shell.command.Command;
import terra.shell.command.Terminal;
import terra.shell.config.Configuration;
import terra.shell.utils.perms.Permissions;
import terra.shell.utils.system.user.InvalidUserException;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement;

public class Login extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3992841719130735454L;

	private User user = null; // TODO Add code to return User object, and then use getUser method to retrieve
								// user in Terminal

	public Login(Terminal t) {
		super(t);
	}

	@Override
	public String getName() {
		 
		return "login";
	}

	@Override
	public String getVersion() {
		 
		return null;
	}

	@Override
	public String getAuthor() {
		 
		return null;
	}

	@Override
	public String getOrg() {
		 
		return null;
	}

	@Override
	public boolean isBlocking() {
		 
		return true;
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
	public void halt() {
		// getLogger().log("You can't halt a Login!");
		return;
	}

	public User login() throws InvalidUserException {
		start();
		if (user == null)
			throw new InvalidUserException();
		return user;
	}

	private boolean run;
	private boolean fst;
	private static boolean print = true;

	@Override
	public boolean start() {
		// Take over terminal stream
		setOutputStream(term.getGOutputStream());

		// Load the user info conf
		Configuration conf = Launch.getConfig("uinf");
		final File cnf = new File(Launch.getConfD() + "/uinf");
		boolean newconf = false;
		if (conf == null) {
			try {
				cnf.createNewFile();
				fst = true;
				newconf = true;
			} catch (Exception e) {
				e.printStackTrace(new PrintStream(getLogger().getOutputStream()));
				getLogger()
						.log("Unable to obtain system credentials or create new Configuration for System Credentials");
				while (true)
					;
			}
		}
		conf = new Configuration(cnf);

		// Attempt to lock the user info conf
		// try {
		// RandomAccessFile raf = new RandomAccessFile(cnf, "rw");
		// raf.getChannel().lock();
		// getLogger().log("\"uinf\" locked successfully");
		// } catch (Exception e) {
		// getLogger().log("Failed to get file lock on uinf");
		// getLogger().err("FAILED TO GET FILE LOCK ON \"uinf\", user login information
		// is susceptible to tampering");
		// }
		// TODO Lock cnf to prevent manipulation
		// TODO Create MD5 or other Hash file alongside to prevent modification of cnf
		// when
		// system is down

		// Initialize character masking for password entry
		Thread mask = new Thread(new Runnable() {

			public void run() {
				run = true;
				while (run) {
					if (print)
						getLogger().print("\010" + "*");
					try {
						Thread.sleep(2);
					} catch (Exception e) {
						getLogger().log("Warning: Password masking failed! Your password is visible!");
						run = false;
						return;
					}
				}
				return;
			}

		});
		mask.setPriority(Thread.MAX_PRIORITY);
		print = false;
		mask.start();

		// Check if this is the first login, if it is, then run user setup
		if (fst) {
			while (true) {
				getLogger().log("First time setup!");
				getLogger().log("Please enter a username:");
				String username = sc.nextLine();
				getLogger().log("Got username: " + username + ", is this correct? [Y/n]");
				String response = sc.nextLine();
				if (response.equalsIgnoreCase("n")) {
					continue;
				}
				getLogger().log("Please enter a desired password: ");
				print = true;
				String password = sc.next();
				getLogger().endln();
				sc.nextLine();
				print = false;
				if (UserManagement.createNewUser(username, password, null, null)) {
					getLogger().clear();
					getLogger().log("Fist Time User Setup Complete");
					break;
				}
			}
		}
		while (true) {
			getLogger().print("Username: ");
			String user = sc.nextLine();
			getLogger().print("Password: ");
			print = true;
			String pass = sc.next();
			getLogger().endln();
			sc.nextLine();
			print = false;
			try {
				this.user = UserManagement.logIn(user, pass);
				break;
			} catch (InvalidUserException e) {
				return false;
			}
		}
		mask.stop();
		return true;
	}
}
