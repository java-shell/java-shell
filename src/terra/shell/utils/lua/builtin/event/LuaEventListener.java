package terra.shell.utils.lua.builtin.event;

import com.hk.lua.Lua;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;

import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventListener;

public class LuaEventListener extends LuaUserdata {
	private LuaObject callback;
	private String eventType;
	private EventListener eventListener;
	private static final LuaObject luaEventListenerMetatable = Lua.newTable();

	public LuaEventListener(String eventType, LuaObject callback) {
		this.eventType = eventType;
		this.callback = callback;
		metatable = luaEventListenerMetatable;
		eventListener = new EventListener() {

			@Override
			public void trigger(Event e) {
			}

		};
	}

	@Override
	public Object getUserdata() {
		return null;
	}

	@Override
	public String name() {
		return null;
	}

	static {
		luaEventListenerMetatable.rawSet("__name", Lua.newString("LOGGER"));
		luaEventListenerMetatable.rawSet("__index", luaEventListenerMetatable);
	}

}
