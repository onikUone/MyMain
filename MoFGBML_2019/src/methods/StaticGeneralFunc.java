package methods;

import gbml.Consts;

//static なメソッドを記述したクラス
public class StaticGeneralFunc {

	StaticGeneralFunc(){}

	//実験パラメータ定数を出力
	public static String getExperimentSettings(String[] args) {
		String allSettings = "";
		String endLine = System.lineSeparator();
		allSettings += endLine;
		for(int i = 0; i < args.length; i++) {
			allSettings += args[i] + endLine;
		}
		Consts consts = new Consts();
		allSettings += consts.getStaticValues();
		return allSettings;
	}

}
