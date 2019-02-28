package terra.shell.utils.keys;

public class EventInformation {
	private int code;
	private int value;
	private int type;

	public EventInformation(int code, int value, int type) {
		this.code = code;
		this.type = type;
		this.value = value;
	}

	public int code() {
		return code;
	}

	public int value() {
		return value;
	}

	public int type() {
		return type;
	}

}
