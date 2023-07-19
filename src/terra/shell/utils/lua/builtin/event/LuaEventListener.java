package terra.shell.utils.lua.builtin.event;

import java.util.function.Consumer;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;

import terra.shell.utils.keys.Event;
import terra.shell.utils.system.EventListener;

public class LuaEventListener extends LuaUserdata {
	private LuaObject callback;
	private String eventType;
	private EventListener eventListener;
	private static final LuaObject luaEventListenerMetatable = Lua.newTable();

	public LuaEventListener(LuaInterpreter interp, String eventType, LuaObject callback) {
		this.eventType = eventType;
		this.callback = callback;
		metatable = luaEventListenerMetatable;
		eventListener = new EventListener() {

			@Override
			public void trigger(Event e) {
				if (callback != null) {
					LuaEventWrapper lew = new LuaEventWrapper(e);
					callback.call(interp, lew);
				}
			}

		};
		eventListener.register(eventType);
	}

	@Override
	public Object getUserdata() {
		return null;
	}

	@Override
	public String name() {
		return null;
	}

	private void setCallback(LuaObject callback) {
		this.callback = callback;
	}

	static {
		luaEventListenerMetatable.rawSet("__name", Lua.newString("EVENTLISTENER"));
		luaEventListenerMetatable.rawSet("__index", luaEventListenerMetatable);

		LuaObject setCallbackFunctionFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaEventListener))
					throw Lua.badArgument(0, "SetHandler", "EVENTLISTENER type expected");

				LuaEventListener lel = (LuaEventListener) args[0];
				lel.setCallback(args[1]);
			}
		});

		luaEventListenerMetatable.rawSet("SetHandler", setCallbackFunctionFunction);
	}

}
