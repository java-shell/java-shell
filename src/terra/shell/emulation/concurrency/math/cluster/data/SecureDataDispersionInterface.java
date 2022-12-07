package terra.shell.emulation.concurrency.math.cluster.data;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;

import terra.shell.utils.system.user.InvalidUserException;
import terra.shell.utils.system.user.User;
import terra.shell.utils.system.user.UserManagement;
import terra.shell.utils.system.user.UserManagement.UserValidation;

public class SecureDataDispersionInterface extends DataDispersionInterface {

	private static final long serialVersionUID = 8060377801521590774L;
	private User u;

	public SecureDataDispersionInterface(String mCastAddress, int port, NetworkInterface nic, User u)
			throws SocketException {
		super(mCastAddress, port, nic);
	}

	public void disperseData(BasicImmutableData<byte[]> data, UserValidation token)
			throws InvalidUserException, IOException {
		if (!UserManagement.checkUserValidation(u, token)) {
			throw new InvalidUserException();
		}
		super.disperseData(data);
	}

	public void registerListener(DataDispersionListener l, UserValidation token) throws InvalidUserException {
		if (!UserManagement.checkUserValidation(u, token)) {
			throw new InvalidUserException();
		}
		super.registerListener(l);
	}

	public void unregisterListener(DataDispersionListener l, UserValidation token) throws InvalidUserException {
		if (!UserManagement.checkUserValidation(u, token)) {
			throw new InvalidUserException();
		}
		super.unregisterListener(l);
	}
}
