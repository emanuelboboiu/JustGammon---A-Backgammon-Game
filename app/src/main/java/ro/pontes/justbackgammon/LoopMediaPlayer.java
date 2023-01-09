package ro.pontes.justbackgammon;

import android.content.Context;
import android.media.MediaPlayer;

public class LoopMediaPlayer {

	private Context mContext = null;
	private int mResId = 0;

	private MediaPlayer mCurrentPlayer = null;
	private MediaPlayer mNextPlayer = null;

	// The constructor:
	public LoopMediaPlayer(Context context, int resId) {
		mContext = context;
		mResId = resId;
		mCurrentPlayer = MediaPlayer.create(mContext, mResId);
		mCurrentPlayer
				.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mediaPlayer) {
						mCurrentPlayer.start();
					}
				});

		createNextMediaPlayer();
	}

	private void createNextMediaPlayer() {
		mNextPlayer = MediaPlayer.create(mContext, mResId);
		mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
		mCurrentPlayer.setOnCompletionListener(onCompletionListener);
	}

	private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.release();
			mCurrentPlayer = mNextPlayer;

			createNextMediaPlayer();
		}
	};

	public void stop() {
		mCurrentPlayer.stop();
		mNextPlayer.stop();
	} // end stop() method.
} // end LoopMediaPlayer class.
