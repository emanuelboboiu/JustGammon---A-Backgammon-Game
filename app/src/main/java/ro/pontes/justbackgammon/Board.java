package ro.pontes.justbackgammon;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Board {

	private final Activity mActivity;
	private final Context mContext;
	private Resources res;
	private MySoundPool mSP;
	private int isColor = 1;
	private boolean isInHand = false;
	private int lastTakenPos = 0;
	private int lastTakenPulls = 0;
	private int lastTakenColor = 0;

	// An array for all positions as image views:
	private ImageView[] ivBs;
	// An array for borders:
	private ImageView[] ivBorders;

	// An array for colours letter used in file names:
	String[] tempAColor = { "", "w", "b" };
	private boolean isAccessibleTheme = false;

	// An array with strings for empty, black and white words:
	private String[] aStringColors;
	// An array for borders names:
	private String[] aBordersNames;

	/*
	 * An arrayList for checker having the content description changed for put
	 * hint:
	 */
	ArrayList<ImageView> arrIVsChangedHints = new ArrayList<ImageView>(2);
	ArrayList<String> arrStringsOldCDs = new ArrayList<String>(2);

	// For content description:
	private int cdNotationType = 1; // A and B notation.

	// For disable / enable animations:
	private boolean isAnimWherePut = true;
	private boolean isAnimWhereEnter = true;
	private boolean isAnimWhereTaken = true;

	// Some global animations:
	private Animation animSelected;
	private Animation animForTaken;
	private Animation animForPut;

	// The constructor:
	public Board(Activity activity, Context context, MySoundPool mSP) {
		mActivity = activity;
		mContext = context;
		res = mContext.getResources();
		chargeBoardSettings();
		// Charge the array for colours as strings:
		aStringColors = res.getStringArray(R.array.colors_array);
		// Charge the array for borders names:
		aBordersNames = res.getStringArray(R.array.borders_names_array);
		this.mSP = mSP;

		// Change the tempAColors array if accessible checkers were chosen:
		if (isAccessibleTheme) {
			tempAColor = new String[] { "", "g", "r" };
		} // / end if it isAccessibleTheme

		// Charge the animation for position selected:
		animSelected = AnimationUtils.loadAnimation(mContext,
				R.anim.position_selected);
		// Charge also the animation for taken:
		animForTaken = AnimationUtils
				.loadAnimation(mContext, R.anim.fade_taken);
		// Charge also the animation for put available position:
		animForPut = AnimationUtils
				.loadAnimation(mContext, R.anim.where_to_put);
	} // end constructor.

	// A method to charge some settings:
	private void chargeBoardSettings() {
		Settings set = new Settings(mContext);
		isAccessibleTheme = set.getBooleanSettings("isAccessibleTheme");
		cdNotationType = set.getIntSettings("cdNotationType");
		isAnimWherePut = set.getBooleanSettingsTrueDefault("animWherePut");
		isAnimWhereEnter = set.getBooleanSettingsTrueDefault("animWhereEnter");
		isAnimWhereTaken = set.getBooleanSettingsTrueDefault("animWhereTaken");
		set = null;
	} // end chargeBoardSettings() method.

	// A method for initial things:

	public void initializeBoard() {
		chargePositionsInArray();
	} // end initializeBoard() method.

	// A method to charge all points and borders into an array:
	private void chargePositionsInArray() {
		// Instantiate the array:
		ivBs = new ImageView[24];
		for (int i = 0; i < 24; i++) {
			int resID = mContext.getResources().getIdentifier("ivB" + (i + 1),
					"id", mContext.getPackageName());
			ivBs[i] = (ImageView) mActivity.findViewById(resID);
			onPositionsFocusActions(i);
			// Try to add accessibility focus:
			// In construction...
		} // end for charge array for positions.

		// Charge also the borders in their array:
		// Instantiate the array:
		ivBorders = new ImageView[6];
		ivBorders[0] = (ImageView) mActivity
				.findViewById(R.id.ivBorderRightDown);
		ivBorders[1] = (ImageView) mActivity.findViewById(R.id.ivBorderRightUp);
		ivBorders[2] = (ImageView) mActivity
				.findViewById(R.id.ivBorderMiddleDown);
		ivBorders[3] = (ImageView) mActivity
				.findViewById(R.id.ivBorderMiddleUp);
		ivBorders[4] = (ImageView) mActivity
				.findViewById(R.id.ivBorderLeftDown);
		ivBorders[5] = (ImageView) mActivity.findViewById(R.id.ivBorderLeftUp);
		for (int i = 0; i < ivBorders.length; i++) {
			onBordersFocusActions(i);
		} // end for borders.
	}// end chargePositionsInArray() method.

	public void setBoardPerspective(int color) {
		isColor = color; // to have colour for board objects.
		// If colour is 1, white and the board is black perspective:
		String tempTagString = ivBs[0].getTag().toString();
		int tempTag = Integer.parseInt(tempTagString);
		int isActualPerspective = 1; // it means white perspective.
		if (tempTag == 24) {
			isActualPerspective = 2; // it means black perspective.
		}

		// If isPerspective isn't equal with colour, reverse the board:
		if (isActualPerspective != color) {
			turnTheBoardPerspective();
		}
	} // end setBoardPerspective() method.

	public void turnTheBoardPerspective() {
		// First turn the positions:
		for (int i = 0; i < ivBs.length / 2; i++) {
			ImageView temp = ivBs[i];
			ivBs[i] = ivBs[ivBs.length - i - 1];
			ivBs[ivBs.length - i - 1] = temp;
		} // end for reversing the positions.
	} // end turnTheBoardPerspective() method.

	private void onPositionsFocusActions(final int pos) {
		// Set on focus events:
		ivBs[pos].setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					setAnimationForSelectPosition(pos);
					mSP.playSoundPositioned(3, pos);
				} else {
					// Focus left, nothing yet.
				}
			}
		});
		// End focus changed.
	} // end onPositionFocusActions() method.

	private void onBordersFocusActions(final int pos) {
		// Set on focus events:
		ivBorders[pos].setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) {
					int soundPos = 0; // initial value right down border.
					if (pos == 1)
						soundPos = 23;
					else if (pos == 4)
						soundPos = 11;
					else if (pos == 5)
						soundPos = 12;
					else if (pos == 2)
						soundPos = 100; // middle down.
					else if (pos == 3)
						soundPos = 101; // middle up.

					mSP.playSoundPositioned(4, soundPos);
				} else {
					// Focus left, nothing yet.
				}
			}
		});
		// End focus changed.
	} // end onBordersFocusActions() method.

	// Two methods which sets the content description:
	private void setPositionContentDescription(int position, int pulls,
			int color) {
		// Set the content description effectively:
		ivBs[position]
				.setContentDescription(determinePositionContentDescription(
						position, pulls, color));
	} // end setPositionContentDescription() method.

	private String determinePositionContentDescription(int position, int pulls,
			int color) {
		String cdPosStatus = mContext.getString(R.string.cd_position_status);
		String curPosName = "";
		String strPos = aStringColors[0]; // As empty.;

		// First of all, determine current position name:
		if (cdNotationType == 1) { // with A and B:
			if (position < 12) { // part A:
				curPosName = "A" + (position + 1) + ":";
			} else { // part B:
				curPosName = "B" + (24 - position) + ":";
			} // end part b.
		} else if (cdNotationType == 2) { // from 1 one to 24:
			curPosName = "" + (position + 1) + ":";
		} // end if notation from 1 to 24.

		// Now create the description of the position:
		if (pulls > 0) { // not empty position:
			// Take the plural resource depending of the colour:
			if (color == 1) { // white:
				strPos = res.getQuantityString(
						R.plurals.cd_number_of_white_pulls, pulls, pulls);
			} else { // black:
				strPos = res.getQuantityString(
						R.plurals.cd_number_of_black_pulls, pulls, pulls);
			} // end if is a black position.
		} // end if is not an empty position.

		// Format now the string:
		String contentDescription = String.format(cdPosStatus, curPosName,
				strPos);
		return contentDescription;
	} // end determinePositionContentDescription() method.

	// The method which takes a pull from a position:
	public void takePull(int pos, int newPulls, int newColor) {
		// The position for real board, for sounds:
		int tempPos = pos;
		// If is black, the real position changes:
		if (isColor == 2) {
			tempPos = 23 - tempPos;
		} // end if it is black perspective.
		mSP.playSoundPositioned(7, tempPos);

		/*
		 * We save the position, pulls and colour to call after put in another
		 * place the pull, at the end of the animation. We also save the
		 * information that's in hand now. We will call when put down the pull,
		 * at animation finish the setPosition method using these values.
		 */
		isInHand = true;
		lastTakenPos = pos;
		lastTakenPulls = newPulls;
		lastTakenColor = newColor;

		// New behaviour, the pull is taken now:
		setPosition(lastTakenPos, lastTakenPulls, lastTakenColor);
		// Set animation and content description:
		// We change also the content description for almost taken pull:
		if (isAnimWhereTaken) {
			setCDForAlmostTakenPosition(pos, newPulls, newColor);
		} // end if is hint for almost taken.
		setAnimationForTakenPull(pos);
	} // end takePull() method.

	// A method which set a content description for an almost taken pull:
	private void setCDForAlmostTakenPosition(int pos, int pulls, int color) {
		/* Change the content description hint only if it is not opponent turn: */
		// The initCD contains initial content description:
		if (!Game.isOpponentTurn) {
			// The initCD contains string like after successfully move:
			String initCD = determinePositionContentDescription(pos, pulls,
					color);
			String newCD = String.format(
					mContext.getString(R.string.cd_almost_taken), initCD);
			ivBs[pos].setContentDescription(newCD);
		} // end if it is not opponent turn.
	} // end setCDForAlmostTakenPosition() method.

	// The method which sets the animation for a selected position:
	private void setAnimationForSelectPosition(int pos) {
		ivBs[pos].startAnimation(animSelected);
	} // end setAnimationForTakenPull() method.

	// The method which sets the animation for a taken pull:
	private void setAnimationForTakenPull(int pos) {
		if (isAnimWhereTaken) {
			ivBs[pos].startAnimation(animForTaken);
		}
	} // end setAnimationForTakenPull() method.

	// A method to set the hint for available to put position:
	public void setHintToPut(int pos) {
		if (isAnimWherePut) {
			ivBs[pos].startAnimation(animForPut);

			/*
			 * Change the content description hint only if it is not opponent
			 * turn:
			 */
			// The initCD contains initial content description:
			if (!Game.isOpponentTurn) {
				String initCD = ivBs[pos].getContentDescription().toString();
				/*
				 * We add into the array lists the image view changed and also
				 * the old content description for it, to make it back after
				 * moving on another position than this. There are two parallel
				 * array lists, one for strings and one for image views:
				 */
				arrIVsChangedHints.add(0, ivBs[pos]);
				arrStringsOldCDs.add(0, initCD);

				String newCD = String.format(
						mContext.getString(R.string.cd_hint_can_put), initCD);
				ivBs[pos].setContentDescription(newCD);
			} // end if it is not opponent turn.
		} // end if disabled hint where to put.
	} // end setHintToPut() method.

	// A method to set the hint for available to re-enter position:
	public void setHintToEnter(int pos) {
		if (isAnimWhereEnter) {
			ivBs[pos].startAnimation(animForPut);

			/*
			 * Change the content description hint only if it is not opponent
			 * turn:
			 */
			// The initCD contains initial content description:
			if (!Game.isOpponentTurn) {
				// The initCD contains initial content description:
				String initCD = ivBs[pos].getContentDescription().toString();
				/*
				 * We add into the array lists the image view changed and also
				 * the old content description for it, to make it back after
				 * moving on another position than this. There are two parallel
				 * array lists, one for strings and one for image views:
				 */
				arrIVsChangedHints.add(0, ivBs[pos]);
				arrStringsOldCDs.add(0, initCD);

				String newCD = String.format(
						mContext.getString(R.string.cd_hint_can_enter), initCD);
				ivBs[pos].setContentDescription(newCD);
			} // end if it is not opponent turn.
		} // end if is hint for where to enter.
	} // end setHintToEnter() method.

	// The method which puts a pull on a position:
	public void putPull(int pos, int newPulls, int newColor) {
		// The position for real board, for sounds:
		int tempPos = pos;
		// If is black, the real position changes:
		if (isColor == 2) {
			tempPos = 23 - tempPos;
		} // end if it is black perspective.
		mSP.playSoundPositioned(8, tempPos);
		/*
		 * If a pull is in hand, we must take it effectively from last position,
		 * in takePull we only set an animation. We also stop here that
		 * animation. It must be in hand and also not to be put back, on
		 * previous position:
		 */
		if (isInHand && pos != lastTakenPos) {
			// Next line is commented:
			// setPosition(lastTakenPos, lastTakenPulls, lastTakenColor);
			/*
			 * Instead setPosition, we must set only the content description, it
			 * will not contain the information that's in hand::
			 */
			setPositionContentDescription(lastTakenPos, lastTakenPulls,
					lastTakenColor);
			isInHand = false;
		} // end if it is in hand.

		// Remove also all animations on board:
		clearAllBoardAnimations();

		/*
		 * Make back the content description for other changed hints. We have
		 * array lists for old content descriptions and changed image views:
		 */
		for (int i = 0; i < arrIVsChangedHints.size(); i++) {
			// Don't change back the current image view if put not back:
			if (ivBs[pos] != arrIVsChangedHints.get(i)) {
				arrIVsChangedHints.get(i).setContentDescription(
						arrStringsOldCDs.get(i));
			} // end if isn't the same iV like current changed.
		} // end for changing back content description for old hints.
			// Clear the ArrayLists:
		arrIVsChangedHints.clear();
		arrStringsOldCDs.clear();

		setPosition(pos, newPulls, newColor);
	} // end putPull() method.

	// The method which removes a pull from a position:
	public void removePull(int pos, int newPulls, int newColor) {
		// The position for real board, for sounds:
		int tempPos = pos;
		// If is black, the real position changes:
		if (isColor == 2) {
			tempPos = 23 - tempPos;
		} // end if it is black perspective.
		mSP.playSoundPositioned(7, tempPos);

		setPosition(pos, newPulls, newColor);
	} // end removePull() method.

	// A method to remove all animations on a board:
	private void clearAllBoardAnimations() {
		animForTaken.cancel();
		animForPut.cancel();
		for (int i = 0; i < ivBs.length; i++) {
			ivBs[i].clearAnimation();
		} // end for.
	} // end clearAllBoardAnimations() method.

	// The method which fires at a click on a border:
	public void makeActionOnBorders(int curBorder) {
		// Do nothing here yet.
	} // end makeActionOnBorders() method.

	// A method which arranges the board according to parallel arrays:
	public void arrangeBoard(int[] aPulls, int[] aColors, int[] aBorderPulls,
			int[] aBorderColors, boolean isArrangeGradually, boolean makeSound) {
		if (isArrangeGradually) {
			arrangeBoardGradually(aPulls, aColors, aBorderPulls, aBorderColors,
					makeSound);
		} else {
			arrangeBoardInstantly(aPulls, aColors, aBorderPulls, aBorderColors,
					makeSound);
		}
	} // end arrangeBoard() method.

	// A method which arranges the board instantly:
	private void arrangeBoardInstantly(int[] aPulls, int[] aColors,
			int[] aBorderPulls, int[] aBorderColors, boolean makeSound) {
		if (makeSound) {
			mSP.playSound(28); // arranging instant board sound.
		} // end if play sound is needed.
			// A for in the parallel arrays for pieces:
		for (int i = 0; i < aPulls.length; i++) {
			setPosition(i, aPulls[i], aColors[i]);
		} // end for.

		// A for in the parallel arrays for borders:
		for (int i = 0; i < aBorderPulls.length; i++) {
			setPiecesOnBorder(i, aBorderPulls[i], aBorderColors[i]);
		} // end for.
	} // end arrangeBoardInstantly() method.

	// A method to arrange the board gradually:
	private void arrangeBoardGradually(final int[] aPulls, final int[] aColors,
			final int[] aBorderPulls, final int[] aBorderColors,
			boolean makeSound) {
		if (makeSound) {
			mSP.playSound(1); // arranging instant board sound.
		} // end if play sound is needed.

		if (makeSound) {
			mSP.playSound(1); // arranging board sound.
		} // end if play sound is needed.

		// We need total time sound length:
		int durationGradually = 4500;
		// Let's see how many pieces we have on the border:
		int nrCheckers = 0;
		for (int i = 0; i <= 23; i++) {
			nrCheckers = nrCheckers + aPulls[i];
		} // end for add number of pieces.
			// Interval between putting a checker:
		final int interval = durationGradually / nrCheckers;
		// Put 15 on right bar:
		aBorderPulls[0] = 15;
		aBorderPulls[1] = 15;
		aBorderColors[0] = 1;
		aBorderColors[1] = 2;

		// Put in a new thread piece by piece with sleep:
		new Thread(new Runnable() {
			public void run() {
				// Do task here in this new thread:
				// Go through all positions:
				for (int i = 0; i < aPulls.length; i++) {
					/*
					 * If on a position there are pieces, another for for each
					 * of them:
					 */
					if (aPulls[i] > 0) {
						final int curPos = i;
						final int curColor = aColors[i];
						final int curBorder = curColor - 1;

						for (int j = 1; j <= aPulls[i]; j++) {
							final int curJ = j;
							// Consume the checkers from borders arrays:
							aBorderPulls[curBorder]--;
							// If 0 pulls on a border, make also colour as 0:
							if (aBorderPulls[curBorder] <= 0) {
								aBorderColors[curBorder] = 0;
							}
							// A new thread on main thread in this thread:
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// Here happens on main thread:
									setPosition(curPos, curJ, curColor);
									setPiecesOnBorder(curBorder,
											aBorderPulls[curBorder],
											aBorderColors[curBorder]);
								}
							});
							// end thread on UI runnable..
							try {
								Thread.sleep(interval);
							} catch (InterruptedException e) {
								// e.printStackTrace();
							} // end try catch.
						} // end inner loop for each checkers on a position.
					} // end if there are checkers on position.
				} // end for loop through positions.
					// If there are also checkers on the bar, redraw the board:
				if (aBorderPulls[2] > 0 || aBorderColors[3] > 0) {
					// Again A new thread on main:
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int wPullsBar = aBorderPulls[2];
							int bPullsBar = aBorderPulls[3];
							aBorderPulls[0] -= wPullsBar;
							aBorderPulls[1] -= bPullsBar;
							if (aBorderPulls[0] == 0) {
								aBorderColors[0] = 0;
							} // end if not remained.
							if (aBorderPulls[1] == 0) {
								aBorderColors[1] = 0;
							} // end if not remained.
							arrangeBorders(aBorderPulls, aBorderColors);
						} // end run() method.
					});
					// end thread on UI runnable..
				} // end if there are also on the bar.
			}
		}).start();
		// end thread to do AI move.
	} // end arrangeBoardGradually() method.

	// A method to set a position on the board:
	private void setPosition(int position, int pulls, int color) {
		// If the number of pulls is 0, we set the position transparent:
		if (pulls == 0) {
			ivBs[position].setImageResource(android.R.color.transparent);
		}

		else { // there are pieces:
				// Get the image and resize it:
				// Determine the imageName with pieces:
			String imageName = "p_" + tempAColor[color] + pulls;
			int w = ivBs[position].getWidth();
			int h = ivBs[position].getHeight();
			Bitmap srcImage = GUITools.resizeImage(mContext, imageName, w, h);

			if ((position >= 12 && isColor == 1)
					|| (position < 12 && isColor == 2)) { // rotate the image:
				Matrix mat = new Matrix();
				mat.postRotate(180);
				Bitmap bMapRotate = Bitmap.createBitmap(srcImage, 0, 0, w, h,
						mat, true);
				srcImage = bMapRotate;
			} // end if must be rotated.
				// Set the source image effectively:
			ivBs[position].setImageBitmap(srcImage);
		} // end if there are pieces, not transparent.

		// Set also the content description:
		setPositionContentDescription(position, pulls, color);
	} // end setPosition() method.

	// A method to put a pull on border:
	public void putPullOnBorder(int which, int pulls, int color) {
		setPiecesOnBorder(which, pulls, color);
	} // end putPullOnBorder() method.

	// A method to take pull from border:
	public void takePullFromBorder(int which, int pulls, int color) {
		setPiecesOnBorder(which, pulls, color);
	} // end takePullFromBorder() method.

	// A method to set pieces on borders:
	private void setPiecesOnBorder(int which, int pulls, int color) {
		// If the number of pulls is 0, we set the border transparent:
		if (pulls == 0) {
			ivBorders[which].setImageResource(android.R.color.transparent);
		}

		else { // there are pieces on the border:
				// Get the image and resize it:
				// Determine the imageName with pieces on the boards:
				// It is different if is edge or middle image:
			String prefixImageName = "edge_";
			int pullsToShown = pulls;
			if (which == 2 || which == 3) { // for middle, hit pulls:
				prefixImageName = "middle_";
				// We haven't pictures for more than 5 hit pulls:
				if (pulls > 5) {
					pullsToShown = 5;
				} // end if there are more pulls than 5.
			} // end if middle prefix name.
				// Don't let to go above 15:
			if (pullsToShown > 15) {
				pullsToShown = 15;
			}
			String imageName = prefixImageName + tempAColor[color]
					+ pullsToShown;
			int w = ivBorders[which].getWidth();
			int h = ivBorders[which].getHeight();
			Bitmap srcImage = GUITools.resizeImage(mContext, imageName, w, h);

			// Set the source image effectively:
			ivBorders[which].setImageBitmap(srcImage);
		} // end if there are pulls on borders.

		// Set also the content description:
		setBorderContentDescription(which, pulls, color);
	} // end setPiecesOnBorder() method.

	// A method which sets the content description for borders:
	private void setBorderContentDescription(int which, int pulls, int color) {
		String cdBorderStatus = mContext.getString(R.string.cd_border_status);
		String curBorderName = "";
		String strPos = aStringColors[0]; // As empty.;
		// If it's left borders, we don't need a string for empty there:
		if (which >= 4) {
			strPos = "";
		} // end if it is left borders, no empty string.

		// First of all, determine current border name from an array:
		curBorderName = aBordersNames[which];

		// Now create the description of the border:
		if (pulls > 0) { // not empty position:
			// Take the plural resource depending of the colour:
			if (color == 1) { // white:
				strPos = res.getQuantityString(
						R.plurals.cd_number_of_white_pulls, pulls, pulls);
			} else { // black:
				strPos = res.getQuantityString(
						R.plurals.cd_number_of_black_pulls, pulls, pulls);
			} // end if is a black position.
		} // end if is not an empty position.

		// Format now the string:
		String contentDescription = String.format(cdBorderStatus,
				curBorderName, strPos);

		// Set the content description effectively:
		ivBorders[which].setContentDescription(contentDescription);
	} // end setBorderContentDescription() method.

	// A method to remade the borders only:
	public void arrangeBorders(int[] aBorderPulls, int[] aBorderColors) {
		// A for in the parallel arrays for borders:
		for (int i = 0; i < aBorderPulls.length; i++) {
			setPiecesOnBorder(i, aBorderPulls[i], aBorderColors[i]);
		} // end for.
	} // end arrangeBorders() method.

	// A method called at destroy activity:
	public void destroy() {
		mSP.release();
	} // end destroy() method.

} // end Board class.
