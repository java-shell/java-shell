package terra.shell.utils.system.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public final class Updater {

	private static final Configuration conf = getConf();

	private static final int versionID = 11212021;
	private static final String ip = "http://repo.java-shell.com";
	private static final Logger log = LogManager.getLogger("UpdateManager");
	private static boolean hasUpdates = false;
	private static int remoteVersion = -1;

	/*
	 * TODO General plan is to pull from an update repository, download the updated
	 * jar, and install/restart the program.
	 */

	public final static boolean checkForUpdates() throws UnknownHostException, IOException {
		if (hasUpdates) {
			return true;
		}
		// TODO Check for updates based on a pre-set server/repository location
		URL versionLogURL = new URL("https://repo.java-shell.com/versionLog");
		Scanner vLogIn = new Scanner(versionLogURL.openStream());
		String remoteVersionLine = vLogIn.nextLine();
		int remoteVersion = Integer.parseInt(remoteVersionLine);
		if (remoteVersion != versionID) {
			log.log("Update Available");
			log.log("Current Version: " + versionID);
			log.log("Remote Version: " + remoteVersion);
			log.log("Please run \"update\" to initialize update");
			Updater.remoteVersion = remoteVersion;
			hasUpdates = true;
			return true;
		}
		vLogIn.close();
		return false;
	}

	public final static File downloadUpdate() throws IOException {
		// TODO Download the latest update from a pre-set server/repository location
		URL updateJarDownload = new URL("https://repo.java-shell.com/" + remoteVersion + ".jar");
		File updateJar = new File(Launch.getConfD().getParent(), remoteVersion + ".jar");
		FileOutputStream jarOut = new FileOutputStream(updateJar);
		updateJarDownload.openStream().transferTo(jarOut);
		File runSh = new File(Launch.getConfD().getParent(), "run.sh");
		if (!runSh.exists()) {
			runSh.createNewFile();
		}
		FileOutputStream runShOut = new FileOutputStream(runSh);
		PrintStream out = new PrintStream(runShOut);
		out.println("#!/bin/sh");
		out.println("java -jar " + updateJar.getAbsolutePath());
		out.flush();
		out.close();
		return updateJar;
	}

	private final static Configuration getConf() {
		Configuration conf = Launch.getConfig("updater");
		if (conf == null) {
			conf = new Configuration(new File(Launch.getConfD(), "updater"));
			conf.setValue("versionId", versionID);
			conf.setValue("repoUrl", "repo.java-shell.com");
		}
		return conf;
	}

	private final class UpdateShutdownHook implements Runnable {

		@Override
		public void run() {
			// TODO Launch a separate process which waits until this JSH instance is
			// completely destroyed, and then restarts a new instance
			try {
				Runtime.getRuntime().exec("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
