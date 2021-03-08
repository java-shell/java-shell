package terra.shell.utils.system;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.UUID;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.JProcess;

/**
 * <h1>Process Management Class</h1>
 * <p>
 * This class is the process management interface for all processes within JSH.
 * Any JProcess or extension of a JProcess should be routed through this class
 * to allow for System management of processes
 */
public class JSHProcesses {
	private static ProcessTable processes = new ProcessTable();
	private static ArrayList<String> names = new ArrayList<String>();
	private static HashSet<UUID> used = new HashSet<UUID>();
	private static Logger log = LogManager.getLogger("JSHPM");
	private static JProcess top;

	/**
	 * Add a JProcess to be managed
	 * 
	 * @param p JProcess to be managed
	 */
	public static void addProcess(JProcess p) {
		if (p.getUUID() != null)
			return;
		names.add(p.getName());
		UUID u = UUID.randomUUID();
		while (true)
			if (used.contains(u)) {
				u = UUID.randomUUID();
			} else
				break;
		p.setUUID(u);
		processes.put(u, p);
		top = p;
	}

	/**
	 * Halt a JProcess by its Name
	 * 
	 * @param process String name of the JProcess to halt
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
	 * @param jp String name of the JProcess to access
	 * @return Accessed JProcess
	 */
	public static JProcess getProcess(String jp) {
		return processes.get(jp);
	}

	public static JProcess getProcess(UUID id) {
		return processes.get(id);
	}

	public static JProcess[] getProcessBySUID(UUID suid) {
		return processes.getBySUID(suid);
	}

	/**
	 * Halt a JProcess by its object
	 * 
	 * @param process JProcess to halt
	 */
	public static void stopProcess(JProcess process) {
		try {
			if (processes.contains(process.getUUID())) {
				process.halt();
				processes.remove(process.getUUID());
			}
			if (names.contains(process.getName())) {
				names.remove(process.getName());
			}
			if (used.contains(process.getUUID())) {
				used.remove(process.getUUID());
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Halt a JProcess by its UUID
	 * 
	 * @param u The UUID of the JProcess to halt
	 */
	public static void stopProcess(UUID u) {
		try {
			if (processes.containsKey(u)) {
				processes.get(u).stop();
				names.remove(processes.get(u).getName());
				processes.remove(u);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to stop process: " + u.toString() + "!");
		}
	}

	@Deprecated
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
			if (processes.contains(process.getUUID())) {
				process.halt();
				processes.remove(process.getUUID());
			}
			if (names.contains(process.getName())) {
				names.remove(process.getName());
			}
			if (used.contains(process.getUUID())) {
				used.remove(process.getUUID());
			}
		} catch (Exception e) {
			log.log("Unable to kill process: " + process.getName() + "!");
		}
	}

	public static void killProcess(UUID u) {
		try {
			if (processes.containsKey(u)) {
				processes.get(u).stop();
				names.remove(processes.get(u).getName());
				processes.remove(u);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Unable to kill process: " + u.toString() + "!");
		}
	}

	public static boolean suspendProcess(JProcess p) {
		return _suspendProcess(p);
	}

	@Deprecated
	public static boolean suspendProcess(String name) {
		if (processes.contains(name)) {
			return _suspendProcess(processes.get(name));
		}
		return false;
	}

	public static boolean suspendProcess(UUID uid) {
		if (processes.contains(uid)) {
			return _suspendProcess(processes.get(uid));
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

	public static int getCount() {
		return processes.size();
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<String> getAll() {
		return names;
	}

	private static class ProcessTable extends Hashtable<UUID, JProcess> {
		Hashtable<ProcessDescriptor, JProcess> descripted = new Hashtable<ProcessDescriptor, JProcess>();

		public ProcessTable() {
			descripted = new Hashtable<ProcessDescriptor, JProcess>();
		}

		public JProcess put(UUID u, JProcess p) {
			return descripted.put(new ProcessDescriptor(u, p.getSUID(), p.getName()), p);
		}

		public JProcess get(UUID u) {
			return getByUUID(u);
		}

		public int size() {
			return descripted.size();
		}
		
		public void remove(UUID u) {
			Enumeration<ProcessDescriptor> keys = descripted.keys();
			while(keys.hasMoreElements()) {
				ProcessDescriptor pd = keys.nextElement();
				if(pd.getUUID().equals(u)) {
					descripted.remove(u);
					return;
				}
			}
		}

		public JProcess getByUUID(UUID u) {
			Enumeration<ProcessDescriptor> keys = descripted.keys();
			while (keys.hasMoreElements()) {
				ProcessDescriptor pd = keys.nextElement();
				if (pd.getUUID().equals(u)) {
					return descripted.get(pd);
				}
			}
			return null;
		}

		public JProcess[] getBySUID(UUID u) {
			Enumeration<ProcessDescriptor> keys = descripted.keys();
			HashSet<JProcess> fnd = new HashSet<JProcess>();
			while (keys.hasMoreElements()) {
				ProcessDescriptor pd = keys.nextElement();
				if (pd.getSID().equals(u)) {
					fnd.add(descripted.get(pd));
				}
			}
			JProcess[] procs = new JProcess[fnd.size()];
			return fnd.toArray(procs);
		}

		public JProcess[] getByName(String name) {
			Enumeration<ProcessDescriptor> keys = descripted.keys();
			HashSet<JProcess> fnd = new HashSet<JProcess>();
			while (keys.hasMoreElements()) {
				ProcessDescriptor pd = keys.nextElement();
				if (pd.getName().equals(name)) {
					fnd.add(descripted.get(pd));
				}
			}
			JProcess[] procs = new JProcess[fnd.size()];
			return fnd.toArray(procs);
		}

		public boolean contains(UUID u) {
			return descripted.contains(u);
		}

		public boolean containsSUID(UUID s) {
			Enumeration<ProcessDescriptor> keys = descripted.keys();
			while (keys.hasMoreElements())
				if (keys.nextElement().getSID().equals(s))
					return true;

			return false;
		}

		private class ProcessDescriptor {
			private final UUID uid, sid;
			private final String name;

			public ProcessDescriptor(UUID uid, UUID sid, String name) {
				this.uid = uid;
				this.sid = sid;
				this.name = name;
			}

			public UUID getUUID() {
				return uid;
			}

			public UUID getSID() {
				return sid;
			}

			public String getName() {
				return name;
			}
		}

	}

}
