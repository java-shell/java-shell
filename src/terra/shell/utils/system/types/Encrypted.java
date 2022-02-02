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
import java.security.Signature;
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

	public Encrypted(Object decrypted, String key) throws Exception {
		byte[] salt = new byte[64];
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(salt);

		KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 20000, 160);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

		this.salt = Base64.getEncoder().encodeToString(salt);
		this.encodedKey = Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded());
		this.encodedObject = Base64.getEncoder().encodeToString(encryptObject(decrypted));
	}

	private byte[] encryptObject(Object o) throws NoSuchAlgorithmException, IOException, Exception {
		InputStream cIn = o.getClass().getClassLoader().getResourceAsStream(o.getClass().getName());
		byte[] bytes = cIn.readAllBytes();
		Signature sign = Signature.getInstance("SHA256withRSA");
		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
		keyPair.initialize(2048);
		KeyPair pair = keyPair.generateKeyPair();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
		cipher.update(bytes);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(bout);
		objOut.writeObject(o);
		objOut.flush();
		objOut.close();
		objOut = null;

		byte[] objBytes = cipher.doFinal();

		cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
		byte[] serializedBytes = cipher.doFinal(bout.toByteArray());
		this.serializedObj = Base64.getEncoder().encodeToString(serializedBytes);

		objKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

		return objBytes;
	}

	public Object getDecryptedObject(String key, String priv) throws Exception {
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

	@Override
	public String getTypeName() {
		return "__ENCRYPTED__";
	}

}
