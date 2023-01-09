package ro.pontes.justbackgammon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class Manager {

	private final Context mContext;
	private View managerView = null;;
	private Settings set = null;
	private MySoundPool mSP = null;
	private Resources res = null;

	// The constructor:
	public Manager(Context context, MySoundPool mSP) {
		this.mContext = context;
		set = new Settings(mContext);
		this.mSP = mSP;
		res = mContext.getResources();
	} // end constructor.

	// Methods to show manager in an alert:
	@SuppressLint("InflateParams")
	public void showManager() {
		// Inflate the main settings contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		managerView = inflater.inflate(R.layout.manager_game_types, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.manager_title);
		builder.setView(managerView);
		builder.setPositiveButton(
				mContext.getString(R.string.bt_close_manager),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // element finished sound.
					}
				});

		builder.create();
		builder.show();

		// Check the radio button depending of the gameType static variable:
		int gameType = MainActivity.gameType;
		String rb = "rbGameType" + gameType;
		int resID = mContext.getResources().getIdentifier(rb, "id",
				mContext.getPackageName());
		RadioButton radioButton = (RadioButton) managerView.findViewById(resID);
		radioButton.setChecked(true);
		// End set chosen radio button for chosen game type.

		// Now charge at start current game type settings:
		chargeSettingsLayoutForCurrentGameType();
	} // end showManager() method.

	// A method to show the dialog to input nickname in off-line:
	@SuppressLint("InflateParams")
	public void showNicknameInOfflineGames() {
		// Inflate the layout for nickname alert:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View nicknameView = inflater.inflate(R.layout.manager_local_nickname,
				null);

		// Changes in the inflated layout:
		final EditText et = (EditText) nicknameView.findViewById(R.id.etName);

		AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.mng_nickname_title);
		builder2.setView(nicknameView);
		builder2.setPositiveButton(mContext.getString(R.string.bt_save),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // element finished sound.
						// Things to take the new nickname:
						String newNickname = et.getText().toString();
						// Filter not to have more spaces using fromHtml()
						// method:
						Spanned tempNickname = MyHtml.fromHtml(newNickname);
						newNickname = tempNickname.toString();
						// Trim also the string:
						newNickname = newNickname.trim();
						// Check if it is accepted:
						boolean isOK = GUITools.isAcceptedNickname(newNickname);
						// Now, if everything is OK, let's continue to save:
						if (isOK) {
							// We set the MainActivity variable:
							MainActivity.localNickname = new String(newNickname);
							// Save it in shared preferences:
							set.saveStringSettings("localNickname",
									MainActivity.localNickname);
							// Charge again the game type 2 layout:
							chargeSettingsLayoutForCurrentGameType();
						} else { // it is not OK:
							GUITools.alert(
									mContext,
									mContext.getString(R.string.warning),
									mContext.getString(R.string.mng_wrong_local_nickname));
						} // end if nickname isn't OK.
							// end things to take the new nickname.
					}
				});
		builder2.setNegativeButton(mContext.getString(R.string.bt_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // element finished sound.
					}
				});

		builder2.create();
		builder2.show();
	} // end showNicknameInOfflineGames() method.

	/*
	 * A method to choose the interval between computer moves or to disable
	 * showing them, an alert with radio buttons:
	 */
	@SuppressLint("InflateParams")
	public void showChangeShowingInterval() {
		// Inflate the layout for choosing interval alert:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View intervalView = inflater.inflate(
				R.layout.manager_change_showing_moves, null);

		// Changes in the inflated layout:
		// Add the radio buttons into the radio group:
		// Find the RadioGroup:
		RadioGroup rg = (RadioGroup) intervalView
				.findViewById(R.id.rgShowingMovesInterval);
		// A LayoutParams to add radio buttons into the RG:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// Charge the array with buttons names:
		String[] arrNames = res.getStringArray(R.array.moves_interval_array);
		// Get first the current interval:
		int movesInterval = set.preferenceExists("movesInterval") ? set
				.getIntSettings("movesInterval") : 1000;
		int rbIndex = movesInterval / 500;
		// A for loop for all available interval:
		for (int i = 0; i < arrNames.length; i++) {
			RadioButton rb = new RadioButton(mContext, null,
					R.style.style_radio_buttons_in_settings);
			rb.setText(arrNames[i]);
			// Check if needed:
			if (i == rbIndex) {
				rb.setChecked(true);
			} // end check the selected one.
				// Add also onClick method to save checked:
			final int finalI = i;
			rb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Save here in preferences the current value chosen:
					int newInterval = finalI * 500;
					set.saveIntSettings("movesInterval", newInterval);
					mSP.playSound(17); // element clicked sound.
				} // end onClick.
			});
			rg.addView(rb, lp);
		} // end for create radio buttons for all available intervals.

		// Create the alert effectively:
		AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.mng_showing_interval_title);
		builder2.setView(intervalView);
		builder2.setPositiveButton(mContext.getString(R.string.bt_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // element finished sound.
						// Charge again the game type 2 layout:
						chargeSettingsLayoutForCurrentGameType();
					}
				});

		builder2.create();
		builder2.show();
	} // end showChangeShowingInterval() method.

	// A method to charge in the right SV settings for current game type:
	@SuppressLint("InflateParams")
	private void chargeSettingsLayoutForCurrentGameType() {
		int gameType = MainActivity.gameType;
		ScrollView svRight = (ScrollView) managerView
				.findViewById(R.id.svGameTypeSettings);
		svRight.removeAllViews();

		// Inflate the chosen game type settings:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View managerSettingsView = null;

		// A switch to know which layout to add, depending of the game type:
		switch (gameType) {
		case 1: // off-line two in same room:
			managerSettingsView = inflater.inflate(
					R.layout.manager_settings_game_type_1, null);
			break;
		case 2: // AI games:
			managerSettingsView = inflater.inflate(
					R.layout.manager_settings_game_type_2, null);
			break;
		case 0: // two bots games:
			managerSettingsView = inflater.inflate(
					R.layout.manager_settings_game_type_0, null);
			break;
		} // end switch on gameType.

		// Now add the layout into the svRight scroll view:
		svRight.addView(managerSettingsView);

		// Now set control states in layout chosen:
		if (gameType == 1) {
			// Not check and uncheck the check boxes in this alert:
			// For change perspective automatically:
			CheckBox cbtChangePerspective = (CheckBox) managerView
					.findViewById(R.id.cbtMngChangeBoardPerspective);
			cbtChangePerspective.setChecked(set
					.getBooleanSettings("changePerspective"));

			// For change perspective automatically on TV:
			CheckBox cbtChangePerspectiveTV = (CheckBox) managerView
					.findViewById(R.id.cbtMngChangeBoardPerspectiveTV);
			cbtChangePerspectiveTV.setChecked(set
					.preferenceExists("changePerspectiveTV") ? set
					.getBooleanSettings("changePerspectiveTV") : true);
		} // end if gameType is 1, local games.
		else if (gameType == 2) { // AI games:
			// Set the current nickname on the text view:
			TextView tvCurName = (TextView) managerView
					.findViewById(R.id.tvCurrentNickname);
			Spanned msgCurName = MyHtml.fromHtml(String.format(
					mContext.getString(R.string.mng_current_nickname),
					MainActivity.localNickname));
			tvCurName.setText(msgCurName);

			// Set the showing interval current choose:
			// Get first the current interval:
			int movesInterval = set.preferenceExists("movesInterval") ? set
					.getIntSettings("movesInterval") : 1000;
			/*
			 * It is possible 0, 500, 1000 etc, to detect the order number of
			 * current choose, we divide the actual interval to 500:
			 */
			int rbIndex = movesInterval / 500;
			// Now get from array of strings for radio buttons:
			String strInterval = res
					.getStringArray(R.array.moves_interval_array)[rbIndex];
			// Format now the interval current settings message:
			TextView tvCurInterval = (TextView) managerView
					.findViewById(R.id.tvCurrentShowingMoves);
			Spanned msgCurInterval = MyHtml.fromHtml(String.format(
					mContext.getString(R.string.mng_current_moves_interval),
					strInterval));
			tvCurInterval.setText(msgCurInterval);
			// end moves interval shown current choose.

			// Check the radio button depending of the difficulty level chosen:
			int maxDepth = MiniMax.maxDepth;
			String rb = "rbDifficultyLevel" + (maxDepth + 1);
			int resID = mContext.getResources().getIdentifier(rb, "id",
					mContext.getPackageName());
			RadioButton radioButton = (RadioButton) managerView
					.findViewById(resID);
			radioButton.setChecked(true);
		} // end if gameType is 2, AI games.
	} // end chargeSettingsLayoutForCurrentGameType() method.

	// A method which acts when manager is chosen in settings:
	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked:
		int tempId = view.getId();
		switch (tempId) {
		// For game type, radio buttons:
		case R.id.rbGameType1: // local games:
			if (checked) {
				int gameType = 1;
				// We change the MainActivity value to have it in next method:
				MainActivity.gameType = gameType;
				set.saveIntSettings("gameType", MainActivity.gameType);
				chargeSettingsLayoutForCurrentGameType();
			}
			break;

		case R.id.rbGameType2: // AI games:
			if (checked) {
				int gameType = 2;
				// We change the MainActivity value to have it in next method:
				MainActivity.gameType = gameType;
				set.saveIntSettings("gameType", MainActivity.gameType);
				chargeSettingsLayoutForCurrentGameType();
			}
			break;
		case R.id.rbGameType0: // two bots games:
			if (checked) {
				int gameType = 0;
				// We change the MainActivity value to have it in next method:
				MainActivity.gameType = gameType;
				set.saveIntSettings("gameType", MainActivity.gameType);
				chargeSettingsLayoutForCurrentGameType();
			}
			break;
		// End choose game types zone.

		// A zone for difficulty levels:
		case R.id.rbDifficultyLevel1: // level 2:
			if (checked) {
				int maxDepth = 0;
				// We change the MiniMax value to save it:
				MiniMax.maxDepth = maxDepth;
				set.saveIntSettings("maxDepth", MiniMax.maxDepth);
			}
			break;

		case R.id.rbDifficultyLevel2: // level 2:
			if (checked) {
				int maxDepth = 1;
				// We change the MiniMax value to save it:
				MiniMax.maxDepth = maxDepth;
				set.saveIntSettings("maxDepth", MiniMax.maxDepth);
			}
			break;

		// case R.id.rbDifficultyLevel3: // level 3:
		// if (checked) {
		// int maxDepth = 2;
		// We change the MiniMax value to save it:
		// MiniMax.maxDepth = maxDepth;
		// set.saveIntSettings("maxDepth", MiniMax.maxDepth);
		// }
		// break;
		// end zone for difficulty levels.
		} // end switch.
	} // end onRadioButtonClicked() method.

	// A method for check boxes clicked:
	public void onCheckboxClicked(View view) {
		// Is the view now checked?
		boolean checked = ((CheckBox) view).isChecked();

		// Check which check box was clicked
		switch (view.getId()) {
		case R.id.cbtMngChangeBoardPerspective:
			if (checked) {
				set.saveBooleanSettings("changePerspective", true);
			} else {
				set.saveBooleanSettings("changePerspective", false);
			}
			break;

		case R.id.cbtMngChangeBoardPerspectiveTV:
			if (checked) {
				set.saveBooleanSettings("changePerspectiveTV", true);
			} else {
				set.saveBooleanSettings("changePerspectiveTV", false);
			}
			break;

		} // end switch.
	} // end onCheckboxClicked() method.

	// A method to reset the score for local or AI games:
	public void showResetScore(final int gt) {
		// Inflate the question:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View questionView = inflater.inflate(R.layout.question_reset_score,
				null);

		// Charge the text view and set the corresponding message:
		TextView tv = (TextView) questionView.findViewById(R.id.tvQuestion);
		String msg = "";
		if (gt == 1) {
			msg = mContext.getString(R.string.mng_reset_score_body_gt_1);
		} else if (gt == 2) {
			msg = mContext.getString(R.string.mng_reset_score_body_gt_2);
		} else if (gt == 0) {
			msg = mContext.getString(R.string.mng_reset_score_body_gt_0);
		}
		tv.setText(msg);
		AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setIcon(R.drawable.ic_launcher);
		builder2.setTitle(R.string.mng_reset_score_title);
		builder2.setView(questionView);
		builder2.setPositiveButton(mContext.getString(R.string.bt_yes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						resetTheScoreNow(gt);
					}
				});
		builder2.setNegativeButton(mContext.getString(R.string.bt_no),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				});

		builder2.create();
		builder2.show();
	} // end showResetScore() method.

	// A method to reset the score effectively:
	private void resetTheScoreNow(int gt) {
		set.saveIntSettings("scoreWhite" + gt, 0);
		set.saveIntSettings("scoreBlack" + gt, 0);
	} // end resetTheScoreNow() method.

} // end Manager class.
