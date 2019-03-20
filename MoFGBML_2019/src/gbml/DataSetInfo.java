package gbml;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DataSetInfo {

	//Fields ****************************************************
	int Ndim;
	int Cnum;
	int DataSize;

	ArrayList<Pattern> patterns = new ArrayList<Pattern>();

	int setting = 0;
	InetSocketAddress[] serverList = null;

	// **********************************************************

	//Constructor ***********************************************
	public DataSetInfo() {}

	public DataSetInfo(int _DataSize, int _Ndim, int _Cnum, int _setting, InetSocketAddress[] _serverList) {
		this.DataSize = _DataSize;
		this.Ndim = _Ndim;
		this.Cnum = _Cnum;

		this.setting = _setting;
		this.serverList = _serverList;
	}
	// **********************************************************


	//Methods ***************************************************

	public void sortPattern() {
		//クラス数でソート
		Collections.sort( this.patterns, new patternComparator() );
	}

	public int getSetting() {
		return this.setting;
	}

	public int getNdim() {
		return this.Ndim;
	}

	public int getCnum() {
		return this.Cnum;
	}

	public int getDataSize() {
		return this.DataSize;
	}

	public Pattern getPattern(int _index) {
		return this.patterns.get(_index);
	}

	public ArrayList<Pattern> getPattern() {
		return this.patterns;
	}

	public InetSocketAddress[] getServerList() {
		return this.serverList;
	}

	public void setDataSize(int _num) {
		this.DataSize = _num;
	}

	public void setNdim(int _num) {
		this.Ndim = _num;
	}

	public void setCnum(int _num) {
		this.Cnum = _num;
	}

	public void setSetting(int setting, InetSocketAddress[] serverList) {
		this.setting = setting;
		this.serverList = serverList;
	}

	//ファイルから読み込み時用
	public void addPattern(double[] _line) {
		this.patterns.add(new Pattern(_line));
	}

	//Divider用
	public void addPattern(Pattern _pattern) {
		this.patterns.add(_pattern);
	}

	// **********************************************************

	public class patternComparator implements Comparator<Pattern>{
		public int compare(Pattern a, Pattern b) {
			double no1 = a.getConClass();
			double no2 = b.getConClass();
			//昇順でソート
			if(no1 > no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}


