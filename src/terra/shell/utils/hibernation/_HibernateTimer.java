package terra.shell.utils.hibernation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Timer;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.modules.ModuleManagement;
import terra.shell.utils.JProcess;

public class _HibernateTimer extends JProcess {
	private static Timer t;
	private static boolean isHibernating = false;
	private static Logger log = LogManager.getLogger("HT");

	@Override
	public String getName() {
		 
		return "HT";
	}

	@Override
	public boolean start() {
		startTimer();
		return false;
	}

	private static void startTimer() {
		t = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Launch.willHibernate() && !isHibernating) {
					isHibernating = true;
					final Thread thread = new Thread(new Runnable() {
						public void run() {
							log.log("Starting hibernation...");
							final Enumeration<String> lm = ModuleManagement
									.getModules();
							while (lm.hasMoreElements()) {
								ModuleManagement.disable(lm.nextElement());
							}
							System.runFinalization();
							System.gc();

							while (Launch.willHibernate()) {
								try {
									Thread.sleep(10);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							log.log("Reversing Hibernation Sequence...");
							final Enumeration<String> ml = ModuleManagement
									.getModules();
							while (ml.hasMoreElements()) {
								ModuleManagement.enable(ml.nextElement());
							}
							System.runFinalization();
							System.gc();
							log.log("Hibernation Sequence reversed!");
							isHibernating = false;
							return;
						}
					});
					thread.setName("Hibernation");
					thread.start();

				}
			}
		});
		t.setDelay(1000);
		t.start();
	}
}
