package terra.shell.utils.keys;

import java.util.Hashtable;
import java.util.Set;

/**
 * Despite its name this is a manager of Interpreters. Each of these
 * Interpreters is actually an EventType.
 * 
 * @author dan
 * 
 */
public class EventInterpreter {
	private static Hashtable<String, EventType> interpreters = new Hashtable<String, EventType>();
	private static Hashtable<String, EventType> types = new Hashtable<String, EventType>();

	/**
	 * Attempts to get the EventType registered with this type name. Then uses
	 * its createEvent() function to create an event.
	 * 
	 * @param type
	 *            The EventType to use as an interpreter.
	 * @param data
	 *            Raw info.
	 * @return An Event created by the EventType.
	 */
	public static Event interpret(String type, EventInformation data) {
		EventType e = interpreters.get(type);
		if (e != null)
			return e.createEvent(data);
		return null;
	}

	/**
	 * Register an EventType with its textual name.
	 * 
	 * @param type
	 *            The textual representation of this EventType.
	 * @param e
	 *            The EventType to be registered under this name.
	 */
	public static void registerType(String type, EventType e) {
		interpreters.put(type, e);
	}

	/**
	 * Registers an EventType with a textual name specified by this EventTypes
	 * .type() function.
	 * 
	 * @param e
	 *            EventType to register.
	 */
	public static void addType(EventType e) {
		types.put(e.type(), e);
	}

	/**
	 * Gives a Set<> of all available interpreters.
	 * 
	 * @return A Set<> of all available interpreters in a textual
	 *         representation.
	 */
	public static Set<String> availInterpreters() {
		return types.keySet();
	}

	/**
	 * Get the EventType registered with this textual name.
	 * 
	 * @param type
	 *            The textual name under which the desired EventType is
	 *            registered under.
	 * @return The desired EventType or null if not found.
	 */
	public static EventType getType(String type) {
		return types.get(type);
	}

}
