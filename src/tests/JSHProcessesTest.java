package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import terra.shell.command.builtin.Dir;
import terra.shell.utils.JProcess;
import terra.shell.utils.system.JSHProcesses;

public class JSHProcessesTest {

	@Test
	public void testProcessDescriptorBasedListing() {
		JProcess dummyProcess = new Dir();
		JSHProcesses.addProcess(dummyProcess);
		assertEquals(true, JSHProcesses.getProcess(dummyProcess.getUUID()).equals(dummyProcess));
	}
	
	@Test
	public void testAddingDuplicateProcess() {
		JProcess dummyProcess = new Dir();
		JSHProcesses.addProcess(dummyProcess);
		assertEquals(true, JSHProcesses.getProcess(dummyProcess.getUUID()).equals(dummyProcess));
		JSHProcesses.addProcess(dummyProcess);
		assertEquals(1, JSHProcesses.getCount());
	}
	

}
