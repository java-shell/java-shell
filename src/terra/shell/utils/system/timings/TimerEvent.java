package terra.shell.utils.system.timings;

import terra.shell.utils.keys.Event;

public interface TimerEvent extends Event {
	@Override
	public String getCreator();

	public void run();
	public int getInt();

}
