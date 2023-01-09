package ro.pontes.justbackgammon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsManagement {

	private final Context mContext;
	private final Activity mActivity;
	private View settingsView = null;
	private AlertDialog alertDialog;
	private Settings set;
	private String lastChosenLanguage; // we need it to compare.
	private int lastChosenOrientation; // we need it for comparison
	private int[] arrEnabledSounds;
	private MySoundPool mSP = null;
	private int curAPI = 0;

	// The constructor:
	public SettingsManagement(Context context, Activity activity,
			MySoundPool mSP) {
		this.mActivity = activity;
		this.mContext = context;
		set = new Settings(mContext);
		this.mSP = mSP;
		curAPI = Build.VERSION.SDK_INT;
	} // end constructor.

	@SuppressLint("InflateParams")
	public void showSettingsAlert() {
		// Inflate the settings message contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		settingsView = inflater.inflate(R.layout.activity_settings, null);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.title_activity_settings);
		builder.setView(settingsView);
		builder.setPositiveButton(mContext.getString(R.string.close_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
					}
				});

		builder.create();
		alertDialog = builder.show();
	}// end showSettingsAlert() method.

	// The method to create alert for general settings:
	@SuppressLint("InflateParams")
	public void showGeneralSettings() {
		// Inflate the general settings contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View settingsView2 = inflater.inflate(R.layout.settings_general, null);

		final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_general_settings);
		builder2.setView(settingsView2);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_close_general_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
					}
				});

		builder2.create();
		builder2.show();

		// Not check and uncheck the check boxes in this alert:
		// For vibration in program:
		CheckBox cbtVibrationSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtVibrationSetting);
		if (MainActivity.isTV) {
			cbtVibrationSetting.setChecked(false);
			cbtVibrationSetting.setEnabled(false);
		} else {
			cbtVibrationSetting.setChecked(MainActivity.isVibration);
		} // end if is not Android TV.

		// For keeping screen awake:
		CheckBox cbtScreenAwakeSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtScreenAwakeSetting);
		if (MainActivity.isTV) {
			// For Android TV we need it to be unavailable and unchecked:
			cbtScreenAwakeSetting.setChecked(false);
			cbtScreenAwakeSetting.setEnabled(false);
		} else {
			cbtScreenAwakeSetting.setChecked(MainActivity.isWakeLock);
		}

		// For clock cuckoo:
		CheckBox cbtClockSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtClockSetting);
		cbtClockSetting.setChecked(MainActivity.isClock);

		// For clock cuckoo:
		CheckBox cbtHistorySetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtHistorySetting);
		cbtHistorySetting.setChecked(set
				.getBooleanSettingsTrueDefault("isHistory"));
	} // end showGeneralSettings() method.

	// The method to create alert for visual settings:
	@SuppressLint("InflateParams")
	public void showVisualSettings() {
		// Inflate the visual settings contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View settingsView2 = inflater.inflate(R.layout.settings_visual, null);

		final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_visual_settings);
		builder2.setView(settingsView2);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_close_visual_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
						/*
						 * If the lastChosenOrientation is another one than the
						 * saved one, it means it was a change and we need to
						 * restart the activity, to charge the new orientation:
						 */
						int savedOrientation = set
								.getIntSettings("orientation");
						if (savedOrientation != lastChosenOrientation) {
							if (alertDialog != null && alertDialog.isShowing()) {
								// We dismiss the main settings alert:
								alertDialog.dismiss();
							} // end if it is shown.
							mActivity.recreate();
						} // end if it was a change.
					}
				});
		builder2.create();
		builder2.show();

		// Now check the radio button chosen for orientation:
		lastChosenOrientation = MainActivity.orientation;
		String rb = "rbOrientation" + lastChosenOrientation;
		int resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		RadioButton radioButton = (RadioButton) settingsView2
				.findViewById(resID);
		radioButton.setChecked(true);

		// Now check or uncheck the check boxes:

		// For board dynamic arranging:
		CheckBox cbtShowBoardArrangingSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtShowBoardArrangingSetting);
		cbtShowBoardArrangingSetting.setChecked(set
				.getBooleanSettingsTrueDefault("showBoardArranging"));
		// For hint where to put animation:
		CheckBox cbtAnimWhereToPutSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtAnimHintWherePutSetting);
		cbtAnimWhereToPutSetting.setChecked(set
				.getBooleanSettingsTrueDefault("animWherePut"));
		// For hint where to enter animation:
		CheckBox cbtAnimWhereToEnterSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtAnimHintWhereEnterSetting);
		cbtAnimWhereToEnterSetting.setChecked(set
				.getBooleanSettingsTrueDefault("animWhereEnter"));
		// For indicate where from taken animation:
		CheckBox cbtAnimWhereTakenSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtAnimHintWhereTakenSetting);
		cbtAnimWhereTakenSetting.setChecked(set
				.getBooleanSettingsTrueDefault("animWhereTaken"));
		// For announcement when all checkers are in home board:
		CheckBox cbtAnnouncementForAllCheckersInHomeboard = (CheckBox) settingsView2
				.findViewById(R.id.cbtHintIsAllInHomeBoardSetting);
		cbtAnnouncementForAllCheckersInHomeboard.setChecked(set
				.getBooleanSettingsTrueDefault("isAllInHome"));
	} // end showVisualSettings() method.

	// The method to create alert for sounds settings:
	@SuppressLint("InflateParams")
	public void showSoundsSettings() {
		// Inflate the sounds settings layout:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View settingsView2 = inflater.inflate(R.layout.settings_sounds, null);

		/*
		 * Now create programmatically the check boxes for all sounds to be
		 * checked or not:
		 */
		// Find first the linear layout for check boxes:
		LinearLayout llCB = (LinearLayout) settingsView2
				.findViewById(R.id.llSoundsCheckboxes);

		// A LayoutParams to add check boxes into the llCB:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// Get in an array all sounds names, the 0 index is considered as blank:
		String[] arrSoundsNames = set.getSoundsNames();
		/* Get also all statuses enabled or not sounds as array of integers: */
		arrEnabledSounds = set.getEnabledSoundsIds();

		// A for loop for all available sounds:
		for (int i = 1; i < arrSoundsNames.length; i++) {
			CheckBox cb = new CheckBox(mContext, null,
					R.style.style_check_boxes_in_settings);
			// Determine if is 1 or 0, true or false:
			boolean checked = (arrEnabledSounds[i] == 1 ? true : false);
			cb.setChecked(checked);
			cb.setText(arrSoundsNames[i]);
			// Add also onClick method to save checked or not:
			final int finalI = i;
			cb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Call a method with view at parameter to do things:
					enableOrDisableSounds(v, finalI);
				} // end onClick.
			});
			llCB.addView(cb, lp);
		} // end for create check boxes for all available sounds.

		final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_sounds_settings);
		builder2.setView(settingsView2);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_close_sounds_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/*
						 * Charge again the status of sounds to know if enabled
						 * or disabled, especially for elements clicked or
						 * elements finished here in settings management:
						 */
						mSP.chargeStatusOfSoundFromSettings();
						mSP.playSound(18); // the finished alert sound.
					}
				});

		builder2.create();
		builder2.show();

		// Not check and uncheck the check boxes in this alert:
		// For sounds in program:
		CheckBox cbtSoundsSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtSoundsSetting);
		cbtSoundsSetting.setChecked(MainActivity.isSound);

		// For music background:
		CheckBox cbtSoundsBackgroundSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtMusicSetting);
		cbtSoundsBackgroundSetting.setChecked(MainActivity.isAmbience);

		// For music in main menu:
		CheckBox cbtMenuBackgroundSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtMenuMusicSetting);
		cbtMenuBackgroundSetting.setChecked(MainActivity.isFundalMenu);

		// Check the radio button depending of the intVolume static variable:
		int tempVol = MainActivity.intVolume;
		String rb = "rbVolume" + tempVol;
		int resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		RadioButton radioButton = (RadioButton) settingsView2
				.findViewById(resID);
		radioButton.setChecked(true);
		// End set chosen radio button for chosen volume.
	} // end showSoundsSettings() method.

	// A method to react for check or uncheck check boxes for sounds chosen:
	private void enableOrDisableSounds(View v, int id) {
		// If the view is checked play the sound and add it for be saved:
		CheckBox cb = (CheckBox) v;
		if (cb.isChecked()) {
			mSP.playSoundAnyway(id);
			arrEnabledSounds[id] = 1;
		} else { // unchecked:
			arrEnabledSounds[id] = 0;
		} // end if it is unchecked.
			// Save also the new array into the preferences:
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arrEnabledSounds.length; i++) {
			sb.append(arrEnabledSounds[i]);
			sb.append("|");
		} // end for.
			// Save the new string of values:
		set.saveStringSettings("strIdsEnabled", sb.toString());
	} // end enableOrDisableSounds() method.String

	// The method to create alert for accessibility settings:
	@SuppressLint("InflateParams")
	public void showAccessibilitySettings() {
		// Inflate the about message contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View settingsView2 = inflater.inflate(R.layout.settings_accessibility,
				null);

		final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_accessibility_settings);
		builder2.setView(settingsView2);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_close_accessibility_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
					}
				});

		builder2.create();
		builder2.show();

		// Check the check box for accessible checkers:
		boolean isAccessibleTheme = set.getBooleanSettings("isAccessibleTheme");
		CheckBox cbtAccessibleThemeSetting = (CheckBox) settingsView2
				.findViewById(R.id.cbtAccessibleThemeSetting);
		cbtAccessibleThemeSetting.setChecked(isAccessibleTheme);

		// Check the radio button for speech type:
		int tempSpeechType = set.getIntSettings("speechType");
		String rb = "rbSpeechType" + tempSpeechType;
		int resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		RadioButton radioButton = (RadioButton) settingsView2
				.findViewById(resID);
		radioButton.setChecked(true);

		// If it is API older than 19, disable the live region radio button:
		if (curAPI < 19) {
			RadioButton radioButton1 = (RadioButton) settingsView2
					.findViewById(R.id.rbSpeechType1);
			radioButton1.setEnabled(false);
		} // end if it is an older version of Android than 19.
			// End set chosen radio button for speech type.

		// Check the radio button depending of the board notation type:
		int tempCdNotationType = set.getIntSettings("cdNotationType");
		rb = "rbNotation" + tempCdNotationType;
		resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		radioButton = (RadioButton) settingsView2.findViewById(resID);
		radioButton.setChecked(true);
		// End set chosen radio button for board notation.
	} // end showAccessibilitySettings() method.

	// The method to create alert for language settings:
	@SuppressLint("InflateParams")
	public void showLanguageSettings() {
		// Inflate the about message contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View settingsView2 = inflater.inflate(R.layout.settings_language, null);

		final AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_language_settings);
		builder2.setView(settingsView2);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_close_language_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
						/*
						 * If the lastChosenLanguage is another one than the
						 * saved one, it means it was a change and we need to
						 * restart the activity, to charge the new language:
						 */
						String savedLanguage = set
								.getStringSettings("language");
						if (!savedLanguage.equalsIgnoreCase(lastChosenLanguage)) {
							if (alertDialog != null && alertDialog.isShowing()) {
								// We dismiss the main settings alert:
								alertDialog.dismiss();
							} // end if it is shown.
							mActivity.recreate();
						} // end if it was a change.
					}
				});

		builder2.create();
		builder2.show();

		// Check the radio button depending of the language chosen:
		lastChosenLanguage = MainActivity.language;
		String rb = "rbLang" + lastChosenLanguage;
		int resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		RadioButton radioButton = (RadioButton) settingsView2
				.findViewById(resID);
		radioButton.setChecked(true);
		// End set chosen radio button for language.
	} // end showLanguageSettings() method.

	// What happens when a check box is clicked:
	// Let's see what happens when a check box is clicked in settings alert:
	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		// Check which check box was clicked
		switch (view.getId()) {
		case R.id.cbtShowBoardArrangingSetting:
			if (checked) {
				set.saveBooleanSettings("showBoardArranging", true);
			} else {
				set.saveBooleanSettings("showBoardArranging", false);
			}

			break;

		case R.id.cbtAnimHintWherePutSetting:
			if (checked) {
				set.saveBooleanSettings("animWherePut", true);
			} else {
				set.saveBooleanSettings("animWherePut", false);
			}
			break;

		case R.id.cbtAnimHintWhereEnterSetting:
			if (checked) {
				set.saveBooleanSettings("animWhereEnter", true);
			} else {
				set.saveBooleanSettings("animWhereEnter", false);
			}
			break;

		case R.id.cbtAnimHintWhereTakenSetting:
			if (checked) {
				set.saveBooleanSettings("animWhereTaken", true);
			} else {
				set.saveBooleanSettings("animWhereTaken", false);
			}
			break;

		case R.id.cbtHintIsAllInHomeBoardSetting:
			if (checked) {
				set.saveBooleanSettings("isAllInHome", true);
			} else {
				set.saveBooleanSettings("isAllInHome", false);
			}
			break;

		case R.id.cbtAccessibleThemeSetting:
			if (checked) {
				set.saveBooleanSettings("isAccessibleTheme", true);
			} else {
				set.saveBooleanSettings("isAccessibleTheme", false);
			}
			break;

		// Sounds settings check boxes:
		case R.id.cbtSoundsSetting:
			if (checked) {
				MainActivity.isSound = true;
			} else {
				MainActivity.isSound = false;
			}
			set.saveBooleanSettings("isSound", MainActivity.isSound);
			break;

		case R.id.cbtMusicSetting:
			if (checked) {
				MainActivity.isAmbience = true;
			} else {
				MainActivity.isAmbience = false;
			}
			set.saveBooleanSettings("isAmbience", MainActivity.isAmbience);
			break;

		case R.id.cbtVibrationSetting:
			if (checked) {
				MainActivity.isVibration = true;
			} else {
				MainActivity.isVibration = false;
			}
			set.saveBooleanSettings("isVibration", MainActivity.isVibration);
			break;

		case R.id.cbtScreenAwakeSetting:
			if (checked) {
				MainActivity.isWakeLock = true;
			} else {
				MainActivity.isWakeLock = false;
			}
			set.saveBooleanSettings("isWakeLock", MainActivity.isWakeLock);
			break;

		case R.id.cbtClockSetting:
			if (checked) {
				MainActivity.isClock = true;
			} else {
				MainActivity.isClock = false;
			}
			set.saveBooleanSettings("isClock", MainActivity.isClock);
			break;

		case R.id.cbtHistorySetting:
			if (checked) {
				set.saveBooleanSettings("isHistory", true);
			} else {
				set.saveBooleanSettings("isHistory", false);
			}
			break;
		} // end switch.
	} // end onClick method.

	// A method to take the radio button event:
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked:
		int tempId = view.getId();
		switch (tempId) {
		// For orientation, 3 radio buttons:
		case R.id.rbOrientation0:
			if (checked) {
				MainActivity.orientation = 0;
				set.saveIntSettings("orientation", 0);
			}
			break;

		case R.id.rbOrientation1:
			if (checked) {
				MainActivity.orientation = 1;
				set.saveIntSettings("orientation", 1);
			}
			break;

		case R.id.rbOrientation2:
			if (checked) {
				MainActivity.orientation = 2;
				set.saveIntSettings("orientation", 2);
			}
			break;

		// For speech type, 3 radio buttons:
		case R.id.rbSpeechType0:
			if (checked) {
				set.saveIntSettings("speechType", 0);
			}
			break;

		case R.id.rbSpeechType1:
			if (checked) {
				set.saveIntSettings("speechType", 1);
			}
			break;

		case R.id.rbSpeechType2:
			if (checked) {
				set.saveIntSettings("speechType", 2);
			}
			break;

		case R.id.rbSpeechType3:
			if (checked) {
				set.saveIntSettings("speechType", 3);
			}
			break;

		// For notation type, 3 radio buttons:
		case R.id.rbNotation1:
			if (checked) {
				set.saveIntSettings("cdNotationType", 1);
			}
			break;
		case R.id.rbNotation2:
			if (checked) {
				set.saveIntSettings("cdNotationType", 2);
			}
			break;
		case R.id.rbNotation0:
			if (checked) {
				set.saveIntSettings("cdNotationType", 0);
			}
			break;

		// For language radio buttons:
		case R.id.rbLangDefault:
			if (checked) {
				set.saveStringSettings("language", "Default");
			}
			break;
		case R.id.rbLangEnglish:
			if (checked) {
				set.saveStringSettings("language", "English");
			}
			break;
		case R.id.rbLangRomanian:
			if (checked) {
				set.saveStringSettings("language", "Romanian");
			}
			break;
		case R.id.rbLangRussian:
			if (checked) {
				set.saveStringSettings("language", "Russian");
			}
			break;

		// For sound volume:
		case R.id.rbVolume15:
		case R.id.rbVolume30:
		case R.id.rbVolume50:
		case R.id.rbVolume70:
		case R.id.rbVolume85:
		case R.id.rbVolume100:
			if (checked) {
				String rbName = mContext.getResources().getResourceEntryName(
						tempId);
				// Cut the rbVolume from start, the first 8 letters:
				rbName = rbName.substring(8);
				int tempVol = Integer.parseInt(rbName);
				MainActivity.intVolume = tempVol;
				set.saveIntSettings("intVolume", MainActivity.intVolume);
			}
			break;
		} // } // end switch.
	} // end onRadioButtonClicked.

	// The method to reset to defaults:
	@SuppressLint("InflateParams")
	public void resetToDefaults(View view) {
		// Dismiss the settings alert dialog:
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
		} // end if it is shown.

		// Inflate the body of the alert:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View questionView = inflater
				.inflate(R.layout.question_reset_game, null);

		// Make an alert with the question:
		// Get the strings to make an alert:
		String tempTitle = mContext.getString(R.string.title_default_settings);
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(tempTitle);
		alert.setView(questionView);

		alert.setIcon(R.drawable.ic_launcher)
				.setPositiveButton(R.string.bt_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Settings set = new Settings(mContext);
								set.setDefaultSettings();
								set.chargeSettingsInMainActivity();
								// The click sound static method:
								SoundPlayer.playSimple(mContext,
										"element_finished");
								// Try to recreate the MainActivity:
								mActivity.recreate();
							}
						}).setNegativeButton(R.string.bt_no, null).show();
	} // end resetToDefaults() method.

	// Starting here about TTS settings:

	// First the class variables:
	// Some values for TTS settings:
	private Locale lastTempLocale = null;
	private int lastTempVoice = 0;
	private View ttsView = null;
	private TextToSpeech tts = null;
	private String chosenEngine = null;
	private ArrayList<Voice> voiceList = null;
	private float ttsRate = 1.0F;
	private float ttsPitch = 1.0F;
	private float granularity = 10F;
	private float granularityMultiplier = 10F;
	private float granularity2 = granularity * granularityMultiplier;
	AlertDialog dialog = null;

	// Now some methods for TTS settings:
	@SuppressLint("InflateParams")
	public void showTTSSettings() {
		// Inflate the about message contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ttsView = inflater.inflate(R.layout.settings_tts, null);

		/*
		 * Now we detect the engines and we fill the llEngines LinearLayout in a
		 * separate method declared in this class:
		 */
		detectEngines();

		AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.sm_heading_tts_settings);
		builder2.setView(ttsView);
		builder2.setPositiveButton(
				mContext.getString(R.string.sm_bt_save_tts_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
						// Save in SharedPreferences the values:
						saveVoice();
						tts.shutdown();
					}
				});
		builder2.setNegativeButton(
				mContext.getString(R.string.sm_bt_close_tts_settings),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
						tts.shutdown();
					}
				});
		// the dialog object is global:
		dialog = builder2.create();
		dialog.show();
		// Disable the save button, the positive one:
		((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
				.setEnabled(false);
	} // end showAccessibilitySettings() method.

	// A method which detects all engines:
	private void detectEngines() {
		// TextToSpeech initialisation:
		tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != TextToSpeech.ERROR) {
					// Remain the current TTS:
					// tts.setLanguage(locale.US);
				}
			}
		});

		List<TextToSpeech.EngineInfo> mEngines = tts.getEngines();
		// We shut down the TTS and we will recreate it using an engine:
		tts.shutdown();

		// First of all we take the llEngines LinearLayout:
		LinearLayout llEngines = (LinearLayout) ttsView
				.findViewById(R.id.llEngines);
		llEngines.removeAllViews();

		/*
		 * A LayoutParams for weight for text view and buttons in the llEngines,
		 * to be match parent in width and wrap content in height:
		 */
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		/*
		 * Search and change the text view for information about how many
		 * engines are available:
		 */
		TextView tv = (TextView) ttsView.findViewById(R.id.tvNumberOfEngines);
		tv.setMaxLines(1);

		int count = mEngines.size();
		Resources res = mContext.getResources();
		String foundEngines = res.getQuantityString(
				R.plurals.tv_number_of_engines, count, count);
		tv.setText(foundEngines);

		// Make buttons for each engine found:
		for (int i = 0; i < mEngines.size(); i++) {
			Button bt = new Button(mContext, null,
					R.style.style_buttons_in_settings);
			final String engineLabel = mEngines.get(i).label;
			bt.setText(engineLabel);
			final String engineName = mEngines.get(i).name;
			bt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// Let's initialise the chosen TTS engine:
					// TextToSpeech initialisation:
					tts = new TextToSpeech(mContext,
							new TextToSpeech.OnInitListener() {
								@Override
								public void onInit(int status) {
									if (status != TextToSpeech.ERROR) {
										// We do something good:
										detectVoices(engineName, engineLabel);
									} else {
										// Show an error:
										GUITools.alert(
												mContext,
												mContext.getString(R.string.error),
												mContext.getString(R.string.error_after_choosing_engine));
									}
								}
							}, engineName);
					// End initialisation.
				}
			});
			// End add listener for tap on button.
			llEngines.addView(bt, llParams);
		} // end for.
	} // end detectEngines() method.

	// A method to show voices of an engine:
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void detectVoices(String engineName, final String engineLabel) {
		// We disable the Positive Button again to be sure:
		((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
				.setEnabled(false);

		chosenEngine = engineName; // to have it for saving.
		// First of all we take the llVoices LinearLayout:
		LinearLayout llVoices = (LinearLayout) ttsView
				.findViewById(R.id.llVoices);
		llVoices.removeAllViews();

		/*
		 * A LayoutParams for weight for text view and buttons in the llVoices,
		 * to be match_parrent in width and wrap content in height::
		 */
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// Detect all voice locale:
		ArrayList<Locale> localeList = new ArrayList<Locale>();
		if (curAPI >= 21) {
			voiceList = new ArrayList<Voice>(); // for API >21.
		} // end if API 21>.

		try {
			if (curAPI >= 21) {
				Set<Voice> tempVoiceList = tts.getVoices();
				voiceList = new ArrayList<Voice>(tempVoiceList);
				// A for to make the localeList from this voiceList:
				for (int i = 0; i < voiceList.size(); i++) {
					localeList.add(voiceList.get(i).getLocale());
				} // end for.
			} // end if is a newer version.

			else {
				// An older version of android than 21:
				Locale[] locales = Locale.getAvailableLocales();
				for (Locale locale : locales) {
					int res = tts.isLanguageAvailable(locale);
					if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
						localeList.add(locale);
					}
				}
			} // end if is an older version than API 21.
		} catch (Exception e) {
			// e.printStackTrace();
		} // end try .. catch block.

		// We have all voices or languages of this TTS:

		// Now, let's create buttons for each voice found:
		// If no languages are available, we must show a text view:
		if (localeList == null || localeList.size() < 1) {
			TextView tv2 = new TextView(mContext, null,
					R.style.textInSettingsStyle);
			tv2.setText(mContext.getString(R.string.tv_no_voices_available));
			llVoices.addView(tv2, llParams);
		} // end if no languages were detected.

		else { // If languages were detected:
				// Now we want to sort the localeList alphabetically:
			localeList = sortLocaleList(localeList);

			// A text view as a title of this window zone:
			TextView tv = new TextView(mContext, null,
					R.style.textInSettingsStyle);
			// Get the number of voices from plural to make the string after:
			int count = localeList.size();
			Resources res = mContext.getResources();
			String foundVoices = res.getQuantityString(
					R.plurals.tv_number_of_voices, count, count);

			tv.setText(MyHtml.fromHtml(String.format(
					mContext.getString(R.string.tv_choose_a_voice),
					foundVoices, engineLabel)));
			llVoices.addView(tv, llParams);

			// Create now the buttons:
			for (int i = 0; i < localeList.size(); i++) {
				Button bt = new Button(mContext, null,
						R.style.style_buttons_in_settings);
				final int finalI = i;
				final Locale tempLocale = localeList.get(i);
				String language = tempLocale.getDisplayLanguage();
				String country = tempLocale.getDisplayCountry();
				String variant = tempLocale.getDisplayVariant();
				String uniqueName = "";
				if (curAPI >= 21) {
					uniqueName = voiceList.get(i).getName();
				} // end if curAPI is greater than 21.
				CharSequence curVoice = MyHtml.fromHtml(String.format(
						mContext.getString(R.string.one_voice_in_list),
						language, country, variant, uniqueName));
				bt.setText(curVoice.toString().trim());
				bt.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						showVoice(tempLocale, engineLabel, finalI);
					}
				});
				// End add listener for tap on button.
				llVoices.addView(bt, llParams);
			} // end for each button for a language.

			// Try to go to the top after fill:
			ScrollView sv = (ScrollView) ttsView.findViewById(R.id.svVoices);
			sv.fullScroll(ScrollView.FOCUS_UP);
		} // end if at least one language was detected.
	} // end detectVoices();

	// A method which sorts the localeList:
	private ArrayList<Locale> sortLocaleList(ArrayList<Locale> localeList) {
		// I will use Bubble Sort:
		boolean isSorted = false;
		int step = 0;
		while (!isSorted) {
			isSorted = true;
			for (int i = localeList.size() - 1; i > step; i--) {
				if ((localeList.get(i).getDisplayLanguage() + localeList.get(i)
						.getDisplayCountry()).compareTo((localeList.get(i - 1)
						.getDisplayLanguage() + localeList.get(i - 1)
						.getDisplayCountry())) < 0) {
					// Swap first the localeList:
					Locale temp = localeList.get(i);
					localeList.set(i, localeList.get(i - 1));
					localeList.set(i - 1, temp);

					// Swap also the voiceList if API 21>::
					if (curAPI >= 21) {
						Voice temp2 = voiceList.get(i);
						voiceList.set(i, voiceList.get(i - 1));
						voiceList.set(i - 1, temp2);
					} // end if API 21>.

					isSorted = false; // we have still to work.
				} // end if one is greater than another, swap.
			} // end for.
			step++;
		} // end while.

		/*
		 * We have the array lists sorted, now we put at beginning the language
		 * chosen or current locale if default:
		 */
		String curLang = MainActivity.language;
		if (curLang.equalsIgnoreCase("Default")) {
			curLang = Locale.getDefault().getLanguage();
		} // end if default language is saved.
			// Take only the first two letters in lower case:
		curLang = curLang.substring(0, 2);
		/*
		 * A for through all locale to take the curLang matches and to put in
		 * another locale array list. Also for voices object.
		 */
		ArrayList<Locale> importantLocales = new ArrayList<Locale>();
		ArrayList<Voice> importantVoices = new ArrayList<Voice>();

		for (int i = localeList.size() - 1; i >= 0; i--) {
			if (localeList.get(i).getLanguage().substring(0, 2)
					.equalsIgnoreCase(curLang)) {
				importantLocales.add(0, localeList.get(i));
				localeList.remove(i);
				// Also for voices:
				if (curAPI >= 21) {
					importantVoices.add(0, voiceList.get(i));
					voiceList.remove(i);
				} // end if voices, curAPI 21>.
			} // end if found an important locale.
		} // end for take priorities.

		// Now add at beginning of the array lists the important array lists:
		localeList.addAll(0, importantLocales);
		if (curAPI >= 21) {
			voiceList.addAll(0, importantVoices);
		} // end if API 21>.

		return localeList;
	} // end sortLocaleList() method.

	// A method to detail the chosen voice:
	@SuppressLint("NewApi")
	private void showVoice(final Locale tempLocale, String engineLabel,
			int voiceIndex) {
		// Here we can enable the save button, the positive one:
		((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
				.setEnabled(true);

		lastTempLocale = tempLocale;
		// First of all we take the llVoices LinearLayout:
		LinearLayout llVoices = (LinearLayout) ttsView
				.findViewById(R.id.llVoices);
		llVoices.removeAllViews();

		// A LayoutParams to add some controls into llVoices:
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		// Write a title for this zone:
		TextView tv = new TextView(mContext, null,
				R.style.textInSettingsHeadingsStyle);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		String language = tempLocale.getDisplayLanguage();
		String country = tempLocale.getDisplayCountry();
		String variant = tempLocale.getDisplayVariant();
		String uniqueName = "";
		if (curAPI >= 21) {
			lastTempVoice = voiceIndex;
			uniqueName = voiceList.get(lastTempVoice).getName();
		} // end if curAPI is greater than 21.
		CharSequence curVoice = MyHtml.fromHtml(String.format(
				mContext.getString(R.string.one_voice_in_list), language,
				country, variant, uniqueName));

		CharSequence voiceTitle = MyHtml.fromHtml(String.format(
				mContext.getString(R.string.a_voice_chosen_to_check),
				engineLabel, curVoice.toString().trim()));
		tv.setText(voiceTitle);
		llVoices.addView(tv, llParams);

		// We set the chosen voice:
		if (curAPI >= 21) {
			tts.setVoice(voiceList.get(lastTempVoice));
		} else { // older API than 21:
			tts.setLanguage(tempLocale);
		} // end if older API.

		// See about rate and pitch if there are saved:
		Settings set = new Settings(mContext);
		if (set.preferenceExists("ttsRate")) {
			ttsRate = set.getFloatSettings("ttsRate");
		} // end if TTS rate was saved.
		tts.setSpeechRate(ttsRate);

		if (set.preferenceExists("ttsPitch")) {
			ttsPitch = set.getFloatSettings("ttsPitch");
		} // end if TTS rate was saved.
		tts.setPitch(ttsPitch);

		// We need here two seek controls:
		// For rate:
		TextView tvRate = new TextView(mContext, null,
				R.style.textInSettingsStyle);
		tvRate.setText(mContext.getString(R.string.tv_set_tts_rate));
		llVoices.addView(tvRate, llParams);

		SeekBar seekBarRate = new SeekBar(mContext);
		int intRate = getProcentFromFloat(ttsRate);
		seekBarRate.setProgress(intRate);
		seekBarRate.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Do nothing yet.
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Do nothing yet.
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				ttsRate = getFloatFromProcent(progress);
				tts.setSpeechRate(ttsRate);
				speakTheSample("" + progress + "%");
			}
		});
		// end listener for this SeekBar.
		llVoices.addView(seekBarRate, llParams);

		// For pitch:
		TextView tvPitch = new TextView(mContext, null,
				R.style.textInSettingsStyle);
		tvPitch.setText(mContext.getString(R.string.tv_set_tts_pitch));
		llVoices.addView(tvPitch, llParams);

		SeekBar seekBarPitch = new SeekBar(mContext);
		int intPitch = getProcentFromFloat(ttsPitch);
		seekBarPitch.setProgress(intPitch);
		seekBarPitch.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Do nothing yet.
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Do nothing yet.
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				ttsPitch = getFloatFromProcent(progress);
				tts.setPitch(ttsPitch);
				speakTheSample("" + progress + "%");
			}
		});
		// end listener for this SeekBar.
		llVoices.addView(seekBarPitch, llParams);

		// A button to hear a sample in current language of the game or device:
		Button btSample = new Button(mContext, null,
				R.style.textInSettingsStyle);
		btSample.setText(mContext.getString(R.string.bt_tts_sample));
		final String sampleText = mContext.getString(R.string.sample_tts_text);
		btSample.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				speakTheSample(sampleText);
			}
		});
		// End add listener for tap on btSample.
		btSample.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				CharSequence theMessage = MyHtml.fromHtml(String.format(
						mContext.getString(R.string.the_sample_message),
						mContext.getString(R.string.bt_tts_sample), sampleText));
				GUITools.alert(mContext,
						mContext.getString(R.string.the_sample),
						theMessage.toString());
				return true;
			}
		});
		// End add listener for tap on btSample.
		llVoices.addView(btSample, llParams);

		// Another button in this window, not shown yet, just in draft:
		if (curAPI == 1) {
			// A button to hear a sample in current language of the TTS chosen:
			Button btSample2 = new Button(mContext, null,
					R.style.textInSettingsStyle);
			btSample2.setText(mContext.getString(R.string.bt_tts_sample2));
			// Determine the TTS locale sample text:
			// ToDo...
			final String sampleText2 = "Unavailable";
			btSample2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					speakTheSample(sampleText2);
				}
			});
			// End add listener for tap on btSample.
			btSample2.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					CharSequence theMessage = MyHtml.fromHtml(String.format(
							mContext.getString(R.string.the_sample_message),
							mContext.getString(R.string.bt_tts_sample2),
							sampleText2));
					GUITools.alert(mContext,
							mContext.getString(R.string.the_sample),
							theMessage.toString());
					return true;
				}
			});
			// End add listener for tap on btSample.
			llVoices.addView(btSample2, llParams);
		} // end if false, not shown button.
	}// end showVoice() method.

	// A method to speak a sample:
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void speakTheSample(final String text) {
		// Speak the sample:
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after 250ms:
				// Write the text to be spoken:

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
				} else {
					tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
				} // end if it's an older Android version.
			}
		}, 150);
	} // end speakTheSample() method.

	/*
	 * A method which transform the float in int value, 1.0 being 50%, 0.0 being
	 * 0% and 10.0 being 100%.
	 */
	private int getProcentFromFloat(float val) {
		int toReturn = 50;
		if (val >= 1.0F) {
			toReturn = Math.round(50 + granularity * (val - 1));
		} else {
			toReturn = Math.round(50 - granularity2 * (1 - val));
		}
		return toReturn;
	} // end getProcentFromFloat() method.

	/* A method which makes the opposite thing, see the method above: */
	private float getFloatFromProcent(int proc) {
		float toReturn = 1.0F;
		if (proc >= 50) {
			toReturn = (proc - 40) / granularity;
		} else {
			toReturn = (proc + 50) / granularity2;
		}
		return toReturn;
	} // end getFloatFromProcent() method.

	// A method to save the chosen TTS and voice:
	@SuppressLint("NewApi")
	private void saveVoice() {
		// We precede only if lastTempVoice or lastTempLocale is not null:
		if (lastTempLocale != null) {
			tts.stop();
			String language = lastTempLocale.getLanguage();
			String country = lastTempLocale.getCountry();
			String variant = lastTempLocale.getVariant();
			String voiceName = "";
			if (curAPI >= 21) {
				voiceName = voiceList.get(lastTempVoice).getName();
			} // end if newer API than 21.
				// We save now the values:
			Settings set = new Settings(mContext);
			set.saveStringSettings("curEngine", chosenEngine);
			set.saveStringSettings("ttsLanguage", language);
			set.saveStringSettings("ttsCountry", country);
			set.saveStringSettings("ttsVariant", variant);
			set.saveStringSettings("ttsVoiceName", voiceName);
			set.saveFloatSettings("ttsRate", ttsRate);
			set.saveFloatSettings("ttsPitch", ttsPitch);
		} // end if tempLocale is not null.
	} // end saveVoice() method.

	// A method to save the default TTS, blank strings for Locale.
	public void saveDefaultTTS() {
		/*
		 * Save current engine to empty string, this way we will have again
		 * default TTS:
		 */
		set.saveStringSettings("curEngine", "");
		// Save language, country and variant:
		set.saveStringSettings("ttsLanguage", "");
		set.saveStringSettings("ttsCountry", "");
		set.saveStringSettings("ttsVariant", "");
		set.saveStringSettings("ttsVoiceName", "");
		set.saveFloatSettings("ttsRate", 1.0F);
		set.saveFloatSettings("ttsPitch", 1.0F);

		// Make also a toast for this save:
		GUITools.toast(mContext.getString(R.string.tts_set_default_toast),
				2000, mContext);
	} // end saveDefaultTTS() method.
		// end TTS settings fields and methods.

} // end SettingsManagement class.
