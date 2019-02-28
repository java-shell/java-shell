package terra.shell.utils.system;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

/**
 * Resource class which holds Variables defined within the JVM.
 * 
 * @author dan
 * 
 */
public final class Variables {
	private static Hashtable<String, Variable> varByName = new Hashtable<String, Variable>();
	private static Hashtable<Variable.Type, Hashtable<String, Variable>> varByType = new Hashtable<Variable.Type, Hashtable<String, Variable>>();

	/**
	 * Sets the Variable. This replaces any variable previously with the same
	 * Variable.getName(); value.
	 * 
	 * @param var
	 *            Variable to be set.
	 */
	public static void setVar(Variable var) {
		varByName.put(var.getVarName(), var);
		Set<Variable.Type> keys = varByType.keySet();
		for (Variable.Type t : keys) {
			if (var.getType().getRType() == t.getRType()) {
				Hashtable<String, Variable> vars = varByType.get(t);
				if (vars == null) {
					varByType.put(t, new Hashtable<String, Variable>());
					vars = varByType.get(t);
				}
				vars.put(var.getVarName(), var);
				vars = null;
			}
		}
	}

	/**
	 * Clears the Variable defined with this String. The String needs to match
	 * the variables Variable.getName(); value as this is what defines the
	 * variable.
	 * 
	 * @param var
	 *            Matching Variable to clear.
	 */
	public static void clearVar(String var) {
		if (varByName.containsKey(var)) {
			varByType.put(varByName.get(var).getType(), null);
			varByName.put(var, null);
		}
	}

	/**
	 * Get all variables currently in the system.
	 * 
	 * @return An enumeration of all variables currently defined in system.
	 */
	public static Enumeration<Variable> getVars() {
		return varByName.elements();
	}

	/**
	 * Get the value specified by that Variable.
	 * 
	 * @param var
	 *            The Variable name to access.
	 * @return The Variables actual value.
	 */
	public static String getVarValue(String var) {
		if (varByName.containsKey(var))
			return varByName.get(var).getVarValue();

		return null;
	}
}
