package methods;

import java.util.ArrayList;

import gbml.Consts;
import gbml.RuleSet;

//static なメソッドを記述したクラス
public class StaticGeneralFunc {

	StaticGeneralFunc(){}

	//実験パラメータ定数を出力
	public static String getExperimentSettings(String[] args) {
		String allSettings = "";
		String endLine = System.lineSeparator();
		allSettings += endLine;
		for(int i = 0; i < args.length; i++) {
			allSettings += args[i] + endLine;
		}
		Consts consts = new Consts();
		allSettings += consts.getStaticValues();
		return allSettings;
	}


//TODO sampringWithout2()で問題なければ削除
//	//非復元抽出
//	//[0, DataSize-1]の範囲から，非復元でnum個のインデックスをランダムに抽出する
//	public static int[] sampringWithout_old(int num, int DataSize, MersenneTwisterFast rnd) {
//		int[] ans = new int[num];
//		int i, j, same;
//
//		int[] patternNumber2 = new int[DataSize];
//		int generateNumber = num;
//
//		//DataSizeが取り出したい数(num)に足りないときは，
//		//DataSize全体 + 足りない分のインデックスを0から追加していく
//		if(DataSize < num) {
//			for(i = 0; i < DataSize; i++) {
//				patternNumber2[i] = i;
//			}
//			generateNumber = num - DataSize;
//		}
//
//		//非復元抽出
//		for(i = 0; i < generateNumber; i++) {
//			same = 0;
//			ans[i] = rnd.nextInt(DataSize);
//			for(j = 0; j < i; j++) {
//				if(ans[i] == ans[j]) {
//					same = 1;
//				}
//			}
//			if(same == 1) {
//				i--;
//			}
//		}
//
//		if(DataSize < num) {
//			int ii = 0;
//			for(i = generateNumber; i < num; i++) {
//				ans[i] = patternNumber2[ii];
//				ii++;
//			}
//		}
//
//		return ans;
//	}

	//非復元抽出
	//[0, DataSize-1]の範囲から，非復元でnum個のインデックスをランダムに抽出する
	public static int[] sampringWithout(int num, int DataSize, MersenneTwisterFast rnd) {
		int ans[] = new int[num];

		//DataSizeが取り出したい数(num)に足りないとき用
		int[] number2 = new int[DataSize];
		int generateNumber = num;	//問題なければ，num個のNumberをGenerateする

		//DataSizeが取り出したい数(num)に足りないときは，
		//DataSize全体(順番はランダムになる) + 足りない分のインデックスを0から追加していく
		if(DataSize < num) {
			for(int i = 0; i < DataSize; i++) {
				number2[i] = i;
			}
			generateNumber = num - DataSize;
		}

		//非復元抽出
		for(int i = 0; i < generateNumber; i++) {
			boolean isSame = false;
			ans[i] = rnd.nextInt(DataSize);
			for(int j = 0; j < i; j++) {
				if(ans[i] == ans[j]) {
					isSame = true;
				}
			}
			if(isSame) {
				i--;
			}
		}

		if(DataSize < num) {
			int ii = 0;
			for(int i = generateNumber; i < num; i++) {
				ans[i] = number2[ii];
				ii++;
			}
		}

		return ans;
	}

	//バイナリトーナメント (NSGA-II)
	public static int binaryT4(ArrayList<RuleSet> ruleSets, MersenneTwisterFast rnd, int popSize, int objectiveNum) {
		int winner = 0;
		int select1, select2;

		do {
			//トーナメント出場者をランダムで決定
			select1 = rnd.nextInt(popSize);
			select2 = rnd.nextInt(popSize);

			if(objectiveNum == 1) {
				int optimizer = 1; //最小化:1, 最大化:-1
				if( (optimizer * ruleSets.get(select1).getFitness()) < (optimizer * ruleSets.get(select2).getFitness()) ) {
					winner = select1;
				} else {
					winner = select2;
				}
			} else {
				if( ruleSets.get(select1).getRank() > ruleSets.get(select2).getRank()) {
					winner = select2;
				} else if( ruleSets.get(select1).getRank() < ruleSets.get(select2).getRank()) {
					winner = select1;
				} else if( ruleSets.get(select1).getRank() == ruleSets.get(select2).getRank() ) {
					if( ruleSets.get(select1).getCrowding() < ruleSets.get(select2).getCrowding() ) {
						winner = select2;
					} else {
						winner = select1;
					}
				}
			}
		} while(ruleSets.get(winner).getRuleNum() == 0);

		return winner;
	}

}
