package terra.shell.utils.lua.builtin.sockethandler;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;

public enum LuaSocketHandlerLibrary implements BiConsumer<Environment, LuaObject>, LuaMethod {
	createsocket {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			Lua.checkArgs(name(), args, LuaType.STRING, LuaType.INTEGER);
			String inetAddress = args[0].getString();
			int port = args[1].getInt();
			LuaSocketHandler lsh = new LuaSocketHandler(interp, new InetSocketAddress(inetAddress, port));
			return lsh;
		}
	};

	@Override
	public LuaObject call(LuaInterpreter arg0, LuaObject[] arg1) {
		return null;
	}

	@Override
	public void accept(Environment t, LuaObject u) {
		String name = toString();
		if (name != null && !name.trim().isEmpty())
			u.rawSet(name, Lua.newMethod(this));
	}

}
