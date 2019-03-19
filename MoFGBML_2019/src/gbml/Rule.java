package gbml;

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

	//ルール作成
	public void geneMic() {
		rule = new int[Ndim];
	}

	public void makeRuleSingle(Pattern line, MersenneTwisterFast rnd2) {
		rule = StaticFuzzyFunc.selectSingle(line, Ndim, rnd2);
	}
	// ********************************************************
}
