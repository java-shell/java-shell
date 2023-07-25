package terra.shell.utils.lua;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.function.BiConsumer;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaFactory;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.Lua.LuaMethod;

import terra.shell.logging.LogManager;
import terra.shell.logging.Logger;
import terra.shell.utils.lua.exceptions.LuaLibraryLoadException;

public class LuaHookManager {
	private static Hashtable<String, LuaLibrary<?>> luaLibs = new Hashtable<String, LuaLibrary<?>>();
	private static Logger log = LogManager.getLogger("LuaHookManager");

	public static void registerHook(String hookQualifier, LuaLibrary<?> lib) throws LuaLibraryLoadException {
		if (luaLibs.containsKey(hookQualifier) || luaLibs.contains(lib)) {
			throw new LuaLibraryLoadException();
		}
		log.debug("Registering Lua library with qualifer hook: " + hookQualifier);
		luaLibs.put(hookQualifier, lib);
	}

	public static void deregisterHook(String hookQualifier) {
		luaLibs.remove(hookQualifier);
		log.debug("De-registering Lua library with qualifier hook: " + hookQualifier);
	}

	public static LuaFactory injecAlltHooks(LuaFactory factory) {
		Enumeration<LuaLibrary<?>> libsEnum = luaLibs.elements();
		while (libsEnum.hasMoreElements()) {
			factory.addLibrary(libsEnum.nextElement());
		}
		return factory;
	}

	public static LuaFactory injectHook(String hookQualifier, LuaFactory factory) throws LuaLibraryLoadException {
		if (luaLibs.containsKey(hookQualifier)) {
			factory.addLibrary(luaLibs.get(hookQualifier));
			return factory;
		}
		throw new LuaLibraryLoadException();
	}

	public static LuaFactory injectHooks(LuaFactory factory, String... qualifiers) throws LuaLibraryLoadException {
		for (String hook : qualifiers) {
			if (luaLibs.containsKey(hook)) {
				factory.addLibrary(luaLibs.get(hook));
			} else {
				throw new LuaLibraryLoadException();
			}
		}
		return factory;
	}

	public static LuaFactory injectHooks(LuaFactory factory, boolean ignoreMissing, String... qualifiers) {
		for (String hook : qualifiers) {
			if (luaLibs.containsKey(hook))
				factory.addLibrary(luaLibs.get(hook));
		}
		return factory;
	}

}
