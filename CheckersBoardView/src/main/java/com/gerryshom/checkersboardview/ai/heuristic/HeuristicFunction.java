package com.gerryshom.checkersboardview.ai.heuristic;

import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.player.Player;

import java.util.List;

public class HeuristicFunction {
    private static final int KING_WEIGHT = 25;                   // Kings are strong and mobile
    private static final int MAN_WEIGHT = 10;                    // Basic piece value
    private static final int MOBILITY_WEIGHT = 2;                // More options = better control
    private static final int PIECE_PROTECTION_WEIGHT = 15;       // Safer pieces = longer survival
    private static final int CENTER_CONTROL_WEIGHT = 3;          // Central squares give board control
    private static final int ADVANCEMENT_WEIGHT = 1;             // Encourage forward progress
    private static final int BACK_ROW_GUARD_WEIGHT = 5;          // Protects from enemy kinging
    private static final int DOUBLE_CORNER_CONTROL_WEIGHT = 3;   // Strategic corners often matter
    private static final int PIECE_SPREAD_WEIGHT = 2;            // Spread pieces to control space
    private static final int CONNECTED_PIECES_WEIGHT = 4;        // Grouped pieces defend each other
    private static final int ISOLATED_PIECE_PENALTY = -6;        // Isolated = vulnerable
    private static final int PROMOTION_POTENTIAL_WEIGHT = 5;     // Nearing king row? Reward it
    private static final int FORCED_CAPTURE_PENALTY = -8;        // If you're forced to lose a piece
    private static final int UNPROTECTED_EDGE_PENALTY = -4;      // Edge pieces are easy targets
    private static final int TRAPPED_KING_PENALTY = -10;         // If king has no good moves
    private static final int ADVANCED_MAN_BONUS = 2;             // Men close to kinging row
    private static final int CENTER_ROW_BONUS = 3;               // 3rd to 5th row pieces get bonus
    private static final int OPPONENT_KING_PENALTY = -5;         // Penalize enemy kings
    private static final int ATTACK_POTENTIAL_WEIGHT = 6;        // How many captures are available
    private static final int DEFENSE_POTENTIAL_WEIGHT = 4;       // Can a piece block or defend?

    public static float apply(final CheckersBoard checkersBoard) {

        final String maximizerPlayerId = Player.computer().getId();
        final String minimizerPlayerId = checkersBoard.identifyOpponentPlayerId(maximizerPlayerId);

        int maximizerScore = 0;
        int minimizerScore = 0;

        //piece count
        for (Piece piece : checkersBoard.getPieces()) {
            int weight = piece.isKing() ? KING_WEIGHT : MAN_WEIGHT;
            if (piece.getPlayerId().equals(maximizerPlayerId)) {
                maximizerScore += weight;
            } else if (piece.getPlayerId().equals(minimizerPlayerId)) {
                minimizerScore += weight;
            }
        }

        final List<Piece> maximizerMoveablePieces = checkersBoard.findMoveablePiecesByPlayerId(maximizerPlayerId);
        final List<Piece> minimizerMoveablePieces = checkersBoard.findMoveablePiecesByPlayerId(minimizerPlayerId);

        //mobility
        int maximizerMobility = maximizerMoveablePieces.size();
        int minimizerMobility = minimizerMoveablePieces.size();
        int mobilityScore = (maximizerMobility - minimizerMobility) * MOBILITY_WEIGHT;

        //piece protection
        // Penalize for exposed maximizer pieces
        for (Piece piece : maximizerMoveablePieces) {
            if (checkersBoard.pieceIsExposed(piece)) {
                maximizerScore -= PIECE_PROTECTION_WEIGHT;
            }
        }

        // Reward for exposing opponent's pieces
        for (Piece piece : minimizerMoveablePieces) {
            if (checkersBoard.pieceIsExposed(piece)) {
                minimizerScore -= PIECE_PROTECTION_WEIGHT;
            }
        }

        return (maximizerScore - minimizerScore) + mobilityScore;

    }
}
