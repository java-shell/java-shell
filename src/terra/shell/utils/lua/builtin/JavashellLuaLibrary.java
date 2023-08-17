package terra.shell.utils.lua.builtin;

import com.hk.lua.LuaLibrary;

import terra.shell.utils.lua.builtin.event.LuaEventLibrary;
import terra.shell.utils.lua.builtin.event.timer.LuaTimerLibrary;
import terra.shell.utils.lua.builtin.lualogger.LuaLoggerLibrary;
import terra.shell.utils.lua.builtin.sockethandler.LuaSocketHandlerLibrary;

public class JavashellLuaLibrary {
	public static final LuaLibrary<LuaLoggerLibrary> LOGGER = new LuaLibrary<>("logger", LuaLoggerLibrary.class);
	public static final LuaLibrary<LuaSocketHandlerLibrary> SOCKET = new LuaLibrary<>("Socket",
			LuaSocketHandlerLibrary.class);
	public static final LuaLibrary<LuaEventLibrary> EVENTS = new LuaLibrary<>("Events", LuaEventLibrary.class);
	public static final LuaLibrary<LuaTimerLibrary> TIMER = new LuaLibrary<>("Timer", LuaTimerLibrary.class);
	public static final LuaLibrary<JavashellGenericLuaLibrary> JSH = new LuaLibrary<>("Generic",
			JavashellGenericLuaLibrary.class);
}
