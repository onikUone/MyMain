package methods;

import java.io.File;
import java.util.ArrayList;

import gbml.Consts;
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
	public void writeGeneTime(String fileName, ArrayList<Double> times) {
		String sep = File.separator;
		String path = nameDir + sep + fileName;
		Output.writeln(path, times.toArray(new Double[times.size()]));
	}

	public void writeTime(double sec, double ns, int cv_i, int rep_i) {
		String sep = File.separator;
		String fileName = nameDir + sep + Consts.OTHERS + sep + "Atime_0" + String.valueOf(rep_i) + String.valueOf(cv_i) + ".csv";

		String str = sec + ", " + ns;

		Output.writeln(fileName, str);
	}

	//人間が読みやすい形式での出力
	public void outputRulesForReadable(PopulationManager popManager, int gen_i, int cv_i, int repeat_i, int island_i) {
		String sep = File.separator;
		String path = nameDir + sep + Consts.LOGS_READABLE + sep + "result_" + String.valueOf(repeat_i) + String.valueOf(cv_i) + sep + "gene-" + String.valueOf(gen_i + 1) + "_" + String.valueOf(repeat_i) + String.valueOf(cv_i);
		//pathの形式: nameDir/logs_readable/gene-0_00/
		Output.makeDirPath(path);

		//ファイル名形式(例：ruleset_island-1.csv : 島1の個体群の情報)
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



	// **********************************

}
