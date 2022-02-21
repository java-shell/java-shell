package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import terra.shell.emulation.concurrency.helper.LockedVariable;
import terra.shell.utils.system.GeneralVariable;
import terra.shell.utils.system.SerializableVariable;
import terra.shell.utils.system.Variables;
import terra.shell.utils.system.types.Encrypted;
import terra.shell.utils.system.types.Encrypted.LockedVariableKey;

public class LockedVariableTest {

	@Test
	public void test() throws Exception {
		SerializableVariable gVar = new SerializableVariable("Test", "TESTVARIABLE");
		LockedVariable var = new LockedVariable("TestVar", gVar);
		Variables.setVar(var);
		LockedVariableKey key = var.getKey();
		assertEquals(var, Variables.getVar("TestVar"));

		LockedVariable v = (LockedVariable) Variables.getVar("TestVar");
		Encrypted enc = (Encrypted) v.getType();
		assertThrows(IllegalAccessException.class, () -> {
			enc.getKey();
		});

		SerializableVariable testString = (SerializableVariable) enc.getDecryptedObject(key);
		assertEquals(gVar, testString);
	}

}
