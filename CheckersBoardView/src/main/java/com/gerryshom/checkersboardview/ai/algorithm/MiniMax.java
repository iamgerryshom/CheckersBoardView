package com.gerryshom.checkersboardview.ai.algorithm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gerryshom.checkersboardview.ai.model.BoardState;
import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.board.Piece;
import com.gerryshom.checkersboardview.model.guides.LandingSpot;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MiniMax {

    public interface Listener {
        void onBestMoveSequenceFound(final MoveSequence moveSequence);
    }

    public static void searchBestMoveSequence(final CheckersBoard checkersBoard, final int searchDepth, final Listener listener) {

        Executors.newSingleThreadExecutor().execute(()->{
            final BoardState completeBoardStateTree = createBoardStateTree(checkersBoard, searchDepth);

            final BoardState bestBoardState = findBestBordState(completeBoardStateTree);

            new Handler(Looper.getMainLooper()).post(()->{
                listener.onBestMoveSequenceFound(bestBoardState.getMoveSequence());
            });

        });

    }

    // Minimax function to propagate scores and pick the best board state
    private static int minimax(BoardState node, int depth, boolean isMaximizingPlayer) {
        // Base case: if leaf node or max depth reached, return evaluated score
        if (depth == 0 || node.getChildren().isEmpty()) {
            return node.getScore();
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (BoardState child : node.getChildren()) {
                int eval = minimax(child, depth - 1, false);  // minimize for opponent's turn
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (BoardState child : node.getChildren()) {
                int eval = minimax(child, depth - 1, true);  // maximize for AI's turn
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    // This function will help you to find the best move at the root node
    private static BoardState findBestBordState(BoardState root) {
        int bestEval = Integer.MIN_VALUE;
        BoardState bestBoardState = null;

        // Iterate through each child of the root and evaluate using Minimax
        for (BoardState child : root.getChildren()) {
            int eval = minimax(child, 5, true);  // Assume AI is maximizing player
            if (eval > bestEval) {
                bestEval = eval;
                bestBoardState = child;
            }
        }

        return bestBoardState;
    }

    private static int evaluateBoard(CheckersBoard board, String playerId) {
        int score = 0;
        String opponentId = board.identifyOpponentPlayerId(playerId);

        final List<Piece> aiPieces = board.findPiecesByPlayerId(playerId);
        final List<Piece> opponentPieces = board.findPiecesByPlayerId(opponentId);

        // Count pieces with weights
        for (Piece piece : aiPieces) {
            score += piece.isKing() ? 5 : 3;

            // Position-based evaluation (prefer pieces closer to king row)
            int row = piece.getRow();
            if (!piece.isKing()) {
                // Assuming lower rows are opponent's side (king row)
                score += (7 - row);  // Encourage advancement towards king row
            }

            // Bonus for center control
            int col = piece.getCol();
            if (col >= 2 && col <= 5) {
                score += 1;
            }
        }

        for (Piece piece : opponentPieces) {
            score -= piece.isKing() ? 5 : 3;

            // Position-based evaluation
            int row = piece.getRow();
            if (!piece.isKing()) {
                // Assuming higher rows are AI's side (king row)
                score -= row;  // Discourage opponent advancement
            }

            // Penalty for opponent center control
            int col = piece.getCol();
            if (col >= 2 && col <= 5) {
                score -= 1;
            }
        }


        score += board.findMoveablePiecesByPlayerId(playerId).size();
        score -= board.findMoveablePiecesByPlayerId(opponentId).size();;

        return score;
    }


    private static BoardState createBoardStateTree(final CheckersBoard checkersBoard, final int depth) {
        //create root node with original board state
        final BoardState rootBoardState = new BoardState();
        rootBoardState.setBoardSnapshot(checkersBoard);
        rootBoardState.setChildren(new ArrayList<>()); // start here with no child nodes

        return createChildBoardState(rootBoardState, depth, "ai");
    }

    /**
     * recursive method that creates all the child nodes
     * @param boardState first or initial board state
     * @param depth how many levels the tree must have
     * @param playerId the current maximizing(ai) or minimizing player(opponent)
     * @return full game tree if at leaf or current node with its children for next recursive call
     */
    private static BoardState createChildBoardState(final BoardState boardState, final int depth, final String playerId) {
        if (depth == 0) {
            int score = evaluateBoard(boardState.getBoardSnapshot(), playerId);
            boardState.setScore(score);
            return boardState;
        }

        final CheckersBoard originalCheckersBoard = boardState.getBoardSnapshot();
        final String opponentPlayerId = originalCheckersBoard.identifyOpponentPlayerId(playerId);
        final List<BoardState> childBoardStates = new ArrayList<>();
        final List<Piece> moveablePieces = originalCheckersBoard.findMoveablePiecesByPlayerId(playerId);

        for (Piece moveablePiece : moveablePieces) {
            final List<LandingSpot> landingSpots = originalCheckersBoard.commonLandingSpots(
                    moveablePiece, moveablePiece.getRow(), moveablePiece.getCol()
            );

            for (LandingSpot landingSpot : landingSpots) {
                final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();
                final BoardState childBoardState = new BoardState();
                final List<Move> possibleMoves = new ArrayList<>();

                final Move move = buildMove(moveablePiece.getId(), moveablePiece.getRow(), landingSpot.getRowCol().x, moveablePiece.getCol(), landingSpot.getRowCol().y);

                if (landingSpot.isAfterJump()) {
                    final Piece jumpedPiece = clonedCheckersBoard.findPossibleCapture(playerId, move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
                    if (jumpedPiece != null) move.setCapturedPieceId(jumpedPiece.getId());

                }

                possibleMoves.add(move);
                childBoardState.setMoveSequence(buildMoveSequence(opponentPlayerId, possibleMoves));
                childBoardState.setBoardSnapshot(applyMoveSequence(childBoardState.getMoveSequence(), clonedCheckersBoard));
                childBoardState.setChildren(new ArrayList<>());

                final BoardState deeperChild = createChildBoardState(childBoardState, depth - 1, opponentPlayerId);
                childBoardStates.add(deeperChild);
            }
        }

        if (childBoardStates.isEmpty()) return boardState;

        boardState.getChildren().addAll(childBoardStates);
        return boardState;
    }

    private static Move buildMove(final String pieceId, final int fromRow, final int toRow,
                           final int fromCol, final int toCol) {
        final Move move = new Move();
        move.setId(UUID.randomUUID().toString());
        move.setFromRow(fromRow);
        move.setToRow(toRow);
        move.setFromCol(fromCol);
        move.setToCol(toCol);
        move.setPieceId(pieceId);

        return move;
    }

    private static MoveSequence buildMoveSequence(final String destination, final List<Move> moves) {
        final MoveSequence moveSequence = new MoveSequence();

        moveSequence.setMoves(moves);
        moveSequence.setDestination(destination);

        return moveSequence;
    }


    private static CheckersBoard applyMoveSequence(final MoveSequence moveSequence, final CheckersBoard checkersBoard){
        for(Move move : moveSequence.getMoves()) {
            final Piece piece = checkersBoard.findPieceById(move.getPieceId());

            if(move.isCapture()) {
                final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
                checkersBoard.getPieces().remove(capturedPiece);
            }

            //set row and col for the new position
            piece.setRow(move.getToRow());
            piece.setCol(move.getToCol());

        }
        return checkersBoard;
    }

}
