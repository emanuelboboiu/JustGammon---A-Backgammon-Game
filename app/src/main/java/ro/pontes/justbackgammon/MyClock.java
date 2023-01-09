package ro.pontes.justbackgammon;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;

public class MyClock {

	private final Context mContext;
	private Calendar cal;
	private String msgTimeFormat;
	private boolean wasAnnounced = false;
	private StringTools st = null;

	// The constructor:
	public MyClock(Context context) {
		this.mContext = context;
		this.cal = Calendar.getInstance();
		this.msgTimeFormat = mContext.getString(R.string.msg_time);
		/*
		 * Instantiate the StringTools object with constructor 2, without
		 * activity parameter, only context. We don't need here the text zone,
		 * but only the toastOrTTS() method:
		 */
		this.st = new StringTools(this.mContext);
	} // end constructor.

	// A method to get current time in seconds:
	private long getTimeInMilliseconds() {
		long timeInMilliseconds = System.currentTimeMillis();
		return timeInMilliseconds;
	} // end getTimeInSeconds() method.

	// For formating the time:
	public String timeToString(long millisecondsSinceUnix) {
		String time = DateUtils.formatDateTime(mContext, millisecondsSinceUnix,
				DateUtils.FORMAT_SHOW_TIME);
		String message = String.format(msgTimeFormat, time);
		return message;
	} // end timeToString() method.

	// A method to show the time message via toast:
	public void showTime() {
		String msg = timeToString(getTimeInMilliseconds());
		st.toastOrTTS(msg, 2000);
	} // end showTime() method.

	// A method which checks if is time to announce clock:
	public void checkForAnnouncedTime() {
		if (!wasAnnounced) {
			cal.setTimeInMillis(getTimeInMilliseconds());
			switch (cal.get(Calendar.MINUTE)) {
			case 0:
			case 15:
			case 30:
			case 45:
				wasAnnounced = true;
				SoundPlayer.playSimple(mContext, "clock");
				// Delay one second the toast:
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						showTime(); // after a second of sound.
					}
				}, 1000);

				// Delay one minute until wasAnnounced is back to false:
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						wasAnnounced = false;
					}
				}, 60000);
				break;
			default:
				// Do nothing.
			} // end switch.
		} // end if it was not announced.
	} // end checkForAnnouncedTime() method.

} // end MyClock class.
