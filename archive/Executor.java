package terra.shell.utils.system.executable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

//import javax.xml.bind.DatatypeConverter;

import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.ByteClassLoader;
import terra.shell.utils.system.Encoder;
import terra.shell.utils.system.TX;

public final class Executor {
	private static ByteClassLoader bcl = Launch.getClassLoader();
	private static Logger log = LogManager.getLogger("Executor");
	{
		log.setOutputStream(System.err);
	}

	public static TX execute(InputStream in)
			throws InstantiationException, IllegalAccessException, IOException, MalformedExecutableException {
		return execute(in, true);
	}

	public static TX execute(InputStream in, boolean checkMD5)
			throws IOException, MalformedExecutableException, InstantiationException, IllegalAccessException {
		byte b;

		log.log("Executing TX Stream");

		log.log("Searching for MD5 Header");

		if ((b = (byte) in.read()) == 53) {
			final List<Byte> md5 = new ArrayList<Byte>();
			// Read MD5 length header
			log.log("Reading MD5 Header");
			while ((b = (byte) in.read()) != 48) {
				md5.add(b);
			}

			final byte[] md5Length = new byte[md5.size()];
			for (int i = 0; i < md5Length.length; i++) {
				md5Length[i] = md5.get(i);
			}

			final String lengthString = Encoder.parseByteArray(md5Length);

			log.log("Got MD5 length string: " + lengthString);

			int length = -1;

			try {
				length = Integer.parseInt(lengthString);
				if (length == -1)
					throw new Exception("Got negative MD5 length");
			} catch (Exception e) {
				throw new MalformedExecutableException("Failed to parse MD5 length");
			}

			md5.clear();
			log.log("Reading MD5");
			// Reverse MD5
			for (int i = 0; i < length; i++) {
				md5.add((byte) in.read());
			}

			final byte[] md5Encoded = new byte[md5.size()];

			for (int i = 0; i < md5.size(); i++)
				md5Encoded[i] = md5.get(i);

			String md5Hash = Encoder.parseByteArray(md5Encoded);

			if (checkMD5) {

				// Read Entire Stream, and create MD5 Digest, compare hashes
				log.log("Checking...");
				try {
					MessageDigest md = MessageDigest.getInstance("MD5");
					md5.clear();
					while (true) {
						b = (byte) in.read();
						md5.add(b);
						if (b == 49)
							break;
					}
					byte[] entireStream = new byte[md5.size()];
					for (int i = 0; i < entireStream.length; i++)
						entireStream[i] = md5.get(i);
					md.update(entireStream);
					// String currentHash =
					// DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
					// if (currentHash != md5Hash) {
					// throw new MD5MismatchException("Expected MD5: " + md5Hash + "\nGot: " +
					// currentHash);
					// }
					// Redirect 'in' to entireStream[]
					ByteArrayInputStream bin = new ByteArrayInputStream(entireStream);
					in = bin;
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		} else {
			log.log("Skipping MD5 Check");

		}
		log.log("Searching for File Start"); // Read File Start
		if (!(in.read() == 52)) {
			while (!(in.read() == 52)) {
			}
			// throw new MalformedExecutableException();
		}
		log.log("Found File Start");
		// Read Header Start
		if (!(in.read() == 50)) {
			throw new MalformedExecutableException();
		}
		log.log("Found File Header");
		String header = "";
		// Read Header Until End
		while ((b = (byte) in.read()) != 47) {
			header += Encoder.parseByte(b);
		}
		// Parse header
		// Read Class Start
		log.log("Searching for Class Start");
		if (!(in.read() == 51)) {
			throw new MalformedExecutableException();
		}
		log.log("Loading Class");
		Class<?> mainClass = loadClass(in);
		ArrayList<Class<?>> peripheralClasses = new ArrayList<Class<?>>();
		while ((b = (byte) in.read()) != 49) {
			if (b == 51) {
				peripheralClasses.add(loadClass(in));
			}
		}

		TX tx = (TX) mainClass.newInstance();
		return tx;
	}

	private static Class<?> loadClass(InputStream in) throws IOException, MalformedExecutableException {
		byte b;
		String classHeader = "";
		// Read Class Header Until End
		while ((b = (byte) in.read()) != 47) {
			classHeader += Encoder.parseByte(b);
		}

		// Parse classHeader
		final String[] classInfo = classHeader.split(":");

		if (classInfo.length != 2)
			throw new MalformedExecutableException();

		final String className = classInfo[0];
		final int classLength = Integer.parseInt(classInfo[1]);

		byte[] classBytes = new byte[classLength];
		return bcl.getClass(className, classBytes);

	}

}
