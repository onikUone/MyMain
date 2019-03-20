package gbml;

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

	//ルール結論部 決定メソッド
	public void calcRuleConc(DataSetInfo trainData, ForkJoinPool forkJoinPool) {
		//trainDataから、このミシガン型ルールにおける各クラスに対する信頼度を計算
		double[] trust = StaticFuzzyFunc.calcTrust(trainData, this.rule, trainData.getCnum(), forkJoinPool);
		this.conclusion = StaticFuzzyFunc.calcConclusion(trust, trainData.getCnum());
		this.cf = StaticFuzzyFunc.calcCf(this.conclusion, trust, trainData.getCnum());
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
	// ********************************************************
}
