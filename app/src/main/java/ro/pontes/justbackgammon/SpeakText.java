package ro.pontes.justbackgammon;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

public class SpeakText {

	private Context mContext;
	private TextToSpeech mTTS;
	private String curEngine = null;
	private Locale curTTSLocale = null;
	private float ttsRate = 1.0F;
	private float ttsPitch = 1.0F;

	// The constructor:
	public SpeakText(Context context) {
		mContext = context;

		/*
		 * First of all, charge current engine, the saved one, the default would
		 * be no change if no saved curEngine:
		 */
		Settings set = new Settings(mContext);
		curEngine = set.getStringSettings("curEngine");
		// Check if a setting was saved for this:
		if (curEngine == null || curEngine.equals("")) {
			curEngine = null;
		} // end if no engine was saved.

		// For TextToSpeech initialisation:
		mTTS = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				// If no saved Locale let things like they are:
				if (curEngine == null || status == TextToSpeech.ERROR) {
					// Do nothing
				} else { // there is saved:
					setSavedVoice();
				} // end if we have saved.
			}
		}, curEngine);
		// end for TextToSpeech.
	} // end constructor.

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void say(final String toSay, final boolean interrupt) {
		if (mTTS != null) {
			if (MainActivity.isSpeech) {
				int speakMode = 0;
				if (interrupt) {
					speakMode = TextToSpeech.QUEUE_FLUSH;
				} else {
					speakMode = TextToSpeech.QUEUE_ADD;
				} // end if is not interruption.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mTTS.speak(toSay, speakMode, null, null);
				} else {
					mTTS.speak(toSay, speakMode, null);
				}
			} // end if isSpeech.
		} // end if it is not null.
	} // end say method.

	// A method to speak a text delayed:
	public void sayDelayed(final String toSay, final boolean interrupt,
			int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after some milliseconds:
				say(toSay, interrupt);
			}
		}, delay);
	} // end sayDelayed() method.

	public void stop() {
		if (mTTS != null) {
			mTTS.stop();
		} // end if it is not null the TTS object.
	} // end stop method of the SpeakText class.

	// A method to shut down the TTS:
	public void shutdown() {
		if (mTTS != null) {
			mTTS.shutdown();
		} // end if it is not null.
	} // end shutdown() method.

	// A method to set the language of this instance, make a locale:
	@SuppressLint("NewApi")
	private void setSavedVoice() {
		Settings set = new Settings(mContext);
		String language = set.getStringSettings("ttsLanguage");
		if (language == null || language.equals("")) {
			language = "en";
		}
		String country = set.getStringSettings("ttsCountry");
		if (country == null || country.equals("")) {
			country = Locale.getDefault().getCountry();
		}
		String variant = set.getStringSettings("ttsVariant");
		if (variant == null) {
			variant = "";
		}

		curTTSLocale = new Locale(language, country, variant);

		// Now depending of the API:
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// We need the voice object to set the TTS voice:
			String voiceName = set.getStringSettings("ttsVoiceName");
			Voice voice = new Voice(voiceName, curTTSLocale, 0, 0, false, null);
			mTTS.setVoice(voice);
		} else { // older API than 21:
			// We haven't voice object and we set the locale only:
			mTTS.setLanguage(curTTSLocale);
		} // end if older API than 21.

		// See about rate and pitch:
		if (set.preferenceExists("ttsRate")) {
			ttsRate = set.getFloatSettings("ttsRate");
		} // end if TTS rate was saved.
		mTTS.setSpeechRate(ttsRate);

		if (set.preferenceExists("ttsPitch")) {
			ttsPitch = set.getFloatSettings("ttsPitch");
		} // end if TTS rate was saved.
		mTTS.setPitch(ttsPitch);
	} // end setCurrentLanguage() method.

} // end SpeakText class.
