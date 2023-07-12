package terra.shell.utils.lua.builtin;

import com.hk.lua.LuaLibrary;

import terra.shell.utils.lua.builtin.lualogger.LuaLoggerLibrary;
import terra.shell.utils.lua.builtin.sockethandler.LuaSocketHandlerLibrary;

public class JavashellLuaLibrary {
	public static final LuaLibrary<LuaLoggerLibrary> LOGGER = new LuaLibrary<>("logger", LuaLoggerLibrary.class);
	public static final LuaLibrary<LuaSocketHandlerLibrary> SOCKET = new LuaLibrary<>("Socket",
			LuaSocketHandlerLibrary.class);
}
