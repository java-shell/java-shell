package terra.shell.utils.perms;

import java.util.UUID;

import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement.UserValidation;

public abstract class PermissionToken {
	private final UUID permID = UUID.randomUUID();
	private User associatedUser;
	private UserValidation token;

	public PermissionToken(User u, UserValidation token) {
		associatedUser = u;
		this.token = token;
	}

	public User getUser() {
		return associatedUser;
	}

	public UUID getUUID() {
		return permID;
	}

	public abstract String getPermissionValue();
}
