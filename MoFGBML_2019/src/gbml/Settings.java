package gbml;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import methods.CommandLineFunc;
import methods.OsSpecified;

//TODO
/*
 * コマンドラインから個別に指定（今は順に指定）
 * 設定ファイルから指定（今はコマンドラインからのみ）
 */

//プログラムの実行時における各種実験設定を操作するクラス
//引数について詳しくは[methods.CommandLineFunc.java]を参照
public class Settings {
	//Field - メンバ変数 ****************************************************/
	int osType;
	ForkJoinPool forkJoinPool = null;

	//コマンドライン引数
	//並列分散実装設定
	int calclationType = 0;	//0:Single node, 1:Apache Spark, 2:Simple Socket

	String dataName = "default";	//データセット名
	int generationNum = 1000;	//世代数
	int populationSize = 100;	//個体群サイズ
	int objectiveNum = 2;	//目的数
	int emoType = 0;	//EMOA (0: NSGA-II, 1:WS, 2:TCH, 3:PBI, 4:IPBI, 5:AOF)
	int crossValidationNum = 1;	// x-CrossValidation
	int startRepeatPos = 0;	//CVの繰り返しのスタート位置
	int finishRepeatPos = 1;	//CVの繰り返しのストップ位置
	int seed = 2019;	//乱数シード値
	int preDivNum = 1;	//プレサンプリングの分割数
	boolean isOnceExe = true;	//CVの試行終了タイミング（true:1回ずつ試行を終了させる）
	String saveDir = "default";	//データ保存先ディレクトリ名
	int islandNum = 1;	//島数
	int migrationItv = 100;	//最良個体 移住間隔
	int rotationItv = 100;	//部分データ 交換間隔 (=部分個体群 移住間隔)

	// calclationType == 0 :single nodeの時の使用コア数
	int parallelCores = 1;	//使用する並列コア数

	// 並列分散環境
	boolean isDistributed = true;	//分散環境かどうか
	String dirLocation = "";
	int serverNum = 4;	//データの分割数
	int portNum = 50000;	//ポート番号
	int threadNum = 18;	//各ノードのスレッド数? つかってない
	ArrayList<String> nodeNames;	//ノードの名前
	InetSocketAddress[] serverList;	//サーバリスト


	//*********************************************************************/

	//constructor - コンストラクタ
	public Settings(String args[]) {

		//the type of OS - OSの種類の読取
		if(OsSpecified.isLinux()==true || OsSpecified.isMac()==true) {
			osType = Consts.UNIX;	// Linux or Mac
			System.out.println("OS: Linux or Mac");
		} else {
			osType = Consts.WINDOWS;	//windows
			System.out.println("OS: Windows");
		}

		//各種コマンドライン引数読取
		calclationType = Integer.parseInt(args[0]);
		//コマンドライン引数が足りているかの確認
		if(calclationType == 0) {
			CommandLineFunc.lessArgs(args,  17);
		}else if(calclationType == 1) {
			CommandLineFunc.lessArgs(args, 20);
		}
		//基本設定
		dataName = args[1];
		generationNum = Integer.parseInt(args[2]);
		populationSize = Integer.parseInt(args[3]);
		objectiveNum = Integer.parseInt(args[4]);
		emoType = Integer.parseInt(args[5]);
		crossValidationNum = Integer.parseInt(args[6]);
		startRepeatPos = Integer.parseInt(args[7]);
		finishRepeatPos = Integer.parseInt(args[8]);
		seed = Integer.parseInt(args[9]);
		preDivNum = Integer.parseInt(args[10]);
		isOnceExe = Boolean.parseBoolean(args[11]);
		saveDir = args[12];
		migrationItv = Integer.parseInt(args[13]);
		rotationItv = Integer.parseInt(args[14]);


		//個別設定
		if(calclationType == 0) {
			setSingleNode(args);
		}else if(calclationType == 1) {
			setSimpleSocket(args);
		}

	}

	//methods - メンバメソッド
	// calclationType == 0 :Single Nodeの時の設定
	void setSingleNode(String args[]) {
		parallelCores = Integer.parseInt(args[15]);
		forkJoinPool = new ForkJoinPool(parallelCores);
		islandNum = Integer.parseInt(args[16]);
	}
	//calclationType == 1 :Apach Sparkの時の設定
	void setSimpleSocket(String args[]) {

		isDistributed = Boolean.parseBoolean(args[15]);
		dirLocation = args[16];
		serverNum = Integer.parseInt(args[17]);
		portNum = Integer.parseInt(args[18]);
		//島の分割数 or データ分割数
		islandNum = Integer.parseInt(args[19]);

		nodeNames = new ArrayList<String>();
		//nodeNamesの取得
		for(int i = 0; i < serverNum; i++) {
			nodeNames.add(args[i + 20]);
		}
		//サーバリストの作成
		serverList = new InetSocketAddress[nodeNames.size()];
		for(int i = 0; i < nodeNames.size(); i++) {
			serverList[i] = new InetSocketAddress(nodeNames.get(i), portNum);
		}
		//テスト用
		forkJoinPool = new ForkJoinPool(threadNum);
	}
}