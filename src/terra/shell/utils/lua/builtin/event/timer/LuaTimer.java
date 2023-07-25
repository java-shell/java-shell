package terra.shell.utils.lua.builtin.event.timer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;

import terra.shell.utils.lua.builtin.event.LuaEventListener;

public class LuaTimer extends LuaUserdata {
	private LuaInterpreter interp;
	private Timer timer;
	private LuaObject callback;
	private static final LuaObject luaTimerMetatable = Lua.newTable();

	public LuaTimer(LuaInterpreter interp) {
		this.interp = interp;
		metatable = luaTimerMetatable;
		timer = new Timer();
	}

	@Override
	public Object getUserdata() {
		return this;
	}

	@Override
	public String name() {
		return "Timer";
	}

	private void setCallback(LuaObject callback) {
		this.callback = callback;
	}

	static {
		luaTimerMetatable.rawSet("__name", Lua.newString("TIMER"));
		luaTimerMetatable.rawSet("__index", luaTimerMetatable);

		LuaObject setCallbackFunctionFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				if (args.length < 2 || !(args[0] instanceof LuaTimer))
					throw Lua.badArgument(0, "SetHandler", "TIMER type expected");

				LuaTimer lt = (LuaTimer) args[0];
				lt.setCallback(args[1]);
			}
		});

		LuaObject startTimerFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				Lua.checkArgs("StartTimer", args, LuaType.USERDATA, LuaType.INTEGER);

				LuaTimer lt = (LuaTimer) args[0];
				lt.timer.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						lt.callback.call(lt.interp, Lua.NIL);
					}

				}, args[1].getInt(), args[1].getInt());
			}

		});

		LuaObject stopTimerFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				Lua.checkArgs("StopTimer", args, LuaType.USERDATA);

				LuaTimer lt = (LuaTimer) args[0];
				lt.timer.cancel();
				lt.timer = new Timer();
			}

		});

		LuaObject oneShotFunction = Lua.newFunc(new Consumer<LuaObject[]>() {

			@Override
			public void accept(LuaObject[] args) {
				Lua.checkArgs("OneShot", args, LuaType.USERDATA, LuaType.INTEGER);

				LuaTimer lt = (LuaTimer) args[0];
				lt.timer.schedule(new TimerTask() {

					@Override
					public void run() {
						lt.callback.call(lt.interp, Lua.NIL);
					}

				}, args[1].getInt());
			}

		});

		luaTimerMetatable.rawSet("StartTimer", startTimerFunction);
		luaTimerMetatable.rawSet("StopTimer", stopTimerFunction);
		luaTimerMetatable.rawSet("OneShot", oneShotFunction);
		luaTimerMetatable.rawSet("SetHandler", setCallbackFunctionFunction);
	}

}
