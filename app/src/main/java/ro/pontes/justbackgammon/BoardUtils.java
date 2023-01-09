package ro.pontes.justbackgammon;

import java.util.ArrayList;

public class BoardUtils {

	public static final int BOARD_SIZE = 24;
	public static final int NUM_PIECES = 15;
	public static boolean isOpponentTurn = false;

	public static BoardState getStartingPositionBoard() {
		int[] count = new int[BOARD_SIZE];
		count[0] = +2;
		count[23] = -2;
		count[5] = count[12] = -5;
		count[18] = count[11] = +5;
		count[7] = -3;
		count[16] = +3;
		return new BoardState(count, 0, 0, 0, 0);
	}

	public static BoardState getBoardAfterMovingSinglePiece(BoardState state,
			int boardIndex, int moveLength) {
		boolean whitesTurn = state.isPositionWhite(boardIndex);
		int destIndex = (whitesTurn ? boardIndex + moveLength : boardIndex
				- moveLength);
		int[] count = state.getCountsArray();
		int numWhiteDead = state.getNumDead(true), numBlackDead = state
				.getNumDead(false);
		if (whitesTurn) {
			if (count[destIndex] < 0) {
				numBlackDead++;
				count[destIndex] = 0;
			}
			count[boardIndex]--;
			count[destIndex]++;
		} else {
			if (count[destIndex] > 0) {
				numWhiteDead++;
				count[destIndex] = 0;
			}
			count[boardIndex]++;
			count[destIndex]--;
		}
		return new BoardState(count, numWhiteDead, numBlackDead,
				state.numPutAside(true), state.numPutAside(false));
	} // end getBoardAfterMovingSinglePiece() method.

	public static BoardState getBoardStateAfterPuttingOut(BoardState state,
			int boardIndex, int moveLength) {
		boolean whitesTurn = state.isPositionWhite(boardIndex);
		int[] count = state.getCountsArray();
		int numDeadWhite = state.getNumDead(true), numDeadBlack = state
				.getNumDead(false);
		int numPutWhite = state.numPutAside(true), numPutBlack = state
				.numPutAside(false);
		if (whitesTurn) {
			count[boardIndex]--;
			return new BoardState(count, numDeadWhite, numDeadBlack,
					numPutWhite + 1, numPutBlack);
		} else {
			count[boardIndex]++;
			return new BoardState(count, numDeadWhite, numDeadBlack,
					numPutWhite, numPutBlack + 1);
		}
	} // end getBoardStateAfterPuttingOut() method.

	public static BoardState getBoardForMakingPieceAlive(BoardState state,
			int dice, boolean whitesTurn) {
		int[] count = state.getCountsArray();
		int numDeadWhite = state.getNumDead(true), numDeadBlack = state
				.getNumDead(false);
		int numPutWhite = state.numPutAside(true), numPutBlack = state
				.numPutAside(false);
		if (whitesTurn) {
			if (count[dice - 1] < 0) {
				count[dice - 1] = 0;
				numDeadBlack++;
			}
			count[dice - 1]++;
			numDeadWhite--;
		} else {
			if (count[BoardUtils.BOARD_SIZE - dice] > 0) {
				count[BoardUtils.BOARD_SIZE - dice] = 0;
				numDeadWhite++;
			}
			count[BoardUtils.BOARD_SIZE - dice]--;
			numDeadBlack--;
		}
		return new BoardState(count, numDeadWhite, numDeadBlack, numPutWhite,
				numPutBlack);
	} // end getBoardForMakingPieceAlive() method.

	// A method to take bar differences between two boards for a given colour:
	public static int getBarDifference(BoardState state1, BoardState state2,
			boolean isWhite) {
		int dif = state1.getNumDead(isWhite) - state2.getNumDead(isWhite);
		return dif;
	} // end getBarDifference() method.

	// A method to detect if it is a blockade for a position and colour:
	public static boolean isBlockade(BoardState state, int position,
			boolean isWhite) {
		boolean isB = false;
		if (state.getCountValueForAColor(position, isWhite) >= 2) {
			isB = true;
		}
		return isB;
	} // end isBlockade() method.

	/*
	 * Here we need methods help the addPuttingOutMoves() because it doesn't
	 * know if there are checker nearer the bar. Added by Manu on 25 July 2018,
	 * used in addPuttingOutMoves() method.
	 */
	public static boolean arePullsGreaterInHome(BoardState curState, int pos,
			boolean isWhitesTurn) {
		if (isWhitesTurn) { // white's turn:
			for (int i = pos - 1; i >= 17; i--) {
				if (curState.isPositionWhite(i) && curState.getCountAt(i) > 0)
					return true;
			} // end for.
		} else { // black's turn:
			for (int i = (pos + 1); i <= 6; i++) {
				if (curState.isPositionBlack(i) && curState.getCountAt(i) > 0)
					return true;
			} // end for.
		} // end black turn.
		return false;
	} // end arePullsGreaterInHome() method.

	// A method to clone a board:
	public static BoardState cloneBoardState(BoardState oldState) {
		BoardState newState = new BoardState(oldState.getCountsArray(),
				oldState.getNumDead(true), oldState.getNumDead(false),
				oldState.getTakenOut(true), oldState.getTakenOut(false));
		return newState;
	} // end cloneBoardState() method.

	// A method to clone the board and reverse it also from black to white:
	public static BoardState cloneBlackAsWhiteBoard(BoardState oldState) {
		int[] countsArray = GUITools.reverseArrayOfInts(oldState
				.getCountsArray());
		// Reverse multiplying by minus 1:
		for (int i = 0; i < countsArray.length; i++) {
			countsArray[i] = countsArray[i] * -1;
		} // end for reverse by -1.
		BoardState newState = new BoardState(countsArray,
				oldState.getNumDead(false), oldState.getNumDead(true),
				oldState.getTakenOut(false), oldState.getTakenOut(true));
		return newState;
	} // end cloneBlackAsWhiteBoard() method.

	// A method to reverse only the board:
	public static BoardState reverseTheBoard(BoardState oldState) {
		int[] countsArray = GUITools.reverseArrayOfInts(oldState
				.getCountsArray());
		BoardState newState = new BoardState(countsArray,
				oldState.getNumDead(false), oldState.getNumDead(true),
				oldState.getTakenOut(false), oldState.getTakenOut(true));
		return newState;
	} // end reverseTheBoard() method.

	// A method to determine the moves between two board states:
	public static ArrayList<String> getMoves(BoardState state1,
			BoardState state2, boolean isWhite, int dice1, int dice2,
			int nrMoves) {

		/*
		 * Because we know only to calculate for white perspective for computer
		 * the moves, we convert if it is black turn into white boards and turn:
		 */
		if (isWhite == false) {
			state1 = cloneBlackAsWhiteBoard(state1);
			state2 = cloneBlackAsWhiteBoard(state2);
			// We change also now to be white's turn:
			isWhite = true;
		} // end if it's not white's turn.

		// The arrayList for each move:
		ArrayList<String> arrMoves = new ArrayList<String>();
		boolean isDouble = (dice1 == dice2 ? true : false);

		// A variable to know how many moves we solved:
		int itNrMoves = 0;
		// A variable to be sure the while is not infinite:
		int it = 0;
		// We go through nrMoves with the while:
		while (itNrMoves < nrMoves && it < 10) {
			it++; // increment for second condition in while.
			/*
			 * Check if it is a re-enter, the difference between boards's bars
			 * is greater than 0:
			 */
			int deathDif = getBarDifference(state1, state2, isWhite);
			if (deathDif > 0) { // it means at least one re-enter:
				/*
				 * If it is a double the position re-entered is the dice1
				 * position:
				 */
				if (isDouble) {
					for (int i = 0; i < deathDif; i++) {
						int pos = dice1 - 1;
						arrMoves.add("2|" + pos);
						// get the board after re-enter:
						state1 = getBoardForMakingPieceAlive(state1, dice1,
								isWhite);
						itNrMoves++;
					} // end for each re-enter as double dice.
					continue;
				} // end if it is a double.
				else { // not a double for re-enter:
						// Check for each dice if is available, not 0:
					if (dice1 > 0) {
						// Check if is a the position, new checkers there:
						if (state2.getCountValueForAColor(dice1 - 1, isWhite) > state1
								.getCountValueForAColor(dice1 - 1, isWhite)) {
							// It means the checker was re-entered there:
							int pos = dice1 - 1;
							arrMoves.add("2|" + pos);
							// get the board after re-enter:
							state1 = getBoardForMakingPieceAlive(state1, dice1,
									isWhite);
							itNrMoves++;
							dice1 = 0; // cancel the dice.
						} // end it checker was put there without moving after.
						else if (state2.getCountValueForAColor(dice1 - 1
								+ dice2, isWhite) > state1
								.getCountValueForAColor(dice1 - 1 + dice2,
										isWhite)) {
							// the checker was moved after re-enter:
							/*
							 * We must detect the order of re-enter, first using
							 * dice1, or dice2. It depends of an eventual
							 * blockade or an eventual hit:
							 */
							boolean isFirstD1 = true;
							// Here start detect the order of dice use:
							/*
							 * If is a blockade on dice1 position, impossible to
							 * re-enter with this dice:
							 */
							if (isBlockade(state1, dice1 - 1, !isWhite)) {
								isFirstD1 = false;
							} // end if impossible because blockade.
							else if (state1.getCountValueForAColor(dice2 - 1,
									!isWhite)
									- state2.getCountValueForAColor(dice2 - 1,
											!isWhite) == 1) {
								/*
								 * Detect if a hit was at position of dice2, the
								 * above if checks if one checker disappeared
								 * from dice2 position::
								 */
								isFirstD1 = false;
							} // end if hit at dice2 position.
								// end detect the order of dice use.
							if (isFirstD1) {
								int pos = dice1 - 1;
								arrMoves.add("2|" + pos);
								itNrMoves++;
								// The second step is a move from same position:
								arrMoves.add("1|" + pos); // taken.
								itNrMoves++;
								arrMoves.add("2|" + (pos + dice2)); // put down.
							} // end if isFirstdice1.
							else { // first id dice2:
								int pos = dice2 - 1;
								arrMoves.add("2|" + pos);
								itNrMoves++;
								// The second step is a move from same position:
								arrMoves.add("1|" + pos); // taken.
								itNrMoves++;
								arrMoves.add("2|" + (pos + dice1)); // put down.
							} // end if first is dice2.
							dice1 = 0;
							dice2 = 0;
							break; // impossible something else.
						} // end if checker was moved after re-enter.
					} // end if dice1 was available.
						// Now check re-enter for dice2:
					if (dice2 > 0) {
						// Check if is a the position, new checkers there:
						if (state2.getCountValueForAColor(dice2 - 1, isWhite) > state1
								.getCountValueForAColor(dice2 - 1, isWhite)) {
							// It means the checker was re-entered there:
							int pos = dice2 - 1;
							arrMoves.add("2|" + pos);
							// get the board after re-enter:
							state1 = getBoardForMakingPieceAlive(state1, dice2,
									isWhite);
							itNrMoves++;
							dice2 = 0; // cancel the dice.
						} // end it checker was put there without moving after.
					} // end if dice2 was available.
				} // end if not double for re-enter.
			} // end if at least one re-enter.

			// Now check for moves:
			// We go from 0 to 23 to see where are taken from:
			if (dice1 > 0 || dice2 > 0) {
				for (int i = 0; i <= 23; i++) {
					// Check if there are less checkers here, means taken:
					int dif = state1.getCountValueForAColor(i, isWhite)
							- state2.getCountValueForAColor(i, isWhite);
					if (dif > 0) { // checker were taken:
						/*
						 * Starting here, until finish of the for a lot of
						 * checks to know what happened with the checkers taken:
						 */
						// Now check for double if is on next positions:
						if (isDouble) {
							int curPos = i;
							int nextPos = i + dice1;

							/*
							 * First check if nextPos is not outside, a remove
							 * or not a blockade, but if checkers were taken
							 * with a double, it means no blockade possible:
							 */
							if (nextPos > 23) {
								break; // impossible further.
							} // end if nextPos > 23, remove.
							/*
							 * Now, an else to see how many are in plus on the
							 * next position:
							 */
							else { // not a remove 1:
									// Anyway, the DIF moves were done for
									// dice1:
								for (int j = 0; j < dif; j++) {
									arrMoves.add("1|" + curPos);
									arrMoves.add("2|" + nextPos);
									itNrMoves++;
									state1 = getBoardAfterMovingSinglePiece(
											state1, curPos, dice1);
								} // end for set moves step1.
								/*
								 * Check how many are more in plus on next
								 * position:
								 */
								int movesRemained = nrMoves - itNrMoves;
								if (movesRemained > 0) {
									/*
									 * Check how many were put on nextPos and
									 * let there:
									 */
									int dif2 = state1.getCountValueForAColor(
											nextPos, isWhite)
											- state2.getCountValueForAColor(
													nextPos, isWhite);
									// If dif2 is not 0, other moves:
									if (dif2 > 0) {
										// Again check if it is not outside:
										curPos = nextPos;
										nextPos = nextPos + dice1;
										if (nextPos > 23) {
											continue;
										} // end if outside.
										else { // not outside:
												// Moves equals with dif2 were
												// done:
											for (int k = 0; k < dif2; k++) {
												arrMoves.add("1|" + curPos);
												arrMoves.add("2|" + nextPos);
												itNrMoves++;
												state1 = getBoardAfterMovingSinglePiece(
														state1, curPos, dice1);
											} // end for second moves in double.

											// Another step 3:
											/*
											 * Check how many are more in plus
											 * on next position, step3:
											 */
											movesRemained = nrMoves - itNrMoves;
											if (movesRemained > 0) {
												/*
												 * Check how many were put on
												 * nextPos and let there:
												 */
												int dif3 = state1
														.getCountValueForAColor(
																nextPos,
																isWhite)
														- state2.getCountValueForAColor(
																nextPos,
																isWhite);
												// If dif3 is not 0, other
												// moves:
												if (dif3 > 0) {
													// Again check if it is not
													// outside:
													curPos = nextPos;
													nextPos = nextPos + dice1;
													if (nextPos > 23) {
														continue;
													} // end if outside 3.
													else { // not outside:
															// Moves equals with
															// dif3 were done:
														for (int l = 0; l < dif3; l++) {
															arrMoves.add("1|"
																	+ curPos);
															arrMoves.add("2|"
																	+ nextPos);
															itNrMoves++;
															state1 = getBoardAfterMovingSinglePiece(
																	state1,
																	curPos,
																	dice1);
														} // end for third moves
															// in
															// double.

														// Another step 4:
														/*
														 * Check how many are
														 * more in plus on next
														 * position, step4:
														 */
														movesRemained = nrMoves
																- itNrMoves;
														if (movesRemained > 0) {
															/*
															 * Check how many
															 * were put on
															 * nextPos and let
															 * there:
															 */
															int dif4 = state1
																	.getCountValueForAColor(
																			nextPos,
																			isWhite)
																	- state2.getCountValueForAColor(
																			nextPos,
																			isWhite);
															// If dif4 is not 0,
															// other moves:
															if (dif4 > 0) {
																// Again check
																// if it
																// is not
																// outside:
																curPos = nextPos;
																nextPos = nextPos
																		+ dice1;
																if (nextPos > 23) {
																	break; // 4
																			// moves.
																} // end if
																	// outside
																	// 4.
																else { // not
																		// outside:
																		// Moves
																		// equals
																		// with
																		// dif4
																		// were
																		// done:
																	for (int m = 0; m < dif4; m++) {
																		arrMoves.add("1|"
																				+ curPos);
																		arrMoves.add("2|"
																				+ nextPos);
																		itNrMoves++;
																		state1 = getBoardAfterMovingSinglePiece(
																				state1,
																				curPos,
																				dice1);
																	} // end for
																		// fourth
																		// moves
																		// in
																		// double.

																	// Impossible
																	// more
																	// steps,
																	// we can
																	// break
																	// here:
																	break;
																} // end if not
																	// outside4.
															} // end if dif4 not
																// 0.
														} // end if moveRemained
															// not
															// 0 for 4.
													} // end if not outside3.
												} // end if dif3 not 0.
											} // end if moveRemained not 0 for
												// 3.
										} // end if not outside2.
									} // end if dif2 not 0.
								} // end if moveRemained not 0.
							} // end if not outside1.
						} // end if it is double.

						else { // not double:
								// First if dice1 is available:
							if (dice1 > 0) {
								/*
								 * First not to be outside the board, that means
								 * remove:
								 */
								int nextPos = i + dice1;
								if (nextPos > 23) { // means remove:
									// Do nothing, it will be a remove.
								}
								// Check if it is a simple move, this dice:
								else if (state2.getCountValueForAColor(nextPos,
										isWhite) > state1
										.getCountValueForAColor(nextPos,
												isWhite)) {
									arrMoves.add("1|" + i); // taken.
									arrMoves.add("2|" + nextPos); // put down.
									itNrMoves++;
									// Change the state1 board:
									state1 = getBoardAfterMovingSinglePiece(
											state1, i, dice1);
									dice1 = 0;
								} // end if is a simple move with dice1.
							} // end if dice1 is available.

							if (dice2 > 0) { // dice 2 is available:
								// We check the dice 2 simple move:
								/*
								 * First not to be outside the board, that means
								 * remove:
								 */
								int nextPos = i + dice2;
								if (nextPos > 23) { // means remove:
									// Do nothing, it will be a remove.
								}
								// Check if it is a simple move, this dice:
								else if (state2.getCountValueForAColor(nextPos,
										isWhite) > state1
										.getCountValueForAColor(nextPos,
												isWhite)) {
									arrMoves.add("1|" + i); // taken.
									arrMoves.add("2|" + nextPos); // put.
									itNrMoves++;
									// Change the state1 board:
									state1 = getBoardAfterMovingSinglePiece(
											state1, i, dice2);
									dice2 = 0;
								} // end if is a simple move with dice2..
							} // end if dice2 is available.
								// Now check if both dice were used:
								// If both dice are available:
							if (dice1 > 0 && dice2 > 0) {
								int pos = i;
								int finalPos = pos + dice1 + dice2;
								/*
								 * We need here to be sure that current position
								 * is not to close to the board edge, because it
								 * is sure not both dice can be used from same
								 * start position. Then, the start position i
								 * must be at least board limit minus dice1:
								 */
								int tempLimit = 23 - dice1;
								if (pos <= tempLimit) {
									// If the finalPos is > 23, second is a
									// remove:
									boolean isSecondRemove = false;
									if (finalPos > 23) {
										isSecondRemove = true;
									} // end if is a remove second.
									/*
									 * We set the first move detecting which die
									 * was used:
									 */
									boolean isFirstD1 = true;
									// Here start detect the order of dice use:
									/*
									 * If is a blockade on position + dice1,
									 * impossible to move first with this dice:
									 */
									if (isBlockade(state1, pos + dice1,
											!isWhite)) {
										isFirstD1 = false;
									} // end if impossible because blockade.
									else if (state1.getCountValueForAColor(pos
											+ dice2, !isWhite)
											- state2.getCountValueForAColor(pos
													+ dice2, !isWhite) == 1) {
										/*
										 * Detect if a hit was at position of
										 * dice2, the above if checks if one
										 * checker disappeared from dice2
										 * position::
										 */
										isFirstD1 = false;
									} // end if hit at dice2 position.
										// end detect the order of dice use.

									// Now we have first action:
									if (isFirstD1) {
										arrMoves.add("1|" + pos); // taken.
										arrMoves.add("2|" + (pos + dice1)); // put.
										itNrMoves++;
									} else { // second die first:
										arrMoves.add("1|" + pos); // taken.
										arrMoves.add("2|" + (pos + dice2)); // put.
										itNrMoves++;
									} // end if second die first.
									/*
									 * Now second action, move or remove using
									 * the other dice:, but if first dice was
									 * used the second one, the dice must be
									 * swapped to know where from to take and
									 * put the second action:
									 */
									if (!isFirstD1) {
										int temp = dice1;
										dice1 = dice2;
										dice2 = temp;
									} // end if swap needed.

									// Now we add move or remove using second
									// dice:
									if (isSecondRemove) {
										arrMoves.add("3|" + (pos + dice1)); // removed.
										itNrMoves++;
									} else { // simple move:
										arrMoves.add("1|" + (pos + dice1)); // taken.
										arrMoves.add("2|" + finalPos); // put.
										itNrMoves++;
									} // end if simple move as second.
									dice1 = 0;
									dice2 = 0;
									break; // impossible other.
								} // end if current position is <= tempLimit.
							} // end if both dice are available and used
								// together..
						} // end if it is not double.
					} // end if there were less checkers here.
				} // end for each position loop.
			} // end if at least one die is available.
				// end moves calculations.

			// Now only removes are possible, check for them:
			if (itNrMoves < nrMoves) {
				// Check only from 19 to 23 if dice are available:
				if (dice1 > 0 || dice2 > 0) {
					for (int i = 18; i <= 23; i++) {
						// Check if there are less checkers here, means removed:
						if (state1.getCountValueForAColor(i, isWhite) > state2
								.getCountValueForAColor(i, isWhite)) {

							if (isDouble) {
								int dif = state1.getCountValueForAColor(i,
										isWhite)
										- state2.getCountValueForAColor(i,
												isWhite);
								if (dif > 0) { // checker were removed:
									for (int j = 0; j < dif; j++) {
										arrMoves.add("3|" + i);
										state1 = getBoardStateAfterPuttingOut(
												state1, i, dice1);
										itNrMoves++; // increment.
									} // end for each removed.
								} // end detect number of removes.
							} // end if isDouble.
							else { // not double:
									// If first die is available:
								if (dice1 > 0) {
									/*
									 * It is possible only if no checkers on a
									 * closer to centre position being not white
									 * position here:
									 */
									if ((state1.getCountValueForAColor(i,
											isWhite) >= 1 && (24 - dice1) == i)
											|| ((i + dice1) > 23 && !arePullsGreaterInHome(
													state1, i, isWhite))) {
										arrMoves.add("3|" + i);
										state1 = getBoardStateAfterPuttingOut(
												state1, i, dice1);
										itNrMoves++; // increment.
										dice1 = 0;
									} // end if a valid position for die1 to
										// remove.
								} // end if diie1 is available.
								/*
								 * Second die is available, moves must be done
								 * and there are still checkers on state1::
								 */
								if (dice2 > 0
										&& itNrMoves < nrMoves
										&& state1.getCountValueForAColor(i,
												isWhite) >= 1) {
									if ((state1.getCountValueForAColor(i,
											isWhite) >= 1 && (24 - dice2) == i)
											|| ((i + dice2) > 23 && !arePullsGreaterInHome(
													state1, i, isWhite))) {
										arrMoves.add("3|" + i);
										state1 = getBoardStateAfterPuttingOut(
												state1, i, dice2);
										itNrMoves++; // increment.
										dice2 = 0;
									} // end if a valid position for die2 to
										// remove.
								} // end if second die is available.
							} // end if is not double.
						} // end if were less checkers.
					} // end for positions in home board.
				} // end if there is at least a die available.
			} // end if no enough moves were done in while.
				// end removes calculations.
		} // end while through nrMoves.

		return arrMoves;
	} // end getMoves() method.

} // end BoardUtils class.
