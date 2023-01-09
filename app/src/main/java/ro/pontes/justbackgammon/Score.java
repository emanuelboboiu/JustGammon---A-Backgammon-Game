package ro.pontes.justbackgammon;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class Score {

	private Context mContext;
	private final Activity mActivity;
	private int scoreWhite = 0;
	private int scoreBlack = 0;
	private ImageView[] ivTurns;
	private TextView[] tvScores;
	private Drawable isTurnWhite, isTurnBlack, isNotTurnWhite, isNotTurnBlack;
	private boolean isAccessibleTheme = false;
	private String[] tempAColor = new String[] { "", "white", "black" };

	// The constructor:
	public Score(Context mContext, Activity mActivity) {
		this.mContext = mContext;
		this.mActivity = mActivity;
		Settings set = new Settings(mContext);
		isAccessibleTheme = set.getBooleanSettings("isAccessibleTheme");
		if (isAccessibleTheme) {
			tempAColor = new String[] { "", "green", "red" };
		} // end if it isAccessibleTheme.
			// Charge turn text views into their array:
		ivTurns = new ImageView[2];
		ivTurns[0] = (ImageView) this.mActivity
				.findViewById(R.id.ivIsTurnWhite);
		ivTurns[1] = (ImageView) this.mActivity
				.findViewById(R.id.ivIsTurnBlack);
		// Charge text views for scores into their array:
		tvScores = new TextView[2];
		tvScores[0] = this.mActivity.findViewById(R.id.tvScoreWhite);
		tvScores[1] = this.mActivity.findViewById(R.id.tvScoreBlack);
		if (MainActivity.gameType < 3) { // local or AI:
			setSavedScore();
		} // end if local or AI.
	} // end constructor.

	// A method to save current score in shared preferences:
	public void saveScore(int score1, int score2) {
		if (MainActivity.gameType < 3) { // local or AI:
			Settings set = new Settings(mContext);
			set.saveIntSettings("scoreWhite" + MainActivity.gameType, score1);
			set.saveIntSettings("scoreBlack" + MainActivity.gameType, score2);
		} // end if local or AI.
	} // end saveScore() method.

	// A method which sets the initial score saved:
	private void setSavedScore() {
		Settings set = new Settings(mContext);
		this.scoreWhite = set.getIntSettings("scoreWhite"
				+ MainActivity.gameType);
		this.scoreBlack = set.getIntSettings("scoreBlack"
				+ MainActivity.gameType);
		setScore(scoreWhite, scoreBlack);
	} // end setSavedScore.

	// A method which set whose turn is visually:
	public void setTurnVisually(int isTurn, String cd) {
		if (isTurn == 0) { // nobody's turn:
			ivTurns[0].setBackground(isNotTurnWhite);
			ivTurns[1].setBackground(isNotTurnBlack);
		} else if (isTurn == 1) { // white's turn:
			ivTurns[0].setBackground(isTurnWhite);
			ivTurns[1].setBackground(isNotTurnBlack);
		} else { // blacks turn:
			ivTurns[0].setBackground(isNotTurnWhite);
			ivTurns[1].setBackground(isTurnBlack);
		} // end if it is black's turn.
			// Set also the content description for both image views:
		for (int i = 0; i < ivTurns.length; i++) {
			ivTurns[i].setContentDescription(cd);
		} // end for set content description.
	} // end setTurnVisually() method.

	// A method to set the score:
	public void setScore(int scoreWhite, int scoreBlack) {
		this.scoreWhite = scoreWhite;
		this.scoreBlack = scoreBlack;
		tvScores[0].setText("" + this.scoreWhite);
		tvScores[1].setText("" + this.scoreBlack);
	} // end setScore() method.

	/*
	 * A method to resize the is turn image views and charge the drawable
	 * objects. It is called from game in atLayoutCharged, after interface is
	 * created:
	 */
	private void changeSizes() {
		// Let's see the width of the one is turn ImageView:
		int w = ivTurns[0].getWidth();
		// The original is turn images dimension is 110 X 86:
		int h = w * 87 / 110;

		// Resize the four images for is turn status:
		isTurnWhite = new BitmapDrawable(
				mContext.getResources(),
				GUITools.resizeImage(mContext, "is_turn_" + tempAColor[1], w, h));
		isTurnBlack = new BitmapDrawable(
				mContext.getResources(),
				GUITools.resizeImage(mContext, "is_turn_" + tempAColor[2], w, h));
		isNotTurnWhite = new BitmapDrawable(mContext.getResources(),
				GUITools.resizeImage(mContext, "is_not_turn_" + tempAColor[1],
						w, h));
		isNotTurnBlack = new BitmapDrawable(mContext.getResources(),
				GUITools.resizeImage(mContext, "is_not_turn_" + tempAColor[2],
						w, h));

		// We need to resize the ImageView itself using LayoutParams:
		for (int i = 0; i < ivTurns.length; i++) {
			ivTurns[i].getLayoutParams().height = h;
		} // end for resize ivTurns.
	} // end changeSizes() method.

	// The method called from outside to resize ivTurns image views:
	public void resizeIvTurns() {
		// We need to resize the is turn screens to be at original ratio:
		// Let's postpone a little:
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				changeSizes();
				// Set it now with new dimensions and content description:
				// Charge the message for nobody is at turn:
				String message = mContext
						.getString(R.string.msg_nobody_must_move);
				setTurnVisually(0, message);
			}
		}, 700);
	} // end resizeIvTurns() method.

} // end Score class.
