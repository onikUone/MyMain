package gbml;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import moead.Moead;
import nsga2.Nsga2;

public class PopulationManager implements Serializable{

	//Fields ******************************************************
	MersenneTwisterFast uniqueRnd;

	//個体群
	public ArrayList<RuleSet> currentRuleSets = new ArrayList<RuleSet>();	//現世代個体群
	public ArrayList<RuleSet> newRuleSets = new ArrayList<RuleSet>();	//子個体群
	public ArrayList<RuleSet> margeRuleSets = new ArrayList<RuleSet>();	//現世代 + 子世代

	//Michigan型GBML用
	public RuleSet bestOfAllGen;

	//評価のみ並列用
	ArrayList<Integer> vecIdxes = new ArrayList<Integer>();

	//For Island Model
	boolean isIsland = true;
	int nowGen = 0;
	int intervalGen;
	int terminationGen;
	boolean isEvaluation = false;
	int dataIdx;
	int islandPopNum;

	Nsga2 nsga2;
	Moead moead;

	int emoType;

	//読み取った値
	int generationNum;
	int osType;

	int attributeNum;
	int classNum;
	int trainDataSize;
	int testDataSize;

	int objectiveNum;
	// ************************************************************

	//Constructor *************************************************
	PopulationManager(){}

	public PopulationManager(MersenneTwisterFast rnd, int objectiveNum) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		this.objectiveNum = objectiveNum;
	}

	public PopulationManager(MersenneTwisterFast rnd, int objectiveNum, int generationNum) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		this.objectiveNum = objectiveNum;
		this.terminationGen = generationNum;
	}

	//PopulationManagerの設定を引き継いだ初期化
	public PopulationManager(PopulationManager popManager) {
		this.uniqueRnd = new MersenneTwisterFast( popManager.uniqueRnd.nextInt() );
		this.objectiveNum = popManager.objectiveNum;
		this.osType = popManager.osType;
		this.attributeNum = popManager.attributeNum;
		this.classNum = popManager.classNum;
		this.objectiveNum = popManager.objectiveNum;
		this.terminationGen = popManager.terminationGen;
		this.emoType = popManager.emoType;

		this.isIsland = popManager.isIsland;
		this.nowGen = popManager.nowGen;
		this.intervalGen = popManager.intervalGen;
		this.isEvaluation = popManager.isEvaluation;
		this.dataIdx = popManager.dataIdx;

		this.generationNum = popManager.generationNum;
		this.trainDataSize = popManager.trainDataSize;
		this.testDataSize = popManager.testDataSize;
	}

	//分割された個体群から一つの統合個体群を作成
	public PopulationManager(PopulationManager[] popManagers) {
		this.objectiveNum = popManagers[0].objectiveNum;
		this.osType = popManagers[0].osType;
		this.attributeNum = popManagers[0].attributeNum;
		this.classNum = popManagers[0].classNum;
		this.objectiveNum = popManagers[0].objectiveNum;
		this.terminationGen = popManagers[0].terminationGen;

		this.currentRuleSets.clear();
		this.newRuleSets.clear();
		for(int divide_i = 0; divide_i < popManagers.length; divide_i++) {
			this.currentRuleSets.addAll(popManagers[divide_i].currentRuleSets);
			this.newRuleSets.addAll(popManagers[divide_i].newRuleSets);
		}

		this.bestOfAllGen = popManagers[0].bestOfAllGen;
	}

	//統合個体群作成（ArrayList用）
	public PopulationManager(ArrayList<PopulationManager> popManagers){
		this.objectiveNum = popManagers.get(0).objectiveNum;
		this.osType = popManagers.get(0).osType;
		this.attributeNum = popManagers.get(0).attributeNum;
		this.classNum = popManagers.get(0).classNum;
		this.objectiveNum = popManagers.get(0).objectiveNum;
		this.terminationGen = popManagers.get(0).terminationGen;

		currentRuleSets.clear();
		newRuleSets.clear();
		for(int divide_i = 0; divide_i < popManagers.size(); divide_i++){
			this.currentRuleSets.addAll(popManagers.get(divide_i).currentRuleSets);
			this.newRuleSets.addAll(popManagers.get(divide_i).newRuleSets);
		}
		this.bestOfAllGen = popManagers.get(0).bestOfAllGen;
	}
	// ************************************************************

	//Methods *****************************************************

	//初期個体群の生成メソッド
	public void generateInitialPopulation(DataSetInfo dataSetInfo, int populationSize, ForkJoinPool forkJoinPool,
											int calclationType, int dataIdx, InetSocketAddress[] serverList) {
		//学習用データ（問題環境）の詳細データ読み込み
		attributeNum = dataSetInfo.getNdim();
		classNum = dataSetInfo.getCnum();
		trainDataSize = dataSetInfo.getDataSize();

		for(int pop_i = 0; pop_i < populationSize; pop_i++) {
			//個体群サイズだけ[RuleSet]オブジェクトのインスタンス生成
			currentRuleSets.add(new RuleSet(uniqueRnd, attributeNum, classNum, trainDataSize, testDataSize, objectiveNum, pop_i));
		}

		if(calclationType == 0) {
			for(int pop_i = 0; pop_i < currentRuleSets.size(); pop_i++) {
				//各個体（=ピッツバーグ型個体）の各ルールを生成
				currentRuleSets.get(pop_i).generalInitialRules(dataSetInfo, forkJoinPool);
				//TODO 2019/03/19
			}
		}else if(calclationType == 1) {
			//TODO spark用
		}
	}

	// ************************************************************
}