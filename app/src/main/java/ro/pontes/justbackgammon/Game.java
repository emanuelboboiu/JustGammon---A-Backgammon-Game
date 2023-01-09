package ro.pontes.justbackgammon;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;

public class Game {

	private final Activity mActivity;
	private final Context mContext;
	private Board board;
	private MySoundPool mSP;
	private StringTools st;
	private Vibration vibration;
	private MyClock clock;

	// A chronometer:
	Chronometer chron = null;
	private int thinkingOpponentSoundInterval = 1;

	// Two parallel arrays for number of pulls and colours:
	// Set the parallel arrays to contains pieces for initial board:
	private int[] aPulls = new int[] { 2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, 5, 5,
			0, 0, 0, 3, 0, 5, 0, 0, 0, 0, 2 };
	private int[] aColors = new int[] { 2, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 2, 1,
			0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 1 };

	// Two parallel arrays for number of pulls and colours on borders:
	// Set the parallel arrays to contains pieces for initial board:
	private int[] aBorderPulls = new int[] { 15, 15, 0, 0, 0, 0 };
	private int[] aBorderColors = new int[] { 1, 2, 0, 0, 0, 0 };

	private Dice dice;
	private Resources res;

	// Status variables in games:
	private boolean isPause = false;
	private boolean isSuspended = false;
	private int clicks = 0;
	private boolean isAbandon = false;
	private boolean isAbandonClosing = false;

	private Timer timer;
	private Timer timerWhite;
	private int cdNotationType = 1;
	private int gameType = 2; // changed in constructor.
	private boolean isAnnounceAllInHomeBoard = true;
	private boolean isArrangeGradually = true;
	private boolean changePerspective = false; // changed in chargeSettings.
	private boolean changePerspectiveTV = true; // changed in chargeSettings.
	private int isTurn = 1; // default white's turn, 2 is opponent.
	private int isColor = 1; // the white board perspective.
	private int gameNumberInMatch = 1;
	private int diceRolls = 0;
	public static boolean isOpponentTurn = false;
	private boolean opponentStartedAsWhite = false;
	private boolean computerMustStartActions = false;
	private boolean opponentIsThinking = false; // at least for thinking sound.
	private int movesInterval = 1000;
	// private int remainedMoves = 0;
	private boolean isInHand = false;
	private int colorInHand = 0;
	private int posInHand = 0; // the position where is taken from.
	private String isTurnMessage = null;
	private Score score = null;
	private int scoreWhite = 0;
	private int scoreBlack = 0;
	private boolean wasFirsDiceRolled = false;
	private int[] nrOfCaptures;
	private boolean wasAnnouncedAllInHomeBoardWhite = false;
	private boolean wasAnnouncedAllInHomeBoardBlack = false;

	// We need an ArrayList for insert into DB:
	ArrayList<String> arrGameStatistics = new ArrayList<String>();
	private boolean isHistory = true;

	// The constructor:
	public Game(Activity activity, Context context, MySoundPool mSP) {
		mActivity = activity;
		mContext = context;
		gameType = MainActivity.gameType;
		nrOfCaptures = new int[] { 0, 0 };
		chargeSettings();
		// Initialise also the MySoundPool object:
		this.mSP = mSP;
		st = new StringTools(mContext, mActivity);
		clock = new MyClock(mContext);
		board = new Board(mActivity, mContext, mSP);
		dice = new Dice(mActivity, mContext, mSP, gameType);
		res = mContext.getResources();
		if (gameType == 1) { // local game:
			isTurnMessage = res
					.getString(R.string.msg_it_is_turn_in_local_game);
		} else { // not local games:
			isTurnMessage = res.getString(R.string.msg_it_is_turn);
		} // end charge isTurnMessage.
		GUITools.chargeOpponents(mContext, gameType);
		score = new Score(mContext, mActivity);
		vibration = new Vibration(mContext);
		timer = new Timer();
		// Charge also the chronometer if it isn't already charged:
		if (chron == null) {
			chron = (Chronometer) mActivity.findViewById(R.id.tvChronometer);
		} // end if chronometer is null.
		timerWhite = new Timer();
		isOpponentTurn = false;
	} // end constructor.

	// A method to charge values from shared settings for game class:
	private void chargeSettings() {
		Settings set = new Settings(mContext);
		cdNotationType = set.getIntSettings("cdNotationType");
		// For board perspectives automatic change in local games:
		if (gameType == 1) {
			changePerspective = set.getBooleanSettings("changePerspective");
			changePerspectiveTV = set
					.getBooleanSettingsTrueDefault("changePerspectiveTV");
		} // end if gameType is 1, local.
		if (gameType == 2 || gameType == 0) { // AI games:
			movesInterval = set.preferenceExists("movesInterval") ? set
					.getIntSettings("movesInterval") : 1000;
		} // end if gameType=2 or 0, AI.
		isArrangeGradually = set
				.getBooleanSettingsTrueDefault("showBoardArranging");

		// Charge also the score for gameType 1 and 2:
		if (gameType < 3) { // Local or AI:
			this.scoreWhite = set.getIntSettings("scoreWhite" + gameType);
			this.scoreBlack = set.getIntSettings("scoreBlack" + gameType);
		} // end if gameType less than 3.
		isHistory = set.getBooleanSettingsTrueDefault("isHistory");
		isAnnounceAllInHomeBoard = set
				.getBooleanSettingsTrueDefault("isAllInHome");
	} // end chargeSettings() method.

	/*
	 * Swap score in AI games to reflect the computer versus user, not white
	 * versus black:
	 */
	private void swapScores() {
		int temp = scoreWhite;
		scoreWhite = scoreBlack;
		scoreBlack = temp;
		score.setScore(scoreWhite, scoreBlack);
	} // end swapScores() method.

	public void initializeBoard() {
		board.initializeBoard();
	} // end initializeBoard() method in Game class.

	// A method called from GameActivity to change sizes:
	public void atLayoutCharged() {
		clearBoard();
		score.resizeIvTurns();
	} // end atLayoutCharged() method.

	// It happens at onBackPressed() in GameActivity:
	public void atDestroy() {
		board.destroy();
		st.destroy();
		MainActivity.isStarted = false;
	} // end atDestroy() method.

	// A method for closing the board:
	public void closeBoard() {
		if (MainActivity.isStarted || gameNumberInMatch > 1) {
			mSP.playSound(16); // close board sound.
			// Check if also is an abandon:
			int[] stats = dice.getStatsValues();
			int nrRolls = stats[0] + stats[1];
			if (wasFirsDiceRolled && nrRolls > 5 && gameType != 0) {
				isAbandonClosing = true;
				isAbandon = true;
				checkForFinish();
			} // end if it is considered also an abandon.
		} // end if it is started.
	} // end closeBoard() method.

	// A method for next action, for instance change turn:
	public void next() {
		/*
		 * If we arrived here having still dice available, it means change turn
		 * forcefully:
		 */
		if (dice.getRemainedMoves() > 0) {
			cannotMoveEffects(GUITools.aOpponents[isTurn]);
		} // end if still dice available.
		/*
		 * We must consume the dice, if we arrived here because no available
		 * moves. Anyway, the dice must be consumed for the next turn of the
		 * game:
		 */
		dice.consumeAll();
		// Change now the turn:
		changeTurn();
	} // end next() method.

	// This method is called when cannot move having dice not used:
	private void cannotMoveEffects(final String who) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mSP.playSound(5); // reverse sound for cannot move.
				// Say also that's impossible:
				st.addText(
						String.format(
								(gameType == 1 ? mContext
										.getString(R.string.msg_impossible_move_in_local_game)
										: mContext
												.getString(R.string.msg_impossible_move)),
								who), true);
			}
		}, 300);
	} // end cannotMoveEffects() method.

	// A method which changes turn, from white to black and vice versa:
	private void changeTurn() {
		if (MainActivity.isStarted) {
			// Swap two values for isTurn:
			isTurn = 3 - isTurn;
			if (gameType == 1) {
				changeAutomaticallyBoardPerspective(isTurn);
			} else if (gameType > 1) { // AI or on-line game:
				isOpponentTurn = !isOpponentTurn;
				/*
				 * If AI turn, change the boolean for automatic action in
				 * timerEvent:
				 */
				computerMustStartActions = isOpponentTurn ? true : false;
			} // end if AI or on-line.
			else if (gameType == 0) { // two bots:
				isOpponentTurn = !isOpponentTurn;
				/*
				 * Every time change the boolean for automatic action in
				 * timerEvent:
				 */
				computerMustStartActions = true;
			} // end if two bots.

			String message = determineIsTurnMessage(isTurn);
			st.addText(message, true, 500);
			score.setTurnVisually(isTurn, message);
			// Also keep track the timer for colours:
			if (isTurn == 1)
				timerWhite.resume();
			else
				timerWhite.pause();
		} // end if it is started.
	}// end changeTurn() method.

	// For content description for is turn IVs:
	private String determineIsTurnMessage(int isTurn) {
		String message = "";
		if (isTurn > 0) { // someone's turn:
			message = String.format(isTurnMessage, GUITools.aOpponents[isTurn]);
		} else { // nobody's turn:
			message = res.getString(R.string.msg_nobody_must_move);
		} // end if nobody's turn.
		return message;
	} // end determineIsTurnMessage() method.

	// A method which checks if a pull can be taken by a dice specified:
	private boolean canTakeByDice(int pos, int d1) {
		boolean canTake = true;
		// A condition is to have moves available:
		if (!(dice.getRemainedMoves() > 0)) {
			canTake = false;
		} // end if there are not available moves in dice object.

		/*
		 * A condition if there are pulls there and the colour is at move turn:
		 */
		if (!(aPulls[pos] > 0 && aColors[pos] == isTurn)) {
			canTake = false;
		} // end if there are not pulls and same colour as the turn.
		/*
		 * We check also if there is possible the move according to dice. A die
		 * becomes 0 if it is used and we need to check if a die is greater than
		 * 0.
		 */
		// For white:
		if (isTurn == 1) {
			/*
			 * The order of the checks are: dice greater than 0, from dice to
			 * edge enough space, the position where to go not to be a block:
			 */
			if (!(d1 > 0 && (pos - d1) >= 0 && !(aColors[pos - d1] == 2 && aPulls[pos
					- d1] >= 2))) {
				canTake = false;
			} // end dice verification for white.
		} // end if it is white's turn.

		// For black:
		if (isTurn == 2) {
			/*
			 * The order of the checks are: dice greater than 0, from dice to
			 * edge enough space, the position where to go not to be a block:
			 */
			if (!(d1 > 0 && (pos + d1) <= 23 && !(aColors[pos + d1] == 1 && aPulls[pos
					+ d1] >= 2))) {
				canTake = false;
			} // end dice verification for black.
		} // end if it is black's turn.
		return canTake;
	} // end canTakeByDice() method.

	/*
	 * A method which checks how many moves are available for current colour
	 * turn:
	 */
	private int getAvailableMoves() {
		int avm = 0; // available moves.
		// We need the dice:
		int d1 = dice.getDice(1);
		int d2 = dice.getDice(2);
		boolean isDouble = d1 == d2 ? true : false;
		// First for white colour:
		if (isTurn == 1) {
			if (getPullsOnTheBar(isTurn) > 0) {
				// Check if possible to enter with first die:
				if (d1 > 0
						&& (aPulls[24 - d1] <= 1 || aColors[24 - d1] == isTurn)) {
					avm++;
				} // end for first die.
					// Check if possible to enter with second die:
				if (d2 > 0
						&& (aPulls[24 - d2] <= 1 || aColors[24 - d2] == isTurn)) {
					if (!isDouble)
						avm++;
				} // end for second die.
			} // end if there are on the bar for white.
			else { // no pulls on the bar:
					// Go in a for from 23 to 0 and check if can be taken or
					// bear off:
				for (int i = 23; i >= 0; i--) {
					if (canTakeByDice(i, d1)) {
						avm++; // increment for each pull which can be taken.
					}
					if (!isDouble && canTakeByDice(i, d2)) {
						avm++; // increment for each pull which can be taken.
					}
					if (canBearOff(i)) {
						avm++; // increment for each pull which can be taken.
					}
				} // end for.
			} // end if no pulls are on the bar.
		} // end for white colour.

		// Now also for black colour:
		if (isTurn == 2) {
			if (getPullsOnTheBar(isTurn) > 0) {
				// Check if possible to enter with first die:
				if (d1 > 0
						&& (aPulls[d1 - 1] <= 1 || aColors[d1 - 1] == isTurn)) {
					avm++;
				} // end for first die.
					// Check if possible to enter with second die:
				if (d2 > 0
						&& (aPulls[d2 - 1] <= 1 || aColors[d2 - 1] == isTurn)) {
					if (!isDouble)
						avm++;
				} // end for second die.
			} // end if there are on the bar for black.
			else { // no pulls on the bar:
					// Go in a for from 0 to 23 and check if can be taken or
					// bear off:
				for (int i = 0; i <= 23; i++) {
					if (canTakeByDice(i, d1)) {
						avm++; // increment for each pull which can be taken.
					}
					if (!isDouble && canTakeByDice(i, d2)) {
						avm++; // increment for each pull which can be taken.
					}
					if (canBearOff(i)) {
						avm++; // increment for each pull which can be taken.
					}
				} // end for.
			} // end if no pulls are on the bar.
		} // end for black colour.
		return avm;
	} // end getAvailableMoves() method.

	// A method which returns the pulls on the bar for a colour:
	private int getPullsOnTheBar(int color) {
		return aBorderPulls[color + 1];
	} // end getPullsOnTheBar() method.

	// A method to get pulls in inner boards:
	private int getPullsInInnerBoards(int color) {
		int pib = 0; // pulls in inner boards.
		// Count first for white case:
		if (color == 1) {
			for (int i = 0; i < 6; i++) {
				if (aPulls[i] > 0 && aColors[i] == 1) {
					pib += aPulls[i];
				} // end if there are white pulls there.
			} // end for.
		} // end if white colour.

		// Count second for black:
		else if (color == 2) {
			for (int i = 23; i >= 18; i--) {
				if (aPulls[i] > 0 && aColors[i] == 2) {
					pib += aPulls[i];
				} // end if there are black pulls there.
			} // end for.
		} // end if black colour.

		return pib;
	} // end getPullsInInnerBoards() method.

	// A method which counts the pulls bear off:
	private int getBearOffPulls(int color) {
		return aBorderPulls[color - 1];
	} // end getBearOffPulls() method.

	// A method which checks if is time to bear off:
	private boolean isTimeToBearOff(int isTurn) {
		boolean itb = true; // we make it false if needed.
		// If not started it must be also false:
		if (!MainActivity.isStarted) {
			itb = false;
		}
		// First, if there are pulls on the bar, it is no time:
		if (getPullsOnTheBar(isTurn) > 0) {
			itb = false;
		} // end if there are pulls on the bar.

		// If the sum of the bear off and in home is not 15:
		if (!((getPullsInInnerBoards(isTurn) + getBearOffPulls(isTurn)) >= 15)) {
			itb = false;
		} // end if sum of bear off and in homes are not 15.

		return itb;
	} // end isTimeToBearOff() method.

	// A method which checks if can bear off a position:
	private boolean canBearOff(int pos) {
		boolean canBear = false;
		/* This is only if there are pulls there and if is time to bear off: */
		if (aPulls[pos] > 0 && aColors[pos] == isTurn
				&& isTimeToBearOff(isTurn)) {
			// We need now the dice:
			int d1 = dice.getDice(1);
			int d2 = dice.getDice(2);

			if (isTurn == 1) { // white colour turn:
				// First dice:
				if (d1 > 0) {
					/*
					 * The order conditions are: dice value same as the
					 * position, or, dice greater than position but no other
					 * pieces towards centre of the table.
					 */
					if (d1 == (pos + 1)
							|| (d1 > (pos + 1) && !arePullsGreaterInHome(pos))) {
						canBear = true;
					}
				} // end first dice.
					// Second dice:
				if (d2 > 0) {
					if (d2 == (pos + 1)
							|| (d2 > (pos + 1) && !arePullsGreaterInHome(pos))) {
						canBear = true;
					}
				} // end second dice.
			} // end white turn.
			else if (isTurn == 2) { // black's turn:
				// First dice:
				if (d1 > 0) {
					if (d1 == (24 - pos)
							|| (d1 > (24 - pos) && !arePullsGreaterInHome(pos))) {
						canBear = true;
					}
				} // end first dice.
					// Second dice:
				if (d2 > 0) {
					if (d2 == (24 - pos)
							|| (d2 > (24 - pos) && !arePullsGreaterInHome(pos))) {
						canBear = true;
					}
				} // end second dice.
			} // end black's turn.
		} // end if is time to bear off.
		return canBear;
	} // end canBearOff() method.

	/*
	 * This method checks if there are pull in home board on a greater position
	 * than current position, useful to know if it is possible to bear a checker
	 * using a dice greater as value than current position:
	 */
	private boolean arePullsGreaterInHome(int pos) {
		boolean are = false;
		if (isTurn == 1) { // white's turn:
			for (int i = (pos + 1); i < 6; i++) {
				if (aPulls[i] > 0 && aColors[i] == 1) {
					are = true;
					break;
				}
			} // end for.
		} else if (isTurn == 2) { // black's turn:
			for (int i = pos - 1; i >= 18; i--) {
				if (aPulls[i] > 0 && aColors[i] == 2) {
					are = true;
					break;
				}
			} // end for.
		} // end black turn.
		return are;
	} // end arePullsGreaterInHome() method.

	// A method which reacts at the long click on a position:
	public void positionLongClicked(int pos) {
		if (gameType == 0 && MainActivity.isStarted && wasFirsDiceRolled) {
			suspendedInTwoBotsGames();
		} // end if it is during the game two bots.
		else {
			if (isPause) {
				pauseClicks();
			} else if (isSuspended || isOpponentTurn) {
				suspendedClicks();
			} else { // can act:
				beforeBearOff(pos);
			} // end if it is not paused.
		} // end if not two bots game.
	}// end positionLongClicked() method.

	private void beforeBearOff(int pos) {
		// If is black perspective, change the position number accordingly:
		if (isColor == 2) {
			pos = 23 - pos;
		} // end if it is black perspective.

		// check if can bear off::
		if (canBearOff(pos)) {
			bearOffPull(pos);
		} else { // impossible to bear off there:
			mSP.playSoundPositioned(9, pos); // forbidden sound.
		} // end if not possible to bear off there.
	} // end beforeBearOff() method.

	private void bearOffPull(int pos) {
		aPulls[pos]--; // decrement with one.
		// Make the colour 0 if no more pulls there:
		if (aPulls[pos] == 0) {
			aColors[pos] = 0;
		}
		// Put also the pull on the right bar:
		aBorderPulls[isTurn - 1]++;
		// Set also the colour on the bar:
		aBorderColors[isTurn - 1] = isTurn;
		// Send the move to the board:
		board.removePull(pos, aPulls[pos], aColors[pos]);
		// Add it effectively on the right bar:
		board.putPullOnBorder(isTurn - 1, aBorderPulls[isTurn - 1],
				aBorderColors[isTurn - 1]);
		// Announce also this action:
		String message = String.format(
				res.getString(R.string.msg_removed_from),
				st.determinePositionName(pos, cdNotationType));
		st.addText(message, true);

		// Make Also a sound:
		mSP.playSoundPositioned(8, isTurn == 1 ? 0 : 23, 300);

		// Consume the dice and one available move:
		// Determine used dice:
		if (isTurn == 1) {
			dice.consumeDice(pos + 1);
		} else if (isTurn == 2) { // if is black's turn:
			dice.consumeDice(24 - pos);
		} // end if it is black's turn.

		// Let's check if it's winner here:
		checkForFinish();

		// If no remained moves or not finish, go next:
		if (MainActivity.isStarted
				&& (dice.getRemainedMoves() <= 0 || getAvailableMoves() == 0)) {
			this.next();
		} // end if no remained moves available or not finished.
	} // end bearOffPull() method.

	// A method which reacts at the short click on a position:
	public void positionClicked(int pos) {
		// Here we must act differently if is TV and accessibility:
		if (MainActivity.isAccessibility && MainActivity.isTV && !isInHand
				&& isInInnerBoard(pos)) {
			final int tempPos = pos;
			clicks++; // increment number of clicks.
			if (clicks == 1) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// If clicks is 1 is short click, if 2 is long click:
						if (clicks == 1) {
							allPosibilitiesForPositionClicked(tempPos); // normal
																		// click.
						} else {
							positionLongClicked(tempPos);
						}
						clicks = 0;
					}
				}, 500);
			} // end if number of clicks is 1.
		} else { // normal actions for short click:
			allPosibilitiesForPositionClicked(pos);
		} // end if not TV and accessibility.
	}// end positionClicked() method.

	private void allPosibilitiesForPositionClicked(int pos) {
		if (!MainActivity.isStarted) { // start the game:
			playButtonClicked();
		} // end if it is not started and must start.
		else if (isPause) {
			pauseClicks();
		} // end if it is pause.
		else if (gameType == 0 && MainActivity.isStarted && wasFirsDiceRolled) {
			suspendedInTwoBotsGames();
		} // end if it is during the game two bots.
		else if (isSuspended || isOpponentTurn) {
			suspendedClicks();
		} else if (dice.isTimeToRoll) {
			diceClickedActions();
		} else { // normal actions for position clicked:
			positionClickedActions(pos);
		} // end if is not time to roll, but normal actions.
	} // end allPosibilitiesForPositionClicked() method.

	// The method which acts normally at a click on a position:
	private void positionClickedActions(int pos) {
		// If is black perspective, change the position number accordingly:
		if (isColor == 2) {
			pos = 23 - pos;
		} // end if it is black perspective.

		// Check if must enter hit pulls:
		if (isOnBar()) {
			if (canEnter(pos)) {
				enterPull(pos);
			} else { // impossible to enter there:
				mSP.playSoundPositioned(9, pos); // forbidden sound.
			} // end if not possible to enter there.
		} // end if it is on bar.

		// See if it is in hand or not, first if it is not:
		else if (!isInHand) { // possibly take the pull:
			if (canTake(pos)) {
				takePull(pos);
			} else { // can not move:
				mSP.playSoundPositioned(9, pos); // forbidden sound.
			} // end if cannot take..
		} // end if it is not in hand, but also not on the bar:
			// Check now if it is put back:
		else if (isInHand && posInHand == pos && colorInHand == isTurn) {
			putPullBack(pos);
		} // end if put back.
			// If it is in hand:
		else if (isInHand) { // in hand, put down the pull:
			if (canPut(pos)) {
				putPull(pos);
			} else { // impossible to put there:
				mSP.playSoundPositioned(9, pos); // forbidden sound.
			} // end if not possible to put there.
		} // end if is in hand.
	} // end positionClickedActions() method.

	// A method which checks if there is a pull on bar for current colour turn:
	private boolean isOnBar() {
		boolean isb = false;
		if (isTurn == 1) { // white's turn:
			if (aBorderPulls[2] > 0) {
				isb = true;
			}
		} else if (isTurn == 2) { // black's turn:
			if (aBorderPulls[3] > 0) {
				isb = true;
			}
		} // end if it is black's turn.
		return isb;
	} // end isOnBar() method.

	/*
	 * A method which detects the position where can re-enter and sets the
	 * hints:
	 */
	private void determineAndSetHintsWhereToEnter() {
		if (isTurn == 2) { // black:
			for (int i = 0; i < 6; i++) {
				if (canEnter(i)) {
					board.setHintToEnter(i);
				} // end if can enter there.
			} // end for.
		} // end if black colour.
		else if (isTurn == 1) { // white:
			for (int i = 23; i >= 18; i--) {
				if (canEnter(i)) {
					board.setHintToEnter(i);
				} // end if can enter there.
			} // end for.
		} // end if white colour.
	} // end determineAndSetHintsWhereToEnter() method.

	// A method which checks if a pull can be re-entered:
	private boolean canEnter(int pos) {
		boolean canEnter = true;
		// We need also the dice:
		int d1 = dice.getDice(1);
		int d2 = dice.getDice(2);

		if (!(aPulls[pos] <= 1 || aColors[pos] == isTurn)) {
			canEnter = false;
		} // end first condition.

		// Now check depending of the dice:
		// For white:
		if (isTurn == 1) {
			if (!((d1 > 0 && pos == (23 - d1 + 1)) || (d2 > 0 && pos == (23 - d2 + 1)))) {
				canEnter = false;
			} // end dice verification for white.
		} // end if it is white's turn.

		// For black:
		if (isTurn == 2) {
			/*
			 * The order of the checks are: dice greater than 0 and the position
			 * as the dice
			 */
			if (!((d1 > 0 && pos == (d1 - 1)) || (d2 > 0 && pos == (d2 - 1)))) {
				canEnter = false;
			} // end dice verification for black.
		} // end if it is black's turn.

		return canEnter;
	} // end canEnter() method.

	// A method which enters a hit pull:
	private void enterPull(int pos) {
		boolean isHit = false;
		// First of all, take it from the bar:
		aBorderPulls[isTurn + 1]--; // decrement the number of pulls there.
		// Make also 0 the colour if no pulls anymore:
		if (aBorderPulls[isTurn + 1] == 0) {
			aBorderPulls[isTurn + 1] = 0;
		}
		board.arrangeBorders(aBorderPulls, aBorderColors);

		// Now, put it on the position:
		// If is one opposite, put it on the bar:
		if (aPulls[pos] == 1 && aColors[pos] == (3 - isTurn)) {
			aPulls[pos] = 1;
			// Set it also to appear on the bar:
			isHit = true;
			aBorderPulls[1 + (3 - isTurn)]++;
			aBorderColors[1 + (3 - isTurn)] = 3 - isTurn;
			hitEffects();
			board.arrangeBorders(aBorderPulls, aBorderColors);
			// end set to appear on the bar.
		} else { // no hit:
			aPulls[pos]++; // increment with one the number of pulls.
		} // end if no hit, only put.
		aColors[pos] = isTurn; // to make the colour sure.
		// Sent the move to the board:
		board.putPull(pos, aPulls[pos], aColors[pos]);
		// Announce also this action:
		if (isHit) {
			String message = String.format(
					res.getString(R.string.msg_enter_and_hit),
					st.determinePositionName(pos, cdNotationType));
			st.addText(message, true);
		} else { // not hit:
			String message = String.format(
					res.getString(R.string.msg_enter_to),
					st.determinePositionName(pos, cdNotationType));
			st.addText(message, true);
		} // end if no hit

		// Consume the dice and one available move:
		// Determine used dice:
		if (isTurn == 1) {
			dice.consumeDice(24 - pos);
		} else if (isTurn == 2) { // if is black's turn:
			dice.consumeDice(pos + 1);
		} // end if it is black's turn.
			// If no remained moves, go next:
		if (dice.getRemainedMoves() <= 0 || getAvailableMoves() == 0) {
			this.next();
		} // end if no remained moves available.
			// Here we must set again the hint where to re-enter if necessary:
		else if (getPullsOnTheBar(isTurn) > 0) {
			determineAndSetHintsWhereToEnter();
		} // end if must set hint for second die for re-enter.
	} // end enterPull() method.

	// A method which checks if a pull can be taken from a position:
	private boolean canTake(int pos) {
		boolean canTake = true;
		// We need also the dice:
		int d1 = dice.getDice(1);
		int d2 = dice.getDice(2);
		// A condition is to have moves available:
		if (!(dice.getRemainedMoves() > 0)) {
			canTake = false;
		} // end if there are not available moves in dice object.

		/*
		 * A condition if there are pulls there and the colour is at move turn:
		 */
		if (!(aPulls[pos] > 0 && aColors[pos] == isTurn)) {
			canTake = false;
		} // end if there are not pulls and same colour as the turn.
		/*
		 * We check also if there is possible the move according to dice. A die
		 * becomes 0 if it is used and we need to check if a die is greater than
		 * 0.
		 */
		// For white:
		if (isTurn == 1) {
			/*
			 * The order of the checks are: dice greater than 0, from dice to
			 * edge enough space, the position where to go not to be a block:
			 */
			if (!((d1 > 0 && (pos - d1) >= 0 && !(aColors[pos - d1] == 2 && aPulls[pos
					- d1] >= 2)) || (d2 > 0 && (pos - d2) >= 0 && !(aColors[pos
					- d2] == 2 && aPulls[pos - d2] >= 2)))) {
				canTake = false;
			} // end dice verification for white.
		} // end if it is white's turn.

		// For black:
		if (isTurn == 2) {
			/*
			 * The order of the checks are: dice greater than 0, from dice to
			 * edge enough space, the position where to go not to be a block:
			 */
			if (!((d1 > 0 && (pos + d1) <= 23 && !(aColors[pos + d1] == 1 && aPulls[pos
					+ d1] >= 2)) || (d2 > 0 && (pos + d2) <= 23 && !(aColors[pos
					+ d2] == 1 && aPulls[pos + d2] >= 2)))) {
				canTake = false;
			} // end dice verification for black.
		} // end if it is black's turn.
		return canTake;
	} // end canTake() method.

	// The method which takes the pull:
	private void takePull(int pos) {
		aPulls[pos]--; // decrement with one.
		posInHand = pos;
		colorInHand = aColors[pos];
		// Make the colour 0 if no more pulls there:
		if (aPulls[pos] == 0) {
			aColors[pos] = 0;
		}
		// Send the move to the board:
		board.takePull(pos, aPulls[pos], aColors[pos]);
		isInHand = true;
		// Say also about it:
		String message = String.format(res.getString(R.string.msg_taken_from),
				st.determinePositionName(pos, cdNotationType));
		st.addText(message, true);
		// Set also hints for positions where is possible to put in future:
		determineAndSetHintsWhereToPut();
	} // end takePull() method.

	/* A method which detects the position where can be put and sets the hints: */
	private void determineAndSetHintsWhereToPut() {
		if (isTurn == 1) { // white's turn:
			for (int i = posInHand - 1; i >= 0; i--) {
				if (canPut(i)) {
					board.setHintToPut(i);
				} // end if can put there.
			} // end for.
		} // end white's turn.
		else if (isTurn == 2) { // black's turn:
			for (int i = posInHand + 1; i < 24; i++) {
				if (canPut(i)) {
					board.setHintToPut(i);
				} // end if can put there.
			} // end for.
		} // end black's turn.
	} // end determineAndSetHintsWhereToPut() method.

	// A method which checks if a pull can be put in a position:
	private boolean canPut(int pos) {
		boolean canPut = true;
		// We need also the dice:
		int d1 = dice.getDice(1);
		int d2 = dice.getDice(2);

		/*
		 * Check if nothing or only one there or is the same colour
		 */
		if (!(aPulls[pos] <= 1 || aColors[pos] == colorInHand)) {
			canPut = false;
		} // end first condition.

		// Now check depending of the dice:
		// For white:
		if (isTurn == 1) {
			/*
			 * The order of the checks are: dice greater than 0 and the position
			 * to be difference from initial position, posInHand:
			 */
			if (!((d1 > 0 && (posInHand - d1) == pos) || (d2 > 0 && (posInHand - d2) == pos))) {
				canPut = false;
			} // end dice verification for white.
		} // end if it is white's turn.

		// For black:
		if (isTurn == 2) {
			if (!((d1 > 0 && (posInHand + d1) == pos) || (d2 > 0 && (posInHand + d2) == pos))) {
				canPut = false;
			} // end dice verification for black.
		} // end if it is black's turn.

		return canPut;
	} // end canPut() method.

	// A method which puts the pull on a position:
	private void putPull(int pos) {
		boolean isHit = false;
		// If is one opposite, put it on the bar:
		if (aPulls[pos] == 1 && aColors[pos] == (3 - isTurn)) {
			aPulls[pos] = 1;
			// Set it also to appear on the bar:
			isHit = true;
			aBorderPulls[1 + (3 - isTurn)]++;
			aBorderColors[1 + (3 - isTurn)] = 3 - isTurn;
			hitEffects();
			board.arrangeBorders(aBorderPulls, aBorderColors);
			// end set to appear on the bar.
		} else { // no hit:
			aPulls[pos]++; // increment with one the number of pulls.
		} // end if no hit, only put.
		aColors[pos] = colorInHand; // to make the colour sure.
		// Sent the move to the board:
		board.putPull(pos, aPulls[pos], aColors[pos]);
		isInHand = false; // no in hand anymore.
		// Announce also this action:
		if (isHit) {
			String message = String.format(
					res.getString(R.string.msg_put_and_hit),
					st.determinePositionName(pos, cdNotationType));
			st.addText(message, true);
		} else { // not hit:
			String message = String.format(res.getString(R.string.msg_put_to),
					st.determinePositionName(pos, cdNotationType));
			st.addText(message, true);
		} // end if no hit
			// Announce if necessary if all checkers are in home board:
		allCheckersAreInHomeBoardEffects();

		// Consume the dice and one available move:
		// Determine used dice:
		if (isTurn == 1) {
			dice.consumeDice(posInHand - pos);
		} else if (isTurn == 2) { // if is black's turn:
			dice.consumeDice(pos - posInHand);
		} // end if it is black's turn.
			// If no remained moves, go next:
		if (dice.getRemainedMoves() <= 0 || getAvailableMoves() == 0) {
			this.next();
		} // end if no remained moves available.
	} // end putPull() method.

	// A method which puts effectively , after verification, a checker back:
	private void putPullBack(int pos) {
		isInHand = false;
		aPulls[pos]++; // increment with one the number of pulls.
		aColors[pos] = colorInHand;
		board.putPull(pos, aPulls[pos], aColors[pos]);
		String message = String.format(res.getString(R.string.msg_put_back),
				st.determinePositionName(pos, cdNotationType));
		st.addText(message, true);
	} // end putPullBack() method.

	// A method to make sounds and vibrate when an opponent is hit:
	private void hitEffects() {
		// Add also in array for statistics:
		nrOfCaptures[isTurn - 1]++; // increment.
		vibration.vibrate(421); // the length of the hit sound.
		mSP.playSound(10); // Additional hit sound 421 in length.
		mSP.playSoundPositioned(8, (102 - isTurn), 300);
	} // end hitEffects() method.

	// A method which detects that all checkers came into home board:
	private void allCheckersAreInHomeBoardEffects() {
		// white's turn:
		if (!wasAnnouncedAllInHomeBoardWhite && isTurn == 1
				&& getPullsInInnerBoards(1) == 15) {
			wasAnnouncedAllInHomeBoardWhite = true;
			mSP.playSoundPositioned(27, 100, 500);
			// Announce also it in toast or TTS:
			if (isAnnounceAllInHomeBoard) {
				String msg = "";
				if (gameType == 1) {
					msg = res
							.getString(R.string.msg_all_in_home_board_local_games);
				} else {
					msg = res.getString(R.string.msg_all_in_home_board);
				}
				msg = String.format(msg, GUITools.aOpponents[1]);
				st.toastOrTTS(msg, 1500);
			} // end if enabled announcement of all in home board.
		} // end for white.
		else if (!wasAnnouncedAllInHomeBoardBlack && isTurn == 2
				&& getPullsInInnerBoards(2) == 15) {
			wasAnnouncedAllInHomeBoardBlack = true;
			mSP.playSoundPositioned(27, 100, 500);
			// Announce also it in toast or TTS:
			if (isAnnounceAllInHomeBoard) {
				String msg = "";
				if (gameType == 1) {
					msg = res
							.getString(R.string.msg_all_in_home_board_local_games);
				} else {
					msg = res.getString(R.string.msg_all_in_home_board);
				}
				msg = String.format(msg, GUITools.aOpponents[2]);
				st.toastOrTTS(msg, 1500);
			} // end if enabled announcement of all in home board.
		} // end if black.
	} // end allCheckersAreInHomeBoardEffects() method.

	public void borderClicked(int curBorder) {
		if (curBorder == 0) { // right bottom border:
			// Announce positions checkers in home boards:
			String msg = String.format(
					res.getString(R.string.sg_checkers_in_inner_boards),
					getPullsInInnerBoards(1), getPullsInInnerBoards(2));
			Spanned spanned = MyHtml.fromHtml(msg);
			st.toastOrTTS(spanned.toString(), 2000);
		} else if (curBorder == 1) { // right top border:
			// Show necessary dice points to finish:
			StatisticsGame sg = new StatisticsGame(mContext, gameType, aPulls,
					aColors, cdNotationType);
			int whitePointsNeeded = sg.getNecessaryDicePointsToFinish(1,
					getPullsOnTheBar(1), getPullsOnTheBar(2));
			int blackPointsNeeded = sg.getNecessaryDicePointsToFinish(2,
					getPullsOnTheBar(1), getPullsOnTheBar(2));
			String msg = String.format(
					res.getString(R.string.sg_dice_points_to_finish),
					whitePointsNeeded, blackPointsNeeded);
			Spanned spanned = MyHtml.fromHtml(msg);
			st.toastOrTTS(spanned.toString(), 2000);
			sg = null;
		} else if (curBorder == 2) { // middle lower:
			// Announce vulnerable checkers:
			StatisticsGame sg = new StatisticsGame(mContext, gameType, aPulls,
					aColors, cdNotationType);
			String msg = sg.determineVulnerableCheckers();
			Spanned spanned = MyHtml.fromHtml(msg);
			st.toastOrTTS(spanned.toString(), 2000);
			sg = null;
		} else if (curBorder == 3) { // middle upper border:
			// Announce where are vulnerable checkers:
			StatisticsGame sg = new StatisticsGame(mContext, gameType, aPulls,
					aColors, cdNotationType);
			String whites = sg.getAllVulnerableCheckers(1, cdNotationType);
			String blacks = sg.getAllVulnerableCheckers(2, cdNotationType);
			String msg = String.format(
					res.getString(R.string.msg_say_vulnerable_checkers),
					whites, blacks);
			Spanned spanned = MyHtml.fromHtml(msg);
			st.toastOrTTS(spanned.toString(), 2000);
			sg = null;
		} else if (curBorder == 4) { // left lower border:
			// Announce number of possibilities:
			int availableMoves = getAvailableMoves();
			String msg = res.getQuantityString(
					R.plurals.plural_available_moves, availableMoves,
					availableMoves);
			Spanned spanned = MyHtml.fromHtml(msg);
			st.toastOrTTS(spanned.toString(), 2000);
		} else if (curBorder == 5) { // left upper border:
			clock.showTime();
		} // end upper left border clicked.
	} // end borderClicked() method.

	// A method for play button clicked:
	public void playButtonClicked() {
		if (gameType == 0 && wasFirsDiceRolled) {
			suspendedInTwoBotsGames();
		} else if (isSuspended || isOpponentTurn) {
			suspendedClicks();
		} // end if it is suspended.
		else if (!MainActivity.isStarted) { // If is not started:
			startNewGame(gameType);
		} // end if it is not started.
		else if (MainActivity.isStarted && !isPause) { // is not paused, but
														// started:
			pauseCurrentGame();
		} // end if is started and not paused.
		else if (MainActivity.isStarted && isPause) { // resume:
			resumeCurrentGame();
		} // end if it is paused, resume.
	} // end playButtonClicked() method.

	// A method which resumes a paused game:
	private void resumeCurrentGame() {
		isPause = false;
		chronResume();
		setPlayOrPauseButton();
		mSP.playSound(13); // 12 is resume sound.
	} // end resumeCurrentGame() method.

	// A method to pause current game:
	private void pauseCurrentGame() {
		isPause = true;
		chronPause();
		setPlayOrPauseButton();
		mSP.playSound(12); // 12 is pause sound.
	} // end pauseCurrentGame() method.

	// A method which is called when clicks in pause:
	private void pauseClicks() {
		st.toastOrTTS(res.getString(R.string.msg_game_is_paused), 1500);
	} // end pauseClicks() method.

	// A method which is called when clicks in suspended mode:
	private void suspendedClicks() {
		String msg = res.getString(R.string.msg_wait_opponent_turn);
		if (!isOpponentTurn) { // simple suspension:
			msg = res.getString(R.string.msg_please_wait);
		}
		st.toastOrTTS(msg, 1500);
	} // end suspendedClicks() method.

	private void suspendedInTwoBotsGames() {
		String msg = res.getString(R.string.msg_not_not_available_in_two_bots);
		st.toastOrTTS(msg, 1500);
	} // end suspendedInTwoBotsGames() method.

	// A method which starts a new game depending of its type:
	private void startNewGame(int gameType) {
		// Only if not started, start the game:
		if (!MainActivity.isStarted) {
			isSuspended = true;
			int durationGradually = 100;
			if (isArrangeGradually) {
				durationGradually = 4550;
			} // end if isArrangeGradually.
			st.clearHistory();
			setInitialBoard();
			wasAnnouncedAllInHomeBoardWhite = false;
			wasAnnouncedAllInHomeBoardBlack = false;
			// delay the start until the board is set:
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// Do something after some milliseconds:
					// Add also a text info in text zone and history:
					st.addText(mContext
							.getString(R.string.msg_board_arranged_initially),
							true);
					MainActivity.isStarted = true;
					isSuspended = false;
					isPause = false;
					setPlayOrPauseButton();
					dice.makeAnimationTimeToRoll();
				}
			}, durationGradually);
		} // end if it is not started.
	} // end startNewGame() method.

	// Text zone clicked:
	public void textZoneClicked() {
		st.showHistory();
	} // end textZoneClicked() method.

	// A method which fires when a die is clicked:
	public void dieClicked() {
		if (wasFirsDiceRolled && gameType == 0) {
			suspendedInTwoBotsGames();
		} else if (isSuspended || isOpponentTurn) {
			suspendedClicks();
		} // end if it is suspended.
		else if (!MainActivity.isStarted) { // start the game:
			playButtonClicked();
		} // end if it is not started and must start.
		else if (isPause) {
			pauseClicks();
		} // end if it is pause.
		else if (dice.isTimeToRoll) {
			diceClickedActions();
		} // end if it is time to roll the dice.
		else { // not time to roll:
				// We announce the remained moves:
			dice.anounceRemainedMoves();
		} // end if is not time to roll.
	} // end dieClicked() method.

	private void diceClickedActions() {
		if (MainActivity.isStarted) {
			isSuspended = true;
			// See if not first dice rolled:
			if (!wasFirsDiceRolled) { // no first die rolled:
				boolean wasDoubleFirstDice = false;
				dice.rollFirstDice();
				diceRolls++; // increase.
				isTurn = 1; // White starts anyway.
				if (dice.getDice(1) == dice.getDice(2)) { // double not decided:
					diceRolls--; // decreased.
					wasDoubleFirstDice = true;
					// Postpone the message about not decided:
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							String notDecided = res
									.getString(R.string.msg_first_dice_not_decided);
							st.addText(notDecided, true);
							mSP.playSound(5); // sound for can not do anything.
							isSuspended = false;
						}
					}, 2000);
				} else if (dice.getDice(1) > dice.getDice(2)) { // white's turn:
					if (gameType == 1) { // local game:
						// Do nothing, everything is normal, even the board
						// perspective.
					} else { // AI or on-line game:
						if (gameNumberInMatch == 1) {
							opponentStartedAsWhite = false;
							isOpponentTurn = false;
						} else { // no first game in match:
							if (opponentStartedAsWhite) {
								isTurn = 1;
								isOpponentTurn = true;
							} else { // opponent started as black in first game:
								isTurn = 1;
								isOpponentTurn = false;
							} // end if opponent started as black.
						} // end if no first game in match.
					} // end if AI or on-Line with white colour.
				} // end if it is white's turn after first die rolling.
				else { // second die greater after first rolling:
					if (gameType == 1) { // local game:
						isTurn = 2;
						// Change the board perspective if necessary:
						changeAutomaticallyBoardPerspective(isTurn);
					} else { // off-line or AI for black start:
						if (gameNumberInMatch == 1) { // first game in match:
							opponentStartedAsWhite = true;
							isOpponentTurn = true;
							GUITools.swapOpponents();
							swapScores();
							// Make also black perspective of the board:
							isColor = 2;
							board.setBoardPerspective(isColor);
							arrangeBoard(false, false); // false means no
														// gradually, no sound.
						} else { // no first game in match:
							if (opponentStartedAsWhite) {
								isTurn = 2;
								isOpponentTurn = false;
							} else { // opponent started as black:
								isTurn = 2;
								isOpponentTurn = true;
							} // end if started as black in first game.
						} // end if no first game in match for black.
					} // end if it is AI or on-line game.
				} // end if black's turn after first dice.
					// If was double, nothing happens:
				if (!wasDoubleFirstDice) {
					wasDoubleFirstDice = false;
					String message = determineIsTurnMessage(isTurn);
					score.setTurnVisually(isTurn, message);
					// Start also the chronometer:
					chronRestart();
					if (isTurn == 2)
						timerWhite.pause();
					chronResume();
					wasFirsDiceRolled = true;

					// A delay to make game not to be suspended.
					// The delay will be more than the dice animation:
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							// Do something after some milliseconds:
							isSuspended = false; // makes true if make AI Move.
							// If it is now computers turn:
							if (isOpponentTurn) {
								makeAIMoveThread();
							} // end if it is computer's turn.
							else if (!isOpponentTurn && gameType == 0) { // Bot1
								makeAIMoveThread();
							} // end if it is Bot1's turn..
							else { // not conditions above:
									// Do nothing.
							} // end if not conditions above.
						}
					}, 2000);
				} // end if was not double as first.
					// end about first dice.

			} else { // first die already rolled, roll normally:
				dice.roll(isTurn);
				// A delay to check if possible moves, after animation:
				// The delay will be more than the dice animation:
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// Do something after some milliseconds:
						if (getAvailableMoves() == 0) { // no moves available:
							next();
						} // end if no moves available.
						else {
							/*
							 * If there are hit pulls, check if can re_enter and
							 * set the hint:
							 */
							if (getPullsOnTheBar(isTurn) > 0) {
								determineAndSetHintsWhereToEnter();
							} // end if are pulls on bar.
							if (isOpponentTurn || gameType == 0) { // computer:
								makeAIMoveThread();
							}
						} // end if moves available.
						isSuspended = false;
					}
				}, 2000);
			} // end if it is normal dice roll.
		} // end if the game is started.
	} // end diceClickedActions() method.

	/*
	 * A method to change automatically the board perspective for isTurn in
	 * local games:
	 */
	private void changeAutomaticallyBoardPerspective(int isTurn) {
		if (changePerspective || (changePerspectiveTV && MainActivity.isTV)) {
			isColor = isTurn;
			board.setBoardPerspective(isColor);
			arrangeBoard(false, false); // false means no sound.
		} // end if must be changed automatically.
	} // end changeAutomaticallyBoardPerspective() method.

	// A method to arrange the board:
	public void arrangeBoard(boolean isGradually, boolean makeSound) {
		board.arrangeBoard(aPulls, aColors, aBorderPulls, aBorderColors,
				isGradually, makeSound);
	} // end arrangeBoard() method.

	// A method to set the initial board:
	// A method to arrange initial board:
	public void setInitialBoard() {
		if (isArrangeGradually) {
			clearBoard();
		} // end if it is gradually.

		// Set the parallel arrays to contains pieces for initial board:
		aPulls = new int[] { 2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, 5, 5, 0, 0, 0, 3,
				0, 5, 0, 0, 0, 0, 2 };
		aColors = new int[] { 2, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 2, 1, 0, 0, 0,
				2, 0, 2, 0, 0, 0, 0, 1 };

		// Set the borders to be like at the start:
		aBorderPulls = new int[] { 0, 0, 0, 0, 0, 0 };
		aBorderColors = new int[] { 0, 0, 0, 0, 0, 0 };

		// Show a message about arranging:
		st.addText(mContext.getString(R.string.msg_setting_up_board), false);

		// Now arrange the board accordingly to parallel arrays:
		arrangeBoard(isArrangeGradually, true);
	} // end arrangeInitialBoard() Method().

	// A method which clears the board:
	private void clearBoard() {
		for (int i = 0; i < aPulls.length; i++) {
			aPulls[i] = 0;
			aColors[i] = 0;
		} // end for empty all positions.

		// Make also the borders in their initial state:
		aBorderPulls = new int[] { 15, 15, 0, 0, 0, 0 };
		aBorderColors = new int[] { 1, 2, 0, 0, 0, 0 };

		board.arrangeBoard(aPulls, aColors, aBorderPulls, aBorderColors, false,
				false);
	} // end clearBoard() method.

	// A method to get pulls in opposite inner boards:
	private int getPullsInOppositeInnerBoard(int color) {
		int pib = 0; // pulls in inner boards.
		// Count first for black case:
		if (color == 2) {
			for (int i = 0; i < 6; i++) {
				if (aPulls[i] > 0 && aColors[i] == 2) {
					pib += aPulls[i];
				} // end if there are black pulls there.
			} // end for.
		} // end if black colour.

		// Count second for white:
		else if (color == 1) {
			for (int i = 23; i >= 18; i--) {
				if (aPulls[i] > 0 && aColors[i] == 1) {
					pib += aPulls[i];
				} // end if there are white pulls there.
			} // end for.
		} // end if white colour.

		return pib;
	} // end getPullsInOppositeInnerBoard() method.

	// A method to check if a position is in inner boards:
	private boolean isInInnerBoard(int pos) {
		boolean isIn = false;
		if (pos >= 18 && pos <= 23) {
			isIn = true;
		} else if (pos >= 0 && pos <= 5) {
			isIn = true;
		}
		return isIn;
	} // end isInInnerBoard() method.

	// A method to abandon the game:
	@SuppressLint("InflateParams")
	public void abandon() {
		if (MainActivity.isStarted && gameType != 0) {
			/*
			 * The abandon is only if number of rolls i greater than 5 and it is
			 * your turn:
			 */
			int[] stats = dice.getStatsValues();
			int nrRolls = stats[0] + stats[1];
			if (wasFirsDiceRolled && nrRolls > 5) {
				// Now if it is not opponent turn and local game:
				if (!isOpponentTurn || gameType == 1) {
					// We must create an alert to as if it is sure:
					// Make an alert with the question:
					// Get the strings to make an alert:
					String tempTitle = res.getString(R.string.title_abandon);

					// Inflate the body of the alert:
					LayoutInflater inflater = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View questionView = inflater.inflate(
							R.layout.question_abandon_game, null);

					AlertDialog.Builder alert = new AlertDialog.Builder(
							mContext);
					alert.setTitle(tempTitle);
					alert.setView(questionView);
					alert.setIcon(R.drawable.ic_launcher)
							.setPositiveButton(R.string.bt_yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											isAbandon = true;
											checkForFinish();
										}
									}).setNegativeButton(R.string.bt_no, null)
							.show();
				} // end if not opponent turn.
				else { // opponent turn:
					GUITools.alert(mContext, res.getString(R.string.warning),
							res.getString(R.string.abandon_not_your_turn));
				} // end if opponent turn.
			} // end if there are enough rolls to abandon.
			else { // to early for abandon:
				GUITools.alert(mContext, res.getString(R.string.warning),
						res.getString(R.string.abandon_not_enough_rolls));
			} // end if not enough rolls.
		} // end if it is started.
	} // end abandon() method.

	// A method to open a saved game:
	public void open() {
		// Not implemented yet.
	} // end open() method.

	// A method to save current game:
	public void save() {
		// Not implemented yet.
	} // end save() method.

	// A method which checks if the game is finished:
	private void checkForFinish() {
		if (MainActivity.isStarted) {
			// Let's see if there are 15 pulls on the right bar for current
			// colour or is an abandon:
			if (getBearOffPulls(isTurn) >= 15 || isAbandon) {
				MainActivity.isStarted = false;
				isPause = false;
				setPlayOrPauseButton();
				wasFirsDiceRolled = false; // to start again from scratch.
				dice.consumeAll();
				setGameWinOrLostActions();
			} // end if there are 15 pulls for current colour.
		} // end if it is started.
	} // end checkForFinish() method.

	// A method where we show win or lost things:
	private void setGameWinOrLostActions() {
		int winType = 1; // one point win.
		if (!isAbandon) { // not an abandon:
			// Check if it is gammon, backgammon or double backgammon:
			int looserColor = 3 - isTurn;
			if (getBearOffPulls(looserColor) == 0) {
				// It is at least a gammon:
				winType = 2;
				/*
				 * If there is at least one pull in opposite inner board, it
				 * means even a backgammon:
				 */
				if (getPullsInOppositeInnerBoard(looserColor) > 0) {
					winType = 3;
				} // end if looser has pulls in opposite inner board.
					// But if it has pulls on the bar is double backgammon:
				if (getPullsOnTheBar(looserColor) > 0) {
					winType = 4;
				} // end if has pulls on the bar.
			} // end if looser didn't bear off any pull.
		} // end if it is not abandon.
		else { // if it is abandon:
				// Here we must determine if abandon was for Gammon or
				// Backgammon.
			// winType = getWinTypeAfterAbandon();
		} // end if abandon.

		/*
		 * We charge different message, depending of the game type, verbs in
		 * other language is accorded differently if is White or black the
		 * subject or a nickname:
		 */
		String message = (gameType == 1 ? res
				.getString(R.string.msg_winner_in_local_game) : res
				.getString(R.string.msg_winner));
		String[] aPointsWon = res.getStringArray(R.array.msg_win_types_array);

		// Determine colour winner:
		int winnerColor = (isTurn == 1 ? 1 : 2);
		// If abandon we reverse the colorWinner:
		if (isAbandon) {
			winnerColor = 3 - winnerColor;
		} // end if it is an abandon.

		// Write also the message on text zone:
		message = String.format(message,
				GUITools.aOpponents[isAbandon ? 3 - isTurn : isTurn],
				aPointsWon[winType]);
		// If it is abandon, set also something before the message, the notice
		// about an abandon:
		if (isAbandon) {
			String abandonMessage = String.format(
					(gameType == 1 ? res
							.getString(R.string.abandon_message_in_local_game)
							: res.getString(R.string.abandon_message)),
					GUITools.aOpponents[isTurn]);
			message = abandonMessage + " " + message;
		} // end if it is abandon.

		// We must change also the score:
		if (winnerColor == 1) { // white won:
			scoreWhite = scoreWhite + winType;
		} else { // black won:
			scoreBlack = scoreBlack + winType;
		} // end if black won.

		int youWon = 0; // for AI or on-line games:
		if (gameType == 0 || gameType == 1) { // two in same room:
			// We put only the winner sounds because both players are there:
			// Only if it is not abandon closing:
			if (!isAbandonClosing) {
				SoundPlayer.playSimple(mContext, "win" + winType);
			} // end if is not abandon closing.
		} // end if gameType is 1, two in same room.
		else if (gameType == 2) { // against AI:
			// Here we need to play also lost sounds if user lost:
			if (winnerColor == 1 && !opponentStartedAsWhite || winnerColor == 2
					&& opponentStartedAsWhite) { // user won:
				if (!isAbandonClosing) {
					SoundPlayer.playSimple(mContext, "win" + winType);
				} // end if it is not abandon closing.
				youWon = 1;
			} else { // computer won:
				if (!isAbandonClosing) {
					SoundPlayer.playSimple(mContext, "lose" + winType);
				} // end if is not abandon closing.
				youWon = 0;
			} // end if computer won.
		} // end if AI game.

		// Write the message on the text zone:
		st.addText(message, true);
		// Set also the score visually effectively:
		score.setScore(scoreWhite, scoreBlack);
		int score1 = scoreWhite;
		int score2 = scoreBlack;
		if ((gameType == 0 || gameType == 2) && opponentStartedAsWhite) {
			score1 = scoreBlack;
			score2 = scoreWhite;
		}
		score.saveScore(score1, score2);
		// Make not to appears one's turn:
		score.setTurnVisually(0, determineIsTurnMessage(0));

		// Post also the statistics into DB:
		// Let's see nickname and nickname2:
		String nickname = "";
		String nickname2 = "";
		if (gameType == 0 || gameType == 2) { // AI games
			if (opponentStartedAsWhite) {
				nickname = GUITools.aOpponents[2];
			} else {
				nickname = GUITools.aOpponents[1];
			} // end if computer was white.
		} // end if gameType was AI.
		else if (gameType == 3) {
			if (opponentStartedAsWhite) {
				nickname = GUITools.aOpponents[2];
				nickname2 = GUITools.aOpponents[1];
			} else {
				nickname = GUITools.aOpponents[1];
				nickname2 = GUITools.aOpponents[2];
			} // end if opponent was white.
		} // end if gameType was on-line game.
		int abandonType = 0;
		if (isAbandonClosing) {
			abandonType = 2;
		} else if (isAbandon) {
			abandonType = 1;
		}
		Statistics stats = new Statistics(mContext);
		stats.postFinishedTestLocally(MainActivity.myAccountName, nickname,
				nickname2, gameType, (MiniMax.maxDepth + 1), winType,
				winnerColor, youWon, timer.getElapsed(),
				timerWhite.getElapsed(), dice.getStatsValues(), nrOfCaptures,
				(MainActivity.isTV ? 1 : 0), abandonType, 0);
		stats = null;

		// Save also the game history into database if enabled:
		if (isHistory) {
			// Call a method to get the array list of game statistics:
			showOrGetGameStatistics(1);
			st.insertHistoryIntoDB(arrGameStatistics);
		} // end if is history.

		// Other things at the end of a game:
		afterGameOver();

		/*
		 * Try to rate the application if it is winning or gameType1, and not
		 * abandoned.
		 */
		if ((!isAbandon && !isAbandonClosing) && (youWon == 1 || gameType == 1)) {
			tryToRate();
		} // end if moment for rate.
	} // end setGameWinOrLostActions() method.

	/*
	 * private int getWinTypeAfterAbandon() { return 0; } // end
	 * getWinTypeAfterAbandon() method.
	 */

	// A method to do things after game is finished:
	private void afterGameOver() {
		isAbandon = false;
		isOpponentTurn = false;
		gameNumberInMatch++; // increment the number of games.
		chronPause();
		timer.pause();
		timerWhite.pause();
	} // end thingsAtGameOver() method.

	// Methods to start, reset and stop the Chronometer:
	public void chronResume() {
		timer.resume();
		if (isTurn == 1) { // it is white's turn:
			timerWhite.resume();
		}
		chron.setBase(timer.getElapsedSinceDeviceStart() - timer.getElapsed());
		chron.start();
	} // end chronResume() method.

	public void chronPause() {
		timer.pause();
		if (isTurn == 1) { // it is white's turn:
			timerWhite.pause();
		}
		chron.stop();
	} // end chronPause() method.

	public void chronRestart() {
		timer.restart();
		timerWhite.restart();
		chron.setBase(timer.getElapsedSinceDeviceStart() - timer.getElapsed());
	} // end chronRestart() method.
		// end chronometer methods.

	// A method to make play or pause button:
	private void setPlayOrPauseButton() {
		ImageButton ib = (ImageButton) mActivity.findViewById(R.id.ibPlay);
		if (!MainActivity.isStarted && !isPause) { // not started, not pause:
			// Set the play button:
			ib.setImageResource(R.drawable.button_play);
			ib.setContentDescription(res.getString(R.string.cd_bt_play));
		} // end if is started and not pause.
		else if (MainActivity.isStarted && !isPause) { // paused game when
														// started:
			ib.setImageResource(R.drawable.button_pause);
			ib.setContentDescription(res.getString(R.string.cd_bt_pause));
		} // end if it is paused.
		else if (MainActivity.isStarted && isPause) { // started, and pause:
			ib.setImageResource(R.drawable.button_play);
			ib.setContentDescription(res.getString(R.string.cd_bt_resume));
		} // end if it is pause when started.
	} // end setPlayOrPauseButton() method.

	// This is called from GameActivity each second:
	private int secondsElapsed = 0;

	public void timerEvent() {
		secondsElapsed++; // increment each second.
		if ((gameType == 0 || gameType == 2) && computerMustStartActions) {
			computerMustStartActions = false;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					diceClickedActions();
				}
			}, 2000);
		} // end if computer must turn;
		if (MainActivity.isClock) {
			clock.checkForAnnouncedTime();
		} // end if cuckoo clock is enabled.
		/*
		 * Play opponent is thinking sound:
		 */
		if (opponentIsThinking
				&& secondsElapsed % thinkingOpponentSoundInterval == 0) {
			mSP.playSound(15); // thinking computer.
		} // end if must play thinking sound for opponent.
	} // end timerEvent() method.

	// A method which starts the game statistics alert:
	public void showOrGetGameStatistics(int getType) {
		StatisticsGame sg = new StatisticsGame(mContext, gameType, aPulls,
				aColors, cdNotationType);
		int availableMoves = getAvailableMoves();
		String timeElapsed = null;
		if (MainActivity.curAPI >= 21
				&& GUITools.isAccessibilityEnabled(mContext)) {
			timeElapsed = chron.getContentDescription().toString();
		} // end if new API and accessibility.
		if (timeElapsed == null
				|| !(GUITools.isAccessibilityEnabled(mContext) && timeElapsed != null)) {
			timeElapsed = chron.getText().toString();
		} // end if no available content description.
		int t = timer.getElapsed();
		int tw = timerWhite.getElapsed();
		int inWhite = getPullsInInnerBoards(1);
		int inBlack = getPullsInInnerBoards(2);
		int whiteBar = getPullsOnTheBar(1);
		int blackBar = getPullsOnTheBar(2);
		int whiteRemoved = getBearOffPulls(1);
		int blackRemoved = getBearOffPulls(2);
		int[] diceStats = dice.getStatsValues();
		sg.getGameStatistics((MainActivity.isStarted ? isTurn : 0),
				availableMoves, timeElapsed, t, tw, inWhite, inBlack, whiteBar,
				blackBar, whiteRemoved, blackRemoved, diceStats, nrOfCaptures);
		if (getType == 0) { // show in an alert:
			sg.showStatisticsAlert();
		} else if (getType == 1) { // get only the array list:
			arrGameStatistics = sg.getArrStatistics();
		} // end if getType is 1.
	} // end showOrGetGameStatistics() method.

	// A method called from activity when chronometer is clicked:
	public void chronClicked() {
		if (MainActivity.isStarted && wasFirsDiceRolled) {
			String timeElapsed = chron.getContentDescription().toString();
			if (timeElapsed == null) {
				timeElapsed = chron.getText().toString();
			} // end if no available content description.

			// Need an object of type SG:
			StatisticsGame sg = new StatisticsGame(mContext, gameType, aPulls,
					aColors, cdNotationType);
			String msg = sg.determineTimeElapsedInfo(timeElapsed,
					timer.getElapsed(), timerWhite.getElapsed());
			st.toastOrTTS(msg, 1500);
		} // end if it is started.
	} // end chronClicked() method.

	// Methods for AI things:
	private BoardState convertBoardFromMyToAI() {
		/*
		 * Make an array to match BoardState requirements - white negative
		 * numbers, black positive, but white AI play ending at 23:
		 */
		int[] bsBoard = new int[24];
		for (int i = 0; i <= 23; i++) {
			if (aColors[i] == 2) { // black pieces:
				bsBoard[23 - i] = aPulls[i] * -1;
			} else { // white pieces:
				bsBoard[23 - i] = aPulls[i];
			} // end if white pieces.
		} // end for.
			// Create the object adding also checkers on the borders:
		BoardState mState = new BoardState(bsBoard, getPullsOnTheBar(1),
				getPullsOnTheBar(2), getBearOffPulls(1), getBearOffPulls(2));
		return mState;
	} // end convertBoardFromMyToAI() method.

	// Convert now from AI board into my one:
	private void convertBoardFromAIToMy(BoardState state) {
		for (int i = 0; i <= 23; i++) {
			int counts = state.getCountValue(i);
			aPulls[23 - i] = (counts > 0 ? counts : counts * -1);
			int color = (counts > 0 ? 1 : 2);
			if (counts == 0) {
				color = 0;
			}
			aColors[23 - i] = color;
		} // end for.
			// Put the bear off pieces:
			// White:
		aBorderPulls[2] = state.getNumDead(true);
		aBorderColors[2] = 1;
		// Black:
		aBorderPulls[3] = state.getNumDead(false);
		aBorderColors[3] = 2;
		// Put also dead pieces in the middle:
		// White:
		aBorderPulls[0] = state.getTakenOut(true);
		aBorderColors[0] = 1;
		// Black:
		aBorderPulls[1] = state.getTakenOut(false);
		aBorderColors[1] = 2;
	} // end convertBoardFromAIToMy() method.

	// A new thread method for make AI move:
	private void makeAIMoveThread() {
		new Thread(new Runnable() {
			public void run() {
				// Do task here in this new thread:
				opponentIsThinking = true;
				makeAIMove();
				opponentIsThinking = false;
				// A new thread in this thread:
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Here happens on main thread:
						// Do moves detected in a method:
						showAIMoves();
					}
				});
				// end thread on UI runnable..
			}
		}).start();
		// end thread to do AI move.
	} // end makeAIMove new thread.

	// We need two boards as global:
	private BoardState oldState;
	private BoardState newState;
	private int nrMoves = 0;
	private ArrayList<String> arrMoves = new ArrayList<String>();

	// A method to makeAIMove:
	private void makeAIMove() {
		// Try to make a move:
		int dice1 = dice.getDice(1);
		int dice2 = dice.getDice(2);
		// Determine the ideal number of moves:
		if (dice1 == dice2) { // double:
			nrMoves = 4;
		} else { // not a double:
			nrMoves = 2;
		} // end if not double.

		// Determine whose turn, white or black:
		boolean isWhite = isTurn == 1 ? true : false; // false means is black.

		oldState = convertBoardFromMyToAI();
		newState = MiniMax.makeOptimalMove(oldState, dice1, dice2, isWhite);

		/*
		 * We compare now if the boards are the same, it means no possibility to
		 * use both dice by AI, we start to use them one by one:
		 */
		if (newState.isSameArrangement(oldState)) {
			// If dice are different, no double:
			if (dice1 != dice2) {
				// Try to make moves only with one die:
				// First using the biggest one:
				newState = MiniMax.makeOptimalMove(oldState, dice1, 0, isWhite);
				nrMoves = 1;
				/*
				 * If still are the same boards, means only the second die can
				 * be used:
				 */
				if (newState.isSameArrangement(oldState)) {
					newState = MiniMax.makeOptimalMove(oldState, dice2, 0,
							isWhite);
					nrMoves = 1;
				} // end if after trying first die is still same.
			} // end if it is not a double.
			else { // is a double, dice are equals:
					// We use dice1 4 times or until the boards don't changes
					// anymore:
				int it = 0; // for times.
				do {
					newState = MiniMax.makeOptimalMove(newState, dice1, 0,
							isWhite);
					it++; // increment.
				} while (it < 4 && !newState.isSameArrangement(oldState));
				nrMoves = it;
			} // end if it was a double.
		} // end if the board states are the same.

		/*
		 * Detect each move done by AI if this is set in Settings, movesInterval
		 * must be greater than 0::
		 */
		if (movesInterval > 0) {
			arrMoves = BoardUtils.getMoves(oldState, newState, isWhite, dice1,
					dice2, nrMoves);
		} // end if movesInterval>0, show moves.

		/*
		 * The interface changes are in the UI thread, the methods showAIMove
		 * and afterAI..
		 */
	} // end makeAIMove() method.

	// A method which will show with handler moves:
	private void showAIMoves() {
		// Only if movesInterval is greater than 0:
		if (movesInterval > 0) {
			// A for through arrMovesArray:
			for (int i = 0; i < arrMoves.size(); i++) {
				final int it = i;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						parseAndShowOneMove(arrMoves.get(it));
						// If is last move, make also things after:
						if (it == arrMoves.size() - 1) {
							afterMakeAndShowAiMove();
						} // end if last i of the for.
					}
				}, (i * movesInterval));
			} // end for.
		} // end if movesInterval > 0.
		/*
		 * If now we haven't moves to show, it means we must call manually the
		 * afterMoves method. This happens also if no moves must be shown,
		 * movesInterval is set to 0:
		 */
		if (movesInterval == 0 || arrMoves.isEmpty() || arrMoves.size() == 0) {
			afterMakeAndShowAiMove();
		}
	} // end showAIMoves() method.

	// This method has a string for a move done by AI:
	private void parseAndShowOneMove(String move) {
		// Split the string by vertical bar:
		String[] arrMove = move.split("\\|");
		// Convert the two indexes into integers:
		if (arrMove.length == 2) {
			int moveType = Integer.parseInt(arrMove[0]);
			int position = Integer.parseInt(arrMove[1]);

			// If not opponent turn in two BOTS, reverse the direction:
			if (gameType == 0 && !isOpponentTurn) {
				position = 23 - position;
			} // end if BOT1 at move.

			// A switch on move to use the correct method for movement:
			switch (moveType) {
			case 1: // taken:
				positionClickedActions(position);
				break;

			case 2: // put down.
				positionClickedActions(position);
				break;

			case 3: // remove:
				beforeBearOff(position);
				break;
			} // end switch on moveType..
		} // end if arrMove length is 2.
	} // end parseAndShowOneMove() method.

	// A method to make interface things after logic stuff for AI move:
	private void afterMakeAndShowAiMove() {
		// Next lines are useful to correct the detected moves with true moves:
		/*
		 * We take the new board after each move, convert into BoardState style
		 * and we compare it with the last newState. If they are the same it is
		 * OK, otherwise we must re-arrange the board according to the new state
		 * and also to set some values, like change turn and consume all dice
		 * etc.
		 */
		// newStateNow will be after each move shown:
		BoardState newStateNow = convertBoardFromMyToAI();
		// Now check if they are not the same:
		if (!newStateNow.isSameArrangement(newState)) {
			if (movesInterval > 0) { // delete this if after Monika.
				// GUITools.beep(); // just to know.
			}
			// Convert to our system from the true newBoard:
			convertBoardFromAIToMy(newState);
			arrangeBoard(false, false);

			/*
			 * Now next lines are used in if no all moves where shown if moves
			 * shown by detection are not the same done by AI and we correct the
			 * board:
			 */
			dice.consumeAll();
			checkForFinish();
		} // end if no same board the new ones.
			// If it is not computer turn, we must do it ourselves:
		if (isOpponentTurn && gameType != 0) {
			this.next();
		} // end if it is not your turn yet.
		playIsYourTurnSound();
	} // end afterMakeAndShowAiMove() method.

	private void playIsYourTurnSound() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (MainActivity.isStarted) {
					mSP.playSound(14); // Is your turn sound.
				} // end if it is still started.
			}
		}, 500);
	} // end playIsYourTurn() method.

	// A method to rate the application after a win:
	private void tryToRate() {
		GUITools.checkIfRated(mContext);
	} // end tryToRate() method.

} // end Game class.
