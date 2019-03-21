package nsga2;

import java.io.Serializable;
import java.util.ArrayList;

import gbml.Consts;
import gbml.RuleSet;
import methods.MersenneTwisterFast;

public class Nsga2 implements Serializable{

	//Constructor ***********************************
	public Nsga2(int objectives, MersenneTwisterFast rnd) {

	}
	// **********************************************

	//Fields ****************************************
	int objectiveNum;	//目的数
	MersenneTwisterFast rnd;
	ArrayList<RuleSet> rankedList = new ArrayList<RuleSet>();	//個体群の非劣ランクリスト
	// **********************************************

	//Methods ***************************************

	//優越ランキングを計算
	//引数に与えられた[ArrayList<RuleSet>]のそれぞれのfitnessesを用いて優越ランキングを計算
	public void calcRank(ArrayList<RuleSet> ruleSets) {
		rankedList.clear();

		//n_i[pop_i] : pop_i を 優越する個体の数
		int[] n_i = new int[ruleSets.size()];

		@SuppressWarnings("unchecked")

		//S_i[pop_i] : pop_i が 優越する個体のインデックスをリストで保持
		ArrayList<Integer>[] S_i = new ArrayList[ruleSets.size()];

		//rankが昇順になるようなruleSetsのインデックスリスト
		ArrayList<Integer> F_i = new ArrayList<Integer>();

		//優越ランク計算開始
		for(int pop_i = 0; pop_i < ruleSets.size(); pop_i++) {
			n_i[pop_i] = 0;
			S_i[pop_i] = new ArrayList<Integer>();

			for(int pop_j = 0; pop_j < ruleSets.size(); pop_j++) {
				if(pop_i != pop_j) {
					if( isDominate(pop_i, pop_j, ruleSets) ) {
						//pop_i が pop_j を優越する
						S_i[pop_i].add(pop_j);
					} else if(isDominate(pop_j, pop_i, ruleSets)) {
						n_i[pop_i]++;
					}
				}
			}
			if(n_i[pop_i] == 0) {
				//pop_i を 優越する個体が存在しない
				ruleSets.get(pop_i).setRank(0);
				rankedList.add(ruleSets.get(pop_i));
				F_i.add(pop_i);
			}
		}

		//Crowding Distance 計算開始
		int popSize = ruleSets.size();
		double firstMax = 0;
		double firstMin = 100;
		boolean isNormalize = Consts.DO_CD_NORMALIZE;
		ArrayList<Double> firstObj = new ArrayList<Double>();
		ArrayList<Double> nowFirst = new ArrayList<Double>();

		if(isNormalize) {
			//Crowding Distance を正規化する

			//fitness[0]について最小値と最大値を獲得
			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				nowFirst.add(ruleSets.get(pop_i).getFitness(0));
				if(firstMax < nowFirst.get(pop_i)) {
					firstMax = nowFirst.get(pop_i);
				} else if(firstMin > nowFirst.get(pop_i)) {
					firstMin = nowFirst.get(pop_i);
				}
			}

			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				//正規化の計算
				double afterFirst = ((nowFirst.get(pop_i) - firstMin) / (firstMax - firstMin)) * 99;
				//対数
				double log10First = Math.log10(afterFirst + 1);
				firstObj.add(log10First);
			}
		} else {
			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				firstObj.add(ruleSets.get(pop_i).getFitness(0));
			}
		}

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			ruleSets.get(pop_i).setFirstObj(firstObj.get(pop_i));
		}

		//全ランクについてCrowding Distanceを計算する
		int i = 0;
		calcDistance(rankedList);
		rankedList.clear();
		ArrayList<Integer> Q = new ArrayList<Integer>();	//対象とするランクの個体群を保持するリスト
		while(F_i.size() != 0) {
			for(int p = 0; p < F_i.size(); p++) {
				for(int q = 0; q < S_i[F_i.get(p)].size(); q++) {
					n_i[S_i[F_i.get(p)].get(q)] -= 1;
					if(n_i[S_i[F_i.get(p)].get(q)] == 0) {
						ruleSets.get( S_i[F_i.get(p)].get(q) ).setRank(i + 1);
						Q.add(S_i[F_i.get(p)].get(q));
						rankedList.add(ruleSets.get( S_i[F_i.get(p)].get(q) ));
					}
				}
			}
			if(rankedList.size() != 0) {
				calcDistance(rankedList);
			}
			rankedList.clear();
			i++;
			F_i.clear();
			for(int k = 0; k < Q.size(); k++) {
				F_i.add(Q.get(k));
			}
			Q.clear();
		}
	}

	//pがqを優越しているかどうか
	protected boolean isDominate(int p, int q, ArrayList<RuleSet> ruleSets) {
		// Minimize fitness
		//if p dominate q then true
		//	else false

		boolean ans = false;
		int i = 1;	//Minimize したい目的関数には i = 1, Maximize したい目的関数には i = -1, とすれば不等号をこのまま使える
		for(int o = 0; o < objectiveNum; o++) {
			if( i * ruleSets.get(p).getFitness(o) > i * ruleSets.get(q).getFitness(o)) {
				// どこか一つでも「p > q」 ならば pはqを優越しない
				ans = false;
				break;
			} else if(i * ruleSets.get(p).getFitness(o) < i * ruleSets.get(q).getFitness(o)) {
				ans = true;
			}
		}
		return ans;
	}

	//Crowding Distance の計算
	protected void calcDistance(ArrayList<RuleSet> ruleSets) {
		int popSize = ruleSets.size();

		//Crowding Distance 初期化
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			ruleSets.get(pop_i).setCrowding(0);
		}

		for(int o = 0; o < objectiveNum; o++) {
			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				for(int j = pop_i;
					j >= 1 && ruleSets.get(j - 1).getFirstObj(o) > ruleSets.get(j).getFirstObj(o);
					j--) {

					RuleSet tmp = ruleSets.get(j);
					ruleSets.set(j, ruleSets.get(j - 1));
					ruleSets.set(j - 1, tmp);
				}
			}

			ruleSets.get(0).setCrowding(Double.POSITIVE_INFINITY);
			//2目的のときにあると，Infinity増える
			//list.get(popSize - 1).SetCrowding(Double.POSITIVE_INFINITY);

			double min = ruleSets.get(0).getFirstObj(o);
			double max = ruleSets.get(popSize - 1).getFirstObj(o);
			double maxmin = max - min;
			if(maxmin == 0) {
				for(int pop_i = 1; pop_i < popSize; pop_i++) {
					double distance = 0;
					ruleSets.get(pop_i).setCrowding(ruleSets.get(pop_i).getCrowding() + distance);
				}
			} else {
				for(int pop_i = 1; pop_i < popSize; pop_i++) {
					double distance = (Math.abs(ruleSets.get(pop_i + 1).getFirstObj(o)
							- ruleSets.get(pop_i - 1).getFirstObj(o)) / maxmin);
					ruleSets.get(pop_i).setCrowding(ruleSets.get(pop_i).getCrowding() + distance);
				}
			}
		}
	}


	// **********************************************

}
