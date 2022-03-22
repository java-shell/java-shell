package terra.shell.utils.perms;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;

import terra.shell.utils.system.user.InvalidUserException;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement;
import terra.shell.utils.system.user.UserManagement.UserValidation;

@SuppressWarnings({ "deprecation", "removal" })
public class PermissionManager extends JavaShellSecurityManager<PermissionToken, User, UserValidation> {
	private HashMap<User, HashSet<PermissionToken>> permissionTokens = new HashMap<User, HashSet<PermissionToken>>();
	private HashMap<String, PermissionToken> registeredPermissions = new HashMap<String, PermissionToken>();

	@Override
	public synchronized void checkUserPermission(PermissionToken perm, User u, UserValidation validation)
			throws Exception {
		if (UserManagement.checkUserValidation(u, validation)) {
		} else
			throw new InvalidUserException();
		// TODO Change null
		UserManagement.checkUserPermissionAccess(perm.getPermissionValue(), u);

	}

	public synchronized void registerPermissionToken(PermissionToken token) {
		if (registeredPermissions.containsKey(token.getPermissionValue())) {
			return;
		}
		registeredPermissions.put(token.getPermissionValue(), token);
	}

	public synchronized PermissionToken assignPermissionToken(User u, UserValidation validation, PermissionToken perm)
			throws InvalidUserException {
		if (u == null || validation == null || perm == null) {
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

	@Override
	protected Class<?>[] getClassContext() {
		// TODO Auto-generated method stub
		return super.getClassContext();
	}

	@Override
	public Object getSecurityContext() {
		// TODO Auto-generated method stub
		return super.getSecurityContext();
	}

	@Override
	public void checkPermission(Permission perm) {
		// TODO Auto-generated method stub
		super.checkPermission(perm);
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
		// TODO Auto-generated method stub
		super.checkPermission(perm, context);
	}

	@Override
	public void checkCreateClassLoader() {
		// TODO Auto-generated method stub
		super.checkCreateClassLoader();
	}

	@Override
	public void checkAccess(Thread t) {
		// TODO Auto-generated method stub
		super.checkAccess(t);
	}

	@Override
	public void checkAccess(ThreadGroup g) {
		// TODO Auto-generated method stub
		super.checkAccess(g);
	}

	@Override
	public void checkExit(int status) {
		// TODO Auto-generated method stub
		super.checkExit(status);
	}

	@Override
	public void checkExec(String cmd) {
		// TODO Auto-generated method stub
		super.checkExec(cmd);
	}

	@Override
	public void checkLink(String lib) {
		// TODO Auto-generated method stub
		super.checkLink(lib);
	}

	@Override
	public void checkRead(FileDescriptor fd) {
		// TODO Auto-generated method stub
		super.checkRead(fd);
	}

	@Override
	public void checkRead(String file) {
		// TODO Auto-generated method stub
		super.checkRead(file);
	}

	@Override
	public void checkRead(String file, Object context) {
		// TODO Auto-generated method stub
		super.checkRead(file, context);
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
		// TODO Auto-generated method stub
		super.checkWrite(fd);
	}

	@Override
	public void checkWrite(String file) {
		// TODO Auto-generated method stub
		super.checkWrite(file);
	}

	@Override
	public void checkDelete(String file) {
		// TODO Auto-generated method stub
		super.checkDelete(file);
	}

	@Override
	public void checkConnect(String host, int port) {
		// TODO Auto-generated method stub
		super.checkConnect(host, port);
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		// TODO Auto-generated method stub
		super.checkConnect(host, port, context);
	}

	@Override
	public void checkListen(int port) {
		// TODO Auto-generated method stub
		super.checkListen(port);
	}

	@Override
	public void checkAccept(String host, int port) {
		// TODO Auto-generated method stub
		super.checkAccept(host, port);
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
		// TODO Auto-generated method stub
		super.checkMulticast(maddr);
	}

	@Override
	public void checkMulticast(InetAddress maddr, byte ttl) {
		// TODO Auto-generated method stub
		super.checkMulticast(maddr, ttl);
	}

	@Override
	public void checkPropertiesAccess() {
		// TODO Auto-generated method stub
		super.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(String key) {
		// TODO Auto-generated method stub
		super.checkPropertyAccess(key);
	}

	@Override
	public void checkPrintJobAccess() {
		// TODO Auto-generated method stub
		super.checkPrintJobAccess();
	}

	@Override
	public void checkPackageAccess(String pkg) {
		// TODO Auto-generated method stub
		super.checkPackageAccess(pkg);
	}

	@Override
	public void checkPackageDefinition(String pkg) {
		// TODO Auto-generated method stub
		super.checkPackageDefinition(pkg);
	}

	@Override
	public void checkSetFactory() {
		// TODO Auto-generated method stub
		super.checkSetFactory();
	}

	@Override
	public void checkSecurityAccess(String target) {
		// TODO Auto-generated method stub
		super.checkSecurityAccess(target);
	}

	@Override
	public ThreadGroup getThreadGroup() {
		// TODO Auto-generated method stub
		return super.getThreadGroup();
	}
	
}
