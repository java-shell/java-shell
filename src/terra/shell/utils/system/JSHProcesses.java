package terra.shell.utils.system;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.JProcess;

/**
 * <h1>Process Management Class</h1>
 * <p>
 * This class is the process management interface for all running processes
 * within JSH. Any JProcess or extension of a JProcess should be routed through
 * this class to allow for System management of processes
 */
public class JSHProcesses {
	private static Hashtable<String, JProcess> processes = new Hashtable<String, JProcess>();
	private static Hashtable<UUID, JProcess> processByUUID = new Hashtable<UUID, JProcess>();
	private static ArrayList<String> names = new ArrayList<String>();
	private static Logger log = LogManager.getLogger("JSHPM");
	private static JProcess top;

	/**
	 * Add a JProcess to be managed
	 * 
	 * @param p
	 *            JProcess to be managed
	 */
	public static void addProcess(JProcess p) {
		processes.put(p.getName(), p);
		names.add(p.getName());
		UUID u = UUID.randomUUID();
		p.setUUID(u);
		top = p;
	}

	/**
	 * Halt a JProcess by its Name
	 * 
	 * @param process
	 *            String name of the JProcess to halt
	 */
	public static void stopProcess(String process) {
		try {
			processes.get(process).halt();
			names.remove(process);
			processes.remove(process);
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to stop process: " + process + "!");
		}
	}

	/**
	 * Get a JProcess based off of its name
	 * 
	 * @param jp
	 *            String name of the JProcess to access
	 * @return Accessed JProcess
	 */
	public static JProcess getProcess(String jp) {
		return processes.get(jp);
	}

	/**
	 * Halt a JProcess by its object
	 * 
	 * @param process
	 *            JProcess to halt
	 */
	public static void stopProcess(JProcess process) {
		try {
			if (processes.contains(process)) {
				process.halt();
				processes.remove(process.getName());
			}
			if (processByUUID.contains(process)) {
				process.halt();
				processes.remove(process.getUUID());
			}
			if (names.contains(process.getName())) {
				names.remove(process.getName());
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Halt a JProcess by its UUID
	 * 
	 * @param u
	 *            The UUID of the JProcess to halt
	 */
	public static void stopProcess(UUID u) {
		try {
			if (processByUUID.containsKey(u)) {
				processByUUID.get(u).stop();
				names.remove(processByUUID.get(u).getName());
				processByUUID.remove(u);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to stop process: " + u.toString() + "!");
		}
	}

	public static void killProcess(String process) {
		try {
			processes.get(process).halt();
			names.remove(process);
			processes.remove(process);
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to kill process: " + process + "!");
		}
	}

	public static void killProcess(JProcess process) {
		try {
			if (processes.contains(process)) {
				process.halt();
				processes.remove(process.getName());
			}
			if (processByUUID.contains(process)) {
				process.halt();
				processes.remove(process.getUUID());
			}
			if (names.contains(process.getName())) {
				names.remove(process.getName());
			}
		} catch (Exception e) {
			log.log("Unable to kill process: " + process.getName() + "!");
		}
	}

	public static void killProcess(UUID u) {
		try {
			if (processByUUID.containsKey(u)) {
				processByUUID.get(u).stop();
				names.remove(processByUUID.get(u).getName());
				processByUUID.remove(u);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to kill process: " + u.toString() + "!");
		}
	}

	public static boolean suspendProcess(JProcess p) {
		return _suspendProcess(p);
	}

	public static boolean suspendProcess(String name) {
		if (processes.contains(name)) {
			return _suspendProcess(processes.get(name));
		}
		return false;
	}

	public static boolean suspendProcess(UUID uid) {
		if (processByUUID.contains(uid)) {
			return _suspendProcess(processByUUID.get(uid));
		}
		return false;
	}

	private static boolean _suspendProcess(JProcess p) {
		p.suspend();
		return p.isSuspended();
	}

	public static JProcess getCurrent() {
		return top;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> getAll() {
		return names;
	}

}
