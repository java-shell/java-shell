package terra.shell.command.builtin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import terra.shell.command.Command;
import terra.shell.utils.perms.Permissions;
import terra.shell.utils.system.Encoder;

public class Compile extends Command {

	private static final long serialVersionUID = 7295882983372359703L;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "compile";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "DS";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return "T3RRA";
	}

	@Override
	public ArrayList<String> getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean start() {
		if (args.length < 1) {
			getLogger().log("Not Enough Arguments!");
			return false;
		} else {
			if (args.length == 1) {
				if (args[0].equals("usage") || args[0].equals("help")) {
					getLogger().log("Usage: compile <compFile> <outDir>");
				}
			} else if (args.length == 2) {
				// final String type = args[0];
				final File comp = new File(args[0]);
				final File odir = new File(args[1]);
				if (comp.exists() && odir.exists() & odir.isDirectory()) {
					getLogger().log("Compiling .tx from classlist-(Compilation File)");
					try {
						final Scanner sc = new Scanner(new FileInputStream(comp));
						final ArrayList<String> classes = new ArrayList<String>();
						while (sc.hasNextLine()) {
							classes.add(sc.nextLine());
						}
						getLogger().log("Isolating Main...");
						final String packagen = classes.get(0);
						final File main = new File(classes.get(1));
						final File[] peripherals = new File[classes.size() - 2];
						getLogger().log("Loading peripheral resource classes...");
						for (int i = 2; i < classes.size(); i++) {
							peripherals[i - 2] = new File(classes.get(i));
						}
						getLogger().log("Building file...");
						ArrayList<Byte> file = new ArrayList<Byte>();
						getLogger().log("Constructing header...");
						// Add File Start
						file.add((byte) 52);
						// Add Header Start
						file.add((byte) 50);
						final char[] chars = packagen.toCharArray();
						for (int i = 0; i < packagen.length(); i++) {
							file.add(Encoder.parseChar(chars[i]));
						}
						// Add Header End
						file.add((byte) 47);
						getLogger().log("Creating class structure...");
						getLogger().log("Adding main class...  [" + main.getName() + "]");
						getLogger().log("Creating class header...");
						String cname = main.getName().substring(0, main.getName().length() - 6);
						// Embed Class Length in Name
						cname = cname + ":" + main.length();
						final char[] chars1 = cname.toCharArray();
						// Add Class Start
						file.add((byte) 51);
						// Add Class Header Start
						file.add((byte) 50);
						// Encode/Add Class Name
						for (int i = 0; i < cname.length(); i++) {
							file.add(Encoder.parseChar(chars1[i]));
						}
						// Add Class Header End
						file.add((byte) 47);
						getLogger().log("Embedding class...");
						BufferedInputStream in = new BufferedInputStream(new FileInputStream(main));
						int b;
						// Add Class Bytes
						while ((b = in.read()) > -1) {
							file.add((byte) b);
						}
						for (int i = 0; i < peripherals.length; i++) {
							getLogger().log("Adding peripheral resource class... [" + peripherals[i].getName() + "]");
							getLogger().log("Creating class header...");
							String cname1 = peripherals[i].getName().substring(0,
									peripherals[i].getName().length() - 6);
							// Embed Class Length in Name
							cname1 = cname1 + ":" + peripherals[i].length();
							final char[] chars2 = cname1.toCharArray();
							// Add Class Start
							file.add((byte) 51);
							// Add Class Header Start
							file.add((byte) 50);
							// Encode/Add Class Name
							for (int i2 = 0; i2 < cname1.length(); i2++) {
								file.add(Encoder.parseChar(chars2[i2]));
							}
							// Add Class Header End
							file.add((byte) 47);
							getLogger().log("Embedding class...");
							in = new BufferedInputStream(new FileInputStream(peripherals[i]));
							int b1;
							// Add Class Bytes
							while ((b1 = in.read()) > -1) {
								file.add((byte) b1);
							}
						}
						getLogger().log("Closing File...");
						// Close File
						file.add((byte) 49);
						in = null;

						// Generate MD5
						getLogger().log("Generating MD5");

						MessageDigest md = MessageDigest.getInstance("MD5");
						for (byte b0 : file) {
							md.update(b0);
						}
						// Encode MD5
						byte[] md5 = md.digest();

						String mdHash = DatatypeConverter.printHexBinary(md5).toUpperCase();

						byte[] md5Encode = Encoder.parseString(mdHash);

						byte[] md5Length = Encoder.parseString("" + md5Encode.length);

						// Add MD5 length header
						// TODO Add Identifying byte to signal start and end of length statement

						file.add(0, (byte) 53);
						for (int i = 0; i < md5Length.length; i++) {
							file.add(1, md5Length[i]);
						}
						// TODO End length header
						// Add MD5 hash to beginning of file

						file.add(md5Length.length + 1, (byte) 48);

						// Fix add statement to adjust for index shift
						for (int i = 0; i < md5Encode.length; i++) {
							file.add(md5Length.length + 2, md5Encode[i]);
						}

						getLogger().log("Writing File...");
						BufferedOutputStream bout = new BufferedOutputStream(
								new FileOutputStream(new File(odir.getAbsolutePath() + "/out.tx")));
						for (int i = 0; i < file.size(); i++) {
							bout.write(file.get(i));
						}

						getLogger().log("Cleaning up...");
						bout.flush();
						bout.close();
						bout = null;
						return true;
					} catch (Exception e) {
						e.printStackTrace();
						getLogger().log("Compilation terminated! FAILURE");
					}
				} else {
					if (!comp.exists()) {
						getLogger().log("Class List not found!");
					}
					if (!odir.exists() || !odir.isDirectory()) {
						getLogger().log("Output Directory not found!");
					}
				}

			} else {
				getLogger().log("Too Many Arguments!");
			}
		}

		return true;
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return true;
	}
}
