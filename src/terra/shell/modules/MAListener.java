package terra.shell.modules;

import terra.shell.launch.Launch;
import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventListener;

/**
 * Creates an EventListener object specific to a Module, only used by
 * ModuleManagement after Module loading
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
