package ro.pontes.justbackgammon;

import android.os.SystemClock;

public class Timer {

	private long elapsed = 0;
	private long startTime = 0;
	private boolean isPaused = false;

	// The constructor:
	public Timer() {
		this.restart();
		this.pause();
	} // end constructor.

	// The method to restart from 0:
	public void restart() {
		this.elapsed = 0;
		this.startTime = SystemClock.elapsedRealtime();
	} // end restart() method.

	// A method to pause the timer:
	public void pause() {
		isPaused = true;
		this.elapsed = SystemClock.elapsedRealtime() - this.startTime;
	} // end pause() method.

	// A method to resume the timer:
	public void resume() {
		isPaused = false;
		this.startTime = SystemClock.elapsedRealtime() - this.elapsed;
	} // end resume() method.

	// A method to force a start time:
	public void force(int millis) {
		this.startTime = SystemClock.elapsedRealtime() - millis;
		this.elapsed = millis;
	} // end force() method.

	// A method to get the elapsed time:
	public int getElapsed() {
		if (isPaused) {
			return (int) elapsed;
		} else {
			return (int) (SystemClock.elapsedRealtime() - this.startTime);
		}
	} // end getElapsed() method.

	// A method to get the elapsed time since phone start:
	public int getElapsedSinceDeviceStart() {
		return (int) SystemClock.elapsedRealtime();
	} // end getElapsedSinceDeviceStart() method.

} // end Timer class.
