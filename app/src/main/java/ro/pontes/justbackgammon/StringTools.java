package ro.pontes.justbackgammon;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class StringTools {

	private Context mContext;
	private Activity mActivity;
	private static History history;
	private SpeakText speak;
	private TextView tvTextZone;
	Settings set = null;
	private int savedSpeechType = 0; // from preferences.
	private int speechType = 0;
	private int lastSpeechTypeChosen = -1; // no last change.
	private boolean usedWithConstructor2 = false;

	// A constructor for context and initialisation:
	public StringTools(Context context, Activity activity) {
		mContext = context;
		mActivity = activity;
		history = new History(mContext);
		tvTextZone = (TextView) mActivity.findViewById(R.id.tvText);
		tvTextZone.setMovementMethod(new ScrollingMovementMethod());

		set = new Settings(mContext);
		savedSpeechType = set.getIntSettings("speechType");
		initialiseSpeechType();
	} // end constructor.

	// Another constructor to be used without text view:
	// A constructor for context and initialisation:
	public StringTools(Context context) {
		usedWithConstructor2 = true;
		mContext = context;
		set = new Settings(mContext);
		savedSpeechType = set.getIntSettings("speechType");
		initialiseSpeechType();
	} // end second constructor.

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void initialiseSpeechType() {
		/*
		 * If savedSpeechType is 3, automatically detection, choose the best
		 * choice for speechType:
		 */
		if (savedSpeechType == 3) {
			speechType = set.detectBestSpeechMode();
		} // end if speechType is 3, automatically detection.
		else {
			speechType = savedSpeechType;
		} // end if it is not automatically.

		// No initialise things depending of the speechType:
		switch (speechType) {
		case 0: // no speech:
			// Do nothing.
			break;

		case 1: // live region mode:
			/*
			 * We set the live region only if the object is constructed with
			 * first constructor, we have only in that case the text view:
			 */
			if (!usedWithConstructor2) {
				tvTextZone
						.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
			} // end if no constructor 2 object.
			break;

		case 2: // via TTS directly:
			speak = new SpeakText(mContext);
			break;
		} // end switch.
			// Set the last speech type:
		lastSpeechTypeChosen = speechType;
	} // end initialiseSpeechType() method.

	// A method to write text on the text zone:
	public void addText(String text, boolean addIntoHistory) {
		Spanned spannedText = MyHtml.fromHtml(text);
		tvTextZone.setText(spannedText);

		// Check if speech type was changed to reinitialise if automatically:
		if (savedSpeechType == 3) { // automatically detection:
			speechType = set.detectBestSpeechMode();
			if (speechType != lastSpeechTypeChosen) {
				// We must reinitialise the speech:
				initialiseSpeechType();
			} // end if detect was changed.
		} // end if it is automatically detection.

		/*
		 * We need to speak via TTS if speechType is 2, otherwise no
		 * requirements:
		 */
		if (speechType == 2) {
			speak.say(spannedText.toString(), false);
		} // end if it is via TTS.

		if (addIntoHistory) {
			history.add(spannedText);
		} // end if it must be added into the history object.
	}// end addText() method.

	// A method to add text delayed on the text zone:
	public void addText(final String text, final boolean addIntoHistory,
			int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after some milliseconds:
				addText(text, addIntoHistory);
			}
		}, delay);
	} // end addText() delayed method.

	// A method to speak via toast or TTS depending of the accessibility
	// statuses:
	public void toastOrTTS(String message, int duration) {
		/*
		 * In this game, if TTS verbalization is chosen and the accessibility
		 * too, we must ignore toast if also this is set in settings:
		 */
		if (MainActivity.isAccessibility && speechType == 2) {
			speak.say(message, true);
		} else {
			GUITools.toast(message, duration, mContext);
		} // end if no accessibility and TTS verbalization.
	} // end toastOrTTS() method.

	// A method which determine the name of a position for content descriptions:
	public String determinePositionName(int position, int cdNotationType) {
		String curPosName = "";
		/*
		 * If notation is 0, here we need to have at least one notation type,
		 * and we put as default with A and B:
		 */
		if (cdNotationType == 0) {
			cdNotationType = 1;
		} // end if cdNotation is 0.

		if (cdNotationType == 1) { // with A and B:
			if (position < 12) { // part A:
				curPosName = "A" + (position + 1);
			} else { // part B:
				curPosName = "B" + (24 - position);
			} // end part b.
		} else if (cdNotationType == 2) { // from 1 one to 24:
			curPosName = "" + (position + 1);
		} // end if notation from 1 to 24.
		return curPosName;
	} // end determinePositionName() method.

	// Convert to hex:
	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
						: (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	} // end convertToHex() method.

	public static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	} // end SHA1() method.

	// A method to show the history:
	public void showHistory() {
		history.show(0);
	} // end showHistory() method.

	// A method to delete last game from history:
	public void clearHistory() {
		history.clear();
	} // end clearHistory() method.

	// A method to insert the game into database:
	public void insertHistoryIntoDB(ArrayList<String> arrStatistics) {
		history.insertIntoDB(arrStatistics);
	} // end insertHistoryIntoDB() method.

	public void destroy() {
		if (speak != null) {
			speak.stop();
			speak.shutdown();
		} // end if speak object is not null.
	} // end onDestroy() method.

} // end StringTools class.
