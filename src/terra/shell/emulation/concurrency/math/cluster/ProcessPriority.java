package terra.shell.emulation.concurrency.math.cluster;

/**
 * 
 * @author schirripad@moravian.edu
 *
 */
public enum ProcessPriority {

	/**
	 * EXTREME priority, send process to server with least ping, overrides FULL
	 * flag, IMMEDIATE
	 */
	EXTREME(4),
	/**
	 * HIGH priority, send process to server with least ping unless FULL, IMMEDIATE
	 */
	HIGH(3),
	/**
	 * MEDIUM priority, send process to random server unless FULL, adds process to
	 * send queue
	 */
	MEDIUM(2),
	/**
	 * LOW priority, send process to random server unless FULL, adds process to
	 * bottom of send queue
	 */
	LOW(1),
	/**
	 * LOWEST priority, sends process to server with highest ping unless FULL, adds
	 * process to bottom of send queue
	 */
	LOWEST(0);

	private int priority;

	ProcessPriority(int priority) {
		this.priority = priority;
	}

	public int asInt() {
		return priority;
	}

}
