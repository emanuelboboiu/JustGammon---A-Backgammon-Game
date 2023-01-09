package ro.pontes.justbackgammon;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends Activity {

    MySoundPool mSP = null;
    // The object game:
    Game game = null;
    private boolean wasGlobalLayoutFinished = false;
    private boolean doubleBackToExitPressedOnce = false;
    private SoundPlayer snd;
    private Timer t = null;
    private View mDecorView;
    private boolean wasOnCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mark as onCreateHappened:
        wasOnCreate = true;
        // Get the decorView and hide the bottom buttons in
        // onWindowFocusChanged:
        mDecorView = getWindow().getDecorView();
        GUITools.hideSystemUI(mDecorView);

        // Set also the chosen locale if no default:
        GUITools.setLocale(this, MainActivity.language);

        // Charge the layout depending if it is the premium version or not:
        if (MainActivity.isPremium || MainActivity.isTV) {
            setContentView(R.layout.activity_game_premium);
        } else {
            setContentView(R.layout.activity_game);
        } // end if it is not premium.

        firstThings();

        // To keep screen awake:
        if (MainActivity.isWakeLock) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } // end wake lock.

        // Start the AdMob only if it is not TV:
        if (MainActivity.isTV || MainActivity.isPremium) {
            // We don't show the ads.
        } else { // show ads:
            adMobSequence();
        } // end if it is not TV.

        // When layout is fully charged:
        postInitialThings();
    }// end onCreate() method.

    private void firstThings() {
        mSP = new MySoundPool(this);
        // Create the object game which creates the board:
        game = new Game(this, this, mSP);
        game.initializeBoard();
    } // end firstThings() method.

    // A method for things after everything is charged:
    private void postInitialThings() {
        LinearLayout mainLayout = findViewById(R.id.llMain);
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // At this point the layout is complete:
            if (!wasGlobalLayoutFinished) {
                game.atLayoutCharged();
                // Make also for long click in inner boards:
                for (int i = 0; i < 24; i++) {
                    // Only if is inner board:
                    if (i < 6 || i >= 18) {
                        final int tempCurPos = i;
                        int resID = getResources().getIdentifier("ivB" + (i + 1), "id", getPackageName());
                        ImageView iv = findViewById(resID);
                        iv.setOnLongClickListener(view -> {
                            pointLongClickedActions(tempCurPos);
                            return true;
                        });
                        // End add listener for long click.
                    } // end if is inner board position.
                } // end for add long click per inner boards..
                wasGlobalLayoutFinished = true;
            } // end if was first time fired the global finished.
        });
        // End main layout is charged.
    } // end postInitialThings() method.

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            game.closeBoard();
            GUITools.goToMainActivity(this);
            this.finish();
            super.onBackPressed();
            return;
        } // end if it is second press of the back button.

        this.doubleBackToExitPressedOnce = true;
        GUITools.toast(getString(R.string.msg_click_again_back), 1500, this);

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 3000);
    } // end onBackPressed()

    @Override
    protected void onResume() {
        super.onResume();
        // Set again the locale if was in onCreate() method:
        if (wasOnCreate) {
            GUITools.setLocale(this, MainActivity.language);
            wasOnCreate = false;
        } // end if wasOnCreate.
        // Add here what to happen at resume:
        if (MainActivity.isStarted) {
        }
        setTheTimer();
        // Start the sound ambiance:
        if (MainActivity.isAmbience) {
            snd = new SoundPlayer();
            snd.playLooped(this, MainActivity.chosenAmbience);
        } // end if set to have sound ambiance.
        /*
         * If is a game in progress, arrange again the board because maybe AI
         * moved when it was on pause:
         */
        if (MainActivity.isStarted) {
            // game.arrangeBoard(, false, false);
        } // end if a game is in progress.
    } // end onResume method.

    @Override
    protected void onPause() {
        // Add here what you want to happen on pause:
        t.cancel();
        t = null;
        if (snd != null && MainActivity.isAmbience) {
            snd.stopLooped();
        } // end if set to have sound ambiance.
        super.onPause();
    } // end onPause method.

    @Override
    protected void onDestroy() {
        game.atDestroy();
        super.onDestroy();
    } // end onDestroy method.

    // To know when focus is gained, to hide the status buttons:
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Hide the status buttons.
            GUITools.hideSystemUI(mDecorView);
            if (MainActivity.isStarted) {
                game.chronResume();
            } // end if it is started.
        } else { // focus lost:
            if (MainActivity.isStarted) {
                game.chronPause();
            } // end if it is started.
        } // end if focus lost.
    }// end onWindowFocusChanged() method.

    // A method to set the timer in onResume:
    public void setTheTimer() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // Here what happens.
                    game.timerEvent();
                });
            }
        }, 1000, 1000);
    } // end setTheTimer method.

    // The method to generate the AdMob sequence:
    private void adMobSequence() {
    } // end adMobSequence().

    private void pointLongClickedActions(int curPos) {
        game.positionLongClicked(curPos);
    } // end pointLongClickedActions() method.

    public void onPointClicked(View view) {
        pointClickedActions(view);
    } // end onPointClicked() method.

    private void pointClickedActions(View view) {
        String mTag = view.getTag().toString();
        // Determine the internal position from 0 to 23:
        int curPos = Integer.parseInt(mTag) - 1;
        game.positionClicked(curPos);
    } // end pointClickedActions() method.

    // A method for border clicked:
    public void onBorderClicked(View view) {
        String mTag = view.getTag().toString();
        int curBorder = Integer.parseInt(mTag);
        game.borderClicked(curBorder);
    } // end onBorderClicked() method.

    // A die is clicked:
    public void onDiceClicked(View view) {
        game.dieClicked();
    } // end onDiceClicked() method.

    // Text zone is clicked:
    public void textZoneClicked(View view) {
        // We want to show the messages history dialog:
        game.textZoneClicked();
    } // end textZoneClicked() method.

    // Chronometer is clicked:
    public void chronClicked(View view) {
        // We show time elapsed and percentages:
        game.chronClicked();
    } // end chronClicked() method.

    // A method which acts at play button click:
    public void onPlayButtonClicked(View view) {
        playButtonActions();
    } // end onPlayButtonClicked() method.

    private void playButtonActions() {
        game.playButtonClicked();
    } // end playButtonActions() method.

    // A method which acts at statistics button click:
    public void onStatisticsButtonClicked(View view) {
        statisticsButtonActions();
    } // end onStatisticsButtonClicked() method.

    private void statisticsButtonActions() {
        // 0 means show in alert statistics:
        game.showOrGetGameStatistics(0);
    } // end statisticsButtonActions() method.

    // The implementations for context menu:
    // A method which acts at options button click:
    public void onOptionsButtonClicked(View view) {
        optionsButtonActions(view);
    } // end onOptionsButtonClicked() method.

    private void optionsButtonActions(View view) {
        registerForContextMenu(view);
        view.showContextMenu();
    } // end optionsButtonActions() method.

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        String cmTitle = getString(R.string.cm_title);
        menu.setHeaderTitle(cmTitle);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cmAbandon:
                game.abandon();
                return true;

            case R.id.cmOpen:
                game.open();
                return true;

            case R.id.cmSave:
                game.save();
                return true;

            case R.id.cmRate:
                GUITools.showRateDialog(this);
                return true;

            case R.id.cmHelp:
                GUITools.showHelp(this);
                return true;

            default:
                return super.onContextItemSelected(item);
        } // end switch.
    } // End context menu implementation.

} // end GameActivity class.
