package terra.shell.utils.system;

import terra.shell.utils.keys.Event;

public abstract class EventListener {
	public EventListener() {

	}

	public abstract void trigger(Event e);

	public final void register(String type) {
		EventManager.registerListener(this, type);
	}

}
