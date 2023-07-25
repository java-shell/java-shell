package terra.shell.command.builtin;

import java.io.File;
import java.util.ArrayList;

import com.hk.lua.Lua;
import com.hk.lua.LuaFactory;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.lua.LuaObject;

import terra.shell.command.BasicCommand;
import terra.shell.utils.lua.LuaHookManager;
import terra.shell.utils.perms.Permissions;

public class ExecLua extends BasicCommand {

	@Override
	public String getName() {
		return "lua";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return "DJS";
	}

	@Override
	public String getOrg() {
		// TODO Auto-generated method stub
		return "JSH";
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<String> getAliases() {
		return null;
	}

	@Override
	public ArrayList<Permissions> getPerms() {
		return null;
	}

	@Override
	public boolean start() {
		String luaPath = this.getArg(0);
		File luaFile;
		if (luaPath.startsWith("/")) {
			luaFile = new File(luaPath);
		} else {
			luaFile = new File(term.currentDir(), luaPath);
		}

		try {
			LuaFactory factory = Lua.factory(luaFile);
			Lua.importStandard(factory);
			LuaHookManager.injecAllHooks(factory);
			factory.compile();
			LuaInterpreter interp = factory.build();
			LuaObject ret = interp.execute();
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().err("Failed to execute " + e.toString());
			return false;
		}
		return true;
	}

}
