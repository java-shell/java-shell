package terra.shell.utils.lua.builtin.event.timer;

import java.util.function.BiConsumer;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;

public enum LuaTimerLibrary implements BiConsumer<Environment, LuaObject>, LuaMethod {
	CreateTimer {
		public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
			return new LuaTimer(interp);
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
