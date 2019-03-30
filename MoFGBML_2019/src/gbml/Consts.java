package gbml;

import java.lang.reflect.Field;

//各種定数 定義クラス
public class Consts {

	//OS
	public static final int WINDOWS = 0;	//windows
	public static final int UNIX = 1;	//unix

	//Experiment's Parameters - 実験設定パラメータ
	public static final boolean IS_RANDOM_PATTERN_SELECT = false;	//ランダムなパターンで組む

	public static final boolean IS_NOT_EQUAL_DIVIDE_NUM = false;	//部分個体群とデータ分割数を一緒にしない

	public static final boolean IS_ALL_MIGLATION = false;	//true: 各島の最良個体を全島で共有する, false: 各島の最良個体を隣の島に移住

	//Parallel Parameters - 並列用パラメータ
	public static final boolean IS_RULESETS_SORT = false;	//評価の際にルール数でソートするかどうか



	//GBML's parameters
	public static final int ANTECEDENT_LEN = 5;	//don't careにしない条件部の数
	public static final double DONT_CARE_RT = 0.8;	//don't care適応確率
	public static final boolean IS_PROBABILITY_DONT_CARE = false;	//don't careを確率で行う

	public static final double RULE_OPE_RT = 0.5;	//Michigan適用確率
	public static final double RULE_CROSS_RT = 0.9;	//Michigan交叉確率
	public static final double RULE_CHANGE_RT = 0.2;	//ルール入れ替え割合
	public static final boolean DO_LOG_PER_LOG = true;	//ログでログを出力

	public static final double RULESET_CROSS_RT = 0.9;	//Pittsburgh交叉確率

	//NSGA-II's Parameters
	public static final int NSGA2 = 0;	//NSGA-IIの番号
	public static final boolean DO_CD_NORMALIZE = false;	//Crowding Distance を正規化するかどうか

	//MOEAD's parameters
	public static final int SECOND_OBJECTIVE_TYPE = 0;	//2目的目, 0:rule, 1:length, 2:rule * length, 4:length/rule

	//Fuzzy System's parameters
	public static final int FUZZY_SET_NUM = 14;	//ファジィ集合の種類数
	public static final int INITIATION_RULE_NUM = 30;	//初期ルール数
	public static final int MAX_RULE_NUM = 60;	//1識別器あたりの最大ルール数
	public static final boolean DO_HEURISTIC_GENERATION = true;	//ヒューリスティック生成法


	//One Objective Weights
	public static final int W1 = 1000;
	public static final int W2 = -1;
	public static final int W3 = -1;


	//Other parametaers
	public static final int PER_SHOW_GENERATION_NUM = 100;	//表示する世代間隔

	//Folders' Name
	public static final String ROOTFOLDER = "result";
	public static final String RULESET = "ruleset";
	public static final String VECSET = "vecset";
	public static final String SOLUTION = "solution";
	public static final String LOGS = "logs";
	public static final String LOGS_READABLE = "logs_readable";
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
