package gbml;

import java.util.Date;

import methods.MersenneTwisterFast;
import methods.Output;
import methods.ResultMaster;
import methods.StaticGeneralFunc;

public class Main {

	public static void main(String[] args) {
//		if(true) {
//			Consts a = new Consts();
//			String b = a.getStaticValues();
//			System.out.print(b);
//			return ;
//		}

		// The version of this program.
		System.out.println("ver. " + 21.0);	// Yuichi OMOZAKI (2019)

		/******************************************************************************/
		// Basic Settings - 基本設定
		Settings sets = new Settings(args);

		// Output Basic Datas + Start Experiment - 基本データ出力と実行
		//実行ランタイム環境が使用できるコア数を出力
		System.out.println("Processors: " + Runtime.getRuntime().availableProcessors() + " ");
		//コマンドライン引数の出力
		System.out.print("args: ");
		for(int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println("");
		System.out.println("");

		//実験開始タイム保持
		Date date = new Date();
		System.out.print("START: ");
		System.out.println(date);

		//実験開始
		//一回ごとにjarファイルを呼び出すか否か
		if(sets.isOnceExe) {
			onceExection(sets, args);
		} else {
			repeatExection(sets, args);
		}
	}

	//一回ごとに試行を終了する
	static public void onceExection(Settings sets, String[] args) {

	}

	//自動でrepeatする
	static public void repeatExection(Settings sets, String[] args) {
		//読み込みファイル名保持配列
		String[][] traFiles = new String[sets.finishRepeatPos - sets.startRepeatPos][sets.crossValidationNum];
		String[][] tstFiles = new String[sets.finishRepeatPos - sets.startRepeatPos][sets.crossValidationNum];
		Output.makeFileName(sets.dataName, traFiles, tstFiles);

		//データディレクトリを物理的に作成
		String resultDir = null;
		if(sets.calclationType == 1) {
			//sparkあり
		} else {
			resultDir = Output.makeDirName(sets.dataName, sets.calclationType, sets.islandNum, sets.preDivNum, sets.seed);
		}
		Output.makeDirRule(resultDir);	//物理的にmkdirs()を行う

		//実験パラメータ出力 (result/ 直下の datasetName.txt)
		String settings = StaticGeneralFunc.getExperimentSettings(args);
		Output.writeSetting(sets.dataName, resultDir, settings);

		//出力情報専用クラス
		ResultMaster resultMaster = new ResultMaster(resultDir, sets.osType);

		MersenneTwisterFast rnd = new MersenneTwisterFast(sets.seed);

		//GBML開始
		//startRepeatPos と finishRepeatPos に気を付けること
		for(int rep_i = sets.startRepeatPos; rep_i < sets.finishRepeatPos; rep_i++) {
			for(int cv_i = 0; cv_i < sets.crossValidationNum; cv_i++) {
				System.out.print(rep_i + " " + cv_i);

				startExperiment(sets, traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i], rnd, resultMaster,
						cv_i, rep_i, traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i]);

				System.out.println();
			}
		}
	}

	//GBMLメソッド
	static public void startExperiment(Settings sets, String traFile, String tstFile, MersenneTwisterFast rnd,
			ResultMaster resultMaster, int crossValidationNum, int repeatNum, String nowTrainFile, String nowTestFile) {

	}

}
