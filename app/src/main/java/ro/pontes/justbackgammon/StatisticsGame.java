package ro.pontes.justbackgammon;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class StatisticsGame {

	private final Context mContext;
	private Resources res;
	private int gameType = 0;
	private int[] aPulls, aColors;

	// Make an array list of strings to create after text views:
	ArrayList<String> arr = new ArrayList<String>();
	private boolean areStatsAvailable = false;
	private int whoseTurn = 0;
	private int cdNotationType = 1;

	// The constructor:
	public StatisticsGame(Context context, int gameType, int[] aPulls,
			int[] aColors, int cdNotationType) {
		this.mContext = context;
		this.gameType = gameType;
		this.res = this.mContext.getResources();
		this.aPulls = aPulls;
		this.aColors = aColors;
		this.cdNotationType = cdNotationType;
	} // end constructor.

	public void getGameStatistics(int isTurn, int availableMoves,
			String timeElapsed, int timer, int timerWhite, int inWhite,
			int inBlack, int whiteBar, int blackBar, int whiteRemoved,
			int blackRemoved, int[] diceStats, int[] nrOfCaptures) {

		// Fill the array list only if dice were rolled:
		if (!(diceStats[0] == 0 && diceStats[1] == 0)) {

			// Show necessary dice points to finish:
			int whitePointsNeeded = getNecessaryDicePointsToFinish(1, whiteBar,
					blackBar);
			int blackPointsNeeded = getNecessaryDicePointsToFinish(2, whiteBar,
					blackBar);
			arr.add(String.format(
					mContext.getString(R.string.sg_dice_points_to_finish),
					whitePointsNeeded, blackPointsNeeded));

			// Show probability:
			float whiteChance = 1.0f - ((float) whitePointsNeeded / (float) (whitePointsNeeded + blackPointsNeeded));
			whiteChance = whiteChance * 100;
			int wChance = Math.round(whiteChance);
			int bChance = 100 - wChance;
			arr.add(String.format(
					mContext.getString(R.string.sg_chances_of_winning),
					wChance, bChance));

			// Show efficiency:
			int whiteEfficiency = diceStats[2] + whitePointsNeeded - 167;
			whiteEfficiency = whiteEfficiency * 100 / 167;
			whiteEfficiency = 100 - whiteEfficiency;
			int blackEfficiency = diceStats[3] + blackPointsNeeded - 167;
			blackEfficiency = blackEfficiency * 100 / 167;
			blackEfficiency = 100 - blackEfficiency;
			arr.add(String.format(mContext.getString(R.string.sg_efficiency),
					whiteEfficiency, blackEfficiency));

			// Show available moves for current turn:
			arr.add(res.getQuantityString(R.plurals.plural_available_moves,
					availableMoves, availableMoves));

			// Show vulnerable checkers:
			arr.add(determineVulnerableCheckers());

			// Show where are vulnerable checkers:
			String whites = getAllVulnerableCheckers(1, cdNotationType);
			String blacks = getAllVulnerableCheckers(2, cdNotationType);
			arr.add(String.format(
					res.getString(R.string.msg_say_vulnerable_checkers),
					whites, blacks));

			// Show checkers in inner boards:
			arr.add(String.format(
					mContext.getString(R.string.sg_checkers_in_inner_boards),
					inWhite, inBlack));

			// Show checkers on the bar:
			arr.add(String.format(
					mContext.getString(R.string.sg_checkers_on_the_bars),
					whiteBar, blackBar));

			// Show checkers removed:
			arr.add(String.format(
					mContext.getString(R.string.sg_checkers_removed),
					whiteRemoved, blackRemoved));

			// Number of captures done this game:
			arr.add(String.format(
					mContext.getString(R.string.sg_number_of_captures),
					nrOfCaptures[0], nrOfCaptures[1]));

			// Some dice statistics:
			/*
			 * In diceStats array, the order of values is: rolls, totals,
			 * doubles, each for white and black respectively:
			 */
			// Number of throws:
			arr.add(String.format(mContext.getString(R.string.sg_dice_throws),
					diceStats[0], diceStats[1]));

			// Total dice points:
			arr.add(String.format(
					mContext.getString(R.string.sg_dice_total_points),
					diceStats[2], diceStats[3]));

			// Dice average:
			double wad = 0.0f;
			double bad = 0.0f;
			if (diceStats[0] > 0)
				wad = GUITools
						.round(((float) diceStats[2] / ((float) (diceStats[0] * 2) + (float) (diceStats[4] * 2))),
								2);
			if (diceStats[1] > 0)
				bad = GUITools
						.round(((float) diceStats[3] / ((float) (diceStats[1] * 2) + (float) (diceStats[5] * 2))),
								2);
			arr.add(String.format(mContext.getString(R.string.sg_dice_average),
					wad, bad));

			// Number of doubles:
			arr.add(String.format(mContext.getString(R.string.sg_dice_doubles),
					diceStats[4], diceStats[5]));
			// end dice statistics zone.

			// Show time elapsed for current game:
			arr.add(determineTimeElapsedInfo(timeElapsed, timer, timerWhite));

			// Set also some object field for show statistics method:
			areStatsAvailable = true;
		} // end if are stats to be shown in alert.
		whoseTurn = isTurn;
	}// end getGameStatistics() method.

	// The method to show the statistics alert:
	@SuppressLint("InflateParams")
	public void showStatisticsAlert() {
		// Inflate the stub layout for game statistics:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View statisticsView = inflater.inflate(R.layout.statistics_game_dialog,
				null);

		// Add now items in that linear layout:
		LinearLayout ll = (LinearLayout) statisticsView
				.findViewById(R.id.llForContent);

		// A LayoutParams for text views dimensions:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// 3 variables we use repetitive:
		TextView tv;
		CharSequence tvSeq;
		String tvText;

		// Start to fill with text views:
		// Show first whose turn is:
		if (whoseTurn > 0) {
			tvText = String.format(
					(gameType == 1 ? mContext
							.getString(R.string.msg_it_is_turn_in_local_game)
							: mContext.getString(R.string.msg_it_is_turn)),
					GUITools.aOpponents[whoseTurn]);
		} else { // nobody's turn:
			tvText = mContext.getString(R.string.msg_nobody_must_move);
		} // end if nobody's turn.
		tvSeq = MyHtml.fromHtml(tvText);
		tv = createTextView();
		tv.setText(tvSeq);
		ll.addView(tv, lp);

		// Now show other stats:
		if (areStatsAvailable) {

			// Create now the text views in a loop:
			for (int i = 0; i < arr.size(); i++) {
				tvSeq = MyHtml.fromHtml(arr.get(i));
				tv = createTextView();
				tv.setText(tvSeq);
				ll.addView(tv, lp);
			} // end for to create TVs for each information.
		} // end if statistics are available, game is started.
		else {
			tvText = mContext.getString(R.string.sg_no_stats_available);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);
		} // end if no statistics available, game not started.

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.sg_dialog_title);
		builder.setView(statisticsView);
		builder.setPositiveButton(
				mContext.getString(R.string.sg_bt_close_statistics),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		builder.create();
		builder.show();
	} // end showStatisticsAlert() method.

	// A method which returns the array list of game statistics:
	public ArrayList<String> getArrStatistics() {
		return arr;
	} // end getArrStatistics() method.

	private TextView createTextView() {
		TextView tv = new TextView(mContext);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
		// tv.setFocusable(true);
		// tv.setClickable(true);
		return tv;
	} // end createTextView() method.

	// A method which calculates the points to finish:
	public int getNecessaryDicePointsToFinish(int isTurn, int whiteBar,
			int blackBar) {
		int points = 0;
		if (isTurn == 1) { // white's turn:
			// a for to check all the points on the board.
			for (int i = 23; i >= 0; i--) {
				// an if to count only if there is a white checker there:
				if (aColors[i] == 1) {
					points += (aPulls[i] * (i + 1));
				} // end if it's a white checker there.
			} // end the for for all points.
				// Add also as 24 points the pulls on the bar:
			points += (whiteBar * 24);
		} else { // black's turn.
			// A for to check all the points for black pulls:
			for (int i = 0; i <= 23; i++) {
				// an if to count only if there is a black checker there:
				if (aColors[i] == 2) {
					points += (aPulls[i] * (24 - i));
				} // end if it's a black checker there.
			} // end the for for black.
			points += (blackBar * 24);
		} // end black's turn.
		return points;
	} // end getNecessaryDicePointsToFinish() method.

	// A method to determine time elapsed for both in percentage:
	public String determineTimeElapsedInfo(String timeElapsed, int tTotal,
			int tWhite) {
		String msgTimeElapsed = String.format(
				res.getString(R.string.sg_time_elapsed), timeElapsed);
		// calculate the percentages for white and black:
		int whitePercentage = tWhite * 100 / tTotal;
		int blackPercentage = 100 - whitePercentage;
		String msg = String.format(
				res.getString(R.string.sg_time_elapsed_with_percentages),
				msgTimeElapsed, whitePercentage, blackPercentage);
		return msg;
	} // end determineTimeElapsed() method.

	// A method to determine the vulnerable checkers:
	public String determineVulnerableCheckers() {
		String msg = "";
		int whiteVulnerable = 0;
		int blackVulnerable = 0;
		for (int i = 0; i < aPulls.length; i++) {
			if (aPulls[i] == 1) {
				if (aColors[i] == 1)
					whiteVulnerable++;
				if (aColors[i] == 2)
					blackVulnerable++;
			} // end if there is only one checker.
		} // end for.
		msg = String.format(res.getString(R.string.sg_checkers_vulnerable),
				whiteVulnerable, blackVulnerable);
		return msg;
	} // end determineVulnerableCheckers() method.

	// A method which returns a string with vulnerable checkers:
	public String getAllVulnerableCheckers(int curColor, int cdNotation) {
		StringTools st = new StringTools(mContext);
		String msg = "";
		for (int i = 0; i < aPulls.length; i++) {
			if (aPulls[i] == 1) {
				if (aColors[i] == curColor) {
					msg += st.determinePositionName(i, cdNotation) + ", ";
				} // end if it is specified colour.
			} // end if there is only one checker.
		} // end for.
		if (msg.equals("")) {
			msg = mContext
					.getString(R.string.msg_nonexistent_vulnerable_checkers);
		} else { // cut the comma:
			msg = msg.substring(0, msg.length() - 2);
		} // end if cut the comma.
		st = null;
		return msg;
	} // end getAllVulnerableCheckers() method.

} // end StatisticsGame class.
