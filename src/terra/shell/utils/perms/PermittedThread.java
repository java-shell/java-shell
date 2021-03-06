package terra.shell.utils.perms;

import terra.shell.utils.system.user.User;

public class PermittedThread extends Thread {

	private final User user;

	public PermittedThread(Runnable target) {
		super(target);
		Thread t = currentThread();
		if (t instanceof PermittedThread) {
			user = ((PermittedThread) t).retrieveUser();
		} else
			user = null;
	}

	public PermittedThread(Runnable target, User user) {
		super(target);
		this.user = user;
	}

	public User retrieveUser() {
		return user;
	}

}
