package terra.shell.emulation.concurrency.helper;

import terra.shell.utils.system.Variable;
import terra.shell.utils.system.types.Encrypted;

public class LockedVariable implements Variable {
	private Encrypted enc;
	private String varName;

	public LockedVariable(String varName, Object decryptedObject, String key) throws Exception {
		enc = new Encrypted(decryptedObject, key);
		this.varName = varName;
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
		return null;
	}

}
