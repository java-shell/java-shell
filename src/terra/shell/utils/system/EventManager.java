package terra.shell.utils.system;

import java.util.ArrayList;
import java.util.Hashtable;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.keys.Event;

/**
 * Lets see here, the name is EventManager, so I guess it manages cookie baking?
 * 
 * @author dan
 * 
 */
public final class EventManager {
	private static Hashtable<String, ArrayList<EventListener>> listeners = new Hashtable<String, ArrayList<EventListener>>();
	private static Logger log = LogManager.getLogger("EventManager");

	/**
	 * Register a Listener which will be triggered by the Event type specified
	 * with evtype.
	 * 
	 * @param el
	 *            The Listener to be triggered.
	 * @param evtype
	 *            Which event should triggesr the Listener.
	 */
	public static void registerListener(EventListener el, String evtype) {
		if (listeners.containsKey(evtype)) {
			listeners.get(evtype).add(el);
		}
	}

	/**
	 * Let the manager know about this type of event so it can trigger Listeners
	 * listening to this event type.<br>
	 * An event type should match the Event.getType() value of the target Event.
	 * 
	 * @param type
	 *            The event type to register.
	 */
	public static void registerEvType(String type) {
		listeners.put(type, new ArrayList<EventListener>());
	}

	/**
	 * Invoke an Event. This will search through all registered evtypes and
	 * trigger any related Listeners. The Event.getType() value is used as the
	 * evtype.
	 * 
	 * @param e
	 *            Esvent to invoke.
	 */
	public static void invokeEvent(Event e) {
		try {
			if (e == null) {
				return;
			}
			String id = e.getClass().getConstructors()[0].getAnnotation(
					Event.EventPriority.class).id();
			if (id.equals(Event.GENERAL_TYPE)) {
				id = e.getCreator();
			}

			final int priority = e.getClass().getConstructors()[0]
					.getAnnotation(Event.EventPriority.class).value();

			final ArrayList<EventListener> avail = listeners.get(id);

			if (avail == null) {
				return;
			}

			for (int i = 0; i < avail.size(); i++) {
				avail.get(i).trigger(e);
				// log.log("TRIGGERING");
			}
			id = null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
