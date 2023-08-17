package terra.shell.utils.lua.builtin;

import java.util.UUID;

import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;

public class LuaUUID extends LuaUserdata {
	private UUID uuid;
	private static final LuaObject luaUUIDMetatable = Lua.newTable();

	public LuaUUID() {
		this(UUID.randomUUID());
	}

	public LuaUUID(UUID u) {
		this.uuid = u;
		metatable = luaUUIDMetatable;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public Object getUserdata() {
		return this;
	}

	@Override
	public String name() {
		return "uuid";
	}

	static {
		luaUUIDMetatable.rawSet("__name", "UUID");
		luaUUIDMetatable.rawSet("__index", luaUUIDMetatable);

		LuaObject getUUIDAsStringFunction = Lua.newMethod(new LuaMethod() {

			@Override
			public LuaObject call(LuaInterpreter interp, LuaObject[] args) {
				Lua.checkArgs("getUUID", args, LuaType.USERDATA);
				LuaUUID lu = (LuaUUID) args[0];
				return Lua.newString(lu.uuid.toString());
			}

		});
	}

}
