package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

import terra.shell.utils.system.JSHProcesses;

public abstract class ReturnValue implements Serializable {
	protected final UUID uid;
	protected final UUID suid;

	public ReturnValue(JProcess p) {
		UUID tsuid = p.getSUID();
		if(tsuid == null) {
			p.setSUID(UUID.randomUUID());
			tsuid = p.getSUID();
			JSHProcesses.addProcess(p);
		}
		suid = tsuid;
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
