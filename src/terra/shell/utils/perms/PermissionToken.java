package terra.shell.utils.perms;

import java.util.UUID;
import terra.shell.utils.system.user.User;

public abstract class PermissionToken {
	private final UUID permID = UUID.randomUUID();
	private User associatedUser;

	public PermissionToken(User u) {
		associatedUser = u;
	}

	public User getUser() {
		return associatedUser;
	}

	public UUID getUUID() {
		return permID;
	}

	public abstract String getPermissionValue();
}
