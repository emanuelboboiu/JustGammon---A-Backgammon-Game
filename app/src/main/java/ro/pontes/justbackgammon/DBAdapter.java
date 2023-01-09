package ro.pontes.justbackgammon;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {

	// protected static final String TAG = "DataAdapter";

	private final Context mContext;
	private SQLiteDatabase mDb;
	private DataBaseHelper mDbHelper;

	public DBAdapter(Context context) {
		this.mContext = context;
		mDbHelper = new DataBaseHelper(mContext);
	}

	public DBAdapter createDatabase() throws SQLException {
		try {
			mDbHelper.createDataBase();
		} catch (IOException mIOException) {
			// Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
			throw new Error("UnableToCreateDatabase");
		}
		return this;
	}

	public DBAdapter open() throws SQLException {
		try {
			mDbHelper.openDataBase();
			mDbHelper.close();
			mDb = mDbHelper.getReadableDatabase();
		} catch (SQLException mSQLException) {
			// Log.e(TAG, "open >>"+ mSQLException.toString());
			throw mSQLException;
		}
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	// A method to query data from database:
	public Cursor queryData(String sql) {
		try {
			Cursor mCur = mDb.rawQuery(sql, null);
			if (mCur != null) {
				mCur.moveToNext();
			}
			return mCur;
		} catch (SQLException mSQLException) {
			// Log.e(TAG, "getTestData >>"+ mSQLException.toString());
			throw mSQLException;
		}
	} // end queryData() method.

	// A method to update a table:
	public boolean updateData(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end update data.

	// A method to insert into a table:
	public boolean insertData(String sql) {
		mDb.execSQL(sql);
		return true;
	} // end insert data.

} // end class TestAdapter.
