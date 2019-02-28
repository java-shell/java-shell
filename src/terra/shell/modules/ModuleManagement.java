package terra.shell.modules;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.ByteClassLoader;
import terra.shell.utils.system.EventManager;
import terra.shell.utils.system.JSHClassLoader;

/**
 * Resource class which manages all Modules, and is launched by Launch in its
 * initialization functions. This class should be used directly when loading and
 * running a new module.
 * 
 * @author dan
 * 
 */
public final class ModuleManagement {
	static Hashtable<String, Module> modules = new Hashtable<String, Module>();
	static Hashtable<Module, Thread> mThreads = new Hashtable<Module, Thread>();
	private static Logger log = LogManager.getLogger("MM");
	final ByteClassLoader bcl = Launch.getClassLoader();
	static boolean isStarted;

	/**
	 * Shirt Front: Read Back.<br>
	 * Shirt Back: Read Front.
	 */
	public void halt() {
		return;
	}

	public String getName() {
		return "MM";
	}

	public boolean start() {
		// Hook into config file
		if (!isStarted) {
			isStarted = true;
			File mods = new File(Launch.getConfD(), "/module.lst");
			if (!mods.exists()) {
				log.log("/module.lst not found! Unable to load any modules");
			} else if (Boolean.parseBoolean((String) Launch.getConfig("system").getValue("moduleload"))) {
				log.log("Loading modules");
				try {
					Scanner sc = new Scanner(new FileInputStream(mods));
					JSHClassLoader urlc = new JSHClassLoader(new URL[] { new URL("file:///modules/") });

					if (Launch.modularizedCmds) {

					}
					File prefix = Launch.getConfD().getParentFile();
					String s;
					while (sc.hasNext()) {
						Thread.sleep(10);
						s = sc.nextLine();
						final File f = new File(prefix, "/modules/" + s + "/module.class");
						final File dir = new File(prefix, "/modules/" + s + "/");
						File reslist = new File(prefix, "/modules/" + s + "/res.list");
						if (reslist.exists()) {
							Scanner r = new Scanner(new FileInputStream(reslist));
							final ArrayList<File> rf = new ArrayList<File>();
							final ArrayList<String> rff = new ArrayList<String>();
							while (r.hasNext()) {
								final String line = r.nextLine();
								final File fr = new File(prefix, "/modules/" + s + "/" + line);
								rf.add(fr);
								rff.add(line);
							}
							r.close();
							r = null;

							final File[] res = new File[rf.size()];
							final String[] ures = new String[rff.size()];
							for (int i = 0; i < res.length; i++) {
								res[i] = rf.get(i);
								ures[i] = rff.get(i);
							}
							final ByteClassLoader bcl = Launch.getClassLoader();

							log.log("Loading resource classes for " + s);
							for (int i = 0; (i < res.length); i++) {
								if (!res[i].getName().equals(s + ".module") && !res[i].getName().equals("res.list")
										&& !res[i].isDirectory()) {
									try {
										urlc.loadClass(ures[i]);
										log.log("Loaded resource class " + res[i].getName() + " for " + s);
									} catch (Exception e) {
										log.log("Failed to load resource class " + res[i].getName());
									}
								}
								res[i] = null;
								ures[i] = null;
							}
						} else {
							log.log("res.list not found for " + s);
						}
						if (f.exists()) {
							try {
								final Class<?> classtmp = urlc.loadClass(s + ".module");
								final Module mod = (Module) classtmp.newInstance();
								modules.put(mod.getName(), mod);
								Thread t = new Thread(new Runnable() {
									public void run() {
										try {
											mod.onEnable();
											mod.start();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
								t.setName(mod.getName());
								t.start();
								mThreads.put(mod, t);
								log.log("Module loaded: " + mod.getName());
							} catch (Exception e) {
								e.printStackTrace();
								log.log("Unable to load module: " + f.getAbsolutePath());
							}
						} else {
							log.log("Module: " + f.getPath() + " not found!");
						}

					}
					sc.close();
					sc = null;
				} catch (final Exception e) {
					e.printStackTrace();
					log.log("Unable to read modules.lst: " + e.getMessage());
				}
			} else {
				log.log("Modules not loaded");
			}
		}
		// /log.log("INIT: MODULE_ACCESS CMD COMPAT");
		// MAListener ml = new MAListener(this);
		// EventManager.registerListener(ml, "module-access");
		return true;
	}

	/**
	 * Load a module into a running system (CAUTION can be unstable)
	 * 
	 * @param f
	 *            The module.class to load
	 */
	public static void hotload(final File f) {
		final File pd = f.getParentFile();
		log.log("Being asked to hotload a module: " + pd.getName());
		if (isLoaded(pd.getName())) {
			log.log("Failed to hotload module as it is already loaded!");
			return;
		}
		if (f.exists()) {
			log.log("Searching for res.list");
			URLClassLoader urlc = null;
			try {
				urlc = new URLClassLoader(new URL[] { pd.getParentFile().toURI().toURL() });
			} catch (Exception e) {
				log.log("Failed to hotload module: " + pd.getName());
				log.log("Reason: " + e.getMessage());
				e.printStackTrace();
			}
			try {
				File reslist = new File(pd.getAbsolutePath() + "/res.list");
				if (reslist.exists()) {
					final Scanner sc = new Scanner(new FileInputStream(pd.getAbsolutePath() + "/res.list"));
					String tmp;
					while (sc.hasNextLine()) {
						tmp = sc.nextLine();
						urlc.loadClass(tmp);
						log.log("Hotloaded module resource: " + tmp + " for: " + pd.getName());
					}
					sc.close();
				} else {
					log.log("Didn't find res.list, ignoring it!");
				}
				final File mod = new File(pd.getAbsolutePath() + "/module.class");
				if (!mod.exists()) {
					log.log("Failed to hotload module, as module.class is nonexistent!");
					return;
				}
				final Class<?> tmpclass = urlc.loadClass(pd.getName() + ".module");
				final Module module = (Module) tmpclass.newInstance();
				modules.put(module.getName(), module);
				log.log("Hotloaded module: " + module.getName());
				log.log("Enabling hotloaded module...");
				final Thread t = new Thread(new Runnable() {
					public void run() {
						module.onEnable();
						module.run();
					}
				});
				t.setName(module.getName());
				t.start();
				mThreads.put(module, t);
				return;
			} catch (Exception e) {
				log.log("Failed to hotload module: " + pd.getName());
				log.log("Reason: " + e.getMessage());
				e.printStackTrace();
			}
		}
		log.log("Failed to hotload module: File nonexistent");
	}

	/**
	 * Attempt to disable the module with this ID.
	 * 
	 * @param id
	 *            Module to disable.
	 */
	public static void disable(String id) {
		if (modules.containsKey(id)) {
			final Module m = modules.get(id);
			if (mThreads.containsKey(m)) {
				try {
					// mThreads.get(m).sleep(100);
					final Thread t = mThreads.get(m);
					log.log("Suspending " + t.getName());
					Thread th = new Thread(new Runnable() {
						public void run() {
							synchronized (t) {
								try {
									t.wait();
								} catch (Exception e) {
									log.log("Failed to force " + m.getName() + " to pause!");
								}
							}
						}
					});
					th.start();
					log.log("Suspended " + m.getName());
					return;
				} catch (Exception e) {
					e.printStackTrace();
					log.log("Failed to force " + m.getName() + " to pause! " + e.getMessage());
					return;
				}
			}

		}
		return;
	}

	/**
	 * Enable the module with this ID.
	 * 
	 * @param id
	 *            Module to Enable.
	 */
	public static void enable(String id) {
		if (modules.containsKey(id)) {
			final Module m = modules.get(id);
			if (mThreads.containsKey(m)) {
				try {
					mThreads.get(m).notify();
					log.log("Resumed " + m.getName());
				} catch (Exception e) {
					log.log("Failed to force " + m.getName() + " to start!");
				}
			}
		}
	}

	private static boolean isLoaded(String id) {
		Enumeration<String> keys = modules.keys();
		while (keys.hasMoreElements()) {
			if (keys.nextElement() == id)
				return true;
		}
		return false;
	}

	/**
	 * See all loaded modules. Gives enabled and disable modules alike.
	 * 
	 * @return Enumeration of loaded modules.
	 */
	public static Enumeration<String> getModules() {
		return modules.keys();
	}

}
