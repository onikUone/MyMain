package time;

//時間計測用クラス
public class TimeWatcher {
	//Constructor
	public TimeWatcher() {}

	//Fields
	double start;
	double end;
	double time = 0.0;

	//一時中断時間計測
	/*
	 * ファイル読み書きなどの前後で一時中断したいときに使用
	 * ・一時中断するとき：intoSuspend()
	 * ・一時中断から抜けるとき：exitSuspend()
	 * 		exitSuspend()によって、this.suspendに一時中断していた時間が累積される
	 *
	 * ・全実験終了後、[this.time - this.suspend]によってI/Oを引いた時間の計測が可能
	 *
	 * */
	double suspend = 0.0;
	double susStart;
	double susStop;

	//Methods
	public void start() {
		this.start = System.nanoTime();
	}

	public void end() {
		this.end = System.nanoTime();
		this.time += (this.end - this.start);
	}

	public void intoSuspend() {
		this.susStart = System.nanoTime();
	}

	public void exitSuspend() {
		this.susStop = System.nanoTime();
		this.suspend += (this.susStop - this.susStart);
	}

	public void clearSuspend() {
		this.suspend = 0.0;
	}

	public double getSec() {
		return (this.time/ 1000000000.0);
	}

	public double getNano() {
		return this.time;
	}

	public double getSecSuspend() {
		return (this.suspend/ 1000000000.0);
	}

	public double getNanoSuspend() {
		return this.suspend;
	}

}
