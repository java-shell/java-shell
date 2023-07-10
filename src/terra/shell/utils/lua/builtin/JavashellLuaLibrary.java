package terra.shell.utils.lua.builtin;

import com.hk.lua.LuaLibrary;

public class JavashellLuaLibrary {
	public static final LuaLibrary<LuaLoggerLibrary> LOGGER = new LuaLibrary<>("logger", LuaLoggerLibrary.class);
}
