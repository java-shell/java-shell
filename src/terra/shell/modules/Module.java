package terra.shell.modules;

import java.io.Serializable;
import java.util.UUID;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.JProcess;
import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventManager;

/**
 * The Module is an extension of the Shell entirely. A module acts as a daemon
 * run at the start of the Shell, invoked and instantiated by
 * ModuleManagement.class. Modules can be hotloaded using
 * ModuleManagement.hotload(), but this is not recommended unless it is certain
 * not to effect running processes.
 * 
 * @author dan
 * 
 */
public abstract class Module implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7454742804164762194L;
	protected boolean up;
	protected Logger log = LogManager.getLogger(getName());
	private UUID u = UUID.randomUUID();
	protected JProcess container;

	/**
	 * Gets the name of this module.
	 * 
	 * @return The name of module. (Would you look at that)
	 */
	public abstract String getName();

	/**
	 * Runs the module!
	 */
	public abstract void run();

	/**
	 * Gets the modules version.
	 * 
	 * @return What do you think?
	 */
	public abstract String getVersion();

	/**
	 * Gets the Authors name.
	 * 
	 * @return Hmm...
	 */
	public abstract String getAuthor();

	/**
	 * Gets the Organization which created this module.
	 * 
	 * @return Tough one, I'll get back to you on this one.
	 */
	public abstract String getOrg();

	/**
	 * Runs code prior to being run, sort of an init() function.
	 */
	public abstract void onEnable();

	/**
	 * This function is triggered by the modules preloaded MAListener when an event
	 * is passed pointing to this module. The ModuleEvent which invoked MAListener
	 * is then passed to the module.
	 * 
	 * @param event The ModuleEvent which invoked MAListener.
	 */
	public abstract void trigger(Event event);

	/**
	 * Lets think real hard about this one for a second...
	 */
	public void start() {
		EventManager.registerEvType("M:" + getName());
		MAListener ml = new MAListener(this);
		EventManager.registerListener(ml, "M:" + getName());
		run();
	}
}
