package nsga2;

import java.io.Serializable;
import java.util.ArrayList;

import gbml.RuleSet;
import methods.MersenneTwisterFast;

public class Nsga2 implements Serializable{

	//Constructor ***********************************
	public Nsga2(int objectives, MersenneTwisterFast rnd) {

	}
	// **********************************************

	//Fields ****************************************
	int objectiveNum;	//目的数
	MersenneTwisterFast rnd;
	ArrayList<RuleSet> rankedList = new ArrayList<RuleSet>();	//個体群の非劣ランクリスト
	// **********************************************

	//Methods ***************************************






	// **********************************************

}
