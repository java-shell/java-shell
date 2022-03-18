package terra.shell.utils.perms.tokens;

import terra.shell.utils.perms.PermissionToken;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement.UserValidation;

public class WriteToken extends PermissionToken {

	public WriteToken(User u, UserValidation token) {
		super(u, token);
	}

	@Override
	public String getPermissionValue() {
		return "token:write";
	}

}
