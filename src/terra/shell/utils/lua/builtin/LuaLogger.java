package terra.shell.utils.lua.builtin;

import com.hk.lua.LuaUserdata;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;

public class LuaLogger extends LuaUserdata {
	private Logger log;

	public LuaLogger(String name) {
		log = LogManager.getLogger(name);
	}

	@Override
	public String name() {
		return log.getName();
	}

	@Override
	public Object getUserdata() {
		return log;
	}

}
