package terra.shell.utils.system.user;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import terra.shell.utils.perms.PermissionToken;
import terra.shell.utils.system.user.UserManagement.UserValidation;

public class User implements Serializable {

	private int id;
	private List<String> perms = new LinkedList<String>();
	private String userName, userHash;

	public User(String userName, int id) {
		this.userName = userName;
		this.id = id;
	}

	public final String getUserName() {
		return userName;
	}

	public final boolean hasPermission(String token) {
		if (perms.contains(token))
			return true;
		return false;
	}

	public final void givePermission(PermissionToken token, User u, UserValidation validation)
			throws InvalidUserException {
		if (UserManagement.checkUserValidation(u, validation)) {
			perms.add(token.getPermissionValue());
		}
		throw new InvalidUserException();
	}

	public final int getUserID() {
		return id;
	}

}
