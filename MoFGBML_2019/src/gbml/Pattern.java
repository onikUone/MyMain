package gbml;

import java.io.Serializable;

// データセットのパターン一行を表現するクラス
public class Pattern implements Serializable{

	//Fields
	double[] x;
	int conClass;	//ラベル識別問題 教師信号

	//Constructor
	public Pattern(){}

	public Pattern(double[] _line) {
		int Ndim = _line.length - 1;
		x = _line;
		conClass = (int)_line[Ndim];
	}

	//Methods
	public double getDimValue(int i) {
		return this.x[i];
	}

	public int getConClass() {
		return this.conClass;
	}

}
