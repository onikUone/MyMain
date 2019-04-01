package methods;

import java.io.File;
import java.util.ArrayList;

import gbml.Consts;
import gbml.DataSetInfo;
import gbml.PopulationManager;

public class ResultMaster {
	//Constructor ***********************
	public ResultMaster() {}

	public ResultMaster(String nameDir, int os) {
		this.os = os;
		this.nameDir = nameDir;
	}

	// **********************************

	//Fields ****************************
	int os;
	public String nameDir;

	//全体の時間(各試行毎の経過時間を保持)
	ArrayList<Double> times = new ArrayList<Double>();

	// **********************************

	//Methods ***************************

	public void setTimes(double nano) {
		this.times.add(nano);
	}

	//各世代毎の経過時間出力
	public void writeGeneTime(int repeat_i, int cv_i, ArrayList<Double> times) {
		String sep = File.separator;
		String fileName = nameDir + sep + Consts.TIMES + sep + "geneTimes_" + String.valueOf(repeat_i) + String.valueOf(cv_i) + ".csv";
		Output.writeln(fileName, times.toArray(new Double[times.size()]));
	}

	public void writeTime(double sec, double ns, int cv_i, int rep_i) {
		String sep = File.separator;
		String fileName = nameDir + sep + Consts.TIMES + sep + "Atime_0" + String.valueOf(rep_i) + String.valueOf(cv_i) + ".csv";

		String str = sec + ", " + ns;

		Output.writeln(fileName, str);
	}

	//人間が読みやすい形式での出力
	public void outputRulesForReadable(PopulationManager popManager, int gen_i, int cv_i, int repeat_i, int island_i) {
		String sep = File.separator;
		String path = nameDir + sep + Consts.LOGS_READABLE + sep + "result_" + String.valueOf(repeat_i) + String.valueOf(cv_i) + sep + "gene-" + String.valueOf(gen_i + 1) + "_" + String.valueOf(repeat_i) + String.valueOf(cv_i);
		//pathの形式: nameDir/logs_readable/gene-0_00/
		Output.makeDirPath(path);

		//ファイル名形式(例：ruleset_island-1.txt : 島1の個体群の情報)
		String fileName = path + sep + Consts.RULESET + "_island-" + String.valueOf(island_i) + ".txt";

		ArrayList<String> strs = new ArrayList<String>();
		String str = null;
		String nn = System.getProperty("line.separator");

		for(int pop_i = 0; pop_i < popManager.currentRuleSets.size(); pop_i++) {
			str = "";
			//1個体あたりの情報
			str += "------------ pop_" + String.format("%02d", pop_i) + " ------------" + nn;
			str += " MissRate   : " + String.valueOf( popManager.currentRuleSets.get(pop_i).getMissRate() ) + nn;
			str += " RuleNum    : " + String.valueOf( popManager.currentRuleSets.get(pop_i).getRuleNum() ) + nn;
			str += " RuleLength : " + String.valueOf( popManager.currentRuleSets.get(pop_i).getRuleLength() ) + nn;
			str += " Rank       : " + String.valueOf( popManager.currentRuleSets.get(pop_i).getRank() ) + nn;
			str += " Crowding   : " + String.valueOf( popManager.currentRuleSets.get(pop_i).getCrowding() ) + nn;
			str += "----------------------" + nn;

			str += " Confusion Matrix :" + nn + nn;
			int Cnum = popManager.currentRuleSets.get(pop_i).getCnum();
			for(int i = 0; i < Cnum; i++) {
				for(int j = 0; j < Cnum; j++) {
					str += " " + popManager.currentRuleSets.get(pop_i).getMatrix()[i][j];
				}
				str += nn;
			}
			str += nn + "----------------------" + nn;

			//Michigan Rules 出力開始
			str += " Michigan Rules :" + nn;
			for(int rule_i = 0; rule_i < popManager.currentRuleSets.get(pop_i).getRuleNum(); rule_i++) {
				str += " Rule_" + String.format("%02d", rule_i) + ":";
				//各条件部出力
				for(int dim_i = 0; dim_i < popManager.currentRuleSets.get(pop_i).getNdim(); dim_i++) {
					str += " " + String.format("%2d", popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getRule(dim_i) );
				}
				str += ", Class: " + String.valueOf( popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getConc() );
				str += ", CF: " + String.valueOf( popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getCf() );
				str += ", Fitness: " + String.valueOf( popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getFitness() );
				str += nn;
			}

			str += nn;
			str += "------------------------------------------------------------------" + nn;
			//1個体情報終わり

			strs.add(str);
		}

		String[] array = (String[]) strs.toArray(new String[0]);
		Output.writeln(fileName, array);

	}

	//後のプログラムで読み込みしやすい形式での個体群出力
	//island_i dataIdx pop_i rule_i rule[] conclusion cf ruleLength fitness
	public void outputRules(PopulationManager popManager, int gen_i, int cv_i, int repeat_i, int island_i) {
		String sep = File.separator;
		String path = nameDir + sep + Consts.LOGS + sep + "result_" + String.valueOf(repeat_i) + String.valueOf(cv_i) + sep + "gene-" + String.valueOf(gen_i + 1) + "_" + String.valueOf(repeat_i) + String.valueOf(cv_i);
		//pathの形式: nameDir/logs/gene-0_00/
		Output.makeDirPath(path);

		//ファイル名形式(例：ruleset_island-1.csv : 島1の個体群の情報)
		String fileName = path + sep + Consts.RULESET + "_island-" + String.valueOf(island_i) + ".csv";

		ArrayList<String> strs = new ArrayList<String>();
		String str = null;

		for(int pop_i = 0; pop_i < popManager.currentRuleSets.size(); pop_i++) {
			for(int rule_i = 0; rule_i < popManager.currentRuleSets.get(pop_i).getRuleNum(); rule_i++) {
				str = "";
				str += String.valueOf(island_i) + ",";
				str += String.valueOf(popManager.getDataIdx()) + ",";
				str += String.valueOf(pop_i) + ",";
				str += String.valueOf(rule_i) + ",";
				for(int dim_i = 0; dim_i < popManager.currentRuleSets.get(pop_i).getNdim(); dim_i++) {
					str += String.valueOf(popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getRule(dim_i)) + ",";
				}
				str += String.valueOf(popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getConc()) + ",";
				str += String.valueOf(popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getCf()) + ",";
				str += String.valueOf(popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getRuleLength()) + ",";
				str += String.valueOf(popManager.currentRuleSets.get(pop_i).getMicRule(rule_i).getFitness()) + ",";
				strs.add(str);
			}
		}

		String[] array = (String[]) strs.toArray(new String[0]);
		Output.writeln(fileName, array);

	}

	//部分学習用データ出力
	public void outputPartialData(int repeat_i, int cv_i, DataSetInfo[] trainDataInfos) {
		String sep = File.separator;
		String path = nameDir + sep + Consts.DATA + sep + "execute_" + String.valueOf(repeat_i) + String.valueOf(cv_i);
		//pathの形式: nameDir/data/partialData_00/
		Output.makeDirPath(path);

		String fileName = null;

		ArrayList<String> strs = new ArrayList<String>();
		String str = null;

		for(int i = 0; i < trainDataInfos.length; i++) {
			strs.clear();
			fileName = path + sep + "partialData_" + String.valueOf(i) + ".dat";
			for(int pattern_i = 0; pattern_i < trainDataInfos[i].getDataSize(); pattern_i++) {
				str = String.valueOf(i) + ",";
				for(int dim_i = 0; dim_i < trainDataInfos[i].getNdim(); dim_i++) {
					str += String.valueOf( trainDataInfos[i].getPattern(pattern_i).getDimValue(dim_i) ) + ",";
				}
				str += String.valueOf( (int)trainDataInfos[i].getPattern(pattern_i).getDimValue(trainDataInfos[i].getNdim()) );
				strs.add(str);
			}

			String[] array = (String[]) strs.toArray(new String[0]);
			Output.writeln(fileName, array);
		}
	}


	//全試行の平均時間出力
	public void writeAveTime() {
		Double timeAve = 0.0;
		for(int i = 0; i < times.size(); i++) {
			timeAve += times.get(i);
		}
		timeAve /= times.size();

		String sep = File.separator;
		String fileName = nameDir + sep + Consts.TIMES + sep + "AveTime.txt";

		Output.writeln(fileName, String.valueOf(timeAve));
	}

	//全試行の各時間出力
	public void writeAllTime() {
		String sep = File.separator;
		String fileName = nameDir + sep + Consts.TIMES + sep + "AllTime.txt";

		ArrayList<String> strs = new ArrayList<String>();

		for(int i = 0; i < times.size(); i++) {
			strs.add(String.valueOf(times.get(i)));
		}

		String[] array = (String[]) strs.toArray(new String[0]);
		Output.writeln(fileName, array);
	}

	// **********************************

}
