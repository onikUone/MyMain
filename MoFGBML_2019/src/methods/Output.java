package methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import gbml.Consts;

public class Output {
	//Constructor ***********
	Output(){}
	// **********************

	//Methods ***************

	//与えられたdataNameから、使用するファイル名を保持する配列を作成
	public static void makeFileName(String dataName, String[][] traFiles, String[][] tstFiles) {
		for(int rep_i = 0; rep_i < traFiles.length; rep_i++) {
			for(int cv_i = 0; cv_i < traFiles.length; cv_i++) {
				traFiles[rep_i][cv_i] = makeFileNameOne(dataName, cv_i, rep_i, true);
				tstFiles[rep_i][cv_i] = makeFileNameOne(dataName, cv_i, rep_i, false);
			}
		}
	}

	//与えられた{dataName, cv_i, rep_i, isTra}からファイル名を一つ返すメソッド(Sparkなし)
	public static String makeFileNameOne(String dataName, int cv_i, int rep_i, boolean isTra){
		String fileName = "";
		if(isTra){
			fileName = dataName + "/a" + Integer.toString(rep_i) + "_" + Integer.toString(cv_i) + "_" +dataName + "-10tra.dat";
		}else{
			fileName = dataName + "/a" + Integer.toString(rep_i) + "_" + Integer.toString(cv_i) + "_" +dataName + "-10tst.dat";
		}
		return fileName;
	}

	//与えられた引数から結果を保持するディレクトリ名を作成
	public static String makeDirName(String dataname, int executors, int exeCores, int preDiv, int seed){
		String path = "";
		String sep = File.separator;
		path = System.getProperty("user.dir");
		path += sep + Consts.ROOTFOLDER + "_"+ dataname + "_e" + executors + "_c" + exeCores + "_p" + preDiv + "_" + seed;
		return path;
	}

	//物理的にディレクトリ作成
	public static void makeDirRule(String dir) {
		String sep = File.separator;
		String path = dir + sep + Consts.RULESET;
		File newdir = new File(path);
		newdir.mkdirs();

		path = dir + sep + Consts.VECSET;
		newdir = new File(path);
		newdir.mkdirs();

		path = dir + sep + Consts.SOLUTION;
		newdir = new File(path);
		newdir.mkdirs();

		path = dir + sep + Consts.OTHERS;
		newdir = new File(path);
		newdir.mkdirs();
	}

	//実験パラメータ出力
	public static void writeSetting(String dataName, String dir, String settings) {
		String sep = File.separator;
		String fileName = dir + sep + dataName + ".txt";
		writeln(fileName, settings);
	}

	//fileNameのファイルにst(String)を書き込む
	public static void writeln(String fileName, String st) {
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			pw.println(st);
			pw.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	// **********************
}






