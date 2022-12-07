package terra.shell.utils.system;

import terra.shell.utils.system.types.Global;

/**
 * A general text variable.
 * 
 * @author dan
 * 
 */
public class GeneralVariable implements Variable {
	private String value;
	private String name;

	/**
	 * Registers a text value under a variable name.
	 * 
	 * @param name  The name to register under.
	 * @param value The value to register under this name.
	 */
	public GeneralVariable(String name, String value) {
		this.value = value;
		this.name = name;
	}

	@Override
	public Type getType() {
		 
		return new Global();
	}

	@Override
	public String getVarName() {
		 
		return name;
	}

	@Override
	public String getVarValue() {
		return value;
	}

}
