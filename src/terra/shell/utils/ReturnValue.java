package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

public abstract class ReturnValue implements Serializable {
	private UUID id;
	
	public ReturnValue(JProcess p) {
		id = p.getSUID();
	}
	
	public final UUID getProcessID() {
		return id;
	}

	public abstract boolean processReturn(Object... values);

	public abstract Object[] getReturnValue();
}
