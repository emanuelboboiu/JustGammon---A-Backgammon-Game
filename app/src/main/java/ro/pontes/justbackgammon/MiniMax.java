package ro.pontes.justbackgammon;

import java.util.ArrayList;

public class MiniMax {

	public static int maxDepth = 0;
	public static DiceSimulator diceSimulator = DiceSimulator.getInstance();

	public static BoardState makeOptimalMove(BoardState state, int dice1,
			int dice2, boolean whitesTurn) {
		ArrayList<BoardState> states = MoveUtils.getAllPossibleMoveResults(
				state, dice1, dice2, whitesTurn);
		double bestScore = -1e9;
		BoardState optimalState = null;
		for (BoardState bs : states) {
			double scoreForState = getMinimaxScore(bs, !whitesTurn, maxDepth,
					whitesTurn);
			if (bestScore < scoreForState) {
				bestScore = scoreForState;
				optimalState = bs;
			}
		}
		return optimalState;
	} // end makeOptimalMove() method.

	private static double getMinimaxScore(BoardState state, boolean whitesTurn,
			int depth, boolean initialTurnWhite) {
		if (depth == 0) {
			return Evaluator.getScoreOfStateFor(state, initialTurnWhite);
		}
		double score = 0.0;
		for (int d1 = 1; d1 <= 6; d1++) {
			for (int d2 = d1; d2 <= 6; d2++) {
				double diceProbability = (d1 == d2) ? 1.0 / 36.0 : 1.0 / 18.0;
				ArrayList<BoardState> states = MoveUtils
						.getAllPossibleMoveResults(state, d1, d2, whitesTurn);
				boolean min = (initialTurnWhite != whitesTurn);
				double maxScore = -1e9;
				if (min) {
					maxScore = 1e9;
				}
				for (BoardState bs : states) {
					if (!min) {
						maxScore = Math.max(
								maxScore,
								getMinimaxScore(bs, !whitesTurn, depth - 1,
										initialTurnWhite));
					} else {
						maxScore = Math.min(
								maxScore,
								getMinimaxScore(bs, !whitesTurn, depth - 1,
										initialTurnWhite));
					}
				}
				score += diceProbability * maxScore;
			}
		}
		return score;
	}

	public static BoardState makeOptimalMove1ply(BoardState state, int dice1,
			int dice2, boolean whitesTurn) {
		ArrayList<BoardState> states = MoveUtils.getAllPossibleMoveResults(
				state, dice1, dice2, whitesTurn);
		double bestScore = -1e9;
		BoardState optimalState = null;
		for (BoardState bs : states) {
			double scoreForState = getMinimaxScore1ply(bs, !whitesTurn,
					maxDepth, whitesTurn, -1e9, +1e9);
			if (bestScore < scoreForState) {
				bestScore = scoreForState;
				optimalState = bs;
			}
		}
		return optimalState;
	}

	private static double getMinimaxScore1ply(BoardState state,
			boolean whitesTurn, int depth, boolean initialTurnWhite,
			double alfa, double beta) {
		if (depth == 0) {
			return Evaluator.getScoreOfStateFor(state, initialTurnWhite);
		}
		double score = 0.0;
		for (int i = 0; i < 14; i++) {
			int d1 = diceSimulator.getRandomDice(), d2 = diceSimulator
					.getRandomDice();
			double diceProbability = (d1 == d2) ? 1.0 / 36.0 : 1.0 / 18.0;
			ArrayList<BoardState> states = MoveUtils.getAllPossibleMoveResults(
					state, d1, d2, whitesTurn);
			boolean min = (initialTurnWhite != whitesTurn);
			double maxScore = -1e9;
			if (min) {
				maxScore = 1e9;
			}
			for (BoardState bs : states) {
				if (!min) {
					maxScore = Math.max(
							maxScore,
							getMinimaxScore1ply(bs, !whitesTurn, depth - 1,
									initialTurnWhite, alfa, beta));
					alfa = maxScore;
				} else {
					maxScore = Math.min(
							maxScore,
							getMinimaxScore1ply(bs, !whitesTurn, depth - 1,
									initialTurnWhite, alfa, beta));
					beta = maxScore;
				}
				if (beta - alfa <= 0.0001)
					return beta;
			}
			score += diceProbability * maxScore;
		}
		return score;
	} // end getMinimaxScore1ply() method.

} // end MiniMax class.
