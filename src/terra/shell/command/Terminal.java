package terra.shell.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.Timer;

import terra.shell.launch.Launch;
import terra.shell.launch.Login;
import terra.shell.logging.LogManager;
import terra.shell.utils.InteractiveObject;
import terra.shell.utils.streams.InputStreamBuffer;
import terra.shell.utils.streams.OutputStreamMonitorBuffer;
import terra.shell.utils.system.user.InvalidUserException;
import terra.shell.utils.system.user.User;

/**
 * CLI Interface for the JSH
 * 
 * @author dan
 * 
 */
public final class Terminal extends InteractiveObject {
	private final String name = getName();
	private File currentDir = new File("/");
	private OutputStream gOut = LogManager.out;
	private InputStream gIn = System.in;
	private Scanner sc;
	private ArrayList<String[]> history = new ArrayList<String[]>();
	private boolean always = true;
	private boolean kill = false;
	private boolean respawn;
	private User user = null;

	/**
	 * Create a new terminal with default settings
	 */
	public Terminal() {
	}

	/**
	 * Create a new terminal specifying the direction of its output
	 * 
	 * @param gout OutputStream to point all of this terminals output.
	 */
	public Terminal(OutputStream gout) {
		gOut = gout;
	}

	/**
	 * Attempt to kill terminal, will not always work as it essentially alerts the
	 * Terminal to stop, not actually killing it.
	 */
	@Override
	public void halt() {
		getLogger().log("Attempt to kill terminal has been detected, complying");
		kill = true;
	}

	@Override
	public String getName() {
		return "Terminal" + Math.random();
	}

	@Override
	public boolean start() {
		getLogger().debug("Attempting to spawn a Terminal");
		getLogger().setOutputStream(gOut);
		getLogger().log("Starting terminal!");
		currentDir = new File("/");

		getLogger().log("Initializing input!");
		Login l = new Login(this);
		l.setOutputStream(gOut);
		l.redirectIn(gIn);
		try {
			user = l.login();
		} catch (InvalidUserException e) {
			getLogger().err("User Authentication Failure");
			return false;
		}
		sc = new Scanner(gIn);
		String in;
		getLogger().print(currentDir.getName() + ">");
		while (!kill) {
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().log("Thread failed to sleep");
			}
			if (always) {
				try {
					synchronized (gIn) {
						in = sc.nextLine();
					}
					if (in.equals("")) {
						LogManager.out.print(currentDir.getName() + ">");
						continue;
					}
					final String[] total = in.split(" ");
					if (total[0].equals("logout")) {
						respawn = true;
						halt();
					}
					if (history.size() < 10) {
						history.add(0, total);
					} else {
						history.add(0, total);
						history.remove(10);
						history.trimToSize();
					}
					if (Arrays.asList(total).contains(">")) {
						final int loc = Arrays.asList(total).indexOf(">");
						String[] cmd1 = new String[loc];
						String[] cmd2 = new String[total.length - loc];
						cmd1 = Arrays.copyOfRange(total, 0, loc);
						cmd2 = Arrays.copyOfRange(total, loc + 1, total.length);

						getLogger().log(cmd1[0] + ":" + cmd2[0]);

						if (Launch.cmds.containsKey(cmd1[0]) & Launch.cmds.containsKey(cmd2[0])) {
							final Command c1 = Launch.cmds.get(cmd1[0]);
							final Command c2 = Launch.cmds.get(cmd2[0]);
							final String[] args1 = new String[total.length - 1];
							for (int i = 1; i < cmd1.length; i++) {
								args1[i - 1] = cmd1[i];
							}
							final String[] args2 = new String[total.length - 1];
							for (int i = 1; i < cmd2.length; i++) {
								args2[i - 1] = cmd2[i];
							}
							final OutputStreamMonitorBuffer tout = new OutputStreamMonitorBuffer();
							final InputStreamBuffer inb = new InputStreamBuffer(tout);
							c1.addArgs(args1, this);
							c1.setOutputStream(tout);
							c1.redirectIn(gIn);

							c2.addArgs(args2, this);
							c2.setOutputStream(gOut);
							c2.redirectIn(inb);
							try {
								boolean done;
								// if (!c1.isBlocking()) {
								// done = c1.run(false);
								// } else
								done = c1.run(false);
								done = c2.run(true);
								if (done = false)
									getLogger().log(total[0] + " exited with a bad exit status");
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							getLogger().log("Command not found");
						}
						LogManager.out.print(currentDir.getName() + ">");
					} else if (Launch.cmds.containsKey(total[0])) {
						final Command c = Launch.cmds.get(total[0]);
						final String[] args = new String[total.length - 1];
						for (int i = 1; i < total.length; i++) {
							args[i - 1] = total[i];
						}
						c.addArgs(args, this);
						c.setOutputStream(gOut);
						c.redirectIn(gIn);
						try {
							boolean done;
							if (!c.isBlocking()) {
								done = c.run(false);
							} else
								done = c.run();
							if (done = false)
								getLogger().log(total[0] + " exited with a bad exit status");
						} catch (Exception e) {
							e.printStackTrace();
						}
						LogManager.out.print(currentDir.getName() + ">");
					} else if (total[0].equals("cmd")) {
						if (Launch.cmds.containsKey(total[1])) {
							getLogger().log("Class Name: " + Launch.cmds.get(total[1]).getClass().getName());
							LogManager.out.print(currentDir.getName() + ">");
						} else {
							getLogger().log("Command not found");
							LogManager.out.print(currentDir.getName() + ">");
						}
					} else {
						getLogger().log("Command not found");
						LogManager.out.print(currentDir.getName() + ">");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		getLogger().log("Terminal Closed");
		sc.close();
		stop();
		final Terminal ne = new Terminal();
		ne.setGInputStream(this.getGInputStream());
		ne.setGOutputStream(this.getGOutputStream());
		if (respawn) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					ne.run();
				}
			});
			t.setName("Terminal:" + ne.getName());
			t.start();
		}
		return true;
	}

	/**
	 * Run a specific command, calling it using the returned string from
	 * Command.getName();
	 * 
	 * @param cmd  String reference to the Command
	 * @param args Arguments to pass to the command
	 * @return True if the command is run successfully, false otherwise
	 */
	public boolean runCmd(String cmd, String... args) {
		if (user == null) {
			getLogger().err("User is null, unable to run command");
			return false;
		}
		if (Launch.cmds.containsKey(cmd)) {
			Command c = Launch.cmds.get(cmd);
			c.setOutputStream(getGOutputStream());
			c.addArgs(args, this);
			return c.run();
		}
		return false;
	}

	/**
	 * Get the directory this terminal is currently working in
	 * 
	 * @return The directory currently working in
	 */
	public File currentDir() {
		return currentDir;
	}

	/**
	 * Change the directory which this terminal is working in
	 * 
	 * @param dir A new directory
	 */
	public void setCurrentDir(File dir) {
		currentDir = dir;
	}

	/**
	 * Get the current stream that this terminal's output is pointing to
	 * 
	 * @return The OutputStream this terminal's output is being pointed to
	 */
	public OutputStream getGOutputStream() {
		return gOut;
	}

	/**
	 * Set the current stream that this terminal's output is pointing to
	 * 
	 * @param out An OutputStream
	 */
	public void setGOutputStream(OutputStream out) {
		gOut = out;
		setOutputStream(out);
	}

	/**
	 * Set the terminal's InputStream
	 * 
	 * @param in An InputStream
	 */
	public void setGInputStream(InputStream in) {
		redirectIn(in);
		gIn = in;
		always = false;
		sc = new Scanner(in);
		always = true;
	}

	/**
	 * Get the InputStream this terminal is currently listening to
	 * 
	 * @return The InputStream currently being listened to by this terminal
	 */
	public InputStream getGInputStream() {
		return gIn;
	}

	private void startGCTimer() {
		Timer t = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.runFinalization();
				System.gc();
			}
		});
		t.setDelay(1000);
		t.start();
	}

	/**
	 * Get this terminals command history
	 * 
	 * @return A String[][] containing the last executed commands in order
	 */
	public String[][] getHistory() {
		return (String[][]) history.toArray(new String[0][]);
	}
}
