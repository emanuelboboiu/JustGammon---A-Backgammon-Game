package ro.pontes.justbackgammon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;

/*
 * Class started on Monday, 14 May 2018, created by Emanuel Boboiu.
 * This class contains useful methods like save or get settings.
 * */

public class Settings {

	// The file name for save and load preferences:
	private final static String PREFS_NAME = "jbSettings";
	private static boolean isNotFirstRunning = false;

	private Context context;

	// The constructor:
	public Settings(Context context) {
		this.context = context;
	} // end constructor.

	// A method to detect if a preference exist or not:
	public boolean preferenceExists(String key) {
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		return settings.contains(key);
	} // end detect if a preference exists or not.

	// Methods for save and read preferences with SharedPreferences:
	// Save a boolean value:
	public void saveBooleanSettings(String key, boolean value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		// Commit the edits!
		editor.commit();
	} // end save boolean.

	// Read boolean preference:
	public boolean getBooleanSettings(String key) {
		boolean value = false;
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		value = settings.getBoolean(key, false);

		return value;
	} // end get boolean preference from SharedPreference.

	// Read boolean preference wit true as default:
	public boolean getBooleanSettingsTrueDefault(String key) {
		boolean value = false;
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		value = settings.getBoolean(key, true);

		return value;
	} // end get boolean preference from SharedPreference.

	// Save a integer value:
	public void saveIntSettings(String key, int value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		// Commit the edits!
		editor.commit();
	} // end save integer.

	// Read integer preference:
	public int getIntSettings(String key) {
		int value = 0;
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		value = settings.getInt(key, 0);

		return value;
	} // end get integer preference from SharedPreference.

	// For float values in shared preferences:
	public void saveFloatSettings(String key, float value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(key, value);
		// Commit the edits!
		editor.commit();
	} // end save float.

	// Read float preference:
	public float getFloatSettings(String key) {
		float value = 0.0F;
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		value = settings.getFloat(key, 3.0F); // a default value like the value
												// for moderate magnitude.

		return value;
	} // end get float preference from SharedPreference.

	// For double values in shared preferences:
	public void saveDoubleSettings(String key, double value) {
		// We cast the double to float:
		float tempValue = (float) value;
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(key, tempValue);
		// Commit the edits!
		editor.commit();
	} // end save double.

	// Read double preference:
	public double getDoubleSettings(String key) {
		float tempValue = 0.0F;
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		tempValue = settings.getFloat(key, 3.0F);

		return (double) tempValue;
	} // end return a double from a float preference.

	// Save a String value:
	public void saveStringSettings(String key, String value) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		// Commit the edits!
		editor.commit();
	} // end save String.

	// Read String preference:
	public String getStringSettings(String key) {
		String value = "";
		// Restore preferences
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		value = settings.getString(key, null);

		return value;
	} // end get String preference from SharedPreference.
		// End read and write settings in SharedPreferences.

	// Charge Settings function:
	public void chargeSettingsInMainActivity() {
		// Determine if is first launch of the program:
		isNotFirstRunning = getBooleanSettings("isFirstRunning");

		if (!isNotFirstRunning) {
			saveBooleanSettings("isFirstRunning", true);
			// Make default values in SharedPrefferences:
			setDefaultSettings();
		}

		// Now charge settings for main activity effectively:

		// Premium version:
		MainActivity.isPremium = getBooleanSettings("isPremium");

		MainActivity.isStarted = getBooleanSettings("isStarted");

		// The random number used in statistics:
		if (!preferenceExists("randomNumberInsteadAccountName")) {
			int tempRandNumber = GUITools.random(1, 2147483647);
			saveIntSettings("randomNumberInsteadAccountName", tempRandNumber);
		}
		MainActivity.randomNumberInsteadAccountName = getIntSettings("randomNumberInsteadAccountName");

		// The local nickname:
		MainActivity.localNickname = getStringSettings("localNickname");
		if (MainActivity.localNickname == null
				|| MainActivity.localNickname.length() < 3) {
			// Charge the 1 index string from opponents names array:
			MainActivity.localNickname = context.getResources().getStringArray(
					R.array.msg_ai_opponents_array)[1];
		} // end if there is no local nickname saved.

		// Play or not the sounds:
		MainActivity.isSound = getBooleanSettingsTrueDefault("isSound");

		MainActivity.isFundalMenu = getBooleanSettings("isFundalMenu");

		// Speech mode we save on to have it in StringTools class:
		if (!preferenceExists("speechType")) {
			saveIntSettings("speechType", detectBestSpeechMode()); // charged in
																	// StringTools.
		} // end if preference speechType doesn't exist.

		MainActivity.isAmbience = getBooleanSettingsTrueDefault("isAmbience");
		if (preferenceExists("chosenAmbience")) {
			MainActivity.chosenAmbience = getStringSettings("chosenAmbience");
		} else {
			MainActivity.chosenAmbience = "ambience0";
		} // end if chosenAmbience doesn't exits.

		// The int volume, default is 0:
		if (preferenceExists("intVolume")) {
			MainActivity.intVolume = getIntSettings("intVolume");
		} else {
			MainActivity.intVolume = 100;
		} // end if intVolume preference doesn't exist.

		// The game type, default is 2, AI:
		if (preferenceExists("gameType")) {
			MainActivity.gameType = getIntSettings("gameType");
		} else {
			MainActivity.gameType = 2;
		} // end if gameType preference doesn't exist.

		// The maxDepth, default is 1, level 2:
		if (preferenceExists("maxDepth")) {
			MiniMax.maxDepth = getIntSettings("maxDepth");
		} else {
			MiniMax.maxDepth = 1; // level 2, 0 depth means level 1.
		} // end if maxDepth preference doesn't exist.

		if (preferenceExists("language")) {
			MainActivity.language = getStringSettings("language");
		} else {
			MainActivity.language = "Default";
		} // end if chosenAmbience doesn't exits.

		MainActivity.isVibration = getBooleanSettingsTrueDefault("isVibration");

		// Wake lock, keep screen awake:
		MainActivity.isWakeLock = getBooleanSettingsTrueDefault("isWakeLock");
		MainActivity.isClock = getBooleanSettingsTrueDefault("isClock");

		// Charge the chosen orientation, default is 0, automatic:
		MainActivity.orientation = getIntSettings("orientation");

		/* About number of launches, useful for information, rate and others: */
		// Get current number of launches:
		MainActivity.numberOfLaunches = getIntSettings("numberOfLaunches");
		// Increase it by one:
		MainActivity.numberOfLaunches++;
		// Save the new number of launches:
		saveIntSettings("numberOfLaunches", MainActivity.numberOfLaunches);

		if (!preferenceExists("lastAskTime")) {
			int lastAskTime = (int) GUITools.getTimeInSeconds();
			saveIntSettings("lastAskTime", lastAskTime);
		} // / end if not lastAskTime value for rating.
	} // end chargeSettingsInMainActivity() method.

	// A method to make all global values as default:
	public void setDefaultSettings() {
		// We need a random number for this account, to use it instead the
		// account name in statistics only if it doesn't exist:
		if (!preferenceExists("randomNumberInsteadAccountName")) {
			int tempRandNumber = GUITools.random(1, 2147483647);
			saveIntSettings("randomNumberInsteadAccountName", tempRandNumber);
		}

		saveBooleanSettings("isPremium", false);
		MainActivity.myAccountName = MainActivity.DEFAULT_ACCOUNT_NAME;
		saveStringSettings("localNickname", null);
		saveStringSettings("language", "Default");
		saveBooleanSettings("wasAnnounced", false);
		saveBooleanSettings("isStarted", false);
		saveIntSettings("speechType", detectBestSpeechMode());
		saveBooleanSettings("isSound", true);
		saveBooleanSettings("isAmbience", true);
		saveStringSettings("chosenAmbience", "ambience0");
		saveBooleanSettings("isFundalMenu", false);
		saveStringSettings("strIdsEnabled", ""); // no forbidden sounds.
		saveIntSettings("intVolume", 100);
		saveIntSettings("gameType", 2); // AI game.
		saveBooleanSettings("changePerspective", false);
		saveBooleanSettings("changePerspectiveTV", true);
		saveIntSettings("maxDepth", 1); // level 2..
		saveBooleanSettings("isVibration", true);
		saveBooleanSettings("isWakeLock", true);
		saveBooleanSettings("isClock", true);
		// Save DataBases version to 0, commented:
		// saveIntSettings("dbVer", 0);

		// In score class reset the score:
		for (int i = 1; i <= 2; i++) {
			saveIntSettings("scoreWhite" + i, 0);
			saveIntSettings("scoreBlack" + i, 0);
		} // end for gameType 1 and 2.
			// Set not to be accessible checkers:
		saveBooleanSettings("isAccessibleTheme", false);
		// Default settings in board class:
		// The default board notation is 1, with A and B:
		saveIntSettings("cdNotationType", 1);
		// End board class default settings.

		// Orientation in main menu:
		saveIntSettings("orientation", 0);

		// Animations:
		saveBooleanSettings("showBoardArranging", true);
		saveBooleanSettings("animWherePut", true);
		saveBooleanSettings("animWhereEnter", true);
		saveBooleanSettings("animWhereTaken", true);
		saveBooleanSettings("isAllInHome", true);
		saveBooleanSettings("isHistory", true);

		/*
		 * Save current engine to empty string, this way we will have again
		 * default TTS:
		 */
		saveStringSettings("curEngine", "");
		// Save language, country and variant:
		saveStringSettings("ttsLanguage", "");
		saveStringSettings("ttsCountry", "");
		saveStringSettings("ttsVariant", "");
		saveStringSettings("ttsVoiceName", "");
		saveFloatSettings("ttsRate", 1.0F);
		saveFloatSettings("ttsPitch", 1.0F);

		// Game settings:
		saveIntSettings("movesInterval", 1000);
		// end set default values for TTS.
	} // end setDefaultSettings function.

	// A method to detect the best speech mode for current device:
	public int detectBestSpeechMode() {
		// // Activate speech if accessibility, explore by touch is enabled:
		int best = 0; // we consider this at start.
		if (GUITools.isAccessibilityEnabled(context)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				best = 1;
			} else {
				best = 2;
			} // end if older than Android 19.
		} // end if accessibility is enabled.
		return best;
	} // end detectBestSpeechMode() method.

	// A method which returns an array of string with names of sounds:
	public String[] getSoundsNames() {
		Resources res = context.getResources();
		String[] names = res.getStringArray(R.array.sounds_array);
		return names;
	} // end getSoundsNames() method.

	/*
	 * A method which returns the forbidden sounds by its IDs as an array of
	 * integers:
	 */
	public int[] getEnabledSoundsIds() {
		String strIds = getStringSettings("strIdsEnabled");
		/*
		 * The array of integer of IDs, will contain only the values 1, enabled
		 * sounds if no sounds status enabled in preferences:
		 */
		int[] ids = new int[getSoundsNames().length];
		// Make all available at beginnings, value 1 means true:
		for (int i = 0; i < ids.length; i++) {
			ids[i] = 1; // enable them.
		} // end for.
			// If status enabled or disabled sounds saved, change the array:
		if (strIds != null && strIds.length() > 0) {
			String[] strArrIds = strIds.split("\\|");
			/*
			 * If the new string array has same or greater number of items with
			 * IDs, change values in the array of integers IDS:
			 */
			if (strArrIds.length >= ids.length) {
				for (int i = 0; i < ids.length; i++) {
					ids[i] = Integer.parseInt(strArrIds[i]);
				} // end for each ID to integer.
			} // end if strArrIds has same number of values with IDs.
		} // end if something in the saved string of enabled sounds.

		return ids;
	} // end getEnabledSoundsIds() method.

} // end Settings Class.
