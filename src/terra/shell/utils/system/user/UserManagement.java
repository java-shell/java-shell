package terra.shell.utils.system.user;

import java.io.File;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Hashtable;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;

public final class UserManagement {
	private static Hashtable<String, User> registeredUser = new Hashtable<String, User>();
	private static Hashtable<User, Password> userCredentials = new Hashtable<User, Password>();
	private static Hashtable<User, HashSet<UserValidation>> currentValidationTokens = new Hashtable<User, HashSet<UserValidation>>();
	private static boolean init = false;

	public static void init() {
		if (init)
			return;

		Configuration conf = Launch.getConfig("userManagement");
		if (conf == null) {
			conf = new Configuration(new File(Launch.getConfD(), "userManagement"));
			conf.setValue("userNames", "");
		}
		String userNames = (String) conf.getValue("userNames");
		String[] users = userNames.split(",");
		for (String user : users) {
			// TODO register users based on name
		}
		init = true;
	}

	public static User getUser(String userName, User local, UserValidation token) {
		if (checkUserValidation(local, token)) {
			if (registeredUser.contains(userName)) {
				return registeredUser.get(userName);
			}
		}
		return null;
	}

	public static User logIn(String userName, String password) throws InvalidUserException {
		if (registeredUser.contains(userName)) {
			User u = registeredUser.get(userName);
			if (userCredentials.get(u).doesMatch(password)) {
				return u;
			}
		}
		throw new InvalidUserException();
	}

	public static boolean createNewUser(String userName, String password, User local, UserValidation token) {
		if (checkUserValidation(local, token))
			try {
				User u = new User(userName, SecureRandom.getInstanceStrong().nextInt());
				Password pass = PasswordEncrypter.createEncryptedPassword(password);
				userCredentials.put(u, pass);
				registeredUser.put(userName, u);
				currentValidationTokens.put(u, new HashSet<UserValidation>());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return false;
	}

	public static boolean removeUser(String userName, User local, UserValidation token) {
		if (checkUserValidation(local, token)) {
			try {
				User u = registeredUser.get(userName);
				userCredentials.remove(u);
				registeredUser.remove(u);
				currentValidationTokens.remove(u);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	public static boolean checkUserValidation(User u, UserValidation uV) {
		if (currentValidationTokens.contains(u)) {
			if (currentValidationTokens.get(u).contains(uV)) {
				for (UserValidation lV : currentValidationTokens.get(u)) {
					if (lV.checkKey(uV.getRandKey()))
						return true;
				}
			}
		}
		return false;
	}

	public static UserValidation getUserValidationKey(User u, String password) throws InvalidUserException {
		if (userCredentials.contains(u)) {
			if (userCredentials.get(u).doesMatch(password)) {
				UserValidation uV = new UserValidation();
				if (!currentValidationTokens.contains(u))
					currentValidationTokens.put(u, new HashSet<UserValidation>());
				currentValidationTokens.get(u).add(uV);
				return uV;
			}
		}
		throw new InvalidUserException();
	}

	private static final class PasswordEncrypter {

		public static Password createEncryptedPassword(String rawText) {
			byte[] salt = new byte[32];
			try {
				SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
				sR.nextBytes(salt);

				KeySpec keySpec = new PBEKeySpec(rawText.toCharArray(), salt, 20000, 160);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

				String encSalt = Base64.getEncoder().encodeToString(salt);
				String encPass = Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded());
				return new Password(encSalt, encPass);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

	}

	public static final class Password {
		private String salt;
		private String encodedPassword;

		public Password(String salt, String encodedPassword) {
			this.salt = salt;
			this.encodedPassword = encodedPassword;
		}

		public boolean doesMatch(String pass) {
			try {
				KeySpec keySpec = new PBEKeySpec(pass.toCharArray(), Base64.getDecoder().decode(salt), 20000, 160);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				if (encodedPassword == (Base64.getEncoder()
						.encodeToString(keyFactory.generateSecret(keySpec).getEncoded())))
					return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return false;
		}
	}

	public static final class UserValidation implements Serializable {
		private byte[] salt;
		private String randKey;
		private final String encPass;

		public UserValidation() {
			String pass = null;
			try {
				salt = new byte[16];
				SecureRandom sR = SecureRandom.getInstance("SHA1PRNG");
				sR.nextBytes(salt);

				byte[] randKeyBytes = new byte[24];
				sR.nextBytes(randKeyBytes);

				randKey = new String(randKeyBytes);

				KeySpec keySpec = new PBEKeySpec(randKey.toCharArray(), salt, 20000, 160);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

				pass = Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded());
			} catch (Exception e) {
				e.printStackTrace();
			}
			encPass = pass;
		}

		private String getRandKey() {
			return randKey;
		}

		private boolean checkKey(String key) {
			try {
				KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 20000, 160);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

				String newPass = Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded());
				if (newPass.equals(encPass))
					return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

}
