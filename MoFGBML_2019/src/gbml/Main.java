package gbml;

import java.util.Date;

public class Main {

	public static void main(String[] args) {
		// The version of this program.
		System.out.println("ver. " + 21.0);	// Yuichi OMOZAKI (2019)

		/******************************************************************************/
		// Basic Settings - 基本設定
		Settings sets = new Settings(args);

		// Output Basic Datas + Start Experiment - 基本データ出力と実行
		//実行ランタイム環境が使用できるコア数を出力
		System.out.println("Processors: " + Runtime.getRuntime().availableProcessors() + " ");
		//コマンドライン引数の出力
		System.out.print("args: ");
		for(int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println("");
		System.out.println("");

		//実験開始タイム保持
		Date date = new Date();
		System.out.print("START: ");
		System.out.println(date);

		//実験開始
		//一回ごとにjarファイルを呼び出すか否か
		if(sets.isOnceExe) {
			onceExection(sets, args);
		} else {
			repeatExection(sets, args);
		}
	}

	//一回ごとに試行を終了する
	static public void onceExection(Settings sets, String[] args) {

	}

	//自動でrepeatする
	static public void repeatExection(Settings sets, String[] args) {

	}

}
