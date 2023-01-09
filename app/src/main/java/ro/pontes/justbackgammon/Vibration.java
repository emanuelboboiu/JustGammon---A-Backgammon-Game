package ro.pontes.justbackgammon;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Vibration {

	private Context mContext;
	Vibrator vibrator;

	public Vibration(Context context) {
		this.mContext = context;
		vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
	} // end construct.

	// A method to vibrate simple:
	@SuppressWarnings("deprecation")
	@TargetApi(26)
	public void vibrate(int millis) {
		if (MainActivity.isVibration) {
			// First, if Android 8.0, or API 26:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				vibrator.vibrate(VibrationEffect.createOneShot(millis,
						VibrationEffect.DEFAULT_AMPLITUDE));
			} else { // older versions of android:
				// deprecated in API 26
				vibrator.vibrate(millis);
			}
		} // end if vibration is allowed in game settings.
	} // end vibrate() method.

	// A method to vibrate simple after a delay, overloaded:
	public void vibrate(final int millis, int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after a delay:
				vibrate(millis);
			}
		}, delay);
	} // end vibrate() with delay method.

} // end vibration class.
