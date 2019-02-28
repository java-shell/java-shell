package terra.shell.utils.keys;

public interface EventType {
	public Event createEvent(EventInformation data);

	public String type();
}
