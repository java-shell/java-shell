package terra.shell.utils.lua.builtin;

import java.util.function.BiConsumer;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public enum LuaLoggerLibrary implements BiConsumer<Environment, LuaObject>, LuaMethod {
	createlogger {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			LogManager.out.println("LUA CREATELOGGER CALLED");
			Lua.checkArgs(name(), args, LuaType.STRING);
			String loggerName = args[0].getString();
			LuaLogger log = new LuaLogger(loggerName);
			return log;
		}
	},
	log {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			Lua.checkArgs(name(), args, LuaType.USERDATA, LuaType.STRING);
			LuaLogger log = (LuaLogger) args[0];
			((Logger) log.getUserdata()).log(args[1].getString());
			return log;
		}
	},
	debug {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			Lua.checkArgs(name(), args, LuaType.USERDATA, LuaType.STRING);
			LuaLogger log = (LuaLogger) args[0];
			((Logger) log.getUserdata()).debug(args[1].getString());
			return log;
		}

	};

	@Override
	public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
		return null;
	}

	@Override
	public void accept(Environment t, LuaObject table) {
		String name = toString();
		if (name != null && !name.trim().isEmpty())
			table.rawSet(name, Lua.newMethod(this));
	}

}