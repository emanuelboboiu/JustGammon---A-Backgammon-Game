package ro.pontes.justbackgammon;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Started by Manu on Sunday, 24 September 2017, 00:16.
 *Copied into Just Backgammon on Monday, 12 March 2018, 23>42. 
 */

public class GUITools {

	// A static array for opponents:
	public static String[] aOpponents;

	// A method to go to MainActivity:
	public static void goToMainActivity(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	} // end goToMainActivity() method.

	// A method to go to GameActivity:
	public static void goToGameActivity(Context context) {
		Intent intent = new Intent(context, GameActivity.class);
		context.startActivity(intent);
	} // end goToGameActivity() method.

	// This method hides the system bars for an activity:
	@SuppressLint("InlinedApi")
	public static void hideSystemUI(View mDecorView) {
		// Only if it's API 19 or newer:

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the IMMERSIVE flag.
			// Set the content to appear under the system bars so that the
			// content
			// doesn't resize when the system bars hide and show.
			mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide navigation bar
					| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		} // end if is at least API 19.
	} // end hideSystemUI() method.

	// A method to show an alert with title and message, just an OK button:
	@SuppressLint("InflateParams")
	public static void alert(Context mContext, String title, String message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

		// The title:
		alert.setTitle(title);

		// The body creation:
		// Inflate the stub for alert dialog:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View alertView = inflater.inflate(R.layout.alert_dialog, null);

		// Find now the llAlert:
		LinearLayout ll = (LinearLayout) alertView.findViewById(R.id.llAlert);
		// A LayoutParams to add text views into the LL:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		String[] mParagraphs = message.split("\\\n");
		// A for for each paragraph in the message as TextView:
		for (int i = 0; i < mParagraphs.length; i++) {
			TextView tv = new TextView(mContext);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
			tv.setText(mParagraphs[i]);
			ll.addView(tv, lp);
		} // end for.

		alert.setView(alertView);
		alert.setPositiveButton(mContext.getString(R.string.bt_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing yet...
					}
				});
		alert.show();
	} // end alert static method.

	/*
	 * A method to show an alert with title and message and close button, with
	 * HTML:
	 */
	@SuppressLint("InflateParams")
	public static void alertWithHtml(Context mContext, String title,
			String message, String btOK) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

		// The title:
		alert.setTitle(title);

		// The body creation:
		// Inflate the stub for alert dialog:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View alertView = inflater.inflate(R.layout.alert_dialog, null);

		// Find now the llAlert:
		LinearLayout ll = (LinearLayout) alertView.findViewById(R.id.llAlert);
		// A LayoutParams to add text views into the LL:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		String[] mParagraphs = message.split("\\\n");
		// A for for each paragraph in the message as TextView:
		for (int i = 0; i < mParagraphs.length; i++) {
			TextView tv = new TextView(mContext);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
			tv.setText(MyHtml.fromHtml(mParagraphs[i]));
			ll.addView(tv, lp);
		} // end for.

		alert.setView(alertView);
		alert.setPositiveButton(btOK, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing yet...
			}
		});
		alert.show();
	} // end alertWithHtml() static method.

	// A method to show help in an alert containing a WebView:
	public static void showHelp(Context mContext) {
		showWebView(mContext, mContext.getString(R.string.help_alert_title),
				mContext.getString(R.string.bt_close_help), "help");
	} // end showHelp() method.

	// A method to show the news in a WebView:
	public static void showNews(Context mContext) {
		showWebView(mContext, mContext.getString(R.string.news_title),
				mContext.getString(R.string.bt_close_news), "news");
	} // end showNews() method.

	// A method to show the changelog in a WebView:
	public static void showChangelog(Context mContext) {
		showWebView(mContext, mContext.getString(R.string.changelog_title),
				mContext.getString(R.string.bt_close_changelog), "changelog");
	} // end showChangelog() method.

	// A method to show the privacy in a WebView:
	public static void showPrivacy(Context mContext) {
		showWebView(mContext, mContext.getString(R.string.privacy_title),
				mContext.getString(R.string.bt_close_privacy), "privacy");
	} // end showPrivacy() method.

	// A method to show the on-line statistics in a WebView:
	public static void showOnlineStatistics(Context mContext) {
		showWebView(mContext,
				mContext.getString(R.string.online_statistics_title),
				mContext.getString(R.string.bt_close_online_statistics),
				"statistics");
	} // end showOnlineStatistics() method.

	// A method to show WebView in an alert containing a WebView:
	@SuppressLint({ "InflateParams", "SetJavaScriptEnabled" })
	public static void showWebView(Context mContext, String title,
			String positiveButton, String webPageRef) {
		// Only if there is an Internet connection:
		if (isNetworkAvailable(mContext)) {
			// Inflate the stub for WebView HTML file:
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View helpView = inflater.inflate(R.layout.webview_alert_layout,
					null);

			WebView wv = (WebView) helpView.findViewById(R.id.wv);
			wv.getSettings().setJavaScriptEnabled(true);
			String curLang = mContext.getString(R.string.cur_lang);
			String url = "http://www.justgammon.com/" + webPageRef
					+ ".php?web=mobile&lang=" + curLang + "&account="
					+ MainActivity.myAccountName;
			wv.loadUrl(url);

			// Create now the alert:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
			alertDialog.setTitle(title);
			alertDialog.setView(helpView);
			alertDialog.setPositiveButton(positiveButton, null);
			AlertDialog alert = alertDialog.create();
			alert.show();
		}// end if there is network.
		else {
			GUITools.alert(mContext, mContext.getString(R.string.warning),
					mContext.getString(R.string.no_internet_for_webviews));
		} // end if no network available.
	} // end showWebView() method.

	// A method for about dialog for this package:
	@SuppressLint("InflateParams")
	public static void aboutDialog(Context context) {
		// Inflate the about message contents
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View svView = inflater.inflate(R.layout.about_dialog, null);

		/*
		 * Let's try to find the link and hide it it it is Android TV. It is not
		 * allowed a link on Android TV:
		 */
		if (MainActivity.isTV) {
			TextView tvWebAddress = svView.findViewById(R.id.tvWebAddress);
			tvWebAddress.setVisibility(View.GONE);
		} // end if it is Android TV.

		// Charge now and format some strings:
		// the version number from common XML file:
		String text = String.format(context.getString(R.string.app_version),
				context.getString(R.string.common_current_version));
		TextView tv = (TextView) svView.findViewById(R.id.tvAppVersion);
		tv.setText(text);

		// Now the date of initial release:
		text = String
				.format(context.getString(R.string.app_release_date),
						GUITools.timeStampToString(
								context,
								Integer.parseInt(context
										.getString(R.string.common_date_of_first_release_in_seconds))));
		tv = (TextView) svView.findViewById(R.id.tvAppReleaseDate);
		tv.setText(text);

		// Now the date of current release:
		text = String
				.format(context.getString(R.string.app_last_update),
						GUITools.timeStampToString(
								context,
								Integer.parseInt(context
										.getString(R.string.common_date_of_current_release_in_seconds))));
		tv = (TextView) svView.findViewById(R.id.tvLastUpdate);
		tv.setText(text);

		// Now beta testers:
		text = String.format(context.getString(R.string.app_credits2),
				context.getString(R.string.common_beta_testers));
		tv = (TextView) svView.findViewById(R.id.tvBetaTesters);
		tv.setText(text);
		// End formating some strings in about alert.

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.about_title);
		builder.setView(svView);
		builder.setPositiveButton(context.getString(R.string.bt_close), null);
		builder.create();
		builder.show();
	} // end about dialog.

	// A method to rate this application:
	@SuppressLint("InflateParams")
	public static void showRateDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setIcon(
				R.drawable.ic_launcher).setTitle(
				context.getString(R.string.title_rate_app));

		// Inflate the body of the alert:
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View questionView = inflater.inflate(R.layout.question_rate_app, null);

		builder.setView(questionView);
		builder.setPositiveButton(context.getString(R.string.bt_rate),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Settings set = new Settings(context);
						set.saveBooleanSettings("wasRated", true);
						if (context != null) {
							String link = "market://details?id=";
							try {
								// play market available
								context.getPackageManager().getPackageInfo(
										"com.android.vending", 0);
								// not available
							} catch (PackageManager.NameNotFoundException e) {
								e.printStackTrace();
								// Should use browser
								link = "https://play.google.com/store/apps/details?id=";
							}
							// Starts external action
							context.startActivity(new Intent(
									Intent.ACTION_VIEW, Uri.parse(link
											+ context.getPackageName())));
						}
					}
				}).setNegativeButton(context.getString(R.string.bt_not_now),
				null);
		builder.show();
		// Save also the lastAskTime:
		Settings set = new Settings(context);
		int lastAskTime = (int) getTimeInSeconds();
		set.saveIntSettings("lastAskTime", lastAskTime);
	} // end showRateDialog() method.

	// A method which checks if was rated:
	public static void checkIfRated(final Context context) {
		Settings set = new Settings(context);
		boolean wasRated = set.getBooleanSettings("wasRated");
		if (!wasRated) {
			// Check if it is some hours from last ask:
			int lastAskTime = set.getIntSettings("lastAskTime");
			int curTime = (int) getTimeInSeconds();
			int hours = 7;
			int minimumDifTime = hours * 60 * 60;
			if ((curTime - lastAskTime) >= minimumDifTime) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						GUITools.showRateDialog(context);
					}
				}, 1000);
			} // end if were some hours from last ask.
		} // end if it was not rated.
	} // end checkIfRated() method.

	// A method which opens a browser and a URL:
	public static void openBrowser(final Context context, String url) {
		String HTTPS = "https://";
		String HTTP = "http://";

		if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
			url = HTTP + url;
		}

		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(browserIntent);
		// context.startActivity(Intent.createChooser(intent, "Chose browser"));
		// // this would be with possibility to choose the browser.
	} // end start browser with an URL in it.

	// A method to detect if Internet connection is available:
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	} // end isNetworkAvailable() method.

	// A method which detects if accessibility is enabled:
	public static boolean isAccessibilityEnabled(Context context) {
		AccessibilityManager am = (AccessibilityManager) context
				.getSystemService(Context.ACCESSIBILITY_SERVICE);
		// boolean isAccessibilityEnabled = am.isEnabled();
		boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
		return isExploreByTouchEnabled;
	} // end isAccessibilityEnabled() method.

	// A method to play a tone, just to make tests:
	public static void beep() {
		ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
		toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
	} // end beep() method.

	// A method to give a toast, simple message on the screen:
	public static void toast(String message, int duration, Context context) {
		Toast.makeText(context, message, duration).show();
	} // end make toast.

	// A static method to get a random number between two integers:
	public static int random(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	} // end random method.

	// A method to resize image:
	public static Bitmap resizeImage(final Context context, String imageName,
			int mWidth, int mHeight) {

		int resId = context.getResources().getIdentifier(imageName, "drawable",
				context.getPackageName());

		// Get the resized image:
		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), resId);
		Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, mWidth, mHeight,
				true);
		return resizedbitmap;
	} // end resizeImage() method.

	// Convert DP to pixels:
	public static int dpToPx(Context mContext, int dp) {
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		return Math.round(dp
				* (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	} // end dpToPx() method.

	// Convert pixel to DP:
	public static int pxToDp(Context mContext, int px) {
		DisplayMetrics displayMetrics = mContext.getResources()
				.getDisplayMetrics();
		return Math.round(px
				/ (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	} // end pxToDp() method.

	// A method to determine if it is AndroidTV:
	public static boolean isAndroidTV(final Context context) {
		UiModeManager uiModeManager = (UiModeManager) context
				.getSystemService(Context.UI_MODE_SERVICE);
		if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
			return true;
		} else {
			return false;
		}
	} // end determine if isAndroidTV() method.

	/*
	 * A method which makes an unique string. This is for premium outside Google
	 * mechanisms, the serial code:
	 */
	public static String getSerialCode(final String str) {
		String toReturn = "";
		try {
			toReturn = StringTools.SHA1(str);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} // end getSerialCode(() method.

		return toReturn.substring(1, 9).toLowerCase(Locale.US);
	} // end get the SHA1 string for registration.

	// A method to round a double value:
	public static double round(double number, int decimals) {
		double temp = Math.pow(10, decimals);
		double rounded = Math.round(number * temp) / temp;
		return rounded;
	} // end round() method.

	// A method to get current time in seconds:
	public static long getTimeInSeconds() {
		Calendar cal = Calendar.getInstance();
		long timeInMilliseconds = cal.getTimeInMillis();
		return timeInMilliseconds / 1000;
	} // end getTimeInSeconds() method.

	// For formating a date:
	public static String timeStampToString(Context context, int paramCurTime) {
		long curTime = (long) paramCurTime * 1000;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(curTime);

		// Now format the string:
		// See if it is today or yesterday:
		int today = getIsToday(curTime);
		String dayOfWeek = "";
		if (today == 1) {
			dayOfWeek = context.getString(R.string.today);
		} else if (today == 2) {
			dayOfWeek = context.getString(R.string.yesterday);
		} else {
			dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
					Locale.getDefault());
		}

		// Make the hour and minute with 0 in front if they are less
		// than 10:
		String curHour = "";
		int iHour = cal.get(Calendar.HOUR_OF_DAY);
		if (iHour < 10) {
			curHour = "0" + iHour;
		} else {
			curHour = "" + iHour;
		}
		String curMinute = "";
		int iMinute = cal.get(Calendar.MINUTE);
		if (iMinute < 10) {
			curMinute = "0" + iMinute;
		} else {
			curMinute = "" + iMinute;
		}

		String message = String.format(
				context.getString(R.string.date_format),
				dayOfWeek,
				"" + cal.get(Calendar.DAY_OF_MONTH),
				""
						+ cal.getDisplayName(Calendar.MONTH, Calendar.LONG,
								Locale.getDefault()),
				"" + cal.get(Calendar.YEAR), curHour, curMinute);

		return message;
	} // end timeStampToString() method.

	/*
	 * This method returns 1 if a date in milliseconds at parameter is today, 2
	 * if it was yesterday or 0 on another date.
	 */
	public static int getIsToday(long smsTimeInMilis) {
		Calendar smsTime = Calendar.getInstance();
		smsTime.setTimeInMillis(smsTimeInMilis);

		Calendar now = Calendar.getInstance();
		if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
			return 1; // today.
		} else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
			return 2; // yesterday.
		} else if (smsTime.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
			return 3; // tomorrow.
		} else {
			return 0; // another date.
		}
	} // end determine if a date is today or yesterday.

	/*
	 * This method sets the volume for background depending of accessibility
	 * enabled or not:
	 */
	public static void setVolumeForBackground(Context mContext) {
		if (GUITools.isAccessibilityEnabled(mContext)) {
			// Do nothing.
		} else {
			// Do nothing yet.
		} // end if is not accessibility.
	} // end setVolumeForBackground() method.

	// The method to charge the opponents in the public static array:
	public static void chargeOpponents(Context mContext, int gameType) {
		Resources res = mContext.getResources();
		if (gameType == 1) {
			// Charge the array for colours as strings:
			aOpponents = res
					.getStringArray(R.array.msg_offline_opponents_array);
		} // end if it's off-line game.
		else if (gameType == 2) { // AI game:
			// Charge the array for AI opponents as strings:
			aOpponents = res.getStringArray(R.array.msg_ai_opponents_array);
			// We put at 1 index the localName, the nickname set:
			aOpponents[1] = MainActivity.localNickname;
		} // end if it's AI game.
		else if (gameType == 0) { // two bots game:
			// Charge the array for AI bots opponents as strings:
			aOpponents = res.getStringArray(R.array.msg_two_ai_opponents_array);
		} // end if it's to bots game.
	} // end chargeOpponents() method.

	public static void swapOpponents() {
		// Swap the opponents in three lines of code:
		String tempOpponent = aOpponents[1];
		aOpponents[1] = aOpponents[2];
		aOpponents[2] = tempOpponent;
	} // end swapOpponents() method.

	// A method which checks the correct nickname written:
	public static boolean isAcceptedNickname(String newNickname) {
		boolean isOK = true;
		if (newNickname == null || newNickname.isEmpty()) {
			isOK = false;
		} else { // no empty string:
			// Other checks if is not empty:
			// Check for a correct length:
			if (newNickname.length() < 3 || newNickname.length() > 20) {
				isOK = false;
			} // end if nickname has a normal length.
				// A for to check if contains other than
				// alphanumeric:
			for (int i = 0; i < newNickname.length(); i++) {
				if (!Character.isLetterOrDigit(newNickname.charAt(i))) {
					if (newNickname.charAt(i) != ' ') {
						isOK = false;
						break;
					} // end if it is not space.
				} // end if it is not letter or digit.
			} // end for it is not alphanumeric.
				// Check if first letter is upper case:
			if (!Character.isUpperCase(newNickname.charAt(0))) {
				isOK = false;
			} // end if first character is upper case.
		} // end if it is not empty string or null.
		return isOK;
	} // end isAcceptedNickname() method.

	// This method sets the locale independent of the OS language:
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressWarnings("deprecation")
	public static void setLocale(Activity mActivity, String language) {
		// If it is not the device language, normal change:
		if (language.equalsIgnoreCase("Default")) {
			// Do nothing...
		} else { // there is a chosen language:
			String lang = "en"; // first English.
			// We change it if is another one:
			if (language.equalsIgnoreCase("Romanian")) {
				lang = "ro";
			} else if (language.equalsIgnoreCase("Russian")) {
				lang = "ru";
			}
			// Add here more else if for other languages.

			// Set now the current locale:
			Locale locale = new Locale(lang);
			Locale.setDefault(locale);
			Configuration config = mActivity.getBaseContext().getResources()
					.getConfiguration();
			// First, if Android JellyBean, or API 17:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				config.setLocale(locale);
			} else {
				config.locale = locale;
			} // end if older API than 17.
			mActivity
					.getBaseContext()
					.getResources()
					.updateConfiguration(
							config,
							mActivity.getBaseContext().getResources()
									.getDisplayMetrics());
		} // end if there is a chosen language.
	} // end setLocale() method.

	// A method to reverse an array of Integers:
	public static int[] reverseArrayOfInts(int[] x) {
		int[] d = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			d[i] = x[x.length - 1 - i];
		}
		return d;
	} // end reverseArrayOfInts() method.

} // end GUITools class.
