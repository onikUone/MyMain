package methods;

public class CommandLineFunc {

	//コマンドライン引数が足りない場合
	public static void lessArgs(String[] args, int argsLen) {
		if(args.length < argsLen) {
			System.out.println("");
			System.out.println("=======Check args=======");
			System.out.println("0 Calclation Type: int");
			System.out.println("1 DataName: String");
			System.out.println("2 Generations: int");
			System.out.println("3 Population: int");
			System.out.println("4 Objectives: int");
			System.out.println("5 EmoAlgolithm: int (0:NSGAII, 1:WS, 2:TCH, 3:PBI, 4:IPBI, 5:AOF");
			System.out.println("6 CV: int");
			System.out.println("7 Start Repeat Position: int");
			System.out.println("8 Finish Repeat Position: int");
			System.out.println("9 Seed: int");
			System.out.println("10 PreDivNum: int");
			System.out.println("11 One: boolean");
			System.out.println("12 SaveDivrectory: String");
			System.out.println("13 Migration Interval: int");
			System.out.println("14 Rotation Interval: int");

			System.out.println("=======Case: Single node=======");
			System.out.println("15 Parallel Cores Num: int");
			System.out.println("16 Island Num: int");

			System.out.println("=======Case: Apache Spark=======");
			System.out.println("12 isDitributed: boolean");
			System.out.println("11 PartitionNum	int");
			System.out.println("11 HDFS Folder Name String");
			System.out.println("9 master Node Name :String");
			System.out.println("10 AppName: String");
			System.out.println("10 No. of executor: int");
			System.out.println("10 No. of exes'cores: int");

			System.out.println("=======Case: Simple Socket=======");
			System.out.println("12 isDitributed: boolean");
			System.out.println("11 PartitionNum: int");
			System.out.println("11 HDFS Folder Name: String");
			System.out.println("11 Port's number: int");
			System.out.println("11 Thread's number: int");
			System.out.println("11 Nodes' names: String");

			System.out.println("========================");

			System.exit(-1);	//終了エラー
		}
	}
}
