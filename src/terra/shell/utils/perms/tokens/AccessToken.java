package terra.shell.utils.perms.tokens;

import terra.shell.utils.perms.PermissionToken;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement.UserValidation;

public class AccessToken extends PermissionToken {

	public AccessToken(User u, UserValidation token) {
		super(u, token);
	}

	@Override
	public String getPermissionValue() {
		return "token:access";
	}

}
