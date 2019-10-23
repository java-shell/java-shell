package terra.shell.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.Variables;

public class EncryptedConfiguration extends Configuration {
	private static Key key = new SecretKeySpec("f_+=4dshi%ot6)ew2534890*&^hjkl%4$78fhjdksu".getBytes(), "AES");
	private Logger log = LogManager.getLogger("EncryptedConfiguration");

	public EncryptedConfiguration(File f) {
		// Find a way so that the file can be passed to super as an
		// bytearrayinputstream, so it can be passed through a decoder first

	}

	@Override
	protected void _write() {
		Set<String> keys = vlist.keySet();
		try {
			if (getType() == 0) {
				final PrintStream fout = new PrintStream(Encrypter.encrypt(new FileOutputStream(f)), true);
				for (int i = 0; i < vlist.size(); i++) {
					fout.println(keys.toArray()[i] + ":" + vlist.get(keys.toArray()[i]));
				}
				fout.flush();
				fout.close();
			}
			return;
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(log.getOutputStream()));
		}
	}

	@Override
	protected void parse() {
		if (!f.exists())
			return;
		InputStream tmp = null;
		int type = getType();
		if (type == 0) {
			try {
				tmp = new FileInputStream(f);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		if (type == 1) {
			tmp = in;
		}
		if (type == 2) {
			try {
				tmp = u.openStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			tmp = Decrypter.decrypt(tmp);
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(log.getOutputStream()));
			log.err("Failed to decrypt configuration!");
			tmp = null;
		}
		if (tmp == null) {
			log.log("Failed to get InputStream for Configuration!");
			return;
		}
		Scanner sc = new Scanner(tmp);
		String st;
		while (sc.hasNext() && (st = sc.nextLine()) != null) {
			if (!st.startsWith("#")) {
				final String[] bk = st.split(":");
				if (bk.length == 2) {
					if (bk[1].contains("%")) {
						final String tm = Variables.getVarValue(bk[2]);
						if (tm != null) {
							bk[1] = tm;
						}
					}
					vlist.put(bk[0], bk[1]);
				} else {
					if (bk.length == 1)
						vlist.put(bk[0], "null");
				}
			}
		}
		sc.close();
		sc = null;
		st = null;
		tmp = null;
	}

	// CODEAT Probably won't work, throws no errors currently but
	private static class Encrypter {
		public static OutputStream encrypt(OutputStream out) throws NoSuchAlgorithmException, NoSuchPaddingException,
				InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
			// encode stream, then fill bytearrayoutputstream or something of the like
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			// Casting an outputstream to a bytearrayoutputstream most likely will not keep
			// the contents of that stream
			ByteArrayOutputStream bout = (ByteArrayOutputStream) out;
			byte[] enc = cipher.doFinal(bout.toByteArray());

			ByteArrayOutputStream encStream = new ByteArrayOutputStream();
			encStream.write(enc);
			return encStream;
		}
	}

	private static class Decrypter {
		public static InputStream decrypt(InputStream in) throws IOException, IllegalBlockSizeException,
				BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			// Decode stream, and then fill bytearrayinputsream
			byte[] buffer = new byte[256];
			int b, i = 0;
			while ((b = in.read()) != -1) {
				if (i == buffer.length) {
					cipher.update(buffer);
					i = 0;
				} else {
					buffer[i] = (byte) b;
					i++;
				}
			}
			cipher.update(buffer, 0, i);
			byte[] output = cipher.doFinal();
			return new ByteArrayInputStream(output);
		}
	}

}
