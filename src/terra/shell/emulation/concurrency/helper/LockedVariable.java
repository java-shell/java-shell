package terra.shell.emulation.concurrency.helper;

import terra.shell.utils.system.Variable;
import terra.shell.utils.system.types.Encrypted;
import terra.shell.utils.system.types.Encrypted.LockedVariableKey;

public class LockedVariable implements Variable {
	private Encrypted enc;
	private String varName;

	public LockedVariable(String varName, Object decryptedObject) throws Exception {
		enc = new Encrypted(decryptedObject);
		this.varName = varName;
	}

	public LockedVariableKey getKey() throws IllegalAccessException {
		return enc.getKey();
	}

	@Override
	public Type getType() {
		return enc;
	}

	@Override
	public String getVarName() {
		return varName;
	}

	@Override
	public String getVarValue() {
		return "__ENCRYPTED__";
	}

}
