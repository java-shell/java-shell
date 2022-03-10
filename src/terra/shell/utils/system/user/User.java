package terra.shell.utils.system.user;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import terra.shell.utils.perms.PermissionToken;

public class User implements Serializable {

	private int id;
	private List<PermissionToken> perms = new LinkedList<PermissionToken>();
	private String userName, userHash;

	public User(String userName, int id) {
		this.userName = userName;
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public boolean hasPermission(PermissionToken token) {

		return false;
	}

	public int getUserID() {
		return id;
	}

}
