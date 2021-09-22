package terra.shell.utils.keys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public interface Event {
	public static final int HIGHEST = 0;
	public static final int HIGH = 1;
	public static final int MED = 2;
	public static final int LOW = 3;
	public static final int VERY_LOW = 4;

	public static final String GENERAL_TYPE = "general_type";
	public static final String INIT_TYPE = "init_type";

	public String getCreator();

	@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	@Target(ElementType.CONSTRUCTOR)
	public @interface EventPriority {
		String id();

		int value();
	}
}
