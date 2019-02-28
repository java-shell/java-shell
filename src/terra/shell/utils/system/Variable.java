package terra.shell.utils.system;

public interface Variable {
	public Type getType();

	public String getVarName();

	public String getVarValue();

	abstract class Type {
		public abstract String getTypeName();

		public Type getRType() {
			return this;
		}
	}
}
