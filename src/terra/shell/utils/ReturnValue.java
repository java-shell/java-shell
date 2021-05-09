package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

public abstract class ReturnValue implements Serializable {
	protected final UUID uid, suid;

	public ReturnValue(JProcess p) {
		suid = p.getSUID();
		uid = p.getUUID();
	}

	public final UUID getProcessID() {
		return uid;
	}
	
	public final UUID getSUID() {
		return suid;
	}

	public abstract boolean processReturn(Object... values);

	public abstract Object[] getReturnValue();
}
