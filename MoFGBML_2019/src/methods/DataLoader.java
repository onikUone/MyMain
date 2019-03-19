package methods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gbml.DataSetInfo;

//データ読み込み用クラス
public class DataLoader {


	//データ読み込みメソッド
	// [fileName]で指定したデータセットを、[data(DataSetInfo)]に読み込むメソッド
	public static void inputFile(DataSetInfo data, String fileName) {
		List<double[]> lines = new ArrayList<double[]>();
		try( Stream<String> line = Files.lines(Paths.get(fileName)) ) {
			lines =
					line.map(s ->{
						String[] numbers = s.split(",");
						double[] nums = new double[numbers.length];

						//値が無い場合の例外処理
						for(int i = 0; i < nums.length; i++) {
							//if (numbers[i].matches("^([1-9][0-9]*|0|/-)(.[0-9]+)?$") ){
								nums[i] = Double.parseDouble(numbers[i]);
							//}else{
							//	nums[i] = 0.0;
							//}
						}
						return nums;
					})
					.collect( Collectors.toList() );

		} catch (IOException e) {
			e.printStackTrace();
		}

		//1行目の「データの詳細パラメータ」を読み込む
		data.setDataSize( (int)lines.get(0)[0] );
		data.setNdim( (int)lines.get(0)[1] );
		data.setCnum( (int)lines.get(0)[2] );
		lines.remove(0);

		//2行目以降は[属性値 - クラスラベル]の集合
		lines.stream().forEach(data::addPattern);
	}
}
