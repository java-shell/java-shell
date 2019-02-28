package terra.shell.utils.system.user;

import java.util.LinkedList;
import java.util.List;

import terra.shell.utils.perms.PermissionToken;

public class User {

	private int id;
	private List<PermissionToken> perms = new LinkedList<PermissionToken>();

	public User(int id) {
		this.id = id;
	}

	public boolean hasPermission(PermissionToken token) {
		
		return false;
	}

	public int getUserID() {
		return id;
	}

}
