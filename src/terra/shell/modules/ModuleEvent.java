package terra.shell.modules;

import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventManager;

/**
 * Event used to communicate with modules without being within the same scope of
 * the modules. This event will invoke itself using EventManager.invokeEvent()
 * and therefore does not need to be invoked manually.
 * 
 * @author dan
 * 
 */
public class ModuleEvent {
	private Object[] args;
	private String type;

	/**
	 * Create a DummyEvent to be fired towards the Module named
	 * 
	 * @param type Module name to send event to.
	 * @param args Arguments to send to the module.
	 */
	public ModuleEvent(String type, Object... args) {
		this.args = args;
		this.type = type;

		EventManager.invokeEvent(new DummyEvent(this));
	}

	/**
	 * Gets this ModuleEvents module target.
	 * 
	 * @return The Module name that is targeted.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the arguments within this ModuleEvent.
	 * 
	 * @return The arguments within this ModuleEvent.
	 */
	public Object[] getArgs() {
		return args;
	}

	/**
	 * Nothing you need to be concerned with.<br>
	 * These are not the droids you are looking for.
	 * 
	 * @author dan
	 * 
	 */
	public class DummyEvent implements Event {
		private ModuleEvent me;

		@EventPriority(id = GENERAL_TYPE, value = 0)
		public DummyEvent(ModuleEvent me) {
			this.me = me;
		}

		@Override
		public String getCreator() {
			return ("M:" + type);
		}

		public String getType() {
			return type;
		}

		public ModuleEvent getME() {
			return me;
		}

		@Override
		public Object[] getArgs() {
			Object[] args = { getCreator(), me.args };
			return args;
		}

	}

}
