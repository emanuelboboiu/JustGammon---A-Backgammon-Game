package ro.pontes.justbackgammon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Statistics {
	private final Context mContext;
	private Resources res;
	private DBAdapter mDbHelper;

	// The constructor:
	public Statistics(Context context) {
		this.mContext = context;
		this.res = this.mContext.getResources();
		// Start things for our database:
		mDbHelper = new DBAdapter(this.mContext);
		mDbHelper.createDatabase();
		mDbHelper.open();
	} // end constructor.

	// Methods to show statistics in a alert:
	@SuppressLint("InflateParams")
	public void showStats() {
		// Inflate the statistics layout to add information:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View statsView = inflater.inflate(R.layout.statistics_layout, null);

		// First of all we take the llStatistics LinearLayout:
		LinearLayout ll = (LinearLayout) statsView
				.findViewById(R.id.llStatistics);
		// A LayoutParam to add TVs with match parent:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// 3 variables we use repetitive:
		TextView tv;
		CharSequence tvSeq;
		String tvText;

		// First, the introductory message is only changed:
		tv = (TextView) statsView.findViewById(R.id.tvStatsIntro);
		tvText = String.format(mContext.getString(R.string.stats_intro),
				getFirstGameDate());
		tvSeq = MyHtml.fromHtml(tvText);
		tv.setText(tvSeq);

		// Now add new text views in linear layout if games where played:
		int totalGames = getNumberOfPlayedGames();
		if (totalGames > 0) { // there are games played:
			// Number of games:
			int finishedGames = getCount("*", "abandoned", "0");
			int abandonedGames = totalGames - finishedGames;
			tvText = String.format(
					mContext.getString(R.string.stats_games_played),
					totalGames, finishedGames, abandonedGames);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			// Number of games by type:
			int totalLocalGames = getCount("*", "type", "1");
			int totalAIGames = getCount("*", "type", "2");
			int totalTwoBotsGames = getCount("*", "type", "0");
			tvText = String.format(
					mContext.getString(R.string.stats_games_by_type),
					totalLocalGames, totalAIGames, totalTwoBotsGames);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			// Games per difficulty level:
			int level1 = getCount2Conditions("*", "type", "2", "level", "1");
			int level2 = getCount2Conditions("*", "type", "2", "level", "2");
			// int level3 = getCount2Conditions("*", "type", "2", "level", "3");
			tvText = String.format(
					mContext.getString(R.string.stats_games_by_level), level1,
					level2);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			// Win types:
			int winType1 = getCount("*", "points", "1");
			int winType2 = getCount("*", "points", "2");
			int winType3 = getCount("*", "points", "3");
			int winType4 = getCount("*", "points", "4");
			// Also variables for percentages:
			double winType1Percentage = GUITools.round(
					((double) winType1 * 100 / (double) totalGames), 2);
			double winType2Percentage = GUITools.round(
					((double) winType2 * 100 / (double) totalGames), 2);
			double winType3Percentage = GUITools.round(
					((double) winType3 * 100 / (double) totalGames), 2);
			double winType4Percentage = GUITools.round(
					((double) winType4 * 100 / (double) totalGames), 2);
			tvText = String.format(
					mContext.getString(R.string.stats_games_by_win_type),
					winType1, winType1Percentage, winType2, winType2Percentage,
					winType3, winType3Percentage, winType4, winType4Percentage);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			/*
			 * Number of won and lost games in AI games, only if games were
			 * played:
			 */
			if (totalAIGames > 0) {
				int wonGames = getCount2Conditions("*", "type", "2", "youWon",
						"1");
				int lostGames = getCount2Conditions("*", "type", "2", "youWon",
						"0");
				double wonGamesPercentage = GUITools.round(
						((double) wonGames * 100 / (double) totalAIGames), 2);
				double lostGamesPercentage = GUITools.round(
						((double) lostGames * 100 / (double) totalAIGames), 2);
				tvText = String.format(mContext
						.getString(R.string.stats_won_and_lost_games_in_ai),
						wonGames, wonGamesPercentage, lostGames,
						lostGamesPercentage);
				tvSeq = MyHtml.fromHtml(tvText);
				tv = createTextView();
				tv.setText(tvSeq);
				ll.addView(tv, lp);
			} // end if AI games were played.

			/*
			 * Number of white or black won games in local games, only if games
			 * were played:
			 */
			if (totalLocalGames > 0) {
				int wonWhiteGames = getCount2Conditions("*", "type", "1",
						"colorWinner", "1");
				int wonBlackGames = totalLocalGames - wonWhiteGames;
				double wonWhitePercentage = GUITools
						.round(((double) wonWhiteGames * 100 / (double) totalLocalGames),
								2);
				double wonBlackPercentage = GUITools
						.round(((double) wonBlackGames * 100 / (double) totalLocalGames),
								2);

				tvText = String.format(mContext
						.getString(R.string.stats_won_by_color_games_in_local),
						wonWhiteGames, wonWhitePercentage, wonBlackGames,
						wonBlackPercentage);
				tvSeq = MyHtml.fromHtml(tvText);
				tv = createTextView();
				tv.setText(tvSeq);
				ll.addView(tv, lp);
			} // end if local games were played.

			// Determine now total throws:
			int diceThrows = getSum("diceRolls");
			tvText = String.format(
					mContext.getString(R.string.stats_dice_rolls), diceThrows);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);
			// end dice total throws.

			// Determine now the dice value average:
			float diceAverage = getAverage("diceAverage");
			tvText = String.format(
					mContext.getString(R.string.stats_dice_average),
					diceAverage);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);
			// end dice value average.

			// Determine now the dice doubles average:
			float doublesAverage = getAverage("diceDoubles", "abandoned", "0");
			tvText = String.format(
					mContext.getString(R.string.stats_doubles_average),
					doublesAverage);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			// Determine now the dice value average:
			float capturesAverage = getAverage("captures", "abandoned", "0");
			tvText = String.format(
					mContext.getString(R.string.stats_captures_average),
					capturesAverage);
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);
			// end captures average.

			// Determine now the total duration of all games:
			tvText = String.format(
					mContext.getString(R.string.stats_total_duration),
					getTotalDurationAsString());
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

			// Determine now the average duration of a game:
			tvText = String.format(
					mContext.getString(R.string.stats_average_duration),
					getAverageDurationInMinutesAndSeconds());
			tvSeq = MyHtml.fromHtml(tvText);
			tv = createTextView();
			tv.setText(tvSeq);
			ll.addView(tv, lp);

		} // end if there are games played.
		else { // no games where played:
			tv = createTextView();
			tv.setText(mContext
					.getString(R.string.stats_no_stats_available_yet));
			ll.addView(tv, lp);
		} // end if no games where played.

		// The alert dialog is created and shown:
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(mContext.getString(R.string.statistics_title));
		alertDialog.setView(statsView);

		alertDialog.setPositiveButton(
				mContext.getString(R.string.bt_close_statistics),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				});

		alertDialog.create();
		alertDialog.show();
	}// end showStats() method.

	// A method to create a text view for items in this alert:
	private TextView createTextView() {
		TextView tv = new TextView(mContext, null, R.style.textInSettingsStyle);
		return tv;
	} // end createTextView() method.

	// A method to take the first game date:
	private String getFirstGameDate() {
		String sql = "SELECT date FROM statistics ORDER BY date ASC LIMIT 1;";
		Cursor cursor = mDbHelper.queryData(sql);
		String temp = mContext.getString(R.string.stats_no_records_found);
		// If there is a record:
		if (cursor.getCount() > 0) {
			int firstTimeStamp = cursor.getInt(0);
			temp = GUITools.timeStampToString(mContext, firstTimeStamp);
		} // end if there is at least a record.
		return temp;
	} // end getFirstGameDate() method.

	// A method to take number of finished games:
	private int getNumberOfPlayedGames() {
		String sql = "SELECT COUNT(*) FROM statistics;";
		Cursor cursor = mDbHelper.queryData(sql);
		int nrTotal = cursor.getInt(0);
		return nrTotal;
	} // end getNumberOfPlayedGames() method.

	/*
	 * A method with 3 parameters to take count for a column depending of a
	 * condition:
	 */
	private int getCount(String column, String where, String value) {
		String sql = "SELECT COUNT(" + column + ") FROM statistics WHERE "
				+ where + "='" + value + "';";
		Cursor cursor = mDbHelper.queryData(sql);
		int nr = cursor.getInt(0);
		return nr;
	} // end getCount() method.

	/*
	 * A method with 5 parameters to take count for a column depending of two
	 * conditions:
	 */
	private int getCount2Conditions(String column, String where, String value,
			String where2, String value2) {
		String sql = "SELECT COUNT(" + column + ") FROM statistics WHERE "
				+ where + "='" + value + "' AND " + where2 + "='" + value2
				+ "';";
		Cursor cursor = mDbHelper.queryData(sql);
		int nr = cursor.getInt(0);
		return nr;
	} // end getCount2Conditions() method.

	// A method with one parameter to take sum for a column:
	private int getSum(String column) {
		String sql = "SELECT SUM(" + column + ") FROM statistics;";
		Cursor cursor = mDbHelper.queryData(sql);
		int nr = cursor.getInt(0);
		return nr;
	} // end getSum() method.

	/*
	 * A method with 3 parameters to take sum for a column depending of a
	 * condition:
	 */
	@SuppressWarnings("unused")
	private int getSum(String column, String where, String value) {
		String sql = "SELECT SUM(" + column + ") FROM statistics WHERE "
				+ where + "='" + value + "';";
		Cursor cursor = mDbHelper.queryData(sql);
		int sum = cursor.getInt(0);
		return sum;
	} // end getSum() method.

	/*
	 * A method with 3 parameters to take average for a column depending of a
	 * condition:
	 */
	private float getAverage(String column, String where, String value) {
		String sql = "SELECT AVG(" + column + ") FROM statistics WHERE "
				+ where + "='" + value + "';";
		Cursor cursor = mDbHelper.queryData(sql);
		float avg = cursor.getFloat(0);
		return avg;
	} // end getAverage() method.

	/*
	 * A method with one parameters to take average for a column
	 */
	private float getAverage(String column) {
		String sql = "SELECT AVG(" + column + ") FROM statistics;";
		Cursor cursor = mDbHelper.queryData(sql);
		float avg = cursor.getFloat(0);
		return avg;
	} // end getCount() method.

	// A method to get the duration of playing:
	private String getTotalDurationAsString() {
		// Determine total seconds in database:
		String sql = "SELECT SUM(duration) FROM statistics;";
		Cursor cursor = mDbHelper.queryData(sql);
		int totalSeconds = cursor.getInt(0) / 1000;

		int days = totalSeconds / (60 * 60 * 24);
		int rest = totalSeconds % (60 * 60 * 24);
		int hours = rest / (60 * 60);
		rest = rest % (60 * 60);
		int minutes = rest / 60;
		rest = rest % 60;
		int seconds = rest;
		String temp = String.format(mContext
				.getString(R.string.stats_duration_as_string), res
				.getQuantityString(R.plurals.plural_days, days, days), res
				.getQuantityString(R.plurals.plural_hours, hours, hours), res
				.getQuantityString(R.plurals.plural_minutes, minutes, minutes),
				res.getQuantityString(R.plurals.plural_seconds, seconds,
						seconds));

		return temp;
	} // end getTotalDurationAsString() method.

	// A method to get the average duration in seconds:
	private String getAverageDurationInMinutesAndSeconds() {
		String sql = "SELECT AVG(duration) FROM statistics WHERE abandoned=0;";
		Cursor cursor = mDbHelper.queryData(sql);
		double avg = cursor.getFloat(0) / 1000;
		avg = GUITools.round(avg, 0);
		int minutes = (int) avg / 60;
		int seconds = (int) avg % 60;
		String minAndSec = String.format(
				mContext.getString(R.string.stats_minutes_and_seconds),
				String.format("%02d", minutes), String.format("%02d", seconds));
		return minAndSec;
	} // end getAverageDurationInSeconds() method.

	// Starting here post statistics:
	// A method to save statistics locally:
	public void postFinishedTestLocally(String googleId, String nickname,
			String nickname2, int gameType, int level, int points,
			int colorWinner, int youWon, int duration, int durationWhite,
			int[] diceStats, int[] nrOfCaptures, int androidTV, int abandoned,
			int wasPostedOnline) {
		// Determine local time:
		long curTime = GUITools.getTimeInSeconds();
		// Determine number of doubles:
		int diceDoubles = diceStats[4] + diceStats[5];
		// Determine total rolls:

		// Determine the dice average:
		int diceRolls = diceStats[0] + diceStats[1];
		float diceAverage = (float) (diceStats[2] + diceStats[3])
				/ (float) (diceRolls * 2 + (diceDoubles * 2));
		// Determine the total captures:
		int captures = nrOfCaptures[0] + nrOfCaptures[1];

		// We try to insert statistics on-line:
		// If there is an Internet connection:
		if (GUITools.isNetworkAvailable(mContext)) {
			wasPostedOnline = 1;
			postFinishedTestOnline(googleId, nickname, nickname2, gameType,
					level, points, colorWinner, youWon, duration,
					durationWhite, diceRolls, diceDoubles, diceAverage,
					captures, androidTV, abandoned, curTime);
		} // end if Internet is available.

		// We format the SQL string to insert a finished game locally:
		String sql = "INSERT INTO statistics (google_id, nickname, nickname2, type, level, points, colorWinner, youWon, duration, durationWhite, diceRolls, diceAverage, diceDoubles, captures, android_tv, abandoned, date, posted) VALUES ('"
				+ googleId
				+ "', '"
				+ nickname
				+ "', '"
				+ nickname2
				+ "', '"
				+ gameType
				+ "', '"
				+ level
				+ "', '"
				+ points
				+ "', '"
				+ colorWinner
				+ "', '"
				+ youWon
				+ "', '"
				+ duration
				+ "', '"
				+ durationWhite
				+ "', '"
				+ diceRolls
				+ "', '"
				+ diceAverage
				+ "', '"
				+ diceDoubles
				+ "', '"
				+ captures
				+ "', '"
				+ androidTV
				+ "', '"
				+ abandoned
				+ "', '"
				+ curTime
				+ "', '"
				+ wasPostedOnline + "');";
		mDbHelper.insertData(sql);
	} // end postFinishedGameLocaly() method.

	// A method to save statistics on-line:
	public void postFinishedTestOnline(String googleId, String nickname,
			String nickname2, int gameType, int level, int points,
			int colorWinner, int youWon, int duration, int durationWhite,
			int diceRolls, int diceDoubles, float diceAverage, int captures,
			int androidTV, int abandoned, long dateFinished) {
		// Determine the API and language:
		int curAPI = Build.VERSION.SDK_INT;
		String curLang = mContext.getString(R.string.cur_lang);

		int android_tv = MainActivity.isTV ? 1 : 0;

		// Detect if screen reader, 1 yes, 2 not:
		int screenReader = MainActivity.isAccessibility ? 1 : 2;

		// Detect if theme is 2, accessible checkers:
		Settings set = new Settings(mContext);
		boolean isAccessibleTheme = set.getBooleanSettings("isAccessibleTheme");
		int theme = isAccessibleTheme ? 2 : 1;

		String myUrl = "http://www.justgammon.com/stats/insert_stats.php?google_id="
				+ MainActivity.myAccountName
				+ "&nickname="
				+ nickname
				+ "&nickname2="
				+ nickname2
				+ "&gameType="
				+ gameType
				+ "&level="
				+ level
				+ "&points="
				+ points
				+ "&colorWinner="
				+ colorWinner
				+ "&youWon="
				+ youWon
				+ "&duration="
				+ duration
				+ "&durationWhite="
				+ durationWhite
				+ "&diceRolls="
				+ diceRolls
				+ "&diceAverage="
				+ diceAverage
				+ "&diceDoubles="
				+ diceDoubles
				+ "&captures="
				+ captures
				+ "&android_tv="
				+ android_tv
				+ "&abandoned="
				+ abandoned
				+ "&screenReader="
				+ screenReader
				+ "&theme="
				+ theme
				+ "&curAPI="
				+ curAPI
				+ "&language="
				+ curLang + "&dateFinished=" + dateFinished;

		// Post effectively:
		new GetWebData().execute(myUrl);
	} // end postFinishedTestOnline() method.

	// A method to post test online from local DB:
	public void postOnlineNotPostedFinishedTests() {
		if (GUITools.isNetworkAvailable(mContext)) {
			// If there are test in local DB not posted:
			String sql = "SELECT * FROM statistics WHERE posted=0;";
			Cursor cursor = mDbHelper.queryData(sql);
			int nrNotPosted = cursor.getCount();
			// GUITools.alert(mContext, "ABC", "" + nrNotPosted);
			if (nrNotPosted > 0) {
				/*
				 * We try to post maximum 5 tests at a time if they are
				 * available. Only if an Internet connection is available:
				 */
				if (GUITools.isNetworkAvailable(mContext)) {
					cursor.moveToFirst();
					for (int i = 0; i < 5 && i < nrNotPosted; i++) {
						// We create the variables for each value from cursor:
						int id = cursor.getInt(0);

						String googleId = cursor.getString(1);
						String nickname = cursor.getString(2);
						String nickname2 = cursor.getString(3);
						int gameType = cursor.getInt(4);
						int level = cursor.getInt(5);
						int points = cursor.getInt(6);
						int colorWinner = cursor.getInt(7);
						int youWon = cursor.getInt(8);
						int duration = cursor.getInt(9);
						int durationWhite = cursor.getInt(10);
						int diceRolls = cursor.getInt(11);
						float diceAverage = cursor.getFloat(12);
						int diceDoubles = cursor.getInt(13);
						int captures = cursor.getInt(14);
						int androidTV = cursor.getInt(15);
						int abandoned = cursor.getInt(16);
						int dateFinished = cursor.getInt(17);

						// Make current records a posted on-line in local DB:
						sql = "UPDATE statistics SET posted=1 WHERE id=" + id
								+ ";";
						mDbHelper.updateData(sql);

						// Post it effectively:
						postFinishedTestOnline(googleId, nickname, nickname2,
								gameType, level, points, colorWinner, youWon,
								duration, durationWhite, diceRolls,
								diceDoubles, diceAverage, captures, androidTV,
								abandoned, dateFinished);

						cursor.moveToNext();
					} // end for 5 posted tests.
				} // end if there is an Internet connection available.
			} // end if there are not posted tests in local DB.
		} // end if there is a network available.
	} // end postOnlineUnpostedFinishedTests() method.

	// This is a subclass:
	private class GetWebData extends AsyncTask<String, String, String> {

		// execute before task:
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		// Execute task
		String urlText = "";

		@Override
		protected String doInBackground(String... strings) {
			StringBuilder content = new StringBuilder();
			urlText = strings[0];
			try {
				// Create a URL object:
				URL url = new URL(urlText);
				// Create a URLConnection object:
				URLConnection urlConnection = url.openConnection();
				// Wrap the URLConnection in a BufferedReader:
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(urlConnection.getInputStream()));
				String line;
				// Read from the URLConnection via the BufferedReader:
				while ((line = bufferedReader.readLine()) != null) {
					content.append(line);
				}
				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return content.toString();
		} // end doInBackground() method.

		// Execute after task with the task result as string:
		@Override
		protected void onPostExecute(String s) {
			// Do nothing yet.
		} // end postExecute() method.
	} // end subclass.

} // end Statistics class.
