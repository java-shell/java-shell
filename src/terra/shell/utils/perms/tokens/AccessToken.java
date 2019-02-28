package terra.shell.utils.perms.tokens;

import terra.shell.utils.perms.PermissionToken;
import terra.shell.utils.system.user.User;

public class AccessToken extends PermissionToken {

	public AccessToken(User u) {
		super(u);
	}

	@Override
	public String getPermissionValue() {
		return "token:access";
	}

}
