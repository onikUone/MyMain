package gbml;

import java.lang.reflect.Field;

//各種定数 定義クラス
public class Consts {

	//Experiment's Parameters - 実験設定パラメータ
	public static final boolean IS_RANDOM_PATTERN_SELECT = false;	//ランダムなパターンで組む

	public static final boolean IS_NOT_EQUAL_DIVIDE_NUM = false;	//部分個体群とデータ分割数を一緒にしない


	//OS
	public static final int WINDOWS = 0;	//windows
	public static final int UNIX = 1;	//unix


	//GBML's parameters
	public static final int ANTECEDENT_LEN = 5;	//don't careにしない条件部の数
	public static final double DONT_CARE_RT = 0.8;	//don't care適応確率
	public static final boolean IS_PROBABILITY_DONT_CARE = false;	//don't careを確率で行う

	//MOEAD's parameters
	public static final int SECOND_OBJECTIVE_TYPE = 0;	//2目的目, 0:rule, 1:length, 2:rule * length, 4:length/rule

	//Fuzzy System's parameters
	public static final int FUZZY_SET_NUM = 14;	//ファジィ集合の種類数
	public static final int INITIATION_RULE_NUM = 30;	//初期ルール数
	public static final boolean DO_HEURISTIC_GENERATION = true;	//ヒューリスティック生成法

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
