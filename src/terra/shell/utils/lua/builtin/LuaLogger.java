package terra.shell.utils.lua.builtin;

import java.util.function.Consumer;

import com.hk.lua.Lua;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public class LuaLogger extends LuaUserdata {
	private Logger log;
	private static final LuaObject luaLoggerMetatable = Lua.newTable();

	public LuaLogger(String name) {
		log = LogManager.getLogger(name);
	}

	@Override
	public String name() {
		return "logger";
	}

	@Override
	public Object getUserdata() {
		return log;
	}

	static {
		luaLoggerMetatable.rawSet("__name", Lua.newString("LOGGER"));
		luaLoggerMetatable.rawSet("__index", luaLoggerMetatable);

		LuaObject logFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] t) {
			}

		});

		luaLoggerMetatable.rawSet("log", logFunction);
	}

}
