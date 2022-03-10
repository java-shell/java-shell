package terra.shell.utils.system.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

import terra.shell.config.Configuration;
import terra.shell.launch.Launch;
import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public final class UserManagement {
	protected static final Hashtable<String, User> registeredUser = new Hashtable<String, User>();
	protected static final Hashtable<User, Password> userCredentials = new Hashtable<User, Password>();
	protected static final Hashtable<User, HashSet<UserValidation>> currentValidationTokens = new Hashtable<User, HashSet<UserValidation>>();
	protected static final Logger log = LogManager.getLogger("UserManagement");
	protected static HashMap<String, File> userInfoFiles;
	protected static boolean init = false;

	public synchronized static void init() {
		if (init)
			return;

		log.debug("Starting UserManagement");
		init = true;
		Configuration conf = Launch.getConfig("userManagement");
		if (conf == null) {
			conf = new Configuration(new File(Launch.getConfD(), "userManagement"));
			conf.setValue("userNames", "");
		}
		String userNames = (String) conf.getValue("userNames");
		String[] users = userNames.split(",");
		File userDirectory = new File(Launch.getConfD(), "userInfo");
		if (!userDirectory.exists()) {
			userDirectory.mkdir();
		}
		userInfoFiles = new HashMap<String, File>();
		synchronized (userInfoFiles) {
			File[] userFiles = userDirectory.listFiles();
			for (File userFile : userFiles) {
				try {
					FileInputStream fin = new FileInputStream(userFile);
					byte[] userNameBytes = new byte[25];
					fin.read(userNameBytes);

					String userName = new String(userNameBytes).trim();
					userInfoFiles.put(userName, userFile);
					log.debug(userFile.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		init = true;
	}

	public synchronized static User getUser(String userName, User local, UserValidation token) {
		if (checkUserValidation(local, token)) {
			if (registeredUser.containsKey(userName)) {
				return registeredUser.get(userName);
			}
		}
		return null;
	}

	private synchronized static boolean saveUser(User u, Password pass) {
		try {

			File userDirectory = new File(Launch.getConfD(), "userInfo");
			if (!userDirectory.exists()) {
				userDirectory.mkdir();
			}

			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, pass.secret);

			File outputFile;

			if (pass.secretFile == null) {
				byte[] fileNameBytes = new byte[64];
				SecureRandom.getInstanceStrong().nextBytes(fileNameBytes);
				String fileName = Base64.getEncoder().encodeToString(fileNameBytes).replaceAll("/", "-");
				outputFile = new File(userDirectory, fileName);
			} else
				outputFile = pass.secretFile;
			FileOutputStream fout = new FileOutputStream(outputFile);
			CipherOutputStream cout = new CipherOutputStream(fout, c);

			byte[] userNameBytes = u.getUserName().getBytes();
			byte[] userNameWriteBytes = new byte[25];

			for (int i = 0; i < userNameWriteBytes.length; i++) {
				if (i < userNameBytes.length) {
					userNameWriteBytes[i] = userNameBytes[i];
					continue;
				}
				userNameWriteBytes[i] = " ".getBytes()[0];
			}

			fout.write(userNameWriteBytes);
			fout.write(Base64.getDecoder().decode(pass.salt));

			// TODO Read from the encrypted file to load userinfo, add some way to ensure
			// that the user was created on this machine, and not added in while offline

			ObjectOutputStream oos = new ObjectOutputStream(cout);

			oos.writeObject(u);
			oos.writeObject(pass);
			oos.flush();
			cout.flush();
			fout.flush();
			oos.close();
			cout.close();
			fout.close();

			pass.secretFile = outputFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public synchronized static User logIn(String userName, String password) throws InvalidUserException {
		synchronized (userInfoFiles) {
			if (userInfoFiles.containsKey(userName)) {
				try {
					File userInfoFile = userInfoFiles.get(userName);
					FileInputStream fin = new FileInputStream(userInfoFile);
					byte[] userNameBytes = new byte[25];
					fin.read(userNameBytes);
					String userNameVerify = new String(userNameBytes).trim();
					if (!userName.equals(userNameVerify)) {
						fin.close();
						throw new InvalidUserException();
					}
					byte[] salt = new byte[32];
					fin.read(salt);
					KeySpec saveKeySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);

					SecretKey secret = new SecretKeySpec(
							SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(saveKeySpec).getEncoded(),
							"AES");
					Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
					c.init(Cipher.DECRYPT_MODE, secret);
					CipherInputStream cin = new CipherInputStream(fin, c);
					ObjectInputStream oin = new ObjectInputStream(cin);
					User userObject = (User) oin.readObject();
					Password passObject = (Password) oin.readObject();

					if (!passObject.doesMatch(password)) {
						fin.close();
						cin.close();
						oin.close();
						password = null;
						userObject = null;
						passObject = null;
						secret.destroy();
						secret = null;
						saveKeySpec = null;
						c = null;
						System.gc();
						throw new InvalidUserException();
					}

					userCredentials.put(userObject, passObject);
					registeredUser.put(userName, userObject);
					currentValidationTokens.put(userObject, new HashSet<UserValidation>());
					fin.close();
					cin.close();
					oin.close();
					password = null;
					return userObject;
				} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException
						| InvalidKeyException | ClassNotFoundException | DestroyFailedException e) {
					e.printStackTrace();
					if (e instanceof DestroyFailedException) {
						log.err("FATAL ERROR, UNABLE TO REMOVE SENSITIVE INFORMATION FROM MEMORY, KILLING SYSTEM TO PRESERVE DATA");
						System.exit(-5);
					}
					throw new InvalidUserException();
				}
			}
		}
		if (registeredUser.containsKey(userName)) {
			User u = registeredUser.get(userName);
			if (userCredentials.get(u).doesMatch(password)) {
				return u;
			}
		}
		throw new InvalidUserException();
	}

	public synchronized static boolean createNewUser(String userName, String password, User local,
			UserValidation token) {
		// FIXME Need to think up a better way to create first user
		if (registeredUser.size() == 0 && local == null && token == null) {
			if (userName.length() < 26)
				try {
					User u = new User(userName, SecureRandom.getInstanceStrong().nextInt());
					Password pass = PasswordEncrypter.createEncryptedPassword(password);
					userCredentials.put(u, pass);
					registeredUser.put(userName, u);
					currentValidationTokens.put(u, new HashSet<UserValidation>());
					saveUser(u, pass);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			return false;

		}
		if (checkUserValidation(local, token))
			if (userName.length() < 26)
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

	public synchronized static boolean removeUser(String userName, User local, UserValidation token) {
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

	public synchronized static boolean checkUserValidation(User u, UserValidation uV) {
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

	public synchronized static UserValidation getUserValidationToken(User u, String password)
			throws InvalidUserException {
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

				KeySpec saveKeySpec = new PBEKeySpec(rawText.toCharArray(), salt, 65536, 256);
				return new Password(encSalt, encPass,
						new SecretKeySpec(keyFactory.generateSecret(saveKeySpec).getEncoded(), "AES"));
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

	}

	public static final class Password implements Serializable {
		private String salt;
		private String encodedPassword;
		private SecretKey secret;
		private File secretFile = null;

		public Password(String salt, String encodedPassword, SecretKey secret) {
			this.salt = salt;
			this.encodedPassword = encodedPassword;
			this.secret = secret;
		}

		public boolean doesMatch(String pass) {
			try {
				KeySpec keySpec = new PBEKeySpec(pass.toCharArray(), Base64.getDecoder().decode(salt), 20000, 160);
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				if (encodedPassword
						.equals((Base64.getEncoder().encodeToString(keyFactory.generateSecret(keySpec).getEncoded()))))
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
