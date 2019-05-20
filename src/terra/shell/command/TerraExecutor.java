package terra.shell.command;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.ByteClassLoader;
import terra.shell.utils.system.Encoder;
import terra.shell.utils.system.TX;

public class TerraExecutor {
	private static Logger log = LogManager.getLogger("TE");
	private static ByteClassLoader bcl = Launch.getClassLoader();

	public String getName() {
		// TODO Auto-generated method stub
		return "TE";
	}

	public static void run(URL path) throws Exception {
		// TODO Add MD5 checks on TX files
		Class<?> cf = null;
		ArrayList<Class<?>> res = new ArrayList<Class<?>>();
		TX tx = null;
		String bPre = "null";
		boolean hasMain = false;
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(path.openStream());
		} catch (Exception e) {
			try {
				in = new BufferedInputStream(new FileInputStream(new File(path.getPath())));
			} catch (Exception e1) {
				e1.printStackTrace();
				getLogger().log("Failed to open file!");
			}
		}
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		int nxt = in.read();
		bytes.add(Byte.parseByte(nxt + ""));
		while (nxt > -1) {
			nxt = in.read();
			bytes.add((byte) nxt);
		}
		if (bytes.size() > 0) {
			final byte start = bytes.get(0);
			for (int i = 1; i < bytes.size(); i++) {
				final byte b = bytes.get(i);
				if (start == 52 && !hasMain) {
					if (b == 50) {
						final ArrayList<Character> chars = new ArrayList<Character>();
						i++;
						byte tmp;
						while ((tmp = bytes.get(i)) != 47) {
							chars.add(Encoder.parseByte(tmp));
							i++;
						}
						String tot = chars.toString().replaceAll("[, \\[\\]]", "");
						bPre = tot;
					}
					i++;
					final byte b2 = bytes.get(i);
					if (b2 == 51) {
						i++;
						final byte b1 = bytes.get(i);
						if (b1 == 50) {
							final ArrayList<Character> chars = new ArrayList<Character>();
							i++;
							byte tmp;
							while ((tmp = bytes.get(i)) != 47) {
								chars.add(Encoder.parseByte(tmp));
								i++;
							}
							i++;
							Character[] string = new Character[chars.size()];
							string = chars.toArray(string);
							String tot = chars.toString().replaceAll("[, \\[\\]]", "");
							int bl = 0;
							String cname = "null";
							try {
								final String[] temp = tot.split(":");
								if (temp.length == 2) {
									bl = Integer.parseInt(temp[1]);
									cname = temp[0];
								} else {
									throw new ArrayIndexOutOfBoundsException();
								}
							} catch (Exception e) {
								getLogger().log("Failed to parse int during SOC! Unable to load classes!");
								getLogger().log("Error: " + e.getMessage());
								return;
							}
							byte[] cb = new byte[bl];
							for (int i2 = 0; i2 < bl; i2++) {
								cb[i2] = bytes.get(i + i2);
							}
							cf = bcl.getClass(cb);
							try {
								tx = (TX) cf.newInstance();
								hasMain = true;
							} catch (Exception e) {
								getLogger().log("Failed to cast main class to TX class");
								return;
							}
						}
					}
				}
				if (b == 51) {
					getLogger().log("Loading Class");
					final ArrayList<Character> chars = new ArrayList<Character>();
					i++;
					byte tmp, origin;
					origin = bytes.get(i);
					if (origin == 50) {
						i++;
						while ((tmp = bytes.get(i)) != 47) {
							chars.add(Encoder.parseByte(tmp));
							i++;
						}
						i++;
						Character[] string = new Character[chars.size()];
						string = chars.toArray(string);
						String tot = chars.toString().replaceAll("[, \\[\\]]", "");
						int bl = 0;
						String cname = "null";
						try {
							final String[] temp = tot.split(":");
							if (temp.length == 2) {
								bl = Integer.parseInt(temp[1]);
								cname = temp[0];
							} else {
								throw new ArrayIndexOutOfBoundsException();
							}
						} catch (Exception e) {
							getLogger().log("Failed to parse integer during SOC! Unable to load classes!");
							getLogger().log("Error: " + e.getMessage());
							return;
						}
						byte[] cb = new byte[bl];
						for (int i2 = 0; i2 < bl; i2++) {
							cb[i2] = bytes.get(i + i2);
						}
						final Class<?> temp = bcl.getClass(cb);
						res.add(temp);
					}
				}
			}
			if (tx != null)
				tx.start();
			else {
				getLogger().log("Failed to launch TX File, Unable to find main class within file!");
				return;
			}
			tx = null;
			bcl = null;
			res.clear();
			System.gc();
			bcl = Launch.getClassLoader();
		}
	}
}
