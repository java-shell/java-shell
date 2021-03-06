package terra.shell.utils.fs;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

/**
 * A class that allows reading and writing to and from a ZipFile as if it were a
 * filesystem UNFINISHED
 * 
 * @author schirripad@moravian.edu
 *
 */
public class ZipFileSystem {
	private static Logger log = LogManager.getLogger("ZF");
	private static Hashtable<String, File> fbyname = new Hashtable<String, File>();
	private static ArrayList<String> fnames = new ArrayList<String>();
	public final static File top = new File("u", true);

	/**
	 * Read the files within the ZipFile, and store them into a DataStructure
	 */
	public static void readFiles() {
		try {
			log.log("Reading File Zip");
			// Load in User zip system
			ZipFile zf = new ZipFile(new java.io.File("/u/user.zip"));
			Enumeration<? extends ZipEntry> e = zf.entries();
			// Load all entries
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				// If entry is directory, register as such
				if (entry.isDirectory()) {
					File f = new File(entry.getName(), true);
					log.log(f.getPath());
					fbyname.put(f.getPath(), f);
					fnames.add(f.getPath());
					// Otherwise register as regular file
				} else {
					File f = new File(entry.getName(), false);
					log.log(f.getPath());
					fbyname.put(f.getPath(), f);
					fnames.add(f.getPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log("Failed to access user zip!!");
		}
	}

	/**
	 * Add a file to the ZipFile
	 */
	public static void addFile() {

	}

	/**
	 * List all the files within a specific ZipFile
	 * 
	 * @param d The File within the ZipFile that is being read from as a directory
	 * @return A list of Files found under the parent directory 'd'
	 */
	public static File[] listFiles(File d) {
		final String tot = d.getPath();
		log.log(tot);
		ArrayList<File> found = new ArrayList<File>();
		for (int i = 0; i < fnames.size(); i++) {
			if (fnames.get(i).startsWith(tot))
				found.add(fbyname.get(fnames.get(i)));
		}
		File[] f = new File[found.size()];
		for (int i = 0; i < f.length; i++) {
			f[i] = found.get(i);
		}
		return f;
	}

	// TODO Replace file find function with a better one
	// The current one is really bad
	public static boolean findFile(File f) {
		// Search all files by path to find 'f'
		for (int i = 0; i < fnames.size(); i++) {
			if (fnames.get(i).startsWith(f.getPath()))
				return true;
		}
		return false;
	}

	public static File getFile(File f) {
		final String p = f.getPath();
		for (int i = 0; i < fnames.size(); i++) {
			if (fbyname.get(fnames.get(i)).getPath().startsWith(p))
				return fbyname.get(fnames.get(i));
		}
		return f;
	}

	/**
	 * Get a file represented by a String location within the ZipFile
	 * 
	 * @param fn The String path to use to find the File
	 * @return The File representing the data stored in the ZipFile at the location
	 *         determined by the String 'fn'
	 */
	public static File getFile(String fn) {
		for (int i = 0; i < fnames.size(); i++) {
			if (fnames.get(i).equals(fn)) {
				return fbyname.get(fnames.get(i));
			}
		}
		return null;
	}
}
