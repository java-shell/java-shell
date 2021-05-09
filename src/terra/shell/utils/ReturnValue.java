package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

import terra.shell.utils.system.JSHProcesses;

public abstract class ReturnValue implements Serializable {
	protected final UUID uid;
	protected UUID suid;

	public ReturnValue(JProcess p) {
		suid = p.getSUID();
		if(suid == null) {
			p.setSUID(UUID.randomUUID());
			suid = p.getSUID();
			JSHProcesses.addProcess(p);
		}
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
