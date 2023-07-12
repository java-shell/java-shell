package terra.shell.utils.lua.builtin.sockethandler;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;

import terra.shell.logging.LogManager;

import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;

public class LuaSocketHandler extends LuaUserdata {
	private LuaInterpreter interp;
	private Socket sock;
	private Scanner scanner;
	private InetSocketAddress address;
	private LuaObject handlerFunction, errorFunction, disconnectFunction;
	private Thread socketInputMonitorThread;
	private static final LuaObject luaSocketHandlerMetatable = Lua.newTable();

	public LuaSocketHandler(LuaInterpreter interp, InetSocketAddress address) {
		this.interp = interp;
		sock = new Socket();
		this.address = address;
		metatable = luaSocketHandlerMetatable;
		socketInputMonitorThread = new Thread(new SocketInputMonitor());
		socketInputMonitorThread.setName(address.toString() + " LuaSocketHandler");
	}

	@Override
	public Object getUserdata() {
		return scanner;
	}

	@Override
	public String name() {
		return "Socket";
	}

	private Socket getSocket() {
		return sock;
	}

	private void connectSocket() {
		try {
			sock.connect(address);
			scanner = new Scanner(sock.getInputStream());
			socketInputMonitorThread.start();
		} catch (Exception e) {
			fireError(e.getMessage());
			throw new LuaException(e.getMessage());
		}
	}

	private void setDataHandler(LuaObject function) {
		handlerFunction = function;
	}

	private void setErrorHandler(LuaObject function) {
		errorFunction = function;
	}

	private void fireError(String error) {
		if (errorFunction != null)
			errorFunction.call(interp, Lua.newString(error));
	}

	private void setDisconnectHandler(LuaObject function) {
		disconnectFunction = function;
	}

	private class SocketInputMonitor implements Runnable {

		@Override
		public void run() {

			while (!sock.isClosed()) {
				scanner.hasNext();
				if (handlerFunction != null)
					handlerFunction.call(interp, Lua.NIL);
			}

			if (disconnectFunction != null)
				disconnectFunction.call(interp, Lua.NIL);
		}

	}

	static {
		luaSocketHandlerMetatable.rawSet("__name", Lua.newString("SOCKET"));
		luaSocketHandlerMetatable.rawSet("__index", luaSocketHandlerMetatable);

		LuaObject readFunction = Lua.newMethod(new LuaMethod() {

			@Override
			public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
				if (args.length < 1 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Read", "Socket expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				Scanner scanner = (Scanner) lsh.getUserdata();
				try {
					String next = scanner.next();
					return Lua.newString(next);
				} catch (Exception e) {
					lsh.fireError(e.getMessage());
					try {
						lsh.getSocket().close();
					} catch (Exception e1) {
						e1.printStackTrace();
						lsh.fireError(e1.getMessage());
					}
				}
				return Lua.NIL;
			}

		});

		LuaObject readLineFunction = Lua.newMethod(new LuaMethod() {

			@Override
			public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
				if (args.length < 1 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Readline", "Socket expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				Scanner scanner = (Scanner) lsh.getUserdata();
				try {
					String next = scanner.nextLine();
					return Lua.newString(next);
				} catch (Exception e) {
					lsh.fireError(e.getMessage());
					try {
						lsh.getSocket().close();
					} catch (Exception e1) {
						e1.printStackTrace();
						lsh.fireError(e1.getMessage());
					}
				}
				return Lua.NIL;
			}

		});

		LuaObject writeFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Write", "Socket expected");
				if (args[1].type() != LuaType.STRING)
					throw Lua.badArgument(1, "Write", "String expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				Socket sock = lsh.getSocket();
				try {
					sock.getOutputStream().write(args[1].getString().getBytes());
				} catch (Exception e) {
					lsh.fireError(e.getMessage());
				}
			}

		});

		LuaObject setDataHandlerFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Data", "Socket expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				lsh.setDataHandler(args[1]);
			}

		});

		LuaObject setErrorHandlerFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Data", "Socket expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				lsh.setErrorHandler(args[1]);
			}

		});

		LuaObject setDisconnectHandlerFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Data", "Socket expected");

				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				lsh.setDisconnectHandler(args[1]);
			}

		});

		LuaObject connectFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 1 || !(args[0] instanceof LuaSocketHandler))
					throw Lua.badArgument(0, "Connect", "Socket expected");
				LuaSocketHandler lsh = (LuaSocketHandler) args[0];
				lsh.connectSocket();
			}

		});

		luaSocketHandlerMetatable.rawSet("Read", readFunction);
		luaSocketHandlerMetatable.rawSet("ReadLine", readLineFunction);
		luaSocketHandlerMetatable.rawSet("Write", writeFunction);
		luaSocketHandlerMetatable.rawSet("SetDataHandler", setDataHandlerFunction);
		luaSocketHandlerMetatable.rawSet("SetErrorHandler", setErrorHandlerFunction);
		luaSocketHandlerMetatable.rawSet("SetDisconnectHandler", setDisconnectHandlerFunction);
		luaSocketHandlerMetatable.rawSet("Connect", connectFunction);
	}

}
