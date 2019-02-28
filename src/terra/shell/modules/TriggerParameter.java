package terra.shell.modules;

import terra.shell.modules.ModuleEvent.DummyEvent;

public class TriggerParameter {
	public DummyEvent event;

	public TriggerParameter(DummyEvent event) {
		this.event = event;
	}
}