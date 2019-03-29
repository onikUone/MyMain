package gbml;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import methods.ResultMaster;
import moead.Moead;
import nsga2.Nsga2;
import time.TimeWatcher;

//GAを制御するクラス
public class GaManager {
	//Fields ********************************************
	Nsga2 nsga2;
	ArrayList<Moead> moeads;

	MersenneTwisterFast rnd;

	ForkJoinPool forkJoinPool;

	InetSocketAddress[] serverList;
	int serverNum;

	TimeWatcher timeWatcher;

	ResultMaster resultMaster;

	int islandNum;
	int popDivNum;

	int secondObjType = Consts.SECOND_OBJECTIVE_TYPE;

	int objectiveNum;
	long generationNum;

	int emoType;
	int populationSize;

	String dataName;
	// **************************************************

	//Constructor ***************************************
	public GaManager() {}

	public GaManager(Nsga2 nsga2) {
		this.nsga2 = nsga2;
	}

	public GaManager(int popSize, Nsga2 nsga2, ArrayList<Moead> moeads, MersenneTwisterFast rnd, ForkJoinPool forkJoinPool,
			InetSocketAddress[] serverList, int serverNum, int objectiveNum, int generationNum,
			int emoType, int islandNum, ResultMaster resultMaster, TimeWatcher timeWatcher, String dataName) {

		this.rnd = rnd;
		this.nsga2 = nsga2;
		this.moeads = moeads;

		this.forkJoinPool = forkJoinPool;

		this.serverList = serverList;
		this.serverNum = serverNum;

		this.resultMaster = resultMaster;
		this.timeWatcher = timeWatcher;

		this.objectiveNum = objectiveNum;
		this.generationNum = generationNum;
		this.emoType = emoType;
		this.islandNum = islandNum;
		this.populationSize = popSize;

		//条件によって部分個体群数をデータ分割数と同じにするか決める
		boolean isNotEqualDiv = Consts.IS_NOT_EQUAL_DIVIDE_NUM;
		if(isNotEqualDiv) {
			this.popDivNum = this.serverNum;
		} else {
			this.popDivNum = this.islandNum;
		}

		this.dataName = dataName;
	}
	// **************************************************

	//Methods *******************************************

	//GaFrame
	public PopulationManager[] gaFrame(DataSetInfo[] trainDataInfos, int migrationItv, int rotationItv, int calclationType, int repeat_i, int cv_i, TimeWatcher timeWatcher) {

		//各種 経過時間保存用リスト
		ArrayList<Double> times = new ArrayList<Double>();

		//個体群の生成
		PopulationManager[] popManagers = null;

		//各島に対するデータ番号の生成
		int[] dataIdx = new int[popDivNum];	//popDivNum: 個体群を分割する分割数
		int dataInterval = (int)(islandNum / popDivNum);
		for(int i = 0; i < popDivNum; i++) {
			dataIdx[i] = i * dataInterval;
		}

		// **********************************************
		//Step 1. 初期個体群生成と初期個体群の評価
		if(calclationType == 0) {

			//TODO 2019/03/19
			popManagers = generateInitialPop(trainDataInfos, dataIdx, calclationType);

		} else {	//分散の場合はオブジェクトを作るだけでルールは作成しない．
			//TODO 並列分散用
		}

		// **********************************************
		//Step 2. 進化計算開始
		boolean doLog = Consts.DO_LOG_PER_LOG;
		int nowGen = 0;
		for(int gen_i = 0; gen_i < generationNum; gen_i++) {
			if(gen_i % Consts.PER_SHOW_GENERATION_NUM == 0) {
				System.out.print(".");
			}

			//途中結果保持（テストデータは無理）
			if(doLog) {
				timeWatcher.intoSuspend();
//TODO genCheck()の中身を完成させる
//TODO genCheck()はこのタイミングでの途中結果（識別器情報や部分学習用データ情報など）の出力
				genCheck(gen_i, repeat_i, cv_i, trainDataInfos, popManagers);
				timeWatcher.exitSuspend();
			}

			//GA操作
			if(populationSize == 1) {
				//ミシガン型FGBML
//TODO			michiganTypeGA(trainDataInfos[0], popManagers[0], gen_i);
			} else {
				if(calclationType == 1) {
					//TODO sparkあり
				} else {
					if(emoType == 0 || objectiveNum == 1) {
						times.add(nsga2Type2(trainDataInfos, popManagers, dataIdx, gen_i));	//NSGA-IIの実施
						s
					}
				}
			}

		}


		return popManagers;
	}

	//Initialize Population	初期個体群の生成
	protected PopulationManager[] generateInitialPop(DataSetInfo[] trainDataInfos, int[] dataIdx, int calclationType) {
		//初期個体群の宣言
		PopulationManager[] popManagers = null;

		//Generate Initial Population	(初期個体群の生成(複数))
		popManagers = new PopulationManager[islandNum];
		int[] islandPopNums = calcIslandPopNums(populationSize);
		for(int island_i = 0; island_i < islandNum; island_i++) {
			popManagers[island_i] = new PopulationManager(rnd, objectiveNum);	//島内の個体群生成
			popManagers[island_i].generateInitialPopulation(trainDataInfos[island_i], islandPopNums[island_i], forkJoinPool, calclationType, island_i, serverList);
			popManagers[island_i].setIslandPopNum(islandPopNums[island_i]);
			popManagers[island_i].setDataIdx(dataIdx[island_i]);
			popManagers[island_i].setEmoType(emoType);
//TODO			//MOEA/D初期化
		}

		//島ごとのデータ番号初期化
		for(int island_i = 0; island_i < islandNum; island_i++) {
			popManagers[island_i].setDataIdxtoRuleSets(dataIdx[island_i], true);
		}

		// *********************************************************************
		//Evaluate Initial Population	(初期個体群の評価)
		PopulationManager allPopManager = new PopulationManager(popManagers);	//全個体統一マネージャ
		evaluationIndividual(trainDataInfos, allPopManager.currentRuleSets);

		//NSGA-II による評価
		if(objectiveNum != 1 && emoType == 0) {
			for(int island_i = 0; island_i < islandNum; island_i++) {
				nsga2.calcRank(popManagers[island_i].currentRuleSets);
			}
		}

		//ミシガン型の場合
		if(populationSize == 1) {
			popManagers[0].bestOfAllGen = new RuleSet( popManagers[0].currentRuleSets.get(0) );
		}

		return popManagers;
	}

	//各島の個体群サイズを計算
	protected int[] calcIslandPopNums(int populationNum) {
		int[] islandPopNums = new int[popDivNum];
		int patNum = 0;
		while(patNum < populationSize) {
			for(int i = 0; i < popDivNum; i++) {
				if(patNum < populationSize) {
					islandPopNums[i]++;
					patNum++;
				} else {
					break;
				}
			}
		}

		return islandPopNums;
	}

	//与えられた個体群(ArrayList<RuleSet>)の評価値を求める
	public void evaluationIndividual(DataSetInfo[] trainDataInfos, ArrayList<RuleSet> ruleSets) {
		//ルール数でソート
		boolean isSort = Consts.IS_RULESETS_SORT;
		if(isSort) {
			Collections.sort( ruleSets, new RuleSetCompByRuleNum() );
		}

		//評価（分散 or 単一）
		if(trainDataInfos[0].getSetting() == 1 && islandNum == 1) {
			//TODO 並列分散
		} else {
			try {
				forkJoinPool.submit( () ->
				ruleSets.parallelStream()
				.forEach( rule -> rule.evaluationRuleIsland(trainDataInfos) )
				).get();
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch(ExecutionException e) {
				e.printStackTrace();
			}
		}
	}



	//途中結果保持メソッド
	public void genCheck(int gen, int repeat, int cv, DataSetInfo[] trainDataInfos, PopulationManager[] popManagers) {
		if( (gen+1) <= 10 ||
			(gen+1) %10 == 0 && gen<=100||
			(gen+1) %100 == 0 && gen<=1000||
			(gen+1) %1000 == 0 && gen<=10000||
			(gen+1) %10000 == 0 && gen<=100000||
			(gen+1) %100000 == 0 && gen<=1000000||
			(gen+1) %1000000 == 0
			)
		{
//TODO 途中結果を保持するメソッド
//TODO 上の保持するタイミングも後で変更
		}
	}

	//NSGA-II 実行メソッド
	//戻り値は，生成した子個体群の評価にかかった時間
	protected double nsga2Type2(DataSetInfo[] trainDataInfos, PopulationManager[] popManagers, int[] dataIdx, int gen_i) {
		//子個体生成
		for(int island_i = 0; island_i < islandNum; island_i++) {
			geneticOperation(trainDataInfos[dataIdx[island_i]], popManagers[island_i], forkJoinPool);
		}

		//不要なルールの削除
		for(int island_i = 0; island_i < islandNum; island_i++) {
			deleteUnnecessaryRules(popManagers[island_i]);
		}

		//子個体群に対してデータ番号付与
		for(int i = 0; i < popManagers.length; i++) {
			popManagers[i].setDataIdxtoRuleSets(dataIdx[i], false);
		}

		//子個体群の個体評価開始
		TimeWatcher timer = new TimeWatcher();
		timer.start();

		//統合してから評価する(シャローコピー)
		PopulationManager allPopManager = new PopulationManager(popManagers);
		evaluationIndividual(trainDataInfos, allPopManager.newRuleSets);

		timer.end();

		//世代更新
		for()
		//TODO 2019/03/29




	}

	//与えられたpopManagerの個体群に対して，Michigan + Pittsburgh のハイブリッド遺伝的操作を行う
	protected void geneticOperation(DataSetInfo trainDataInfo, PopulationManager popManager, ForkJoinPool forkJoinPool) {
		int length = popManager.getIslandPopNum();
		popManager.newRuleSets.clear();

		for(int child_i = 0; child_i < length; child_i++) {
			popManager.newRuleSetInit();
			//Hybrid Genetic Operation
			popManager.crossOverAndMichiganOpe(child_i, popManager.currentRuleSets.size(), forkJoinPool, trainDataInfo);
			popManager.newRuleSetMutation(child_i, forkJoinPool, trainDataInfo);
		}
	}

	//子個体群(newRuleSets)に対して不要なルールを削除する
	protected void deleteUnnecessaryRules(PopulationManager popManager) {
		for(int rule_i = 0; rule_i < popManager.newRuleSets.size(); rule_i++) {
			popManager.newRuleSets.get(rule_i).removeRule();
		}
	}

	// **************************************************

	//Classes *******************************************

	//ルール数でソートするComparatorルール
	public class RuleSetCompByRuleNum implements Comparator<RuleSet> {
		public int compare(RuleSet a, RuleSet b) {
			int no1 = a.getRuleNum();
			int no2 = b.getRuleNum();

			//降順でソート
			if(no1 < no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	// **************************************************

}
