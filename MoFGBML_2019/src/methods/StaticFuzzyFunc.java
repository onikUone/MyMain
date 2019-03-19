package methods;

import gbml.Consts;
import gbml.Pattern;

public class StaticFuzzyFunc {

	public StaticFuzzyFunc() {}

	static int KK[] = {1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,6,6,6,6,6,7,7,7,7,7,7,7};	//メンバシップの時のK
	static int kk[] = {1,1,2,1,2,3,1,2,3,4,1,2,3,4,5,1,2,3,4,5,6,1,2,3,4,5,6,7};	//メンバシップの時のk

	int LargeK[];
	int SmallK[];

	static double w[]  = {100.0, 1.0, 1.0};	//単目的ファジィ遺伝的アルゴリズム用荷重和適応度関数用重み

	//ファジィ分割の初期化
	public void KKkk(int maxFnum){
		int arrayNum = 0;
		for(int i=0; i<maxFnum; i++){
			arrayNum += (i+1);
		}
		LargeK = new int[arrayNum];
		SmallK = new int[arrayNum];

		int num = 0;
		for(int i=0; i<maxFnum; i++){
			for(int j=0; j<(i+1); j++){
				LargeK[num] = i+1;
				SmallK[num] = j+1;
				num++;
			}
		}
	}

	//Membership Function	メンバシップ関数
	public static double calcMembership(int fuzzySetNum, double x) {
		double uuu = 0.0;

		if(fuzzySetNum == 0) {//=don't care
			uuu = 1.0;
		}else if(fuzzySetNum > 0) {
			double a = (double)(kk[fuzzySetNum] - 1) / (double)(KK[fuzzySetNum] - 1);
			double b = 1.0 / (double)(KK[fuzzySetNum] - 1);

			uuu = 1.0 - (Math.abs(x - a) / b);

			if(uuu < 0.0) {
				uuu = 0.0;
			}
		} else {
			if(fuzzySetNum == x) {
				uuu = 1.0;
			}else {
				uuu = 0.0;
			}
		}

		return uuu;
	}

	//HDFS使わない場合
	//一つのルール前件部を生成
	public static int[] selectSingle(Pattern line, int Ndim, MersenneTwisterFast rnd) {
		int[] rule = new int[Ndim];
		boolean isProb = Consts.IS_PROBABILITY_DONT_CARE;	//固定のdon't care確率を使用するかどうか
		double dcRate;	//don't care確率
		if(isProb) {
			dcRate = Consts.DONT_CARE_RT;
		} else {
			// dcRate = (Ndim - 5) /Ndim
			dcRate = (double)( ((double)Ndim - (double)Consts.ANTECEDENT_LEN) / (double)Ndim );
		}

		double[] membershipValueRoulette = new double[Consts.FUZZY_SET_NUM];
		for(int n = 0; n < Ndim; n++) {
			if(rnd.nextDouble() < dcRate) {
				rule[n] = 0;
			} else {
				if(line.getDimValue(n) < 0) {
					rule[n] = (int) line.getDimValue(n);
				}else {
					double sumMembershipValue = 0.0;
					membershipValueRoulette[0] = 0.0;
					//各ファジィ集合を選択する確率の範囲を生成（適合度の大きさに応じて確率の大きさ(ルーレットの範囲)を決定）
					for(int f = 0; f < Consts.FUZZY_SET_NUM; f++) {
						//各ファジィ集合におけるメンバシップ値を計算
						sumMembershipValue += calcMembership(f + 1, line.getDimValue(n) );
						membershipValueRoulette[f] = sumMembershipValue;
					}
					double rr = rnd.nextDouble() * sumMembershipValue;
					for(int f = 0; f < Consts.FUZZY_SET_NUM; f++) {
						if(rr < membershipValueRoulette[f]) {
							rule[n] = f + 1;
							break;
						}
					}
				}
			}
		}

		return rule;
	}
}
