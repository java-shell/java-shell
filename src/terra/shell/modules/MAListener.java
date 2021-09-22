package terra.shell.modules;

import terra.shell.launch.Launch;
import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventListener;

/**
 * Nothing to see here, its all good in the neighborhood
 * 
 * @author dan
 * 
 */
public final class MAListener extends EventListener {
	private Module m;

	public MAListener(Module m) {
		this.m = m;
	}

	@Override
	public void trigger(Event e) {
		if (e instanceof Launch.InitEvent) {
			m.trigger(e);
			return;
		}
		final ModuleEvent.DummyEvent me = (ModuleEvent.DummyEvent) e;
		m.trigger(me);
	}

}
