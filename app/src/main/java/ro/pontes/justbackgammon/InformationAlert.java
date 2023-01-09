package ro.pontes.justbackgammon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class InformationAlert {

	private final Context mContext;
	private MySoundPool mSP = null;
	private View infoView = null;

	// The constructor:
	public InformationAlert(Context context, MySoundPool mSP) {
		this.mContext = context;
		this.mSP = mSP;
	} // end constructor.

	@SuppressLint("InflateParams")
	public void showInformationAlert() {
		// Inflate the information message contents
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		infoView = inflater.inflate(R.layout.activity_information, null);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(R.string.info_title);
		builder.setView(infoView);
		builder.setPositiveButton(
				mContext.getString(R.string.bt_close_information),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mSP.playSound(18); // the finished alert sound.
					}
				});

		builder.create();
		builder.show();
	}// end showInformationAlert() method.
} // end InformationAlert class.
