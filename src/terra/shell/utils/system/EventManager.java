package terra.shell.utils.system;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.modules.ModuleEvent.DummyEvent;
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
	 * Register a Listener which will be triggered by the Event type specified with
	 * evtype.
	 * 
	 * @param el     The Listener to be triggered.
	 * @param evtype Which event should triggesr the Listener.
	 */
	public static void registerListener(EventListener el, String evtype) {
		if (listeners.containsKey(evtype)) {
			listeners.get(evtype).add(el);
			log.debug("Registered listener for event: " + evtype);
		}
	}

	/**
	 * Let the manager know about this type of event so it can trigger Listeners
	 * listening to this event type.<br>
	 * An event type should match the Event.getType() value of the target Event.
	 * 
	 * @param type The event type to register.
	 */
	public static void registerEvType(String type) {
		listeners.put(type, new ArrayList<EventListener>());
		log.debug("Add new event registration type: " + type);
	}

	/**
	 * Invoke an Event. This will search through all registered evtypes and trigger
	 * any related Listeners. The Event.getType() value is used as the evtype.
	 * 
	 * @param e Event to invoke.
	 */
	public static void invokeEvent(Event e) {
		try {
			if (e == null) {
				return;
			}
			// CODEAT Debug line
			// log.log("Invoking: " + e.getCreator());
			String id = e.getClass().getConstructors()[0].getAnnotation(Event.EventPriority.class).id();
			if (id.equals(Event.GENERAL_TYPE)) {
				id = e.getCreator();
			}

			if (id.equals(Event.INIT_TYPE) && e instanceof Launch.InitEvent) {
				Enumeration<ArrayList<EventListener>> all = listeners.elements();
				while (all.hasMoreElements()) {
					ArrayList<EventListener> evs = all.nextElement();
					for (EventListener ev : evs) {
						ev.trigger(e);
					}
				}
				return;
			}

			final int priority = e.getClass().getConstructors()[0].getAnnotation(Event.EventPriority.class).value();

			final ArrayList<EventListener> avail = listeners.get(id);

			if (avail == null) {
				return;
			}

			for (int i = 0; i < avail.size(); i++) {
				avail.get(i).trigger(e);
				// CODEAT Debug line
				log.debug("Triggering: " + e.getCreator());
			}
			id = null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
