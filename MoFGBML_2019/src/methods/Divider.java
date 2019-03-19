package methods;

import java.net.InetSocketAddress;

import gbml.DataSetInfo;

public class Divider {

	//Fields ********************************************
	static MersenneTwisterFast uniqueRnd;
	static int dpop;	//プレサンプリングサイズ
	// **************************************************

	//Constructor ***************************************
	public Divider(int dpop) {
		Divider.dpop = dpop;
	}

	public Divider(MersenneTwisterFast rnd, int dpop) {
		Divider.uniqueRnd = new MersenneTwisterFast( rnd.nextInt() );
		Divider.dpop = dpop;
	}
	// **************************************************

	//Methods *******************************************
	public DataSetInfo[] letsDivide(DataSetInfo dataSetInfo, int setting, InetSocketAddress[] serverList) {

		int partitionNum = dpop;
		int classNum = dataSetInfo.getCnum();
		int dataSize = dataSetInfo.getDataSize();

		//データのサンプリング時にすべてのデータを使う場合
		if(partitionNum == 1) {
			DataSetInfo[] dataSetInfos = new DataSetInfo[1];
			dataSetInfos[0] = dataSetInfo;
			return dataSetInfos;
		}

		//ここから
		//各クラスのサイズ
		int[] eachClassSize = new int[classNum];
		for(int i = 0; i < dataSize; i++) {
			eachClassSize[dataSetInfo.getPattern(i).getConClass()]++;
		}

		//それぞれの分割データセットにおける各クラスのパターンの数
		int[][] classDividedSize = new int[classNum][partitionNum];
		int remainAddPoint = 0;
		for(int class_i = 0; class_i < classNum; class_i++) {
			for(int i = 0; i < partitionNum; i++) {
				//分割データセット[i]に含まれるクラス[class_i]のパターンの数
				classDividedSize[class_i][i] = eachClassSize[class_i] / partitionNum;
			}
			int remain = eachClassSize[class_i] % partitionNum;	//振り分け切らなかった余り
			for(int i = 0; i < remain; i++) {
				int point = remainAddPoint % partitionNum;	//余ったパターンの振り分け先
				classDividedSize[class_i][point]++;
				remainAddPoint++;
			}
		}

		//それぞれの分割データセットの大きさ
		int[] eachDataSize = new int[partitionNum];
		for(int class_i = 0; class_i < classNum; class_i++) {
			for(int i = 0; i < partitionNum; i++) {
				eachDataSize[i] += classDividedSize[class_i][i];
			}
		}

		//それぞれの分割データにクラスごとにデータを割り当てていく
		DataSetInfo[] divideDatas = new DataSetInfo[partitionNum + 1];
		for(int divide_i = 0; divide_i < partitionNum; divide_i++) {
			divideDatas[divide_i] = new DataSetInfo(eachDataSize[divide_i], dataSetInfo.getNdim(), classNum, setting, serverList);
		}

		//一番後ろに分割前のデータセット全体のDataSetInfoインスタンスを格納
		divideDatas[partitionNum] = dataSetInfo;

		//まず同じクラス同士を固めるソート
		dataSetInfo.sortPattern();

		//各クラスごとに順番に各データに格納（シャローコピー）
		int index = 0;
		for(int class_i = 0; class_i < classNum; class_i++) {
			for(int divide_i = 0; divide_i < partitionNum; divide_i++) {
				for(int pattern_i = 0; pattern_i < classDividedSize[class_i][divide_i]; pattern_i++) {
					divideDatas[divide_i].addPattern( dataSetInfo.getPattern(index++) );
				}
			}
		}

		return divideDatas;
	}
	// **************************************************

}
