package gbml;

import java.util.ArrayList;
import java.util.Date;

import methods.DataLoader;
import methods.Divider;
import methods.MersenneTwisterFast;
import methods.Output;
import methods.ResultMaster;
import methods.StaticGeneralFunc;
import moead.Moead;
import nsga2.Nsga2;
import time.TimeWatcher;

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

	//TODO 一回ごとに試行を終了する
	static public void onceExection(Settings sets, String[] args) {

	}

	//自動でrepeatする
	static public void repeatExection(Settings sets, String[] args) {
		//読み込みファイル名保持配列
		String[][] traFiles = new String[sets.finishRepeatPos - sets.startRepeatPos + 1][sets.crossValidationNum];
		String[][] tstFiles = new String[sets.finishRepeatPos - sets.startRepeatPos + 1][sets.crossValidationNum];
		Output.makeFileName(sets.dataName, traFiles, tstFiles);

		//データディレクトリを物理的に作成
		String resultDir = null;
		if(sets.calclationType == 1) {
			//TODO sparkあり
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

				Output.makeResultDir(resultDir, rep_i, cv_i);

				startExperiment(sets, traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i], rnd, resultMaster,
						cv_i, rep_i, traFiles[rep_i][cv_i], tstFiles[rep_i][cv_i]);

				System.out.println();
			}
		}
	}

	//GBMLメソッド
	static public void startExperiment(Settings sets, String traFile, String tstFile, MersenneTwisterFast rnd,
			ResultMaster resultMaster, int crossValidationNum, int repeatNum, String nowTrainFile, String nowTestFile) {

		// ********************************************************************
		//Read DataSets
		DataSetInfo originalTrainDataInfo = null;	//All of Train DataSet
		if(sets.calclationType == 0) {
			originalTrainDataInfo = new DataSetInfo();
			DataLoader.inputFile(originalTrainDataInfo, nowTrainFile);
		} else if(sets.calclationType == 1) {
			//TODO Sparkあり ヘッダのみ読み込む
		}

		// ********************************************************************
		//Sampling the Datas	データのサンプリング(並列分散処理 各マシン用)
		//データをクラスごとに均等に分けて一部だけ取り出す
		DataSetInfo trainDataInfo = new DataSetInfo();	//学習用データセット
		if(sets.calclationType == 0) {
			Divider preDivider = new Divider(sets.preDivNum);
			trainDataInfo = preDivider.letsDivide(originalTrainDataInfo, 1, sets.serverList)[0];
		} else if(sets.calclationType == 1) {
			//TODO sparkあり
		}

		// ********************************************************************
		//Island Model	島モデル
		//Divide the Datas into Each Island	（各島へのデータの分割）
		DataSetInfo[] trainDataInfos = null;	//各島ごとにDataSetInfoインスタンスを作成
		if(sets.calclationType == 0) {
			if(sets.islandNum == 1) {
				//島分割なし
				trainDataInfos = new DataSetInfo[2];	//島分割がある場合と同じ形式にするために重複させたままにする

				boolean isRandom = Consts.IS_RANDOM_PATTERN_SELECT;	//ランダムなパターンで組むか
				DataSetInfo[] randomTrains = null;
				if(isRandom) {
					//TODO ランダムなパターンで組む？
//					Divider divider = new Divider(rnd, sets.migrationItv);
				} else {
					//島分割がある場合と同様に扱うために、島用DataSetInfo[0]と最後にオリジナルなDataSetInfo[islandNum]の形式にする
					trainDataInfos[0] = trainDataInfo;
					trainDataInfos[1] = trainDataInfo;
				}
				trainDataInfos[0].setSetting(sets.calclationType, sets.serverList);
				trainDataInfos[1].setSetting(sets.calclationType, sets.serverList);
			} else {
				//島分割あり
				Divider divider = new Divider(rnd, sets.islandNum);
				trainDataInfos = divider.letsDivide(trainDataInfo, sets.calclationType, sets.serverList);
			}
		} else {
			//sparkあり
		}

		// ********************************************************************
		//TimeWatch Start	時間計測開始
		TimeWatcher evaWatcher = new TimeWatcher();	//TODO
		TimeWatcher timeWatcher = new TimeWatcher();	//実験全体のTimeWatcher
		timeWatcher.start();

		// ********************************************************************
		//Initialize Instance of EMO Algorithms	（EMOアルゴリズムの初期化）
		//TODO 全体用MOEA/D
		Moead moead = null;	//TODO
		ArrayList<Moead> moeads = null;

		//NSGA-II
		Nsga2 nsga2 = new Nsga2(sets.objectiveNum, rnd);

		//GA(Genetic Algorithm)マネージャ
		GaManager gaManager = new GaManager(sets.populationSize, nsga2, moeads, rnd, sets.forkJoinPool, sets.serverList, sets.serverNum,
											sets.objectiveNum, sets.generationNum, sets.emoType, sets.islandNum, resultMaster, evaWatcher, sets.dataName);

		//Execute Genetic Algorithm (GA実行)
		//GA終了後の最終個体群の情報が[populationManagers]に保存される
		PopulationManager[] populationManagers = gaManager.gaFrame(trainDataInfos, sets.migrationItv, sets.rotationItv, sets.calclationType, repeatNum, crossValidationNum, timeWatcher);

		//1試行 時間計測終了
		timeWatcher.end();
		resultMaster.setTimes( timeWatcher.getNano() );

		/***********************これ以降出力操作************************/
		//評価用DataFrame作成
//		DataSetInfo testDataInfo = null;
//		testDataInfo = new DataSetInfo();
//		if(sets.calclationType == 1) {
//			nowTestFile = sets.dirLocation + nowTestFile;
//		}
//		DataLoader.inputFile(testDataInfo, nowTestFile);



	}

}






























