package methods;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import gbml.Consts;
import gbml.DataSetInfo;
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

	// **********************************************
	//Fuzzy Rule-Based	(ファジィルールベース用メソッド)

	//適合度
	public static double memberMulPure(Pattern line, int[] rule) {
		double ans = 1.0;
		int Ndim = rule.length;
		for(int i = 0; i < Ndim; i++) {
			try {
				ans *= calcMembership(rule[i], line.getDimValue(i));
				if(ans == 0) {//途中で0になれば、必ずゼロのまま
					return ans;
				}
			} catch (Exception e) {

			}
		}
		return ans;
	}

	//信頼度（データから直接なので，データが大きいと結構重い処理（O[n] ) )
	public static double[] calcTrust(DataSetInfo dataSetInfo, int[] rule, int Cnum, ForkJoinPool forkJoinPool) {

		ArrayList<Double> part = new ArrayList<Double>();

		Optional<Double> partSum = null;

		for(int class_i = 0; class_i < Cnum; class_i++) {
			final int CLASSNUM = class_i;
			partSum = null;
			try {
				partSum = forkJoinPool.submit( () ->
					dataSetInfo
					.getPattern().parallelStream()
					.filter( s -> s.getConClass() == CLASSNUM )
					.map( s -> memberMulPure(s, rule) )
					.reduce( (l, r) -> l+r )
					).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			part.add( partSum.orElse(0.0) );
		}

		double all = 0.0;
		for(int class_i = 0; class_i < part.size(); class_i++) {
			all += part.get(class_i);
		}

		double[] trust = new double[Cnum];	//このミシガン型ルールにおける各クラスに対する信頼度
		if(all != 0.0) {
			for(int class_i = 0; class_i < Cnum; class_i++) {
				trust[class_i] = part.get(class_i) / all;
			}
		}

		return trust;
	}

	//結論部クラス決定メソッド
	//与えられた各クラスに対する信頼度から結論部クラスを決定する
	public static int calcConclusion(double[] trust, int Cnum) {
		int ans = 0;
		double max = 0.0;
		//信頼度が一番大きいクラスを調べる
		for(int class_i = 0; class_i < Cnum; class_i++) {
			if(max < trust[class_i]) {
				max = trust[class_i];
				ans = class_i;
			} else if(max == trust[class_i]) {
				ans = -1;	//信頼度が同じになるクラスが存在するとき、識別不能なルールとする
			}
		}
		return ans;
	}

	//ルール重み計算メソッド
	//ルールの結論部クラスと各クラスに対する信頼度からルール重みを計算
	public static double calcCf(int conCla, double[] trust, int Cnum) {
		double ans = 0.0;
		if(conCla == -1 || trust[conCla] <= 0.5) {
			//識別不能なミシガン型ルール or 結論部クラスに対する信頼度が0.5以下
			ans = 0;
		} else {
			double sum = 0.0;
			for(int i = 0; i < Cnum; i++) {
				sum += trust[i];
			}
			ans = trust[conCla] + trust[conCla] - sum;	//sumにはtrust[conCla]が含まれるため
		}
		return ans;
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
