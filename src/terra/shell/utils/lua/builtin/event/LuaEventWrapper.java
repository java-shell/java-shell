package terra.shell.utils.lua.builtin.event;

import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;

import terra.shell.utils.keys.Event;

public class LuaEventWrapper extends LuaUserdata {
	private Event event;
	private static final LuaObject luaEventWrapperMetatable = Lua.newTable();

	public LuaEventWrapper(Event e) {
		event = e;
		metatable = luaEventWrapperMetatable;
	}

	@Override
	public Object getUserdata() {
		// TODO Auto-generated method stub
		return event;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "Event";
	}

	public Object[] getArgs() {
		return event.getArgs();
	}

	static {
		luaEventWrapperMetatable.rawSet("__name", Lua.newString("EVENT"));
		luaEventWrapperMetatable.rawSet("__index", luaEventWrapperMetatable);

		LuaObject getTypeFunction = Lua.newMethod(new LuaMethod() {

			@Override
			public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
				if (!(args[0] instanceof LuaEventWrapper))
					throw Lua.badArgument(0, "Type", "EVENT type expected");

				LuaEventWrapper lew = (LuaEventWrapper) args[0];
				Event e = (Event) lew.getUserdata();
				return Lua.newString(e.getCreator());
			}

		});

		LuaObject getArgsFunction = Lua.newMethod(new LuaMethod() {

			@Override
			public LuaObject call(LuaInterpreter itnerp, LuaObject[] args) {
				if (!(args[0] instanceof LuaEventWrapper))
					throw Lua.badArgument(0, "Type", "EVENT type expected");

				LuaEventWrapper lew = (LuaEventWrapper) args[0];
				Object[] eArgs = lew.getArgs();
				return Lua.newLuaObject(eArgs);
			}

		});

		luaEventWrapperMetatable.rawSet("GetType", getTypeFunction);
		luaEventWrapperMetatable.rawSet("GetArguments", getArgsFunction);
	}

}
