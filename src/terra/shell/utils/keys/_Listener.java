package terra.shell.utils.keys;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.EventManager;

/**
 * _Listener.class is an event interface. Specifically this will interface with
 * any EVDEV block or char device. When an event is fired in EVDEV _Listener
 * will interpret the information given to it using any interpreters provided by
 * EventInterpreter.class.<br>
 * This uses JNI code found in /lib/_JSHIN.so to natively read from the devices.
 * 
 * @author dan
 * 
 */
public class _Listener {
	private static Logger log = LogManager.getLogger("JSHI");

	/**
	 * Start up the Listener, this should happen automagically so don't create a
	 * new one.
	 */
	public _Listener() {
		try {
			final Scanner sc = new Scanner(new FileInputStream(
					"/config/JSHI/config.config"));
			while (sc.hasNextLine()) {
				registerDevice(sc.nextLine());
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.err("JSHI FAILURE");
		}
	}

	/**
	 * Start listening for events from this device.
	 * 
	 * @param dev
	 *            The path to the device
	 */
	public void registerDevice(final String dev) {
		final File f = new File(dev);
		if (!f.exists()) {
			log.log("File not found! " + dev);
			return;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				setupSub(dev);
			}
		});
		t.setName("JSHID:" + dev);
		t.start();
		log.log("Registered Device " + dev);
	}

	/**
	 * Fire an event! *BOOM*<br>
	 * Used only by the JNI code, there is really no other reason to attempt to
	 * fire a raw event from here.
	 * 
	 * @param dev
	 * @param codes
	 * @param values
	 * @param types
	 */
	public static void eventHappened(String dev, int[] codes, int[] values,
			int[] types) {
		// log.log(dev + ": ");
		int type;
		int value;
		int code;
		for (int i = 0; i < types.length; i++) {
			code = codes[i];
			value = values[i];
			type = types[i];
			EventManager.invokeEvent(EventInterpreter.interpret(dev,
					new EventInformation(code, value, type)));
		}
	}

	/**
	 * Prints a message as _Listener.class, used by the JNI code to interact
	 * textually with the Terminal.
	 * 
	 * @param s
	 */
	public void printMessage(String s) {
		log.log(s);
	}

	private native void setupSub(String device);

}
