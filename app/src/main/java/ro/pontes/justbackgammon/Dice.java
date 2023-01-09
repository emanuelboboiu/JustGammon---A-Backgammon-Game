package ro.pontes.justbackgammon;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Dice {

	private final Activity mActivity;
	private final Context mContext;
	private Resources res;
	private int[] aDice;
	private String diceStatus;
	private String msgSomeoneRolled;
	private String cdDieAvailable;
	private String cdDieUsed;
	private String cdDieUsedHalf;
	private StringTools st;
	private MySoundPool mSP;
	private int soundDuration = 800;
	private int remainedMoves = 0;
	private boolean isDouble = false;
	public boolean isTimeToRoll = true;
	private int gameType = 1;
	private int[] totals;
	private int[] rolls;
	private int[] doubles;

	private Vibration vibration;
	private ImageView ivD1, ivD2;

	// Animations for used and available start:
	private Animation animUsed1, animUsed2, animUsedHalf1, animUsedHalf2;
	private Animation animTimeToRoll;

	public Dice(Activity activity, Context context, MySoundPool mSoundPool,
			int gameType) {
		this.mActivity = activity;
		this.mContext = context;
		this.res = mContext.getResources();
		this.gameType = gameType;
		if (this.gameType == 1) {
			// in some languages different verb for common noun:
			msgSomeoneRolled = res
					.getString(R.string.msg_someone_rolled_dice_in_local_game);
		} else { // other than gameType 1:
			msgSomeoneRolled = res.getString(R.string.msg_someone_rolled_dice);
		} // end charge messages for rolling.
		this.diceStatus = res.getString(R.string.dice_status);
		this.cdDieAvailable = res.getString(R.string.cd_die_face_available);
		this.cdDieUsed = res.getString(R.string.cd_die_face_used);
		this.cdDieUsedHalf = res.getString(R.string.cd_die_face_used_half);
		this.aDice = new int[2];
		this.st = new StringTools(mContext, mActivity);
		this.mSP = mSoundPool;
		this.vibration = new Vibration(mContext);
		this.totals = new int[2];
		this.rolls = new int[2];
		this.doubles = new int[2];
		// Reference also to the images:
		ivD1 = (ImageView) mActivity.findViewById(R.id.ivD1);
		ivD2 = (ImageView) mActivity.findViewById(R.id.ivD2);
		// Charge also the animations:
		animUsed1 = AnimationUtils.loadAnimation(mActivity, R.anim.dice_used);
		animUsed2 = AnimationUtils.loadAnimation(mActivity, R.anim.dice_used);
		animUsedHalf1 = AnimationUtils.loadAnimation(mActivity,
				R.anim.dice_used_half);
		animUsedHalf2 = AnimationUtils.loadAnimation(mActivity,
				R.anim.dice_used_half);

		animTimeToRoll = AnimationUtils.loadAnimation(mActivity,
				R.anim.dice_time_to_roll);
	} // end constructor.

	// A method to roll first dice, to decide who play first with white:
	public void rollFirstDice() {
		/*
		 * First of all reset some values, like the totals, rolls, doubles
		 * arrays:
		 */
		resetSomeValues();
		// Write the start on the text zone:
		st.addText(res.getString(R.string.msg_rolling_first_dice), false);
		this.makeNoiseForFirstDice();
		boolean isDoubleFirstDice = false;
		if (gameType < 3) { // off-line or AI game:
			// Fill the array aDice with dice:
			aDice[0] = GUITools.random(1, 6);
			aDice[1] = GUITools.random(1, 6);
			// If it is Android TV, let's make white you:
			if (MainActivity.isTV) {
				sort();
			} // end if it is android TV.
		} // end if game type is 1 or 2, two in same room or AI.
		if (aDice[0] == aDice[1]) {
			isDoubleFirstDice = true;
		} // end if dice are equals.
		remainedMoves = 2; // no double is allowed here.
		isDouble = false;

		// Draw now the dice:
		this.draw();

		// Show also the message:
		announceFirstDice();

		if (!isDoubleFirstDice) {
			isDoubleFirstDice = false;
			/*
			 * First parameter is 0, white starts every time, double can not be
			 * and is by default false:
			 */
			updateValuesForStatistics(0, false);
		} // end if a double as first.
		else { // is a double as first:
			consumeAll();
		} // end if it was a double as first.
	}// end rollFirstDice() method.

	public void roll(int isTurn) {
		// Write the start on the text zone:
		st.addText(res.getString(R.string.msg_rolling_dice), false);
		this.makeNoise();
		if (gameType == 0 || gameType == 1 || gameType == 2) {
			// Fill the array aDice with dice:
			for (int i = 0; i < aDice.length; i++) {
				aDice[i] = GUITools.random(1, 6);
			} // end for filling aDice array.
				// Sort now the dice ascendantly:
			this.sort();
			// Reverse now the array of dice:
			this.reverse();
		} // end if game type 1 or 2, two in same room or AI.

		// Check if it is double and make 4 remained moves:
		if (aDice[0] == aDice[1]) {
			isDouble = true;
			remainedMoves = 4;
		} // end check if is double..
		else { // no double:
			isDouble = false;
			remainedMoves = 2;
		}
		updateValuesForStatistics(isTurn - 1, isDouble);
		// Draw now the dice:
		this.draw();
		// Announce also the dice via TTS:
		this.announce(isTurn);
	} // end roll() method.

	// Sorts the array of dice:
	private void sort() {
		Arrays.sort(aDice);
	} // end sort() method.

	// Reverses the array of dice:
	private void reverse() {
		for (int i = 0; i < aDice.length / 2; i++) {
			int temp = aDice[i];
			aDice[i] = aDice[aDice.length - 1 - i];
			aDice[aDice.length - 1 - i] = temp;
		} // end for.
	} // end reverse() method.

	// / A method to reset some values for dice, like the arrays for rolls and
	// totals:
	private void resetSomeValues() {
		for (int i = 0; i < 2; i++) {
			rolls[i] = 0;
			totals[i] = 0;
			doubles[i] = 0;
		} // end for making 0 in arrays.
	} // end resetSomeValues() method.

	// A method to update values in arrays for statistics:
	private void updateValuesForStatistics(int isTurn, boolean isDouble) {
		rolls[isTurn]++; // incrementing.
		int totalPoints = (aDice[0] + aDice[1]);
		if (isDouble) {
			doubles[isTurn]++; // incrementing.
			totalPoints = aDice[0] * 4;
		} // end if it was a double.
		totals[isTurn] += totalPoints;
	} // end updateValuesForStatistics(() method.

	// A method to return the arrays values for statistics class:
	public int[] getStatsValues() {
		int[] arr = new int[6];
		// The order of values will be: rolls, totals, doubles:
		arr[0] = rolls[0];
		arr[1] = rolls[1];
		arr[2] = totals[0];
		arr[3] = totals[1];
		arr[4] = doubles[0];
		arr[5] = doubles[1];
		return arr;
	} // end getStatsValues() method.

	// A method to make dice sound for normal roll and vibration:
	private void makeNoise() {
		vibration.vibrate(soundDuration + 100);
		// Play the dice sound 6 randomly twice:
		mSP.playSoundPositioned(6, GUITools.random(2, 18));
	} // end makeNoise() method.

	// Sound and vibration for first rolling die:
	private void makeNoiseForFirstDice() {
		vibration.vibrate(soundDuration / 2);
		vibration.vibrate(soundDuration / 2 - 50, soundDuration / 2 + 50);
		mSP.playSoundPositioned(11, GUITools.random(3, 8));
		mSP.playSoundPositioned(11, GUITools.random(3, 8),
				GUITools.random(300, 600));
	} // end makeNoiseForFirstDice() method.

	// A method which draws the dice on the board:
	private void draw() {
		// First, remove animations used before:
		animUsed1.cancel();
		animUsed1.reset();
		ivD1.clearAnimation();
		animUsed2.cancel();
		animUsed2.reset();
		ivD2.clearAnimation();
		final Animation anim1 = AnimationUtils.loadAnimation(mActivity,
				R.anim.shake);
		final Animation anim2 = AnimationUtils.loadAnimation(mActivity,
				R.anim.shake);
		final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Disable the dice:
				isTimeToRoll = false;
			} // end onAnimationStart() method.

			@Override
			public void onAnimationEnd(Animation animation) {
				// Set the random values:
				int value = 0;
				if (animation == anim1) {
					value = aDice[0];
				} else if (animation == anim2) {
					value = aDice[1];
				}

				int res = mContext.getResources().getIdentifier("d" + value,
						"drawable", mContext.getPackageName());

				if (animation == anim1) {
					ivD1.setImageResource(res);
					ivD1.setContentDescription(String.format(cdDieAvailable,
							aDice[0]));
					// ivD1.setClickable(true);
					ivD1.clearAnimation();
				} else if (animation == anim2) {
					ivD2.setImageResource(res);
					ivD2.setContentDescription(String.format(cdDieAvailable,
							aDice[1]));
					// ivD2.setClickable(true);
					ivD2.clearAnimation();
				}
			} // end onAnimationEnd()

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing.
			}
		};

		anim1.setAnimationListener(animationListener);
		anim2.setAnimationListener(animationListener);

		ivD1.startAnimation(anim1);
		ivD2.startAnimation(anim2);
	} // end draw() method.

	public String toString() {
		String message = String.format(diceStatus, aDice[0], aDice[1]);
		return message;
	} // end toString() method.

	public int getDice(int which) {
		return aDice[which - 1];
	} // end getDice() method.

	// A method which consumes the dice and decrements the remained moves:
	public void consumeDice(int face) {
		// If it is double, decrement only the remained moves available:
		if (isDouble) {
			remainedMoves--;
			// Set also for double, used in a half, and used:
			switch (remainedMoves) {
			case 3: // first consumed in a half, second not:
				ivD1.setContentDescription(String.format(cdDieUsedHalf,
						aDice[0]));
				// Set also the animation for it:
				ivD1.startAnimation(animUsedHalf1);
				break;
			case 2: // first consumed totally, second not:
				ivD1.setContentDescription(String.format(cdDieUsed, aDice[0]));
				// Set also the animation for it:
				ivD1.startAnimation(animUsed1);
				break;
			case 1: // first consumed totally, second in a half:
				ivD2.setContentDescription(String.format(cdDieUsedHalf,
						aDice[1]));
				// Set also the animation for it:
				ivD2.startAnimation(animUsedHalf2);
				break;
			case 0: // first consumed totally, second totally:
				ivD2.setContentDescription(String.format(cdDieUsed, aDice[1]));
				// Set also the animation for it:
				ivD2.startAnimation(animUsed2);
				break;
			} // end switch for double remained moves.
		} else { // not double:
			remainedMoves--;
			// Find the used dice and make it 0 as value:
			if (aDice[0] == face) {
				ivD1.setContentDescription(String.format(cdDieUsed, aDice[0]));
				aDice[0] = 0;
				// Set also the animation for it:
				ivD1.startAnimation(animUsed1);
			} else if (aDice[1] == face) {
				ivD2.setContentDescription(String.format(cdDieUsed, aDice[1]));
				aDice[1] = 0;
				// Set also the animation for it:
				ivD2.startAnimation(animUsed2);
			} else { // no similar values:
				/*
				 * It means we are here because bearing off using a bigger die
				 * value than the position.
				 */
				if (aDice[1] > 0 && aDice[1] > face) {
					ivD2.setContentDescription(String.format(cdDieUsed,
							aDice[1]));
					aDice[1] = 0;
					// Set also the animation for it:
					ivD2.startAnimation(animUsed2);
				} else if (aDice[0] > 0 && aDice[0] > face) {
					ivD1.setContentDescription(String.format(cdDieUsed,
							aDice[0]));
					aDice[0] = 0;
					// Set also the animation for it:
					ivD1.startAnimation(animUsed1);
				}
			} // end if no similar values.
		} // end if it is not double.
	} // end consume dice.

	// A method to consume all the dice:
	public void consumeAll() {
		for (int i = 0; i < aDice.length; i++) {
			if (aDice[i] > 0) { // not consumed:
				aDice[i] = 0; // make it consumed.
			}
		} // end for.
		remainedMoves = 0;
		// If no remained moves, make the dice available:
		if (remainedMoves <= 0) {
			ivD1.setClickable(true);
			ivD2.setClickable(true);
			isTimeToRoll = true;
			makeAnimationTimeToRoll();
		} // end if no moves available.
	} // end consumeAll() method.

	// Method to make animation to keep attention to roll the dice:
	public void makeAnimationTimeToRoll() {
		ivD1.startAnimation(animTimeToRoll);
		ivD2.startAnimation(animTimeToRoll);
	} // end makeAnimationTimeToRoll()() method.

	// A method which get the remained moves available:
	public int getRemainedMoves() {
		return remainedMoves;
	} // end getRemainedMoves() method.

	public void announce(int isTurn) {
		String diceMessage = String.format(msgSomeoneRolled,
				GUITools.aOpponents[isTurn], getDice(1), getDice(2));
		st.addText(diceMessage, true, soundDuration);
	} // end announce() method.

	public void announceFirstDice() {
		String diceMessage = String
				.format((gameType == 1 ? res
						.getString(R.string.msg_rolled_first_dice_in_local_game)
						: res.getString(R.string.msg_rolled_first_dice)),
						GUITools.aOpponents[1], getDice(1),
						GUITools.aOpponents[2], getDice(2));
		st.addText(diceMessage, true, soundDuration);
	} // end announceFirstDice() method.

	public void anounceRemainedMoves() {
		String message = res.getQuantityString(
				R.plurals.cd_number_of_remaining_moves, remainedMoves,
				remainedMoves);
		GUITools.toast(message, 2000, mContext);
	} // end anounceRemainedMoves() method.

} // end Dice class.
