package terra.shell.utils.system.timings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import terra.shell.utils.keys.Event;

public final class Timer implements ActionListener {
	private static final javax.swing.Timer t = new javax.swing.Timer(0, new Timer());
	private static final ArrayList<TimerEvent> liveEvents = new ArrayList<TimerEvent>();
	private static final Hashtable<TimerEvent, Hashtable<Integer, Integer>> liveTime = new Hashtable<TimerEvent, Hashtable<Integer, Integer>>();

	public static void start() {
		if (!t.isRunning())
			t.start();
	}

	public static void addEvent(Event e) {
		try {
			TimerEvent te = (TimerEvent) e;
			liveEvents.add(te);
			Hashtable<Integer, Integer> tmp = new Hashtable<Integer, Integer>();
			tmp.put(0, 1);
			liveTime.put(te, tmp);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public static Event removeEvent(Event e) {
		if (liveEvents.contains(e)) {
			Event e1 = liveEvents.get(liveEvents.indexOf(e));
			liveEvents.remove(liveEvents.indexOf(e));
			liveTime.remove(e);
			return e1;
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Enumeration<Hashtable<Integer, Integer>> en = liveTime.elements();
		int i = 0;
		while (en.hasMoreElements()) {
			TimerEvent te = liveEvents.get(i);
			te.run();
			i++;
		}
	}
}
