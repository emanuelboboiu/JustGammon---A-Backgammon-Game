package ro.pontes.justbackgammon;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class History {

	// An ArrayList with strings:
	private ArrayList<Spanned> mHistory;

	// We need also a context at least for show method:
	private Context mContext;
	private Resources res = null;

	// The constructor:
	public History(Context context) {
		this.mContext = context;
		this.mHistory = new ArrayList<Spanned>(100);
		res = mContext.getResources();
	} // end constructor.

	public void add(Spanned text) {
		mHistory.add(text);
	} // end add() method.

	// A method to clear the history:
	public void clear() {
		mHistory.clear();
	} // end clear() method.

	// A method to show history in an alert:
	@SuppressLint("InflateParams")
	public void show(int lastEntries) {
		// Reduce the history to lastEntries entries:
		// If lastEntries is 0, it means all messages:
		if (lastEntries > 0) {
			int limit = mHistory.size() - lastEntries;
			for (int i = 0; i < limit; i++) {
				mHistory.remove(0);
			} // end for reduce the history size().
		} // end if the history must be limited to a number of messages.

		// Inflate the XML layout for history alert:
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View historyView = inflater.inflate(R.layout.history_dialog, null);

		// Take a reference to the linear layout to add text view into it:
		LinearLayout ll = historyView.findViewById(R.id.llHistory);

		int textSize = MainActivity.textSize;
		TextView tv;

		/*
		 * Add first the number of messages text view at the beginning of the
		 * history:
		 */
		String status = String.format(
				res.getString(R.string.history_number_of_messages),
				getNumberOfMessagesAsString(mHistory.size()));
		Spanned msgNumberOfMessages = MyHtml.fromHtml(status);
		tv = new TextView(mContext);
		tv.setClickable(true);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
		tv.setText(msgNumberOfMessages);
		ll.addView(tv);

		// A for for each message in the history as TextView:
		for (int i = mHistory.size() - 1; i >= 0; i--) {
			tv = new TextView(mContext);
			tv.setClickable(true);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
			tv.setText(mHistory.get(i));
			ll.addView(tv);
		} // end for.

		// Create now the alert:
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(mContext.getString(R.string.game_history_title));
		alertDialog.setView(historyView);
		alertDialog.setPositiveButton(
				mContext.getString(R.string.bt_close_history), null);
		AlertDialog alert = alertDialog.create();
		alert.show();
	} // end show() method.

	// A method which takes the corresponding number of messages plural
	// resource:
	public String getNumberOfMessagesAsString(int nr) {
		// First take the corresponding plural resource:
		String numberOfPointsMessage = res.getQuantityString(
				R.plurals.msg_number_of_messages, nr, nr);
		return numberOfPointsMessage;
	} // end getNumberOfPointsAsString() method.

	/*
	 * A method which inserts into database the last game with history and game
	 * statistics. The game statistics are received via parameter from game
	 * passing via StringTools class, the object in game:
	 */
	public void insertIntoDB(ArrayList<String> arrStatistics) {
		// We need here to process things.
		// The history must be in the order of playing, not reverse:
		StringBuilder sbHistory = new StringBuilder();
		/*
		 * Add first the number of messages text view at the beginning of the
		 * history:
		 */
		String status = String.format(
				res.getString(R.string.history_number_of_messages),
				getNumberOfMessagesAsString(mHistory.size()));
		Spanned temp = MyHtml.fromHtml(status);
		sbHistory.append(temp);
		sbHistory.append("\n");
		// We go through history array to build the string:
		for (int i = 0; i < mHistory.size(); i++) {
			sbHistory.append(mHistory.get(i));
			sbHistory.append("\n");
		} // end for.

		StringBuilder sbStats = new StringBuilder();
		// We go through arrStatistics array to build the string:
		for (int i = 0; i < arrStatistics.size(); i++) {
			sbStats.append(arrStatistics.get(i));
			sbStats.append("\n");
		} // end for.

		// We also add the content into the history table in database:

		// Start things for our database:
		DBAdapter mDbHelper = new DBAdapter(mContext);
		mDbHelper.createDatabase();
		mDbHelper.open();
		// Detect the last id in statistics table:
		String sql = "SELECT MAX(id) FROM statistics;";
		Cursor cursor = mDbHelper.queryData(sql);
		cursor.moveToFirst();
		int lastId = cursor.getInt(0);
		cursor = null;

		// Make the query to insert:
		sql = "INSERT INTO history (id, historyText, statisticsText) values ('"
				+ lastId + "', '" + realEscapeString(sbHistory.toString())
				+ "', '" + realEscapeString(sbStats.toString()) + "')";
		mDbHelper.insertData(sql);
		mDbHelper.close();
	} // end insertIntoDB() method.

	// A method instead RealEscapeString:
	private String realEscapeString(String str) {
		String toReturn = str.replaceAll("'", "''");
		toReturn = toReturn.trim();
		return toReturn;
	} // end realEscapeString() method.

} // end History class.
