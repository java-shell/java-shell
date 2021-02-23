package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

public abstract class ReturnValue implements Serializable {
	private final UUID id;
	
	public ReturnValue(JProcess p) {
		id = p.getUUID();
	}
	
	public final UUID getProcessID() {
		return id;
	}

	public abstract boolean processReturn(Object... values);

	public abstract Object[] getReturnValue();
}
