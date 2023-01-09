package ro.pontes.justbackgammon;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

/*
 * This is a class which contains only static methods to play sound in different ways.
 * This class is created by Manu, rewritten on 15 May 2018.
 */

public class SoundPlayer {

	public static long durationForWait = 0; // the duration for thread.sleep.
	public MediaPlayer mpl;
	public static boolean isPlaying = false;

	// The constructor:
	public SoundPlayer() {
		mpl = new MediaPlayer();
	} // end constructor.

	// Play 2 sounds in sequence:
	public static void playTwoSoundsInSequence(final String sound1,
			final String sound2, final Context context) {
		if (MainActivity.isSound) {
			// Determine which sound to be played:
			int resID = context.getResources().getIdentifier(sound1, "raw",
					context.getPackageName());
			// Calculate first the volume needed, depending of the percentage:
			// set from general media volume:
			AudioManager am = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = (float) volumeLevel / maxVolume;
			volume = volume * MainActivity.intVolume / 100;
			// End calculate the current volume.

			MediaPlayer mp = new MediaPlayer();
			mp = MediaPlayer.create(context, resID);
			mp.setVolume(volume, volume);
			mp.start();
			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer player) {
					player.release();

					// Another sound:
					// Calculate first the volume:
					// Calculate first the volume needed, depending of the
					// percentage:
					// set from general media volume:
					AudioManager am = (AudioManager) context
							.getSystemService(Context.AUDIO_SERVICE);
					int volumeLevel = am
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					int maxVolume = am
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					float volume = (float) volumeLevel / maxVolume;
					volume = volume * MainActivity.intVolume / 100;
					// End calculate the current volume.

					int resID = context.getResources().getIdentifier(sound2,
							"raw", context.getPackageName());
					MediaPlayer mp = new MediaPlayer();
					mp = MediaPlayer.create(context, resID);
					mp.setVolume(volume, volume);
					mp.start();
					mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						public void onCompletion(MediaPlayer player) {
							player.release();
						}
					});
					// end another sound.
				}
			});

		} // end if isSound.
	} // end play 2 sounds method.

	// A method to play sound, a static one:
	public static void playSimple(Context context, String fileName) {
		if (MainActivity.isSound) {
			MediaPlayer mp = new MediaPlayer();
			// Calculate first the volume needed, depending of the percentage:
			// set from general media volume:
			AudioManager am = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = (float) volumeLevel / maxVolume;
			volume = volume * MainActivity.intVolume / 100;
			// End calculate the current volume.

			int resID;
			resID = context.getResources().getIdentifier(fileName, "raw",
					context.getPackageName());
			mp = MediaPlayer.create(context, resID);
			mp.setVolume(volume, volume);
			mp.start();
			isPlaying = true;

			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer player) {
					isPlaying = false;
					player.release();
				}
			});
		} // end if is sound activated.
	} // end static method playSimple.

	// A method to play a sound delayed:
	public static void playSimpleDelayed(final Context context,
			final int delay, final String fileName) {
		if (MainActivity.isSound) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// Do something after X milliseconds:
					SoundPlayer.playSimple(context, fileName);
				}
			}, delay);
		} // end if isSound.
	} // end playSimpleDelayed() method.

	// A method to play wait final, a static one:
	public static void playWaitFinal(final Context context,
			final String fileName) {
		if (MainActivity.isSound) {
			// Play in another thread, this way it is possible to be better the
			// playWait method of the SoundPlayer class:
			new Thread(new Runnable() {
				public void run() {

					MediaPlayer mp = new MediaPlayer();
					// Calculate first the volume needed, depending of the
					// percentage:
					// set from general media volume:
					AudioManager am = (AudioManager) context
							.getSystemService(Context.AUDIO_SERVICE);
					int volumeLevel = am
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					int maxVolume = am
							.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					float volume = (float) volumeLevel / maxVolume;
					volume = volume * MainActivity.intVolume / 100;
					// End calculate the current volume.

					int resID = context.getResources().getIdentifier(fileName,
							"raw", context.getPackageName());
					mp = MediaPlayer.create(context, resID);
					mp.setVolume(volume, volume);
					mp.start();
					// Determine the duration of the sound:
					durationForWait = mp.getDuration();
					mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						public void onCompletion(MediaPlayer player) {
							player.release();
						}
					});
					mp = null;
				}
			}).start();
			// Try to make sleep until the sound is played:
			try {
				Thread.sleep(durationForWait + 15);
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		} // end if is sound activated.
	} // end static method playWaitFinal.

	// A method to play a looped sound until it is stopped:
	public void playLooped(Context context, String fileName) {
		if (MainActivity.isAmbience) {
			// Calculate first the volume needed, depending of the percentage:
			// set from general media volume:
			AudioManager am = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float volume = (float) volumeLevel / maxVolume;
			volume = volume * MainActivity.intVolume / 100;
			// End calculate the current volume.

			int resID;
			resID = context.getResources().getIdentifier(fileName, "raw",
					context.getPackageName());
			mpl = MediaPlayer.create(context, resID);
			mpl.setVolume(volume, volume);
			mpl.setLooping(true);
			mpl.start();
		} // end if is music activated.
	} // end playLooped method.

	public void stopLooped() {
		mpl.stop();
		mpl.release();
	} // end stop looped method.

} // end sound player class.
