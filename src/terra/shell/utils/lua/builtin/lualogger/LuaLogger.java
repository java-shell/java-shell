package terra.shell.utils.lua.builtin.lualogger;

import java.util.function.Consumer;

import com.hk.lua.Lua;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public class LuaLogger extends LuaUserdata {
	private Logger log;
	private static final LuaObject luaLoggerMetatable = Lua.newTable();

	public LuaLogger(String name) {
		log = LogManager.getLogger(name);
		metatable = luaLoggerMetatable;
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
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaLogger))
					throw Lua.badArgument(0, "log", "Logger expected");
				if (args[1].type() != LuaType.STRING)
					throw Lua.badArgument(1, "log", "String expected");

				LuaLogger luaLogger = (LuaLogger) args[0];
				Logger log = (Logger) luaLogger.getUserdata();
				log.log(args[1].getString());
			}

		});

		LuaObject debugFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaLogger))
					throw Lua.badArgument(0, "log", "Logger expected");
				if (args[1].type() != LuaType.STRING)
					throw Lua.badArgument(1, "log", "String expected");

				LuaLogger luaLogger = (LuaLogger) args[0];
				Logger log = (Logger) luaLogger.getUserdata();
				log.debug(args[1].getString());
			}

		});

		luaLoggerMetatable.rawSet("log", logFunction);
		luaLoggerMetatable.rawSet("debug", debugFunction);
	}

}
