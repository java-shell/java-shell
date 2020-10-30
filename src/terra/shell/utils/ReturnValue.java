package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

public abstract class ReturnValue implements Serializable {
	public abstract UUID getProcessID();

	public abstract boolean processReturn(Object... values);

	public abstract Object[] getReturnValue();
}
