package ro.pontes.justbackgammon;

import java.util.ArrayList;
import java.util.HashSet;

public class MoveUtils {

	public static ArrayList<BoardState> getAllPossibleMoveResults(
			BoardState currentState, int dice1, int dice2, boolean whitesTurn) {
		HashSet<String> usedKeys = new HashSet<String>();
		ArrayList<BoardState> allPositions = new ArrayList<BoardState>();
		int[][] movesToMake = getMovesToMakeArrays(dice1, dice2);
		for (int i = 0; i < movesToMake.length; i++) {
			addAllPositions(allPositions, currentState, movesToMake[i], 0,
					whitesTurn);
		} // end for all dice.
		ArrayList<BoardState> resultList = new ArrayList<BoardState>();
		for (BoardState state : allPositions) {
			String key = state.getKey();
			if (usedKeys.contains(key))
				continue;
			usedKeys.add(key);
			resultList.add(state);
		} // end for.

		if (resultList.isEmpty() || resultList.get(0) == null) {
			resultList.clear();
			resultList.add(currentState);
		}
		return resultList;
	} // end getAllPossibleMoveResults() method.

	private static int[][] getMovesToMakeArrays(int dice1, int dice2) {
		if (dice1 != dice2) {
			return new int[][] { new int[] { dice1, dice2 },
					new int[] { dice2, dice1 } };
		}
		return new int[][] { new int[] { dice1, dice1, dice1, dice1 } };
	} // end getMovesToMakeArray() method.

	public static void addAllPositions(ArrayList<BoardState> listPositions,
			BoardState currentState, int[] movesToMake, int position,
			boolean whitesTurn) {
		if (position == movesToMake.length) {
			listPositions.add(currentState);
			return;
		}
		if (currentState.getNumDead(whitesTurn) > 0) {
			addAllMovesForDeadPiece(listPositions, currentState, movesToMake,
					position, whitesTurn);
			return;
		}
		if (currentState.isReadyToTakeOut(whitesTurn)) {
			addPuttingOutMoves(listPositions, currentState, whitesTurn,
					movesToMake, position);
		}
		for (int i = 0; i < 24; i++) {
			boolean isValidPosition = (currentState.getCountAt(i) > 0 && ((whitesTurn && currentState
					.isPositionWhite(i)) || (!whitesTurn && !currentState
					.isPositionWhite(i))));
			if (!isValidPosition)
				continue;
			if (canMakeMoveFrom(currentState, whitesTurn, i, position,
					movesToMake[position])) {
				BoardState resultingState = BoardUtils
						.getBoardAfterMovingSinglePiece(currentState, i,
								movesToMake[position]);
				addAllPositions(listPositions, resultingState, movesToMake,
						position + 1, whitesTurn);
			}
		}
	} // end addAllPositions() method.

	private static boolean canMakeMoveFrom(BoardState currentState,
			boolean whitesTurn, int boardIndex, int movePosition, int moveLength) {
		int destIndex = computeIndex(boardIndex, whitesTurn, moveLength);
		if (destIndex == -1)
			return false;
		boolean sameColor = (whitesTurn && currentState
				.isPositionWhite(destIndex))
				|| (!whitesTurn && !currentState.isPositionWhite(destIndex));
		if (currentState.getCountAt(destIndex) > 1 && !sameColor) {
			return false;
		}
		return true;
	} // end canMakeMoveFrom() method.

	private static void addPuttingOutMoves(ArrayList<BoardState> listPositions,
			BoardState state, boolean whitesTurn, int[] movesToMake,
			int position) {
		if (whitesTurn) {
			for (int i = 18; i <= 23; i++) {
				if (state.isPositionWhite(i)
						&& state.getCountAt(i) > 0
						&& ((BoardUtils.BOARD_SIZE - movesToMake[position] == i) || (movesToMake[position]
								+ i >= BoardUtils.BOARD_SIZE && !BoardUtils
									.arePullsGreaterInHome(state, i, true)))) {
					BoardState resultingState = BoardUtils
							.getBoardStateAfterPuttingOut(state, i,
									movesToMake[position]);
					addAllPositions(listPositions, resultingState, movesToMake,
							position + 1, whitesTurn);
				}
			}
		} else { // black
			for (int i = 5; i >= 0; i--) {
				if (state.isPositionBlack(i)
						&& state.getCountAt(i) > 0
						&& ((i == movesToMake[position] - 1) || (i
								- movesToMake[position] < 0 && !BoardUtils
									.arePullsGreaterInHome(state, i, false)))) {
					BoardState resultingState = BoardUtils
							.getBoardStateAfterPuttingOut(state, i,
									movesToMake[position]);
					addAllPositions(listPositions, resultingState, movesToMake,
							position + 1, whitesTurn);
				}
			}
		}
	} // end addPuttingOutMoves() method.

	private static int computeIndex(int currentIndex, boolean whitesTurn,
			int moveLength) {
		int destIndex = (whitesTurn ? currentIndex + moveLength : currentIndex
				- moveLength);
		if (destIndex >= BoardUtils.BOARD_SIZE || destIndex < 0)
			return -1;
		return destIndex;
	} // end computeIndex() method.

	public static void addAllMovesForDeadPiece(
			ArrayList<BoardState> listPositions, BoardState currentState,
			int[] movesToMake, int position, boolean whitesTurn) {
		int dice = movesToMake[position];
		if (dice == 0) {
			// Now before return, we add current board:
			BoardState resultingBoard = BoardUtils
					.cloneBoardState(currentState);
			addAllPositions(listPositions, resultingBoard, movesToMake,
					position + 1, whitesTurn);
			return;
		}
		if (whitesTurn && currentState.isPositionBlack(dice - 1)
				&& currentState.getCountAt(dice - 1) > 1) {
			// Now before return, we add current board:
			BoardState resultingBoard = BoardUtils
					.cloneBoardState(currentState);
			addAllPositions(listPositions, resultingBoard, movesToMake,
					position + 1, whitesTurn);
			return;
		}
		if (!whitesTurn && currentState.isPositionWhite(24 - dice)
				&& currentState.getCountAt(24 - dice) > 1) {
			// Now before return, we add current board:
			BoardState resultingBoard = BoardUtils
					.cloneBoardState(currentState);
			addAllPositions(listPositions, resultingBoard, movesToMake,
					position + 1, whitesTurn);
			return;
		}
		BoardState resultingBoard = BoardUtils.getBoardForMakingPieceAlive(
				currentState, movesToMake[position], whitesTurn);
		addAllPositions(listPositions, resultingBoard, movesToMake,
				position + 1, whitesTurn);
	} // end addAllMovesForDeadPiece() method.

} // end MoveUtils class.
