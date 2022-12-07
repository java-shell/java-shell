package terra.shell.utils.system;

import java.io.Serializable;

public class SerializableVariable extends GeneralVariable implements Serializable {

	public SerializableVariable(String name, String value) {
		super(name, value);
	}

}
