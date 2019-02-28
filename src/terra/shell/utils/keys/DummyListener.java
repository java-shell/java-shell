package terra.shell.utils.keys;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.keys.DummyType.DummyEvent;
import terra.shell.utils.system.EventListener;
import terra.shell.utils.system.GeneralVariable;
import terra.shell.utils.system.Variables;

/**
 * Un-Implemented debugging class.
 * 
 * @author dan
 * 
 */
public class DummyListener extends EventListener {
	private static boolean print;
	private Logger log = LogManager.getLogger("DummyListener");

	public DummyListener() {
		Variables.setVar(new GeneralVariable("Dummy.print", "true"));
	}

	@Override
	public void trigger(Event e) {
		try {
			print = Boolean.parseBoolean(Variables.getVarValue("Dummy.print"));
		} catch (Exception e1) {
		}
		if (e instanceof DummyEvent && print) {
			final DummyEvent de = (DummyEvent) e;
			if (de != null && de.getData() != null) {
				log.log("Type: " + de.getData().type());
				log.log("Code: " + de.getData().code());
				log.log("Value: " + de.getData().value());
				log.endln();
			}
		}
	}

	public static void doPrint(boolean prints) {
		print = prints;
	}

}
