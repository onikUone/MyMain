package gbml;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import methods.StaticFuzzyFunc;

public class Rule {
	//Fields **************************************************
	MersenneTwisterFast uniqueRnd;

	//学習用
	int Ndim;			//次元
	int Cnum;			//クラス数
	int DataSize;		//学習用パターン数
	int TstDataSize;	//評価用パターン数

	//基本情報
	int[] rule;		//前件部ルール
	int conclusion;	//結論部クラス
	double cf;		//ルール重み
	int ruleLength;	//ルール長

	int fitness;	//使用回数（= 単一勝利回数）
	// ********************************************************

	//Constructor *********************************************
	public Rule(MersenneTwisterFast rnd, int Ndim, int Cnum, int DataSize, int TstDataSize) {
		this.uniqueRnd = new MersenneTwisterFast( rnd.nextInt() );
		this.Ndim = Ndim;
		this.Cnum = Cnum;
		this.DataSize = DataSize;
		this.TstDataSize = TstDataSize;
	}

	//Deep Copy
	public Rule(Rule rule) {
		this.uniqueRnd = new MersenneTwisterFast( rule.uniqueRnd.nextInt() );

		this.Ndim = rule.Ndim;
		this.Cnum = rule.Cnum;
		this.DataSize = rule.DataSize;
		this.TstDataSize = rule.TstDataSize;

		this.rule = Arrays.copyOf(rule.rule, rule.Ndim);

		this.conclusion = rule.conclusion;
		this.cf = rule.cf;
		this.ruleLength = rule.ruleLength;
		this.fitness = rule.fitness;
	}
	// ********************************************************

	//Methods *************************************************

	//ルール配列初期化
	public void geneMic() {
		rule = new int[Ndim];
	}

	//前件部ルール生成
	public void makeRuleSingle(Pattern line, MersenneTwisterFast rnd2) {
		rule = StaticFuzzyFunc.selectSingle(line, Ndim, rnd2);
	}

	//前件部をランダムで生成
	public void makeRuleRnd1(MersenneTwisterFast rnd) {
		rule = StaticFuzzyFunc.selectRnd(this.Ndim, rnd);
	}
	//後件部をランダムで生成
	public void makeRuleRnd2() {
		this.conclusion = uniqueRnd.nextInt(Cnum);
		this.cf = uniqueRnd.nextDouble();
		this.ruleLength = ruleLengthCalc();
	}

	//ルール結論部 決定メソッド
	public void calcRuleConc(DataSetInfo trainData, ForkJoinPool forkJoinPool) {
		//trainDataから、このミシガン型ルールにおける各クラスに対する信頼度を計算
		double[] trust = StaticFuzzyFunc.calcTrust(trainData, this.rule, trainData.getCnum(), forkJoinPool);
		this.conclusion = StaticFuzzyFunc.calcConclusion(trust, trainData.getCnum());
		this.cf = StaticFuzzyFunc.calcCf(this.conclusion, trust, trainData.getCnum());
		this.ruleLength = ruleLengthCalc();
	}

	//結論部に存在しないクラスから選択して結論部クラスを決定する
	public void makeRuleNoCla(int[] noClass) {
		this.conclusion = noClass[uniqueRnd.nextInt(noClass.length)];
		this.cf = uniqueRnd.nextDouble();
		this.ruleLength = ruleLengthCalc();
	}

	//ルール数の計算
	public int ruleLengthCalc() {
		int ans = 0;
		for(int i = 0; i < this.Ndim; i++) {
			if(rule[i] != 0) {
				ans++;
			}
		}
		return ans;
	}

	//結論部とルール重みを指定してセットする
	public void makeRuleCross(int ansCla, double cf) {
		this.conclusion = ansCla;
		this.cf = cf;
		this.ruleLength = ruleLengthCalc();
	}

	//突然変異メソッド
	//dim番目の条件部に突然変異操作を行う
	public void mutation(int dim, MersenneTwisterFast rnd2, ForkJoinPool forkJoinPool, DataSetInfo trainData) {
		int newFuzzySet = 0;
		int count = 0;
		do {
			if(count > 10) {
				//10回突然変異の結果が（突然変異なし = 変わらない）場合，終了
				break;
			}
			//ランダムに抽出したパターンのdim番目の値を取り出す
			double rndPat = trainData.getPattern( rnd2.nextInt( trainData.getDataSize() ) ).getDimValue(dim);
			if(rndPat >= 0.0) {
				newFuzzySet = rnd2.nextInt(Consts.FUZZY_SET_NUM + 1);
			} else {
				newFuzzySet = (int)rndPat;	// = don't careになる
			}
			count++;
		} while(newFuzzySet == rule[dim]);

		rule[dim] = newFuzzySet;

		//条件部が変わったことによる結論部の再計算
		calcRuleConc(trainData, forkJoinPool);
	}

	//[num]番目のファジィ集合を[ruleN]にする
	public void setRule(int num, int ruleN) {
		this.rule[num] = ruleN;
	}

	//HDFS使わない場合
	//引数[line]に対する[this]の適合度を返すメソッド
	public double calcAdaptationPure(Pattern line) {
		return StaticFuzzyFunc.memberMulPure(line, this.rule);
	}

	//単一勝利したときに使用回数としてfitnessをインクリメント
	public void addFitness() {
		this.fitness++;
	}

	public void clearFitness() {
		this.fitness = 0;
	}


	//GET SET Methods

	public int getRule(int dim) {
		return this.rule[dim];
	}

	public int getConc() {
		return this.conclusion;
	}

	public double getCf() {
		return this.cf;
	}

	public int getRuleLength() {
		return this.ruleLength;
	}

	public int getFitness() {
		return this.fitness;
	}

	// ********************************************************
}
