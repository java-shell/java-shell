package terra.shell.utils;

import java.io.Serializable;
import java.util.UUID;

import terra.shell.utils.system.JSHProcesses;

/**
 * Returned data from a remote JProcess execution, which originated at this
 * Node.
 * 
 * @author schirripad@moravian.edu
 *
 * @param <T> Type of Data being Returned
 */
public abstract class ReturnValue<T> implements Serializable {
	protected final UUID uid;
	protected final UUID suid;

	/**
	 * Create a ReturnValue that will be referenced using this JProcesses SUID and
	 * UUID values
	 * 
	 * @param p
	 */
	public ReturnValue(JProcess p) {
		UUID tsuid = p.getSUID();
		if (tsuid == null) {
			p.setSUID(UUID.randomUUID());
			tsuid = p.getSUID();
			JSHProcesses.addProcess(p);
		}
		suid = tsuid;
		uid = p.getUUID();
	}

	/**
	 * Return the loaded process ID of the referenced JProcess
	 * 
	 * @return
	 */
	public final UUID getProcessID() {
		return uid;
	}

	/**
	 * Return the running process ID of the referenced JProcess
	 * 
	 * @return
	 */
	public final UUID getSUID() {
		return suid;
	}

	/**
	 * @deprecated Use {@link #setValues(Object...)} instead
	 */
	public abstract boolean processReturn(Object... values);

	/**
	 * Set the ReturnValue data values
	 * 
	 * @param values
	 * @return
	 */
	public abstract boolean setValues(T values);

	/**
	 * Retrieve the ReturnValue data values
	 * 
	 * @return
	 */
	public abstract T getReturnValue();

}
