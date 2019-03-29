package gbml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import methods.StaticGeneralFunc;

public class RuleSet {

	//Fields *************************************************
	MersenneTwisterFast uniqueRnd;

	//学習用
	int Ndim;			//次元
	int Cnum;			//クラス数
	int DataSize;		//学習用データパターン数
	int DataSizeTst;	//評価用データパターン数

	//個体基本情報
	ArrayList<Rule> micRules = new ArrayList<Rule>();
	ArrayList<Rule> newMicRules = new ArrayList<Rule>();	//ミシガン型GBML用子世代ルール

	double missRate;		//学習用データ誤識別率
	double testMissRate;	//評価用データ誤識別率
	int ruleNum;			//ルール数（ミシガンルールの個数）
	int ruleLength;			//総ルール長

	//ミスパターン保存用リスト
	ArrayList<Integer> missPatterns = new ArrayList<Integer>();
	int MissPatNum;

	//Confusion Matrix
	int[][] confusionMatrix;

	//GA用情報
	double fitness;	//1目的用
	double[] fitnesses;	//2目的以上

	//所属している島番号
	int dataIdx = 0;

	//MOEAD用
	int vecNum;
	double otherDataRate;

	//NSGA用
	int evaflag;
	int rank;	//優越ランク
	double crowding;	//混雑距離の評価値
	double[] firstobj;

	//socket用
	int socketMethodNum = 0;
	// *******************************************************

	//Constructor ********************************************
	RuleSet(){}

	RuleSet(MersenneTwisterFast rnd, int Ndim, int Cnum, int DataSize, int DataSizeTst, int objectiveNum, int vecNum){
		this.uniqueRnd = new MersenneTwisterFast( rnd.nextInt() );
		this.Ndim = Ndim;
		this.Cnum = Cnum;
		this.DataSize = DataSize;
		this.DataSizeTst = DataSizeTst;

		this.evaflag = 0;
		this.rank = 0;
		this.crowding = 0;
		this.fitnesses = new double[objectiveNum];
		this.firstobj = new double[objectiveNum];

		this.confusionMatrix = new int[Cnum][Cnum];
	}

	//ミシガン型のbestOfAllGen用
	//Deep Copy
	public RuleSet(RuleSet ruleSet) {
		this.uniqueRnd =  new MersenneTwisterFast( ruleSet.uniqueRnd.nextInt() );
		this.Ndim = ruleSet.Ndim;
		this.Cnum = ruleSet.Cnum;
		this.DataSize = ruleSet.DataSize;
		this.DataSizeTst = ruleSet.DataSizeTst;

		for(int i = 0; i < this.Cnum; i++) {
			for(int j = 0; j < this.Cnum; j++) {
				this.confusionMatrix[i][j] = ruleSet.confusionMatrix[i][j];
			}
		}

		this.missRate = ruleSet.missRate;
		this.ruleNum = ruleSet.ruleNum;
		this.ruleLength = ruleSet.ruleLength;
		this.fitness = ruleSet.fitness;

		this.vecNum = ruleSet.vecNum;

		Rule a;
		this.micRules.clear();
		for(int i=0;i<ruleSet.micRules.size();i++){
			 a = new Rule(ruleSet.micRules.get(i));
			this.micRules.add(a);
		}

		this.evaflag = ruleSet.evaflag;
		this.testMissRate = ruleSet.testMissRate;
		this.rank = ruleSet.rank;
		this.crowding = ruleSet.crowding;
		fitnesses = Arrays.copyOf(ruleSet.fitnesses, ruleSet.fitnesses.length);
		firstobj = Arrays.copyOf(ruleSet.firstobj, ruleSet.fitnesses.length);

		if(ruleSet.missPatterns != null){
			this.missPatterns = new ArrayList<Integer>();
			for(int i=0; i<ruleSet.missPatterns.size(); i++){
				this.missPatterns.add( ruleSet.missPatterns.get(i) );
			}
		}
		this.MissPatNum = ruleSet.MissPatNum;
	}

	RuleSet(MersenneTwisterFast rnd, int Ndim, int Cnum, int DataSize, int DataSizeTst, int objectiveNum){
		this.uniqueRnd = new MersenneTwisterFast( rnd.nextInt() );
		this.Ndim = Ndim;
		this.Cnum = Cnum;
		this.DataSize = DataSize;
		this.DataSizeTst = DataSizeTst;
		this.confusionMatrix = new int[Cnum][Cnum];

		this.evaflag = 0;
		this.rank = 0;
		this.crowding = 0;
		this.vecNum = 0;
		this.fitnesses = new double[objectiveNum];
		this.firstobj = new double[objectiveNum];
	}

	// *******************************************************

	//Methods ************************************************

	//ピッツバーグ個体の初期化メソッド
	public void generalInitialRules(DataSetInfo trainDataInfo, ForkJoinPool forkJoinPool) {
		//ヒューリスティックなルール生成法を行うかどうか
		boolean isHeuris = Consts.DO_HEURISTIC_GENERATION;

		do {	//while( micRules.size() == 0 )
			int[] sampleNums = null;
			if(isHeuris) {	//サンプリング
				sampleNums = new int[Consts.INITIATION_RULE_NUM];
				sampleNums = StaticGeneralFunc.sampringWithout(Consts.INITIATION_RULE_NUM, trainDataInfo.DataSize, uniqueRnd);
			}

			for(int i = 0; i < Consts.INITIATION_RULE_NUM; i++) {
				micRules.add(new Rule(uniqueRnd, trainDataInfo.getNdim(), trainDataInfo.getCnum(), trainDataInfo.getDataSize(), DataSizeTst));
				micRules.get(i).geneMic();
				if(isHeuris) {	//ヒューリスティック生成法
					//サンプリングしたパターンを用いてルールを生成
					micRules.get(i).makeRuleSingle(trainDataInfo.getPattern(sampleNums[i]), uniqueRnd);
					micRules.get(i).calcRuleConc(trainDataInfo, forkJoinPool);
				} else {	//完全ランダム生成
					micRules.get(i).makeRuleRnd1(uniqueRnd);	//前件部をランダムで生成
					micRules.get(i).makeRuleRnd2();				//後件部をランダムで生成
				}
			}

			removeRule();
		} while( this.micRules.size() == 0 );

		this.ruleNum = micRules.size();
		this.ruleLength = ruleLengthCalc();
	}

	//ルール重みが0以下 or ルール長が0（全てdon't care）のルールを削除する
	public void removeRule() {
		for(int i = 0; i < micRules.size(); i++) {
			int size = 0;
			while(micRules.size() > size) {
				if(micRules.get(size).getCf() <= 0 || micRules.get(size).getRuleLength() == 0) {
					micRules.remove(size);
				} else {
					size++;
				}
			}
		}
	}

	//ピッツバーグ個体が保持するミシガン型ルールの全ルール長の総和を調べる
	public int ruleLengthCalc() {
		int ans = 0;
		for(int i = 0; i < this.ruleNum; i++) {
			ans += micRules.get(i).getRuleLength();
		}
		return ans;
	}

	// **********************************************************************************************************
	//島用

	//[this]が保持している島番号[dataIdx]の[trainDataInfos[dataIdx]]の部分学習用データから[this]の評価を行う
	public void evaluationRuleIsland(DataSetInfo[] trainDataInfos) {
		int way = Consts.SECOND_OBJECTIVE_TYPE;	//2目的目が何か
		int objectiveNum = this.fitnesses.length;

		if(getRuleNum() != 0) {

			//各手法ごとの計算
			double ans = 0;
			ans = calcMissPatternsWithRule(trainDataInfos[dataIdx]);	//誤識別したパターン数獲得

			double acc = ans / trainDataInfos[dataIdx].getDataSize();
			setMissRate( acc * 100.0 );
			setNumAndLength();	//このメソッド内で不要なルール（ルール重み0以下など）は削除される

			//各目的関数における値計算
			if(objectiveNum == 1) {
				double fitness = Consts.W1 * getMissRate() + Consts.W2 * getRuleNum() + Consts.W3 * getRuleLength();
				setFitness(fitness, 0);
			} else if(objectiveNum == 2) {
				setFitness( getMissRate(), 0 );
				setFitness( out2objectiveFunc(way), 1);
			} else if(objectiveNum == 3) {
				setFitness( getMissRate(), 0);
				setFitness( getRuleNum(), 1);
				setFitness( getRuleLength(), 2);
			} else {
				System.out.println("not be difined.");
			}
			if(getRuleLength() == 0) {	//全てdon't careのRuleSetの評価値は全て100000（超でかい＝超悪い）で評価する
				for(int o = 0; o < objectiveNum; o++) {
					setFitness(100000, o);
				}
			}
		}
	}


	//ルールで並列化する場合
	//与えられた[dataSetInfo]に対して[this]が誤識別をしたパターン数を返すメソッド
	public int calcMissPatternsWithRule(DataSetInfo dataSetInfo) {
		//初期化
		int ruleNum = this.micRules.size();
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			//ミシガン型ルールのfitnessを初期化
			micRules.get(rule_i).clearFitness();
		}
		missPatterns.clear();
		MissPatNum = 0;
		confusionMatrix = new int[Cnum][Cnum];

		int dataSize = dataSetInfo.getDataSize();
		int ans = -1;
		for(int pattern_i = 0; pattern_i < dataSize; pattern_i++) {
			ans = calcWinClassPalWithRule( dataSetInfo.getPattern(pattern_i) );
			//Confusion Matrix 更新
			if(ans != -1) {
				this.confusionMatrix[dataSetInfo.getPattern(pattern_i).getConClass()][ans]++;
			}
			if( ans != dataSetInfo.getPattern(pattern_i).getConClass() ) {
				//識別結果が間違っている

				//すぐにメモリあふれるのでケア(10,000個まで誤識別したパターンを保持できる)
				if(missPatterns.size() < 10000) {
					missPatterns.add(pattern_i);
				}
				MissPatNum++;
			}
		}

		//ミスパターン数を削減
		int maxMissPatNum = (int)(ruleNum * 0.1) + 1;
		if( maxMissPatNum < missPatterns.size() ) {
			ArrayList<Integer> newMissPats = new ArrayList<Integer>();
			for(int i = 0; i < maxMissPatNum; i++) {
				int rndIdx = uniqueRnd.nextInt(missPatterns.size());
				newMissPats.add(missPatterns.get(rndIdx));
				missPatterns.remove(rndIdx);
			}
			missPatterns.clear();
			missPatterns = newMissPats;
		}

		return this.MissPatNum;
	}

	//ルールで並列化する場合
	//引数[line]に対して[this]の識別結果を返すメソッド
	//単一勝利ルールの結論部を[this]の識別結果として出力する
	public int calcWinClassPalWithRule(Pattern line) {
		int answerClass = 0;
		int winRuleIdx = 0;

		int ruleSize = micRules.size();
		boolean canClassify = true;
		double maxMul = 0.0;

		for(int rule_i = 0; rule_i < ruleSize; rule_i++) {
			// (ルール重み) * (適合度)
			double multiValue = micRules.get(rule_i).getCf() * micRules.get(rule_i).calcAdaptationPure(line);

			if(maxMul < multiValue) {
				maxMul = multiValue;
				winRuleIdx = rule_i;
				canClassify = true;
			} else if( maxMul == multiValue && micRules.get(rule_i).getConc() != micRules.get(winRuleIdx).getConc() ) {
				//(ルール重み)*(適合度) が同じ値 かつ 結論部クラスが異なる
				canClassify = false;	//識別不能
			}
		}
		if( canClassify && maxMul != 0.0) {
			answerClass = micRules.get(winRuleIdx).getConc();
			micRules.get(winRuleIdx).addFitness();
		} else {
			answerClass = -1;	//識別不能
		}

		return answerClass;
	}

	//目的関数の定義
	double out2objectiveFunc(int way) {
		if(way == 4) {
			return (double)(getRuleLength() / getRuleNum());
		} else if(way == 3) {
			return (double)(getRuleNum() + getRuleLength());
		} else if(way == 2) {
			return (double)(getRuleNum() * getRuleLength());
		} else if(way == 1) {
			return (double)getRuleLength();
		} else {
			return (double)getRuleNum();
		}
	}

	//Deep Copy
	public void copyRuleSet(RuleSet ruleSet) {
		this.uniqueRnd = new MersenneTwisterFast( ruleSet.uniqueRnd.nextInt() );
		this.Ndim = ruleSet.Ndim;
		this.Cnum = ruleSet.Cnum;
		this.DataSize = ruleSet.DataSize;
		this.DataSizeTst = ruleSet.DataSizeTst;

		this.missRate = ruleSet.missRate;
		this.ruleNum = ruleSet.ruleNum;
		this.ruleLength = ruleSet.ruleLength;
		this.fitness = ruleSet.fitness;

		this.vecNum = ruleSet.vecNum;

		this.evaflag = ruleSet.evaflag;
		this.testMissRate = ruleSet.testMissRate;
		this.rank = ruleSet.rank;
		this.crowding = ruleSet.crowding;
		this.fitnesses = Arrays.copyOf(ruleSet.fitnesses, ruleSet.fitnesses.length);
		firstobj = Arrays.copyOf(ruleSet.firstobj, ruleSet.fitnesses.length);

		Rule a;
		this.micRules.clear();
		for(int i=0; i<ruleSet.micRules.size(); i++){
			a = new Rule( ruleSet.micRules.get(i) );
			this.micRules.add(a);
		}

		if(ruleSet.missPatterns != null){
			this.missPatterns = new ArrayList<Integer>();
			for(int i=0; i<ruleSet.missPatterns.size(); i++){
				this.missPatterns.add( ruleSet.missPatterns.get(i) );
			}
		}
		this.MissPatNum = ruleSet.MissPatNum;

	}

	//Michigan型交叉操作（ヒューリスティック）
	public void micGenHeuris(DataSetInfo trainDataInfo, ForkJoinPool forkJoinPool) {
		//交叉個体数（ルールの20%）あるいは1個
		int snum;
		if(uniqueRnd.nextDouble() < (double)Consts.RULE_OPE_RT) {
			snum = (int)( (ruleNum - 0.00001) * Consts.RULE_CHANGE_RT) + 1;
		} else {
			snum = 1;
		}

		//合計生成個体数
		int heuNum, genNum = 0;
		if(snum % 2 == 0) {
			heuNum = snum/2;
			genNum = snum/2;
		} else {
			int plus = uniqueRnd.nextInt(2);
			heuNum = (snum - 1)/2 + plus;
			genNum = snum - heuNum;
		}

		//ヒューリスティック生成の誤識別パターン
		//足りない or 無い場合はランダムに追加
		while(missPatterns.size() < heuNum) {
			missPatterns.add( uniqueRnd.nextInt( trainDataInfo.getDataSize() ));
		}

		int[] missPatternsSampleIdx = new int[heuNum];
		missPatternsSampleIdx = StaticGeneralFunc.sampringWithout(heuNum,  missPatterns.size(), uniqueRnd);

		for(int gen_i = 0; gen_i < genNum; gen_i++) {
			ruleCross(gen_i);
		}
	}

	//Michigan型交叉操作（ランダム）
	public void micGenRandom() {
		//交叉個体数（全ルールの20%）あるいは1個
		int snum;
		if(uniqueRnd.nextDouble() < (double)Consts.RULE_OPE_RT) {
			//Michigan型操作 適用確率
			snum = (int)((ruleNum - 0.00001) * Consts.RULE_CHANGE_RT) + 1;
		} else {
			snum = 1;
		}

		//合計生成個体数
		int heuNum, genNum = 0;
		if(snum % 2 == 0) {
			heuNum = snum/2;
			genNum = snum/2;
		} else {
			int plus = uniqueRnd.nextInt(2);
			heuNum = (snum - 1)/2 + plus;
			genNum = snum - heuNum;
		}

		for(int i = 0; i < genNum; i++) {
			ruleCross(i);
		}
		for(int i = genNum; i < snum; i++) {
			randomGeneration(i);
		}
	}

	//Michigan型ルール交叉
	public void ruleCross(int num) {
		newMicRules.add( new Rule(uniqueRnd, Ndim, Cnum, DataSize, DataSizeTst) );
		newMicRules.get(num).geneMic();

		//親個体選択（バイナリトーナメントは計算量が以上にかかるので，同じ結論部の個体同士で交叉，なければ諦める(ルール数回で)）
		int mom = uniqueRnd.nextInt(ruleNum);
		int dad = uniqueRnd.nextInt(ruleNum);
		int count = 0;
		while( micRules.get(dad).getConc() != micRules.get(mom).getConc() && count < ruleNum) {
			dad = uniqueRnd.nextInt(ruleNum);
			count++;
		}

		if(uniqueRnd.nextDouble() < Consts.RULE_CROSS_RT) {
			//UX : Uniformity Crossover (一様交叉)
			int k = 0;	//交叉
			int k2 = 0;	//突然変異
			int o = 0;
			for(int n = 0; n < Ndim; n++) {
				//交叉操作
				k = uniqueRnd.nextInt(2);
				if(k == 0) {
					newMicRules.get(num).setRule(n, micRules.get(mom).getRule(n));
				} else {
					newMicRules.get(num).setRule(n, micRules.get(dad).getRule(n));
				}
				//突然変異操作
				k2 = uniqueRnd.nextInt(Ndim);
				if(k2 == 0) {
					do {
						o = uniqueRnd.nextInt(Consts.FUZZY_SET_NUM + 1);
					} while(o == newMicRules.get(num).getRule(n));	//条件部が変わるまで突然変異させる
					newMicRules.get(num).setRule(n, o);
				}
			}
		} else {
			//交叉なし → 突然変異のみ（ベースはmom）
			int o = 0;
			int k2 = 0;
			for(int n = 0; n < Ndim; n++) {
				newMicRules.get(num).setRule(num, micRules.get(mom).getRule(n));
				k2 = uniqueRnd.nextInt(Ndim);
				if(k2 == 0) {
					do {
						o = uniqueRnd.nextInt(Consts.FUZZY_SET_NUM + 1);
					} while(o == newMicRules.get(num).getRule(n));
				}
			}
		}

		//子個体の結論部はmomに合わせる．ルール重みはランダムな割合で合計
		double momRate = uniqueRnd.nextDouble();
		double newCf = micRules.get(mom).getCf() * momRate + micRules.get(dad).getCf() * (1.0 - momRate);
		newMicRules.get(num).makeRuleCross(micRules.get(mom).getConc(), newCf);
	}

	//与えられた(ruleIdx)番目のルールの(dim)番目の条件部に突然変異操作を行う
	public void micMutation(int ruleIdx, int dim, ForkJoinPool forkJoinPool, DataSetInfo trainData) {
		micRules.get(ruleIdx).mutation(dim, uniqueRnd, forkJoinPool, trainData);
	}

	//
	public void randomGeneration(int num) {
		//足りていないクラスの個体生成を優先
		//識別中のクラス判別
		int noCla[] = calcNoClass();
		newMicRules.add( new Rule(uniqueRnd, Ndim, Cnum, DataSize, DataSizeTst));
		newMicRules.get(num).geneMic();
		newMicRules.get(num).makeRuleRnd1(uniqueRnd);
		if(noCla.length == 0) {
			newMicRules.get(num).makeRuleRnd2();
		} else {
			newMicRules.get(num).makeRuleNoCla(noCla);
		}
	}

	//結論部に存在しないクラスを返すメソッド
	protected int[] calcNoClass() {
		int[] haveClass = calcHaveClass();

		List<Integer> noCla = new ArrayList<Integer>();
		for(int class_i = 0; class_i < Cnum; class_i++) {
			boolean isHave = false;
			for(int have_i = 0; have_i < haveClass.length; have_i++) {
				if(class_i == haveClass[have_i]) {
					isHave = true;
				}
			}
			if(!isHave) {
				noCla.add(class_i);
			}
		}
		int[] noClass = noCla.stream().mapToInt(s -> s).toArray();

		return noClass;
	}

	protected int[] calcHaveClass() {
		int[] noCla = micRules.stream()
					.mapToInt(r -> r.getConc())
					.distinct()
					.sorted()
					.toArray();
		return noCla;
	}

	//GET SET Methods

	public int getRuleNum() {
		return this.ruleNum;
	}

	public double getMissRate() {
		return this.missRate;
	}

	public int getRuleLength() {
		return this.ruleLength;
	}

	public double getFitness(int _o) {
		return this.fitnesses[_o];
	}

	public double[] getFitnesses() {
		return this.fitnesses;
	}

	public void setRuleNum() {
		this.ruleNum = micRules.size();
	}

	public void setDataIdx(int dataIdx) {
		this.dataIdx = dataIdx;
	}

	public void setMissRate(double _missRate) {
		this.missRate = _missRate;
	}

	public void setNumAndLength() {
		removeRule();	//不要なルールを削除する
		this.ruleNum = micRules.size();
		this.ruleLength = ruleLengthCalc();
	}

	public void setFitness(double _fitness, int _o) {
		this.fitnesses[_o] = _fitness;
	}

	//Deep Copy
	public void setMicRule(Rule micRule) {
		Rule mic = new Rule(micRule);
		this.micRules.add(mic);
	}


	//NSGA-II
	public void setRank(int _rank) {
		this.rank = _rank;
	}


	public Rule getMicRule(int idx) {
		return this.micRules.get(idx);
	}

	public int getRank() {
		return this.rank;
	}

	public void setCrowding(double _crowd) {
		this.crowding = _crowd;
	}

	public double getCrowding() {
		return this.crowding;
	}

	public void setFirstObj(double _firstobj) {
		this.firstobj[0] = _firstobj;
		for(int i = 1; i < fitnesses.length; i++) {
			this.firstobj[i] = fitnesses[i];
		}
	}

	public double getFitness() {
		return this.fitness;
	}

	public double getFirstObj(int _num) {
		return this.firstobj[_num];
	}
	// *******************************************************




}
