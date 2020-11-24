package terra.shell.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Scanner;
import java.util.UUID;

import jdk.internal.org.objectweb.asm.commons.Method;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.JSHProcesses;

/**
 * JProcess is a resource class which implements a Thread as its "heart". The
 * JProcess controls this Thread and also controls the I/O of the Thread itself
 * and contained code. A JProcess should be handled with care as the unexpected
 * closure of one can deadlock JSH. JProcesses are extended by classes such as
 * Command , Module. Terminal is a modified version of JProcess to prevent
 * tampering with its code. All JProcesses should be managed using the
 * JSHProcesses management class.
 * 
 * @author dan
 * 
 */
public abstract class JProcess implements Serializable {

	private static final long serialVersionUID = -4944113269698016157L;
	private transient boolean stop, isGoing = true, suspend;
	private transient Thread t = null;
	private transient UUID u;
	private transient boolean uuidset;
	private transient Logger log = null;
	private JProcess me = this;
	private transient InputStream s = null;
	private Class<?>[] deps;
	protected transient Scanner sc = null;
	private transient String name = null;
	private boolean canBeSerialized = false;

	public JProcess() {
		init();
	}

	private final void init() {
		s = System.in;
		sc = new Scanner(s);
		log = LogManager.getLogger(getName());
	}

	protected final Logger getLogger() {
		return log;
	}

	/**
	 * Used for re-initialization of I/O after De-Serialization process, also marks
	 * the process as being foreign
	 */
	public final void reInitialize() {
		init();
	}

	public final void reInitialize(Class<?>[] deps) {
		this.deps = deps;
		reInitialize();
	}

	public final void prepSerialization() {
		log = null;
		this.name = getName();
	}

	/**
	 * Get the name, in text, of this process as specified by the process.
	 * 
	 * @return The processes name.
	 */
	public abstract String getName();

	/**
	 * Execute the processes main running method
	 * 
	 * @return True if the process ended with no errors, otherwise false.
	 */
	public abstract boolean start();

	/**
	 * Sends an interrupt to this process. The process will then take the interrupt
	 * as it sees fit. Use this before halt(), as this is simply to inform the
	 * thread within the process of its imminent disposal.
	 */
	public final void stop() {
		if (t == null)
			return;
		t.interrupt();
	}

	/**
	 * Get final return value for entire process after process completion, if
	 * desired
	 * 
	 * @return ReturnValue to be parsed for use, or NULL if not used
	 */
	public ReturnValue getReturn() {
		return null;
	}

	/**
	 * Stops this process, if not otherwise changed this code will simply kill the
	 * process and clean up after it. This is not a guaranteed clean kill switch,
	 * and can cause deadlocks if the process is not properly informed of its
	 * disposal.
	 */
	public void halt() {
		if (t == null)
			return;
		t.stop();
		if (sc != null)
			sc.close();
		sc = null;
		System.gc();
	}

	/**
	 * Sets the UUID of this process, occurs at spawning of process and cannot be
	 * used to change the UUID later on.
	 * 
	 * @param u
	 *            New UUID to use as the processes UUID.
	 */
	public final void setUUID(UUID u) {
		if (!uuidset) {
			this.u = u;
			uuidset = true;
		}
	}

	/**
	 * Encapsulates process in Thread, executes start() within Thread. Allows for
	 * process monitoring and has built in task suspension capabilities
	 * 
	 * @return True if the process executed successfully
	 */
	public final boolean run() {
		stop = false;
		isGoing = true;
		// Create a new thread in which to run this process
		t = new Thread(new Runnable() {
			public void run() {
				// Add the process to the process manager (JSHProcesses)
				setUUID(UUID.randomUUID());
				JSHProcesses.addProcess(me);
				try {
					// Run the task assigned to this process
					if (start())
						;
					else
						log.log("Returned non-true value!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Cleanup
				stop();
				t.interrupt();
				isGoing = false;
				stop = true;

				return;
			}
		});
		t.setName(getName());
		try {
			// Run the thread which contains the task to be executed
			t.start();
			boolean suspended = false;
			// Simple process monitor
			while (!t.isInterrupted() && !stop && isGoing) {
				// Check every 20ms if the process is either suspended, or stopped
				Thread.sleep(20);
				if (suspend & !suspended) {
					t.wait();
					suspended = true;
				} else {
					if (!suspend & suspended) {
						t.notify();
						suspended = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// If the process has completed, run the stop procedure from the Process Manager
			JSHProcesses.stopProcess(me);
			if (log == null) {
				log.log("LOG");
			}
			// Cleanup
			LogManager.removeLogger(log);
			halt();
		} catch (Exception e) {
			LogManager.out.println("[JSHPM] Unable to deregister Logger for " + getName());
			e.printStackTrace();
		}

		return true;

	}

	/**
	 * This should only be invoked by JProcesses start(); method
	 * 
	 * @param holdup
	 *            If true, acts like a normal JProcess, if false, excludes process
	 *            monitoring and suspension capabilities
	 * @return True if process executes successfully
	 */
	public final boolean run(final boolean holdup) {
		stop = false;
		isGoing = true;
		if (!holdup) {
			s = null;
		}
		t = new Thread(new Runnable() {
			public void run() {
				setUUID(UUID.randomUUID());
				JSHProcesses.addProcess(me);
				try {
					if (start())
						;
					else
						log.log("Returned non-true value!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				stop();
				t.interrupt();
				isGoing = false;
				stop = true;

				if (!holdup) {
					JSHProcesses.stopProcess(me);
					LogManager.removeLogger(log);
					halt();
				}

				return;
			}
		});
		t.setName(getName());
		try {
			t.start();
			if (holdup)
				try {
					boolean suspended = false;
					while (!t.isInterrupted() && !stop && isGoing) {
						Thread.sleep(20);
						if (suspend & !suspended) {
							t.wait();
							suspended = true;
						} else {
							if (!suspend & suspended) {
								t.notify();
								suspended = false;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (holdup) {
				JSHProcesses.stopProcess(me);
				LogManager.removeLogger(log);
				halt();
			}
		} catch (Exception e) {
			LogManager.out.println("[JSHPM] Unable to deregister Logger for " + getName());
		}

		return true;

	}

	/**
	 * Gives the UUID of this process, as generated at the processes spawning.
	 * 
	 * @return The processes UUID.
	 */
	public final UUID getUUID() {
		return u;
	}

	/**
	 * Identify if the process is running
	 * 
	 * @return True if process is running
	 */
	public final boolean isRunning() {
		return isGoing;
	}

	/**
	 * Change the OutputStream that this process will write to.
	 * 
	 * @param out
	 *            The new OutputStream to be written to.
	 */
	public final void setOutputStream(OutputStream out) {
		log.setOutputStream(out);
	}

	/**
	 * Returns the OutputStream currently being written to by the process.
	 * 
	 * @return The current OutputStream being written to.
	 */
	public final OutputStream getOutputStream() {
		return log.getOutputStream();
	}

	/**
	 * Redirect this processes input, will create a new Scanner() object with this
	 * InputStream.
	 * 
	 * @param s
	 *            New InputStream to read from.
	 */
	public final void redirectIn(InputStream s) {
		this.s = s;
		sc = new Scanner(s);
		return;
	}

	/**
	 * Toggle whether or not to suspend this process. Suspension is passed to the
	 * code running in the process, and therefore is not guaranteed to occur.
	 */
	public void suspend() {
		suspend = !suspend;
	}

	public final boolean isSuspended() {
		return suspend;
	}

	/**
	 * Whether or not this JProcess can be successfully serialized
	 * 
	 * @return
	 */
	public final boolean canSerialize() {
		return canBeSerialized;
	}

	// List dependencies for JProcess so it can be reinitialized on other systems
	// properly
	// If the JProcess is a nested class, be sure to include the enclosing class as
	// a dependency
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.LOCAL_VARIABLE, ElementType.TYPE })
	public @interface Depends {
		public Class<?>[] dependencies() default Object.class;

	}

	public @interface ReturnType {
		public terra.shell.utils.system.ReturnType getReturnType() default terra.shell.utils.system.ReturnType.VOID;
	}

	public class ReturnObjectWrapper implements Serializable {
		private Object o;

		public ReturnObjectWrapper(Object o) {
			this.o = o;
		}

		public Object getReturnObject() {
			return o;
		}
	}

}
