package ro.pontes.justbackgammon;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

@SuppressLint("UseSparseArrays")
public class MySoundPool {
	// Array for which sounds must be played or not:
	private int[] enabledSounds;

	private final int ARRANGE_BOARD = 1;
	private final int CLOCK = 2;
	private final int PLACE = 3;
	private final int BORDER = 4;
	private final int REVERSE = 5;
	private final int DICE = 6;
	private final int TAKEN = 7;
	private final int MOVED = 8;
	private final int BLOCKED = 9;
	private final int HIT = 10;
	private final int DICEF = 11;
	private final int PAUSE = 12;
	private final int RESUME = 13;
	private final int IS_YOUR_TURN = 14;
	private final int OPPONENT_THINKING = 15;
	private final int CLOSE_BOARD = 16;
	private final int CLICKS_SETTINGS = 17;
	private final int CLOSE_SETTINGS = 18;
	private final int WIN1 = 19;
	private final int WIN2 = 20;
	private final int WIN3 = 21;
	private final int WIN4 = 22;
	private final int LOSE1 = 23;
	private final int LOSE2 = 24;
	private final int LOSE3 = 25;
	private final int LOSE4 = 26;
	private final int ALLINHOME = 27;
	private final int ARRANGE_BOARD_INSTANTLY = 28;

	private Context mContext;
	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundMap;

	// The constructor:
	@SuppressWarnings("deprecation")
	public MySoundPool(Context context) {
		mContext = context;
		mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		mSoundMap = new HashMap<Integer, Integer>();

		if (mSoundPool != null) {
			mSoundMap.put(ARRANGE_BOARD,
					mSoundPool.load(mContext, R.raw.setupboard, 1));
			mSoundMap.put(CLOCK, mSoundPool.load(mContext, R.raw.clock, 1));
			mSoundMap.put(PLACE, mSoundPool.load(mContext, R.raw.place, 1));
			mSoundMap.put(BORDER, mSoundPool.load(mContext, R.raw.border, 1));
			mSoundMap.put(REVERSE, mSoundPool.load(mContext, R.raw.reverse, 1));
			mSoundMap.put(DICE, mSoundPool.load(mContext, R.raw.dice1, 1));
			mSoundMap.put(TAKEN, mSoundPool.load(mContext, R.raw.taken, 1));
			mSoundMap.put(MOVED, mSoundPool.load(mContext, R.raw.moved, 1));
			mSoundMap.put(BLOCKED, mSoundPool.load(mContext, R.raw.blocked, 1));
			mSoundMap.put(HIT, mSoundPool.load(mContext, R.raw.hit, 1));
			mSoundMap.put(DICEF, mSoundPool.load(mContext, R.raw.dicef, 1));
			mSoundMap
					.put(PAUSE, mSoundPool.load(mContext, R.raw.game_pause, 1));
			mSoundMap.put(RESUME,
					mSoundPool.load(mContext, R.raw.game_resume, 1));
			mSoundMap.put(IS_YOUR_TURN,
					mSoundPool.load(mContext, R.raw.is_your_turn, 1));
			mSoundMap.put(OPPONENT_THINKING,
					mSoundPool.load(mContext, R.raw.opponent_thinking, 1));
			mSoundMap.put(CLOSE_BOARD,
					mSoundPool.load(mContext, R.raw.finishboard, 1));
			mSoundMap.put(CLICKS_SETTINGS,
					mSoundPool.load(mContext, R.raw.element_clicked, 1));
			mSoundMap.put(CLOSE_SETTINGS,
					mSoundPool.load(mContext, R.raw.element_finished, 1));

			mSoundMap.put(WIN1, mSoundPool.load(mContext, R.raw.win1, 1));
			mSoundMap.put(WIN2, mSoundPool.load(mContext, R.raw.win2, 1));
			mSoundMap.put(WIN3, mSoundPool.load(mContext, R.raw.win3, 1));
			mSoundMap.put(WIN4, mSoundPool.load(mContext, R.raw.win4, 1));
			mSoundMap.put(LOSE1, mSoundPool.load(mContext, R.raw.lose1, 1));
			mSoundMap.put(LOSE2, mSoundPool.load(mContext, R.raw.lose2, 1));
			mSoundMap.put(LOSE3, mSoundPool.load(mContext, R.raw.lose3, 1));
			mSoundMap.put(LOSE4, mSoundPool.load(mContext, R.raw.lose4, 1));
			mSoundMap.put(ALLINHOME,
					mSoundPool.load(mContext, R.raw.all_in_home, 1));
			mSoundMap.put(ARRANGE_BOARD_INSTANTLY,
					mSoundPool.load(mContext, R.raw.setupinstantly, 1));

		} // end if SoundPool object is not null.

		chargeStatusOfSoundFromSettings();
	} // end constructor.

	public void chargeStatusOfSoundFromSettings() {
		// Charge the array with enabled or disabled sounds:
		Settings set = new Settings(mContext);
		enabledSounds = set.getEnabledSoundsIds();
		set = null;
	} // end chargeStatusOfSoundFromSettings() method.

	// A method which determine if enabled or not a sound ID:
	private boolean allowPlay(int id) {
		// We take here in account also if all sounds are disabled:
		if (enabledSounds[id] == 1 && MainActivity.isSound) {
			return true;
		} else {
			return false;
		}
	} // end allowPlay() method.

	public void playSound(int sound) {
		// Only if this sound is enabled:
		if (allowPlay(sound)) {
			playSoundAnyway(sound);
		} // end if sound is allowed.
	} // end playSound() method.

	// A method which plays a sound even if it is forbidden by user:
	public void playSoundAnyway(int sound) {
		// Calculate first the volume needed, depending of the percentage:
		// set from general media volume:
		AudioManager am = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = (float) volumeLevel / maxVolume;
		volume = volume * MainActivity.intVolume / 100;
		// End calculate the current volume.

		if (mSoundPool != null) {
			mSoundPool.play(mSoundMap.get(sound), volume, volume, 1, 0, 1.0f);
		}
	} // end playSoundAnyway() method.

	// A method to play a sound on a specific position on the board after a
	// delay:
	public void playSoundPositioned(final int sound, final int position,
			int delay) {
		if (allowPlay(sound)) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// Do something after some milliseconds:
					playSoundPositioned(sound, position);
				}
			}, delay);
		} // end if sound is allowed.
	} // end playSoundPositioned() method, overloaded for delay.

	/*
	 * A method to play a sound on a specific position on the board. For
	 * position, 100 means middle at current media volume, 101 means half of
	 * media volume, centre too. Other parameters are from 1 to 24 and they
	 * Corresponds to the board positions.
	 */
	public void playSoundPositioned(int sound, int position) {
		if (allowPlay(sound)) {
			// Calculate first the volume needed, depending of the percentage:
			// set from general media volume:
			AudioManager am = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);
			int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float generalVolume = (float) volumeLevel / maxVolume;
			generalVolume = generalVolume * MainActivity.intVolume / 100;
			// End calculate the current volume depending of the percentage.

			float lVol = generalVolume; // just to be a first value.
			float rVol = generalVolume; // just to be a first value.

			// If it is a middle border clicked:
			if (position == 100) { // middle down board:
				// Do nothing, the volume is normal.
			} else if (position == 101) { // the middle up border:
				// Half of a volume:
				lVol = generalVolume / 2.0F;
				rVol = generalVolume / 2.0F;
			} // end if is middle up border.
			else { // the normal board positions:
				/*
				 * We have the general volume and we calculate the pan. The
				 * volume difference between positions is the general volume
				 * divided by 6. The difference between centre and the left and
				 * right positions are a half of a step.The generalVolume is
				 * smaller if it is the opposite part of the board.
				 */

				int pos = position + 1;
				if (pos > 12) {
					generalVolume = generalVolume / 2.0F;
				} // end make generalVolume for opposite.

				float stepDifference = generalVolume / 10.0F;

				if (pos <= 6 || pos >= 19) { // right part
					rVol = generalVolume;
					if (pos >= 19) {
						pos = 25 - pos;
					} // end if is greater than 19.
					lVol = generalVolume - stepDifference * (7 - pos);
				} else { // left part:
					lVol = generalVolume;
					if (pos >= 13) {
						pos = 19 - pos;
					} else {
						pos = pos - 6;
					}
					rVol = generalVolume - stepDifference * pos;
				} // end left part.
			} // end if board positions.

			if (mSoundPool != null) {
				mSoundPool.play(mSoundMap.get(sound), lVol, rVol, 1, 0, 1.0f);
			}
		} // end if sound is allowed.
	} // end playSoundPositioned() method.

	public void release() {
		mSoundPool.release();
	} // end release() method.
} // end MySoundPool class.

