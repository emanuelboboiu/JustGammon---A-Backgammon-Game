package ro.pontes.justbackgammon;

public class Evaluator {

	public static double getScoreOfStateForWhite(BoardState state) {
		double pipDifference = EvaluatorUtils.pipCount(state, false)
				- EvaluatorUtils.pipCount(state, true);
		double coverageDifference = EvaluatorUtils.scoreForGoodCoverage(state,
				true) - EvaluatorUtils.scoreForGoodCoverage(state, false);
		double unsafeDifference = EvaluatorUtils.scoreForUnsafePieces(state,
				false) - EvaluatorUtils.scoreForUnsafePieces(state, true);
		return 100 * pipDifference + 50 * coverageDifference + 50
				* unsafeDifference; // STUB
	} // end getScoreOfStateForWhite() method.

	public static double getScoreOfStateFor(BoardState state, boolean white) {
		return white ? getScoreOfStateForWhite(state)
				: -getScoreOfStateForWhite(state);
	} // end getScoreOfStateForWhite() method.

} // end Evaluator class.
