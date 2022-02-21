package terra.shell.utils.system.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import terra.shell.utils.system.ByteClassLoader;
import terra.shell.utils.system.Variable;

public final class Encrypted extends Variable.Type {

	private String salt, encodedKey, encodedObject, objKey, serializedObj;
	private boolean keyRetrieved = false;

	public Encrypted(Object decrypted) throws Exception {
		byte[] salt = new byte[64];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(salt);

		byte[] keyBytes = new byte[25];
		sr.nextBytes(keyBytes);
		String key = Base64.getEncoder().encodeToString(keyBytes);

		KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 20000, 160);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

		this.salt = Base64.getEncoder().encodeToString(salt);
		this.encodedKey = Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded());
		this.encodedObject = Base64.getEncoder().encodeToString(encryptObject(decrypted));
	}

	private byte[] encryptObject(Object o) throws NoSuchAlgorithmException, IOException, Exception {
		InputStream cIn;
		try {
			cIn = o.getClass().getClassLoader()
					.getResourceAsStream(o.getClass().getName().replaceAll("\\.", "/") + ".class");
		} catch (Exception e) {
			cIn = ClassLoader.getSystemClassLoader()
					.getResourceAsStream(o.getClass().getName().replaceAll("\\.", "/") + ".class");
		}
		byte[] bytes = cIn.readAllBytes();
		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
		keyPair.initialize(2048);
		KeyPair pair = keyPair.generateKeyPair();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());

		byte[] buf = new byte[100];
		byte[] itr = new byte[0];
		byte[] fin = new byte[0];
		for (int i = 0; i < bytes.length; i++) {
			if ((i > 0) && (i % 100 == 0)) {
				itr = cipher.doFinal(buf);
				byte[] combiner = new byte[itr.length + fin.length];
				for (int j = 0; j < fin.length; j++) {
					combiner[j] = fin[j];
				}
				for (int j = 0; j < itr.length; j++) {
					combiner[fin.length + j] = itr[j];
				}
				fin = combiner;
				combiner = null;
				if (i + 100 > bytes.length)
					buf = new byte[bytes.length - i];
				else
					buf = new byte[100];
			}
			buf[i % 100] = bytes[i];
		}

		itr = cipher.doFinal(buf);
		byte[] combiner = new byte[itr.length + fin.length];
		for (int j = 0; j < fin.length; j++) {
			combiner[j] = fin[j];
		}
		for (int j = 0; j < itr.length; j++) {
			combiner[fin.length + j] = itr[j];
		}
		fin = combiner;
		combiner = null;

		byte[] objBytes = fin;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(bout);
		objOut.writeObject(o);
		objOut.flush();
		objOut.close();
		objOut = null;

		bytes = bout.toByteArray();

		buf = new byte[100];
		itr = new byte[0];
		fin = new byte[0];
		for (int i = 0; i < bytes.length; i++) {
			if ((i > 0) && (i % 100 == 0)) {
				itr = cipher.doFinal(buf);
				combiner = new byte[itr.length + fin.length];
				for (int j = 0; j < fin.length; j++) {
					combiner[j] = fin[j];
				}
				for (int j = 0; j < itr.length; j++) {
					combiner[fin.length + j] = itr[j];
				}
				fin = combiner;
				combiner = null;
				if (i + 100 > bytes.length)
					buf = new byte[bytes.length - i];
				else
					buf = new byte[100];
			}
			buf[i % 100] = bytes[i];
		}

		itr = cipher.doFinal(buf);
		combiner = new byte[itr.length + fin.length];
		for (int j = 0; j < fin.length; j++) {
			combiner[j] = fin[j];
		}
		for (int j = 0; j < itr.length; j++) {
			combiner[fin.length + j] = itr[j];
		}
		fin = combiner;
		combiner = null;

		cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
		byte[] serializedBytes = fin;
		this.serializedObj = Base64.getEncoder().encodeToString(serializedBytes);

		objKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

		return objBytes;
	}

	//TODO Add block based decryption, similar to how the encryption method functions
	public Object getDecryptedObject(LockedVariableKey lockKey) throws Exception {
		String key = lockKey.key1;
		String priv = lockKey.key2;
		KeySpec keySpec = new PBEKeySpec(key.toCharArray(), Base64.getDecoder().decode(salt), 2000, 160);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		if (encodedKey == (Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded()))) {
			KeyFactory factory = KeyFactory.getInstance("RSA");
			EncodedKeySpec encKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(priv));
			PrivateKey privateKey = factory.generatePrivate(encKeySpec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encodedObject));
			byte[] decryptedSerialized = cipher.doFinal(Base64.getDecoder().decode(serializedObj));

			ByteClassLoader bcl = new ByteClassLoader(null);
			Class<?> c = bcl.getClass(decrypted);

			ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(decryptedSerialized));
			return objIn.readObject();
		}
		return null;
	}

	public LockedVariableKey getKey() throws IllegalAccessException {
		if (!keyRetrieved)
			throw new IllegalAccessException("Attempted to retrieve key twice");
		keyRetrieved = true;
		return new LockedVariableKey(encodedKey, objKey);
	}

	@Override
	public String getTypeName() {
		return "__ENCRYPTED__";
	}

	public final class LockedVariableKey {
		private final String key1, key2;

		public LockedVariableKey(String key1, String key2) {
			this.key1 = key1;
			this.key2 = key2;
		}
	}

}
