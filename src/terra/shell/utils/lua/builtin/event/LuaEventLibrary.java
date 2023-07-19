package terra.shell.utils.lua.builtin.event;

import java.util.function.BiConsumer;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;

public enum LuaEventLibrary implements BiConsumer<Environment, LuaObject>, LuaMethod {
	CreateEventListener {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			Lua.checkArgs(name(), args, LuaType.STRING, LuaType.ANY);
			String eventType = args[0].getString();
			LuaEventListener lel = new LuaEventListener(interp, eventType, args[1]);
			return lel;
		}
	};

	@Override
	public void accept(Environment t, LuaObject table) {
		String name = toString();
		if (name != null && !name.trim().isEmpty())
			table.rawSet(name, Lua.newMethod(this));
	}

	@Override
	public LuaObject call(LuaInterpreter arg0, LuaObject[] arg1) {
		return null;
	}

}
