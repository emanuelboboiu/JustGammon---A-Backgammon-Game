package ro.pontes.justbackgammon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;


public class MainActivity extends Activity {

    private final Context mContext = this;
    public static int curAPI;
    public static boolean isFirstLaunchInSession = true;
    public final static String DEFAULT_ACCOUNT_NAME = "Anonymous";
    public static String myAccountName = DEFAULT_ACCOUNT_NAME;
    // The next will replace the account name in statistics sent to the server:
    public static int randomNumberInsteadAccountName = 0;
    public static String localNickname = "User"; // changed in charge settings;
    public static String language = "Default";
    public static int intVolume = 100; // the percentage of media volume.
    public static int orientation = 0;
    public static boolean isPremium = false;
    private final String mProduct = "justgammon_premium";
    public static String mUpgradePrice = "�";
    public static boolean isTV = false;
    public static boolean isAccessibility = false;
    public static boolean isSpeech = true;
    public static boolean isSound = true;
    public static boolean isClock = true;
    public static boolean isAmbience = true;
    public static String chosenAmbience = "ambience0";
    public static boolean isFundalMenu = true;
    private LoopMediaPlayer fundalMenu = null;
    public static boolean isWakeLock = true;
    public static boolean isVibration = true;
    public static int numberOfLaunches = 0;
    public static boolean isStarted = false;
    public static int textSize = 20;
    public static int gameType = 2; // initial is AI.

    // For GUI:
    private View mDecorView;

    // For Settings management:
    private SettingsManagement sm = null;
    private Manager manag = null;
    private LastGames lastGames = null;
    private InformationAlert infAlert = null;

    // For sounds:
    private MySoundPool mSP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Charge the settings:
        Settings set = new Settings(this);
        set.chargeSettingsInMainActivity();
        GUITools.setLocale(this, MainActivity.language);

        // Determine if it's a TV or not:
        if (GUITools.isAndroidTV(this)) {
            MainActivity.isTV = true;
        } // end determine if it is TV.
        if (GUITools.isAccessibilityEnabled(this)) {
            isAccessibility = true;
        } // end set if isAccessibility.

        // Set the orientation depending of the one chosen:
        if (orientation == 1) { // portrait forced:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation == 2) { // landscape forced:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } // end if must be portrait.
        else { // automatic:
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } // end automatic orientation.

        // Set the content view effectively:
        setContentView(R.layout.activity_main);

        /*
         * Get the decorView and hide the bottom buttons in
         * onWindowFocusChanged:
         */
        mDecorView = getWindow().getDecorView();
        GUITools.hideSystemUI(mDecorView);

        setFirstThings();

        // We need sometimes current Android API:
        curAPI = Build.VERSION.SDK_INT;

        myAccountName = getAccountName();

        showWhatsNew();

        // To post not posted tests from local to on-line DB:
        Statistics stats = new Statistics(this);
        stats.postOnlineNotPostedFinishedTests();
    } // end onCreate() method.

    /*
     * This is a new method, we need an anonymous account name because we
     * removed every ting about the permission view accounts names, now only a
     * random number preceded by an A.
     */
    public String getAccountName() {
        return "A" + MainActivity.randomNumberInsteadAccountName;
    } // end getAccountName() method.

    @Override
    public void onResume() {
        super.onResume();
        // Play the loop:
        if (MainActivity.isFundalMenu) {
            fundalMenu = new LoopMediaPlayer(this, R.raw.fundalmenu);
        }
    } // end onResume method.

    @Override
    public void onPause() {
        if (fundalMenu != null) {
            fundalMenu.stop();
            fundalMenu = null;
        } // end if it is not null.

        super.onPause();
    } // end onPause method.

    @Override
    protected void onDestroy() {
        super.onDestroy();
    } // end onDestroy method.

    // To know when focus is gained, to hide the status buttons:
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Hide the status buttons.
            GUITools.hideSystemUI(mDecorView);
        }
    }// end onWindowFocusChanged() method.

    private void setFirstThings() {
        mSP = new MySoundPool(this);
    } // end setFirstThings() method.

    // Methods to go in different sections from menu:
    // A method to go to game activity:
    public void startGame(View view) {
        GUITools.goToGameActivity(this);
        this.finish();
    } // end startGame() method.

    // A method to go to statistics alert:
    public void goToStatistics(View view) {
        Statistics stats = new Statistics(this);
        stats.showStats();
    } // end goToStatistics() method.

    // A method to go to Manager alert:
    public void goToGameMode(View view) {
        // Instantiate the Manager object if it is not null:
        if (manag == null) {
            manag = new Manager(this, mSP);
        } // end if manager object is null.
        else { // the sound for clicked
            // It happens when clicking the buttons in manager:
            mSP.playSound(17); // element clicked.
        } // end if no instantiate the object, but we want the sound.

        // We open the alert depending of the button clicked:
        int id = view.getId();
        switch (id) {
            case R.id.btChangeNickname:
                manag.showNicknameInOfflineGames();
                break;

            case R.id.btChangeShowingMoves:
                manag.showChangeShowingInterval();
                break;

            case R.id.btResetScoreGT1:
                manag.showResetScore(1);
                break;

            case R.id.btResetScoreGT2:
                manag.showResetScore(2);
                break;

            case R.id.btResetScoreGT0:
                manag.showResetScore(0);
                break;

            default: // manager from Main Menu:
                manag.showManager();
                break;
        } // end switch.
    } // end goToGameMode() method.

    // A method when a radio button is clicked in manager:
    public void onRadioButtonManagerClicked(View view) {
        // It is passed to the method with same name in manager class:
        manag.onRadioButtonClicked(view);
        mSP.playSound(17); // element clicked.
    } // end onRadioButtonManagerClicked() method.

    // A method when a check box is clicked in manager:
    public void onCheckboxClickedInManager(View view) {
        // It is passed to the method with same name in Manager:
        manag.onCheckboxClicked(view);
        mSP.playSound(17); // element clicked.
    } // end onCheckboxClickedInManager() method.
    // end manager methods.

    // Start settings zone:
    // A method to go to settings activity:
    public void settings(View view) {
        // Instantiate the SettingsManagement object if it is not null:
        if (sm == null) {
            sm = new SettingsManagement(this, this, mSP);
        } // end if SM is null.
        else { // the sound for clicked
            // It happens when clicking the buttons for categories:
            mSP.playSound(17); // element clicked.
        } // end if no instantiate the object, but we want the sound.

        // We open the alert depending of the button clicked:
        int id = view.getId();
        switch (id) {
            case R.id.btGeneralSettings:
                sm.showGeneralSettings();
                break;

            case R.id.btVisualSettings:
                sm.showVisualSettings();
                break;

            case R.id.btSoundsSettings:
                sm.showSoundsSettings();
                break;

            case R.id.btAccessibilitySettings:
                sm.showAccessibilitySettings();
                break;

            case R.id.btTTSSettings:
                sm.showTTSSettings();
                break;

            case R.id.btResetTTSSettings:
                sm.saveDefaultTTS();
                break;

            case R.id.btLanguageSettings:
                sm.showLanguageSettings();
                break;

            default: // Settings from Main Menu:
                sm.showSettingsAlert();
                break;
        } // end switch.
    } // end settings() method.

    // A method when a check box is clicked in settings:
    public void onCheckboxClicked(View view) {
        // It is passed to the method with same name in SettingsManagement:
        sm.onCheckboxClicked(view);
        mSP.playSound(17); // element clicked.
    } // end onCheckboxClicked() method.

    // A method when a radio button is clicked in settings:
    public void onRadioButtonClicked(View view) {
        // It is passed to the method with same name in SettingsManagement:
        sm.onRadioButtonClicked(view);
        mSP.playSound(17); // element clicked.
    } // end onRadioButtonClicked() method.

    // The method to enable or disable music in main menu:
    public void enableOrDisableMenuMusic(View view) {
        mSP.playSound(17); // element clicked.
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            isFundalMenu = true;
            // Create the object and also start it:
            fundalMenu = new LoopMediaPlayer(this, R.raw.fundalmenu);
        } else { // not checked:
            isFundalMenu = false;
            if (fundalMenu != null) {
                fundalMenu.stop();
                fundalMenu = null;
            } // end if it is not null.
        } // end if not checked.
        Settings set = new Settings(this);
        set.saveBooleanSettings("isFundalMenu", isFundalMenu);
    } // end enableOrDisableMenuMusic() method.

    // A method which fires when reset to defaults button is clicked:
    public void resetToDefaults(View view) {
        sm.resetToDefaults(view);
    } // end resetToDefaults() method.
    // End settings zone.

    public void showInformation(View view) {
        showInformationEfectively();
    } // end showInformation() method.

    private void showInformationEfectively() {
        if (infAlert == null) {
            infAlert = new InformationAlert(this, mSP);
        } // end if infAlert is null.
        infAlert.showInformationAlert();
    } // end showInformationEfectively() method.

    public void showNews(View view) {
        GUITools.showNews(this);
    } // end showNews() method.

    public void showChangelog(View view) {
        GUITools.showChangelog(this);
    } // end showChangelog() method.

    public void showPrivacy(View view) {
        GUITools.showPrivacy(this);
    } // end showPrivacy() method.

    public void showOnlineStatistics(View view) {
        GUITools.showOnlineStatistics(this);
    } // end showOnlineStatistics() method.

    public void showAbout(View view) {
        GUITools.aboutDialog(this);
    } // end showAbout() method.

    public void showLastGames(View view) {
        // Instantiate the lastGames object if it is not null:
        if (lastGames == null) {
            lastGames = new LastGames(this);
        } // end if lastGames object is null.

        // We open the alert with games list:
        lastGames.showLastGames();
    } // end showLastGames() method.

    // A method to go to rate the application:
    public void rateTheApp(View view) {
        GUITools.showRateDialog(this);
    } // end rateTheApp() method.

    // A method to show the help:
    public void showHelp(View view) {
        GUITools.showHelp(this);
    } // end showHelp() method.

    public void upgradeToPremium(View view) {
        upgradeAlert();
    } // end upgradeToPremium() method.

    @SuppressLint("InflateParams")
    public void upgradeAlert() {
        if (GUITools.isNetworkAvailable(this)) {
            // Create now the alert:
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            // Inflate the stub for alert dialog:
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View alertView = inflater.inflate(R.layout.alert_dialog, null);

            // Find now the llAlert:
            LinearLayout ll = alertView.findViewById(R.id.llAlert);
            // A LayoutParams to add text views into the LL:
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            // The message:
            TextView tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            String message;
            if (isPremium) {
                message = getString(R.string.premium_version_alert_message);
            } else {
                message = String.format(getString(R.string.non_premium_version_alert_message), mUpgradePrice);
            } // end if is not premium.
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv, lp);

            alertDialog.setTitle(getString(R.string.premium_version_alert_title));
            alertDialog.setView(alertView);
            // We show the buy and enter code buttons only if is not premium:
            if (!isPremium) {
                alertDialog.setNeutralButton(mContext.getString(R.string.bt_enter_code_premium), (dialog, whichButton) -> {
                    // Do nothing yet for enter code.
                    // A method to buy by code:
                    upgradeToPremiumByCodeActions();
                    // ...
                });
                alertDialog.setPositiveButton(mContext.getString(R.string.bt_buy_premium), (dialog, whichButton) -> {
                    // Start the payment process:
                    // Only if is not premium:
                    // A method to buy from Play Store:
                    // upgradeToPremiumActions();
                });
            } // end if it is not premium, show buttons.
            alertDialog.setNegativeButton(mContext.getString(R.string.bt_close), (dialog, whichButton) -> {
                // Do nothing, just close the dialog.
            });

            final AlertDialog alert = alertDialog.create();
            alert.show();
        } // end if is connection available.
        else {
            GUITools.alert(this, getString(R.string.warning), getString(R.string.no_connection_available));
        } // end if connection is not available.
    } // end upgradeAlert() method.

    // A method to upgrade to Premium Version by code:
    private void upgradeToPremiumByCodeActions() {
        // First we check if we have the Account name:
        if (myAccountName.equals(DEFAULT_ACCOUNT_NAME)) { // No user account:
            GUITools.alert(this, getString(R.string.warning), getString(R.string.register_by_code_unavailable));
        } else { // we have a user Account name:
            // We show an alert to enter the code:
            AlertDialog.Builder alert2 = new AlertDialog.Builder(this);
            ScrollView sv = new ScrollView(this);
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            // A LayoutParams to add with match_parent the controls in this
            // LinearLayout:
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            // The message:
            TextView tv = new TextView(this);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            String message = getString(R.string.enter_code_message, myAccountName);
            tv.setText(message);
            tv.setFocusable(true);
            ll.addView(tv, lp);
            // Here the edit text:
            final EditText et = new EditText(this);
            et.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            et.setHint(getString(R.string.enter_code_hint));
            et.setFocusable(true);
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(8);
            et.setFilters(filterArray);
            ll.addView(et, lp);

            // Add the LinearLayout into ScrollView:
            sv.addView(ll);
            alert2.setTitle(getString(R.string.enter_code_title));
            alert2.setView(sv);
            alert2.setPositiveButton(mContext.getString(R.string.bt_continue), (dialog, whichButton) -> {
                // Check the code here and try to register:
                String code = et.getText().toString();
                tryToUpgradeByCode(code);
            });
            alert2.setNegativeButton(mContext.getString(R.string.bt_close), (dialog, whichButton) -> {
                // Do nothing, just close the dialog.
            });
            alert2.create();
            alert2.show();
        } // end if we have a Google Account name.
    } // end upgradeToPremiumByCodeActions() method.

    // This method is called after entering a code and tries to register:
    private void tryToUpgradeByCode(String enteredCode) {
        String correctCode = GUITools.getSerialCode(myAccountName);
        // Check if both codes are the same:
        if (enteredCode.equalsIgnoreCase(correctCode)) {
            recreateThisActivityAfterRegistering();
        } else { // wrong code:
            GUITools.alert(this, getString(R.string.error), getString(R.string.enter_code_wrong));
        } // end wrong code entered.
    } // end tryToUpgradeByCode(() method.

    // A method which recreates this activity after buying premium version:
    private void recreateThisActivityAfterRegistering() {
        // We save it as an premium version:
        isPremium = true;
        Settings set = new Settings(this);
        set.saveBooleanSettings("isPremium", isPremium);
        SoundPlayer.playSimple(this, "premium");

    } // end recreateThisActivityAfterRegistering() method.
    // End methods for InAppBilling.

    // A method which recreates this activity:
    private void recreateThisActivity() {
        this.recreate();
    } // end recreateThisActivity() method.

    // Here there were the methods for requesting the account permission,
    // removed on 21 June 2021.

    // A method to show alert for what's new:
    @SuppressLint("InflateParams")
    private void showWhatsNew() {
        /*
         * We show what's new only if is first start of the main menu in current
         * session, no from a finished game:
         */
        if (isFirstLaunchInSession) {
            isFirstLaunchInSession = false;
            Settings set = new Settings(this);
            boolean wasAnnounced = set.getBooleanSettings("wasAnnounced151");
            /*
             * Only if it was not set not to be announced anymore, wasAnnounced
             * true:
             */
            if (!wasAnnounced) {
                // create an alert inflating an XML:
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View newView = inflater.inflate(R.layout.whatsnew_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle(R.string.whatsnew_title);
                builder.setView(newView);
                builder.setPositiveButton(R.string.bt_close, (dialog, whichButton) -> SoundPlayer.playSimple(mContext, "element_finished"));
                builder.create();
                builder.show();
            } // end if it was not announced.
        } // end if it is first launch of this in session.
    } // end showWhatsNew() method.

    // When clicking the got it check-box in what's new:
    public void onCheckboxWhatsNewClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        Settings set = new Settings(this); // to save changes.
        // Check which check box was clicked
        switch (view.getId()) {
            case R.id.cbtGotIt:
                if (checked) {
                    set.saveBooleanSettings("wasAnnounced151", true);
                } else {
                    set.saveBooleanSettings("wasAnnounced151", false);
                }
                break;
        } // end switch.
        SoundPlayer.playSimple(mContext, "element_clicked");
    } // end onClickWhatsNew method.

} // end MainActivity class.
