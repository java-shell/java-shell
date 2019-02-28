package terra.shell.utils.perms.tokens;

import terra.shell.utils.perms.PermissionToken;
import terra.shell.utils.system.user.User;

public class WriteToken extends PermissionToken {

	public WriteToken(User u) {
		super(u);
	}

	@Override
	public String getPermissionValue() {
		return "token:write";
	}

}
