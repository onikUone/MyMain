package gbml;

import java.util.ArrayList;
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
					//TODO 2019/03/20
				}
			}
		} while( this.micRules.size() == 0 );
	}


	// *******************************************************




}
