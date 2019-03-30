package gbml;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import methods.StaticGeneralFunc;
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
			}
		}else if(calclationType == 1) {
			//TODO spark用
		}
	}

	//[RuleSet]に対して所属する島番号インデックスをセットする
	public void setDataIdxtoRuleSets(int dataIdx, boolean isParent) {
		if(isParent) {
			currentRuleSets.stream().forEach( r -> r.setDataIdx(dataIdx) );
		} else {
			newRuleSets.stream().forEach( r -> r.setDataIdx(dataIdx) );
		}
	}

	//[this.newRuleSets]をaddする
	protected void newRuleSetInit() {
		this.newRuleSets.add( new RuleSet(uniqueRnd, attributeNum, classNum, trainDataSize, testDataSize, objectiveNum) );
	}

	//親選択・交叉操作（Pittsburgh型遺伝操作 + Michigan操作）
	//(newRuleSetsIdx)番目の子個体を生成するメソッド
	//Michigan適用確率に従って，Michigan型遺伝的操作 or Pittsburgh型遺伝的操作 のどちらかで子個体を生成
	protected void crossOverAndMichiganOpe(int newRuleSetsIdx, int popSize, ForkJoinPool forkJoinPool, DataSetInfo trainDataInfo) {
		int mom, dad;	//親個体（Pittsburgh型個体）のインデックス
		int Nmom, Ndad;	//親個体に含まれるMichigan型ルールのインデックス

		//親選択
		mom = StaticGeneralFunc.binaryT4(currentRuleSets, uniqueRnd, popSize, objectiveNum);
		dad = StaticGeneralFunc.binaryT4(currentRuleSets, uniqueRnd, popSize, objectiveNum);

		//ルールの操作 (Michigan操作)
		if(uniqueRnd.nextDouble() < (double)Consts.RULE_OPE_RT) {
			RuleSet deep = new RuleSet(currentRuleSets.get(mom));
			newRuleSets.get(newRuleSetsIdx).copyRuleSet(deep);
			newRuleSets.get(newRuleSetsIdx).setRuleNum();

			if(newRuleSets.get(newRuleSetsIdx).getRuleNum() != 0) {
				boolean doHeuris = Consts.DO_HEURISTIC_GENERATION;
				if(doHeuris) {
					//
					newRuleSets.get(newRuleSetsIdx).micGenHeuris(trainDataInfo, forkJoinPool);
				} else {
					newRuleSets.get(newRuleSetsIdx).micGenRandom();
				}
			}
		} else {
			//Pittsburgh型個体（＝識別器自体）の交叉操作
			if(uniqueRnd.nextDouble() < (double)(Consts.RULESET_CROSS_RT)) {
				//それぞれの親個体から，ランダムなRuleのインデックスを選択する
				Nmom = uniqueRnd.nextInt(currentRuleSets.get(mom).getRuleNum()) + 1;	//momから一つのMichigan型ルール
				Ndad = uniqueRnd.nextInt(currentRuleSets.get(dad).getRuleNum()) + 1;	//dadから一つのMichigan型ルール

				if( (Nmom + Ndad) > Consts.MAX_RULE_NUM) {
					int delNum = Nmom + Ndad - Consts.MAX_RULE_NUM;
					for(int v = 0; v < delNum; v++) {
						if(uniqueRnd.nextBoolean()) {
							Nmom--;
						} else {
							Ndad--;
						}
					}
				}

				int pmom[] = new int[Nmom];
				int pdad[] = new int[Ndad];

				//mom個体からNmom個のMichigan型ルールを非復元抽出
				pmom = StaticGeneralFunc.sampringWithout(Nmom, currentRuleSets.get(mom).getRuleNum(), uniqueRnd);
				//dad個体からNdad個のMichigan型ルールを非復元抽出
				pdad = StaticGeneralFunc.sampringWithout(Ndad, currentRuleSets.get(dad).getRuleNum(), uniqueRnd);

				//子個体生成開始
				newRuleSets.get(newRuleSetsIdx).micRules.clear();
				for(int j = 0; j < Nmom; j++) {
					newRuleSets.get(newRuleSetsIdx).setMicRule(currentRuleSets.get(mom).getMicRule(pmom[j]));
				}
				for(int j = 0; j < Ndad; j++) {
					newRuleSets.get(newRuleSetsIdx).setMicRule(currentRuleSets.get(dad).getMicRule(pdad[j]));
				}

				// </ Pittsburgh型遺伝的操作
			} else {
				//Michigan型もPittsburgh型も遺伝的操作を行わない
				//親をそのまま子個体とする
				if(uniqueRnd.nextBoolean()) {//mom or dad
					RuleSet deep = new RuleSet(currentRuleSets.get(mom));
					newRuleSets.get(newRuleSetsIdx).copyRuleSet(deep);
				} else {
					RuleSet deep = new RuleSet(currentRuleSets.get(dad));
					newRuleSets.get(newRuleSetsIdx).copyRuleSet(deep);
				}
			}
			newRuleSets.get(newRuleSetsIdx).setRuleNum();
		}
	}

	//子個体群の各Michigan型ルールの突然変異操作
	protected void newRuleSetMutation(int ruleSetIndex, ForkJoinPool forkJoinPool, DataSetInfo trainData) {
		int rulesNum = newRuleSets.get(ruleSetIndex).newMicRules.size();

		for(int rule_i = 0; rule_i < rulesNum; rule_i++) {
			if(uniqueRnd.nextInt(rulesNum) == 0) {
				int mutDim = uniqueRnd.nextInt(trainData.getNdim());	//突然変異を加える属性を選択
				//(ruleSetIndex番目の子個体)の各ルールに突然変異操作を加える
				newRuleSets.get(ruleSetIndex).micMutation(rule_i, mutDim, forkJoinPool, trainData);
			}
		}
	}

	//個体群中の最良個体の獲得メソッド
	public RuleSet calcBestRuleSet() {
		RuleSet best;
		best = new RuleSet(currentRuleSets.get(0));	//まず一つ目の識別器で初期化

		if(objectiveNum == 1) {
			//TODO 単目的の場合
		} else {	//多目的
			/* 最良個体基準
			*  1. rankの小さいもの
			*  2. 誤識別率が小さいもの
			*  3. ルール数が少ないもの
			*  4. 総ルール長が少ないもの
			*  5. それでも同じ場合,後から読み込んだもので置き換える
			*/
			for(int pop_i = 0; pop_i < currentRuleSets.size(); pop_i++) {
				if(currentRuleSets.get(pop_i).getRank() == 0) {	//最良個体は必ずrank = 0であるため，rank=0以外は見ない
					if(currentRuleSets.get(pop_i).getMissRate() < best.getMissRate()) {

						best = new RuleSet(currentRuleSets.get(pop_i));

					} else if(currentRuleSets.get(pop_i).getMissRate() == best.getMissRate()) {
						if(currentRuleSets.get(pop_i).getRuleNum() < best.getRuleNum()) {

							best = new RuleSet(currentRuleSets.get(pop_i));

						} else if(currentRuleSets.get(pop_i).getRuleNum() == best.getRuleNum()) {
							if(currentRuleSets.get(pop_i).getRuleLength() <= best.getRuleLength()) {

								best = new RuleSet(currentRuleSets.get(pop_i));

							}
						}
					}
				}
			}
		}

		return best;
	}

	//GET SET Methods

	public int getIslandPopNum() {
		return this.islandPopNum;
	}

	public int getEmoType() {
		return this.emoType;
	}

	public void setIslandPopNum(int _popNum) {
		this.islandPopNum = _popNum;
	}

	public void setDataIdx(int _dataIdx) {
		this.dataIdx = _dataIdx;
	}

	public void setEmoType(int _emoType) {
		this.emoType = _emoType;
	}
	// ************************************************************
}
