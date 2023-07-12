package terra.shell.utils.lua.builtin.event;

import org.jetbrains.annotations.NotNull;

import com.hk.lua.LuaUserdata;

import terra.shell.utils.keys.Event;

public class LuaEventWrapper extends LuaUserdata {
	private Event event;

	public LuaEventWrapper(Event e) {
		event = e;
	}

	@Override
	public Object getUserdata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

}
