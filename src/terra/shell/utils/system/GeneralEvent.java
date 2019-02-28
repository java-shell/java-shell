package terra.shell.utils.system;

import terra.shell.utils.keys.Event;

public class GeneralEvent implements Event {
	private String type;
	private Object[] args;
	
	@EventPriority(id=GENERAL_TYPE, value=5)
	public GeneralEvent(String type, Object... args) {
		this.type = type;
		this.args = args;
	}

	@Override
	public String getCreator() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public Object[] getArgs(){
		return args;
	}

}
