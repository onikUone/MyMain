package gbml;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import methods.MersenneTwisterFast;
import methods.ResultMaster;
import moead.Moead;
import nsga2.Nsga2;
import time.TimeWatcher;

//GAを制御するクラス
public class GaManager {
	//Fields ********************************************
	Nsga2 nsga2;
	ArrayList<Moead> moeads;

	MersenneTwisterFast rnd;

	ForkJoinPool forkJoinPool;

	InetSocketAddress[] serverList;
	int serverNum;

	TimeWatcher timeWatcher;

	ResultMaster resultMaster;

	int islandNum;
	int popDivNum;

	int secondObjType = Consts.SECOND_OBJECTIVE_TYPE;

	int objectiveNum;
	long generationNum;

	int emoType;
	int populationSize;

	String dataName;
	// **************************************************

	//Constructor ***************************************
	public GaManager() {}

	public GaManager(Nsga2 nsga2) {
		this.nsga2 = nsga2;
	}

	public GaManager(int popSize, Nsga2 nsga2, ArrayList<Moead> moeads, MersenneTwisterFast rnd, ForkJoinPool forkJoinPool,
			InetSocketAddress[] serverList, int serverNum, int objectiveNum, int generationNum,
			int emoType, int islandNum, ResultMaster resultMaster, TimeWatcher timeWatcher, String dataName) {

		this.rnd = rnd;
		this.nsga2 = nsga2;
		this.moeads = moeads;

		this.forkJoinPool = forkJoinPool;

		this.serverList = serverList;
		this.serverNum = serverNum;

		this.resultMaster = resultMaster;
		this.timeWatcher = timeWatcher;

		this.objectiveNum = objectiveNum;
		this.generationNum = generationNum;
		this.emoType = emoType;
		this.islandNum = islandNum;
		this.populationSize = popSize;

		//条件によって部分個体群数をデータ分割数と同じにするか決める
		boolean isNotEqualDiv = Consts.IS_NOT_EQUAL_DIVIDE_NUM;
		if(isNotEqualDiv) {
			this.popDivNum = this.serverNum;
		} else {
			this.popDivNum = this.islandNum;
		}

		this.dataName = dataName;
	}
	// **************************************************

	//Methods *******************************************

	//GaFrame
	public PopulationManager[] gaFrame(DataSetInfo[] trainDataInfos, int migrationItv, int rotationItv, int calclationType, int repeat_i, int cv_i) {

		//各種 経過時間保存用リスト
		ArrayList<Double> times = new ArrayList<Double>();

		//個体群の生成
		PopulationManager[] popManagers = null;

		//各島に対するデータ番号の生成
		int[] dataIdx = new int[popDivNum];	//popDivNum: 個体群を分割する分割数
		int dataInterval = (int)(islandNum / popDivNum);
		for(int i = 0; i < popDivNum; i++) {
			dataIdx[i] = i * dataInterval;
		}

		//Step 1. 個体群の初期化
		if(calclationType == 0) {

			popManagers = generateInitialPop(trainDataInfos, dataIdx, calclationType);

		} else {	//分散の場合はオブジェクトを作るだけでルールは作成しない．
			//TODO 並列分散用
		}



		return popManagers;
	}

	//Initialize Population	初期個体群の生成
	protected PopulationManager[] generateInitialPop(DataSetInfo[] trainDataInfos, int[] dataIdx, int calclationType) {
		//初期個体群の宣言
		PopulationManager[] popManagers = null;

		//初期個体群の生成(複数)
		popManagers = new PopulationManager[islandNum];
		int[] islandPopNums = calcIslandPopNums(populationSize);
		for(int island_i = 0; island_i < islandNum; island_i++) {
			popManagers[island_i] = new PopulationManager(rnd, objectiveNum);	//島内の個体群生成
//TODO 2019/03/19			popManagers[island_i]
		}

		return popManagers;
	}

	//各島の個体群サイズを計算
	protected int[] calcIslandPopNums(int populationNum) {
		int[] islandPopNums = new int[popDivNum];
		int patNum = 0;
		while(patNum < populationSize) {
			for(int i = 0; i < popDivNum; i++) {
				if(patNum < populationSize) {
					islandPopNums[i]++;
					patNum++;
				} else {
					break;
				}
			}
		}

		return islandPopNums;
	}


	// **************************************************

}
