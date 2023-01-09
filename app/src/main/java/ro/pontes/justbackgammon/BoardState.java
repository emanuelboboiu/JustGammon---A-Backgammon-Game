package ro.pontes.justbackgammon;

public class BoardState {

	private int[] count;
	private int numDeadWhite, numDeadBlack, putOutWhite, putOutBlack;

	// count[i] > 0 -> i-nth position contains count[i] of white pieces
	// count[i] < 0 -> i-nth position contains (-count[i]) of black pieces
	// count[i] = 0 -> i-nth position is empty

	// The constructor:
	public BoardState(int[] count, int numDeadWhite, int numDeadBlack,
			int putOutWhite, int putOutBlack) {
		this.count = count;
		this.numDeadBlack = numDeadBlack;
		this.numDeadWhite = numDeadWhite;
		this.putOutBlack = putOutBlack;
		this.putOutWhite = putOutWhite;
	} // end constructor.

	public int[] getCountsArray() {
		return count.clone();
	} // end getCountsArray() method.

	public int getCountAt(int position) {
		return Math.abs(count[position]);
	} // end getCountAt() method.

	// The true integer value on a position, not absolute:
	public int getCountValue(int position) {
		return count[position];
	} // end getCountValue() method.

	// The number of a given colour checkers on a position:
	public int getCountValueForAColor(int position, boolean isWhite) {
		int nrCount = 0;
		if (isWhite) {
			if (isPositionWhite(position)) {
				nrCount = getCountAt(position);
			} else {
				nrCount = 0;
			}
		} else { // it is black:
			if (isPositionBlack(position)) {
				nrCount = getCountAt(position);
			} else {
				nrCount = 0;
			}
		} // end if it is black.
		return nrCount;
	} // end getCountValueForAColor() method.

	public boolean isPositionWhite(int position) {
		return count[position] > 0;
	} // end isPositionWhite() method.

	public boolean isPositionBlack(int position) {
		return count[position] < 0;
	} // end isPositionBlack() method.

	public int getNumDead(boolean white) {
		return white ? numDeadWhite : numDeadBlack;
	} // end getNumDead() method.

	public int numPutAside(boolean white) {
		return white ? putOutWhite : putOutBlack;
	} // end numPutAside() method.

	public int getTakenOut(boolean white) {
		int totalCount = 0;
		int sign = (white ? 1 : -1);
		for (int index = 0; index < count.length; index++) {
			int countHere = (sign * count[index] > 0) ? sign * count[index] : 0;
			totalCount += countHere;
		}
		if (white)
			totalCount += numDeadWhite;
		else
			totalCount += numDeadBlack;
		return BoardUtils.NUM_PIECES - totalCount;
	} // end getTakenOut() method.

	public int getBitmaskOfPositions(boolean white) {
		int mask = 0;
		int sign = (white ? 1 : -1);
		for (int index = 0; index < count.length; index++) {
			if (sign * count[index] > 0) {
				mask ^= (1 << index);
			}
		}
		return mask;
	} // end getBitmaskOfPositions() method.

	public boolean isReadyToTakeOut(boolean white) {
		if (white && numDeadWhite > 0)
			return false;
		if (!white && numDeadBlack > 0)
			return false;
		int countInside = countPiecesOfColorInside(white);
		return countInside + (white ? putOutWhite : putOutBlack) == BoardUtils.NUM_PIECES;
	} // end isReadyToTakeOut() method.

	public int countPiecesOfColorInside(boolean white) {
		int left = 18, right = 23;
		int sign = (white ? 1 : -1);
		if (!white) {
			left = 0;
			right = 5;
		}
		int countInside = 0;
		for (int index = left; index <= right; index++) {
			if (sign * count[index] > 0) {
				countInside += sign * count[index];
			}
		}
		return countInside;
	} // end countPiecesOfColorInside() method.

	// Returns an unique key for a board state:
	public String getKey() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < BoardUtils.BOARD_SIZE; i++) {
			result.append((count[i] == 0 ? "--" : (count[i] > 0 ? count[i]
					+ "W" : (-count[i]) + "B")));
		}
		result.append(numDeadBlack + "" + numDeadWhite + "" + putOutBlack + ""
				+ putOutWhite);
		return result.toString();
	} // end getKey() method.

	// A method to compare current board with another:
	public boolean isSameArrangement(BoardState anotherState) {
		if (this.getKey().equals(anotherState.getKey())) {
			return true;
		} // end if are not equals the two boards.
		return false;
	} // end isSameArrangement() method.

	@Override
	public String toString() {
		// return BoardUtils.getNiceViewOfBoard(this);
		// Make my string:
		StringBuilder sb = new StringBuilder();
		for (int i = 12; i <= 23; i++) {
			sb.append(makeCell(count[i]));
		} // end for upper part.
		sb.append("\n");
		for (int i = 11; i >= 0; i--) {
			sb.append(makeCell(count[i]));
		} // end for lower part.

		return sb.toString();
	} // end toString() method.

	private String makeCell(int value) {
		String s = "";
		if (value > 0) { // white checker:
			s = value + "w ";
		} else if (value == 0) {
			s = "| ";
		} else { // black checker:
			s = (value * -1) + "b ";
		}
		return s;
	} // end makeCell() method.

} // end BoardState class.
