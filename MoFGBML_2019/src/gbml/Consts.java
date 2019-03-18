package gbml;

import java.lang.reflect.Field;

//各種定数 定義クラス
public class Consts {

	//Experiment's Parameters - 実験設定パラメータ


	//OS
	public static final int WINDOWS = 0;	//windows
	public static final int UNIX = 1;	//unix


	//Folders' Name
	public static final String ROOTFOLDER = "result";
	public static final String RULESET = "ruleset";
	public static final String VECSET = "vecset";
	public static final String SOLUTION = "solution";
	public static final String OTHERS = "write";

	public String getStaticValues() {
		StringBuilder sb = new StringBuilder();
		String sep = System.lineSeparator();
		sb.append("Class: " + this.getClass().getCanonicalName() + sep);
		sb.append("Settings: " + sep);
		for(Field field : this.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				sb.append(field.getName() + " = " + field.get(this) + sep);
			} catch(IllegalAccessException e) {
				sb.append(field.getName() + " = " + "access denied" + sep);
			}
		}
		return sb.toString();
	}
}
