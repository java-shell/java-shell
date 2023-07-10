package terra.shell.utils.lua;

import java.util.Enumeration;
import java.util.Hashtable;

import com.hk.lua.LuaFactory;
import com.hk.lua.LuaLibrary;

import terra.shell.utils.lua.exceptions.LuaLibraryLoadException;

public class LuaHookManager {
	private static Hashtable<String, LuaLibrary<?>> luaLibs = new Hashtable<String, LuaLibrary<?>>();

	public static void registerHook(String hookQualifier, LuaLibrary<?> lib) throws LuaLibraryLoadException {
		if (luaLibs.containsKey(hookQualifier) || luaLibs.contains(lib)) {
			throw new LuaLibraryLoadException();
		}
		luaLibs.put(hookQualifier, lib);
	}

	public static void deregisterHook(String hookQualifier) {
		luaLibs.remove(hookQualifier);
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
