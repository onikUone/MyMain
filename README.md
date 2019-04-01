# MyMain

## 引数

 + calclationType : 0 or 1
 + Dataset Name : String
 + Generation Num : int
 + Population Size : int
 + Objective Num : int
 + Emo Type : index
 + Cross Validation Num : int
 + Start Repeat Position : int
 + Fisnish Repeat Position : int
 + seed : int
 + preDivNum : int
 + isOnceExe : boolean
 + Migration Interval : int
 + Rotation Interval : int

## calclationType == 0

 + Parallel Cores : int
 + Island Num : int

## calclationType == 1 (Apach Spark)

 + isDistributed : boolean
 + Directory Location : String
 + Server Num : int
 + Port Num : int
 + Island Num : int
 + Node Names[0] : String
 + Node Names[1] : String

## resultディレクトリについて

 + datasetName.txt

 実験パラメータが出力されている
