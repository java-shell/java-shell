package terra.shell.utils.perms;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.system.user.InvalidUserException;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement;
import terra.shell.utils.system.user.UserManagement.UserValidation;

@SuppressWarnings({ "deprecation", "removal" })
public class PermissionManager extends SecurityManager {
	private HashMap<User, HashSet<PermissionToken>> permissionTokens = new HashMap<User, HashSet<PermissionToken>>();
	private HashMap<String, PermissionToken> registeredPermissions = new HashMap<String, PermissionToken>();
	private final Logger log = LogManager.getLogger("PermissionsManagement");

	public PermissionManager() {
		log.debug("PermissionManager active ----------------------------------");
	}

	public synchronized void checkUserPermission(PermissionToken perm, User u, User local, UserValidation validation)
			throws InvalidUserException {
		if (UserManagement.checkUserValidation(u, validation)) {
		} else
			throw new InvalidUserException();
		UserManagement.checkUserPermissionAccess(perm.getPermissionValue(), u);
	}

	public synchronized void registerPermissionToken(PermissionToken token, User u, UserValidation validation)
			throws InvalidUserException {
		if (!UserManagement.checkUserValidation(u, validation))
			throw new InvalidUserException();
		if (registeredPermissions.containsKey(token.getPermissionValue())) {
			return;
		}
		registeredPermissions.put(token.getPermissionValue(), token);
	}

	public synchronized PermissionToken assignPermissionToken(User u, User local, UserValidation validation,
			PermissionToken perm) throws InvalidUserException {
		if (u == null || local == null || validation == null || perm == null) {
			return null;
		}
		if (UserManagement.checkUserValidation(u, validation)) {
			if (!UserManagement.checkUserPermissionAccess(perm.getPermissionValue(), u))
				throw new SecurityException(
						"User " + u.getUserName() + " does not have access to permission " + perm.getPermissionValue());
			if (!permissionTokens.containsKey(u)) {
				permissionTokens.put(u, new HashSet<PermissionToken>());
			}
			permissionTokens.get(u).add(perm);
		} else
			throw new InvalidUserException();
		return perm;
	}

	// TODO Need to figure out how to isolate an object within a Thread, and utilize
	// that to give thread-based permissions
	// UPDATE: Utilize PermittedThread to check thread for User access
	private User checkThreadWrapping() {
		Thread t = Thread.currentThread();
		if (t instanceof PermittedThread) {
			User u = ((PermittedThread) t).retrieveUser();
			if (u != null)
				return u;
		}
		throw new SecurityException();
	}

	@Override
	protected Class<?>[] getClassContext() {
		return super.getClassContext();
	}

	@Override
	public Object getSecurityContext() {
		return super.getSecurityContext();
	}

	@Override
	public void checkPermission(Permission perm) {
		log.debug("Got permission request for: " + perm.getName());
		log.debug(perm.getActions());
		return;
		// User threadUser = checkThreadWrapping();
		// if (UserManagement.checkUserPermissionAccess(perm.getName(), threadUser))
		// return;
		// throw new SecurityException();

	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		User threadUser = checkThreadWrapping();
		UserManagement.checkUserPermissionAccess(perm.getName(), threadUser);
	}

	@Override
	public void checkCreateClassLoader() {
		super.checkCreateClassLoader();
	}

	@Override
	public void checkAccess(Thread t) {
		super.checkAccess(t);
	}

	@Override
	public void checkAccess(ThreadGroup g) {
		super.checkAccess(g);
	}

	@Override
	public void checkExit(int status) {
		super.checkExit(status);
	}

	@Override
	public void checkExec(String cmd) {
		super.checkExec(cmd);
	}

	@Override
	public void checkLink(String lib) {
		super.checkLink(lib);
	}

	@Override
	public void checkRead(FileDescriptor fd) {
		super.checkRead(fd);
	}

	@Override
	public void checkRead(String file) {
		super.checkRead(file);
	}

	@Override
	public void checkRead(String file, Object context) {
		super.checkRead(file, context);
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
		super.checkWrite(fd);
	}

	@Override
	public void checkWrite(String file) {
		super.checkWrite(file);
	}

	@Override
	public void checkDelete(String file) {
		super.checkDelete(file);
	}

	@Override
	public void checkConnect(String host, int port) {
		super.checkConnect(host, port);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		super.checkConnect(host, port, context);
	}

	@Override
	public void checkListen(int port) {
		super.checkListen(port);
	}

	@Override
	public void checkAccept(String host, int port) {
		super.checkAccept(host, port);
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		super.checkMulticast(maddr);
	}

	@Override
	public void checkMulticast(InetAddress maddr, byte ttl) {
		super.checkMulticast(maddr, ttl);
	}

	@Override
	public void checkPropertiesAccess() {
		super.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(String key) {
		super.checkPropertyAccess(key);
	}

	@Override
	public void checkPrintJobAccess() {
		super.checkPrintJobAccess();
	}

	@Override
	public void checkPackageAccess(String pkg) {
		super.checkPackageAccess(pkg);
	}

	@Override
	public void checkPackageDefinition(String pkg) {
		super.checkPackageDefinition(pkg);
	}

	@Override
	public void checkSetFactory() {
		super.checkSetFactory();
	}

	@Override
	public void checkSecurityAccess(String target) {
		super.checkSecurityAccess(target);
	}

	@Override
	public ThreadGroup getThreadGroup() {
		return super.getThreadGroup();
	}

}
