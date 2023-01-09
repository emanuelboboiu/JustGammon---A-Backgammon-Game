package ro.pontes.justbackgammon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

// Started on Sunday 16 September 2018, 10:45, by Manu.

public class LastGames {

	private final Context mContext;
	DBAdapter mDbHelper = null;
	public int limit = 10;
	public int offset = 0;
	public int total = 0;
	private View lastGamesView = null;

	public LastGames(Context mContext) {
		this.mContext = mContext;
		// Start things for our database:
		mDbHelper = new DBAdapter(this.mContext);
		mDbHelper.createDatabase();
		mDbHelper.open();

		// Get the total number of games in history:
		String sql = "SELECT COUNT(*) FROM statistics;";
		Cursor cur = mDbHelper.queryData(sql);
		total = cur.getInt(0);
		cur = null;
	} // end constructor.

	// A method to show history in an alert:
	@SuppressLint("InflateParams")
	public void showLastGames() {
		// Inflate the XML layout for last games alert:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		lastGamesView = inflater.inflate(R.layout.last_games_dialog, null);

		fillTheLayoutWithPlayedGames();

		// Create now the alert:
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(mContext
				.getString(R.string.history_last_games_title));
		alertDialog.setView(lastGamesView);
		alertDialog.setPositiveButton(
				mContext.getString(R.string.history_bt_close_last_games), null);
		AlertDialog alert = alertDialog.create();
		alert.show();
	} // end showLastGames() method.

	private void fillTheLayoutWithPlayedGames() {
		// Take a reference to the linear layout to add text view into it:
		LinearLayout ll = lastGamesView.findViewById(R.id.llLastGames);
		ll.removeAllViews();

		// A LayoutParams to add text views into the llCB:
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		// A LayoutParams to add llButons at the right of ll2:
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp2.gravity = Gravity.RIGHT;

		// Connect to the database to retrieve last games:
		String sql = "SELECT id, type, youWon, abandoned, date, colorWinner FROM statistics ORDER BY date DESC LIMIT "
				+ limit + " OFFSET " + offset + ";";
		Cursor cursor = mDbHelper.queryData(sql);
		int count = cursor.getCount();

		// If games are saved, show them:
		if (count > 0) {
			cursor.moveToFirst();

			// Create some arrays from strings:
			Resources res = mContext.getResources();
			String[] arrTypeFinished = res
					.getStringArray(R.array.array_type_of_finish);
			String[] arrTypeGame = res
					.getStringArray(R.array.array_type_of_game);
			String[] whoWon = res.getStringArray(R.array.array_who_won);
			String[] whoWonColor = res
					.getStringArray(R.array.array_who_won_color);
			String tvGame = res.getString(R.string.history_a_game_in_list);

			// / Go in a while for each game played:
			do {
				LinearLayout ll2 = new LinearLayout(mContext);
				ll2.setOrientation(LinearLayout.VERTICAL);

				// Create the text view for a game:
				TextView tv = createTextView();
				final int id = cursor.getInt(0);
				int gameType = cursor.getInt(1);
				int youWon = cursor.getInt(2);
				int abandoned = cursor.getInt(3);
				int date = cursor.getInt(4);
				int colorWinner = cursor.getInt(5);
				String winner = whoWon[youWon];
				if (gameType == 0 || gameType == 1) { // local game:
					winner = whoWonColor[colorWinner];
				} // end if local game.
				Spanned tvText = MyHtml.fromHtml(String.format(tvGame,
						arrTypeFinished[abandoned], arrTypeGame[gameType],
						winner, GUITools.timeStampToString(mContext, date)));
				tv.setText(tvText);
				ll.addView(tv, lp);
				// End add text view for a game.

				// Determine if buttons are or not enabled, if record exists:
				final boolean enabled = reccordExistsInDB(id);
				// Add also the buttons in another linear layout:
				LinearLayout llButtons = new LinearLayout(mContext);
				llButtons.setOrientation(LinearLayout.HORIZONTAL);
				// Create the history button:
				Button btHistory = new Button(mContext);
				btHistory.setText(mContext
						.getString(R.string.history_show_history));
				btHistory.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
				btHistory.setEnabled(enabled);
				// Add the listener:
				btHistory.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						showHistoryOrStatistics(id, 0); // 0 means history.
					}
				});
				// End add listener for tap on button history.
				// Add now the button:
				llButtons.addView(btHistory);
				// Create the statistics button:
				Button btStats = new Button(mContext);
				btStats.setText(mContext
						.getString(R.string.history_show_statistics));
				btStats.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
				btStats.setEnabled(enabled);
				// Add the listener:
				btStats.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						showHistoryOrStatistics(id, 1); // 1 means statistics.
					}
				});
				// End add listener for tap on button statistics.
				// Add now the button:
				llButtons.addView(btStats);

				ll2.addView(llButtons, lp2);
				ll.addView(ll2, lp);
			} while (cursor.moveToNext());

			//
			// Add now the button for next games:
			Button btNext = new Button(mContext);
			btNext.setTextSize(MainActivity.textSize);
			btNext.setText(res.getString(R.string.history_next_games));
			// If there are more games available:
			if ((offset + limit) < total) {
				offset = offset + limit;
			} // end if there are more.
			else { // no other games:
				btNext.setEnabled(false);
			} // end if there are no more games.
			btNext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					fillTheLayoutWithPlayedGames();
				}
			});
			// End add listener for tap on button statistics.
			// Add now the button:
			ll.addView(btNext, lp2);
		} else { // no saved games:
			TextView tv = createTextView();
			tv.setText(mContext.getString(R.string.history_games_not_found));
			ll.addView(tv, lp);
		} // end if no games saved.
	} // end fillTheLayoutWithPlayedGames() method.

	// A method to show history or statistics for a certain game:
	private void showHistoryOrStatistics(int id, int type) {
		// If type is 0 it means history, 1 means statistics.
		// Create the SQL string:
		String sql = "SELECT historyText, statisticsText FROM history WHERE id="
				+ id + ";";
		Cursor cursor = mDbHelper.queryData(sql);
		String title = "";
		if (type == 0) {
			title = mContext.getString(R.string.game_history_title);
		} else if (type == 1) { // statistics:
			title = mContext.getString(R.string.sg_dialog_title);
		} // end if title for statistics.
		GUITools.alertWithHtml(mContext, title, cursor.getString(type),
				mContext.getString(R.string.bt_close));
	} // end showHistoryOrStatistics() method.

	private TextView createTextView() {
		TextView tv = new TextView(mContext);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.textSize);
		// tv.setFocusable(true);
		// tv.setClickable(true);
		return tv;
	} // end createTextView() method.

	private boolean reccordExistsInDB(int id) {
		boolean exists = false;
		String sql = "SELECT COUNT(id) AS total FROM history WHERE id=" + id
				+ ";";
		Cursor cur = mDbHelper.queryData(sql);
		int count = cur.getInt(0);
		cur.close();
		if (count > 0) {
			exists = true;
		}
		return exists;
	} // endReccordExistsInDB() method.

} // end LastGames class.
