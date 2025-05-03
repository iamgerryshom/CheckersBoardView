package com.gerryshom.checkersboardview.ai.algorithm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gerryshom.checkersboardview.ai.model.Node;
import com.gerryshom.checkersboardview.ai.model.Tree;
import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.board.Piece;
import com.gerryshom.checkersboardview.model.guides.LandingSpot;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MiniMax {

    public interface SearchListener {
        void onComplete(final MoveSequence moveSequence);
    }

    public static void search(final CheckersBoard originalCheckersBoard, final int depth, final SearchListener listener) {

        Executors.newSingleThreadExecutor().execute(()->{
            final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();
            final Tree tree = buildTree(clonedCheckersBoard, depth);

            final Node optimalNode = optimalNode(tree.getRoot());

            new Handler(Looper.getMainLooper()).post(()->listener.onComplete(optimalNode.getMoveSequence()));
        });

    }

    private static Node optimalNode(final Node root) {
        final List<Node> children = new ArrayList<>();
        for(Node child : root.getChildren()) {
            if(child.getScore() == root.getScore()) children.add(child);
        }
        return children.get(0);
    }

    private static Tree buildTree(final CheckersBoard checkersBoard, final int depth) {

        final Node root = new Node();
        root.setSnapshot(checkersBoard);
        root.setMaximizing(true);

        final Tree tree = new Tree();
        tree.setRoot(root);

        recursivelyBuildChildren(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        return tree;

    }

    private static int backtrack(final Node root, int alpha, int beta) {
        if(root.getChildren().isEmpty()) {
            return root.getScore();
        }

        int result;

        if(root.isMaximizing()) {
            result = Integer.MIN_VALUE;

            for(Node child : root.getChildren()) {
                final int childScore = backtrack(child, alpha, beta);
                result = Math.max(childScore, result);
                alpha = Math.max(alpha, result);

                if(beta <= alpha) break;

            }

        } else {
            result = Integer.MAX_VALUE;

            for(Node child : root.getChildren()) {
                final int score = backtrack(child, alpha, beta);
                result = Math.min(score, result);
                beta = Math.min(beta, result);

                if(beta <= alpha) break;

            }

        }

        root.setScore(result);

        return result;

    }

    private static int evaluateBoard(CheckersBoard board, String playerId) {
        int score = 0;
        String opponentId = board.identifyOpponentPlayerId(playerId);

        final List<Piece> aiPieces = board.findPiecesByPlayerId(playerId);
        final List<Piece> opponentPieces = board.findPiecesByPlayerId(opponentId);

        int aiPieceCount = 0;
        int opponentPieceCount = 0;
        int aiKingCount = 0;
        int opponentKingCount = 0;
        int aiCenterControl = 0;
        int opponentCenterControl = 0;
        int aiAdvance = 0;
        int opponentAdvance = 0;

        // Score AI pieces
        for (Piece piece : aiPieces) {
            aiPieceCount++;
            if (piece.isKing()) aiKingCount++;

            int row = piece.getRow();
            int col = piece.getCol();

            // Encourage center control
            if (col >= 2 && col <= 5) aiCenterControl++;

            // Encourage advancement if not king
            if (!piece.isKing()) aiAdvance += (7 - row);
        }

        // Score opponent pieces
        for (Piece piece : opponentPieces) {
            opponentPieceCount++;
            if (piece.isKing()) opponentKingCount++;

            int row = piece.getRow();
            int col = piece.getCol();

            if (col >= 2 && col <= 5) opponentCenterControl++;
            if (!piece.isKing()) opponentAdvance += row;
        }

        // Mobility (move options)
        int aiMobility = board.findMoveablePiecesByPlayerId(playerId).size();
        int opponentMobility = board.findMoveablePiecesByPlayerId(opponentId).size();

        // Material + Piece Type
        score += (aiPieceCount - opponentPieceCount) * 100;
        score += (aiKingCount - opponentKingCount) * 50;

        // Center control
        score += (aiCenterControl - opponentCenterControl) * 5;

        // Advancement
        score += (aiAdvance - opponentAdvance) * 2;

        // Mobility
        score += (aiMobility - opponentMobility) * 3;

        // Bonus if opponent has no pieces or no moves
        if (opponentPieceCount == 0 || opponentMobility == 0) score += 10000;

        // Penalty if AI has no pieces or no moves
        if (aiPieceCount == 0 || aiMobility == 0) score -= 10000;

        return score;
    }

    private static int recursivelyBuildChildren(final Node root, final int depth, int alpha, int beta) {

        if(depth == 0) {
            root.setScore(evaluateBoard(root.getSnapshot(), root.getMoveSequence().getDestination()));
            return root.getScore();
        }

        final String playerId = root.isMaximizing() ? "computer" : root.getSnapshot().identifyOpponentPlayerId("computer");
        final String opponentPlayerId = root.getSnapshot().identifyOpponentPlayerId(playerId);

        List<Piece> pieces = root.getSnapshot().findCapturingPeaces(playerId);

        if(pieces.isEmpty()) {
            pieces = root.getSnapshot().findMoveablePiecesByPlayerId(playerId);
        }

        int bestScore = root.isMaximizing() ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for(Piece piece : pieces) {

            final List<LandingSpot> landingSpots = root.getSnapshot().commonLandingSpots(piece, piece.getRow(), piece.getCol());

            for(LandingSpot landingSpot : landingSpots) {

                final CheckersBoard clonedCheckersBoard = root.getSnapshot().deepClone();

                final Move move = buildMove(
                        piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y
                );

                final Node child = new Node();
                child.setMaximizing(!root.isMaximizing());
                child.setMoveSequence(new MoveSequence(opponentPlayerId, Arrays.asList(move)));
                child.setSnapshot(
                        applyMoveSequence(child.getMoveSequence(), clonedCheckersBoard.deepClone()) // clone board and add it as snapshot
                );

                if(landingSpot.isAfterJump()) {

                    final Node captureRoot = new Node();
                    captureRoot.setSnapshot(root.getSnapshot());
                    captureRoot.setMoveSequence(root.getMoveSequence());

                    captureRoot.getChildren().add(child);
                    child.getChildren().addAll(captureNodes(recursivelyBuildChainTree(child.deepClone(), piece)));

                    for(Node chain : child.getChildren()) {
                        final int chainScore = recursivelyBuildChildren(chain, depth - 1, alpha, beta);

                        if (root.isMaximizing()) {
                            bestScore = Math.max(bestScore, chainScore);
                            alpha = Math.max(alpha, bestScore);
                        } else {
                            bestScore = Math.min(bestScore, chainScore);
                            beta = Math.min(beta, bestScore);
                        }

                        //prune the branch
                        if (beta <= alpha) break;

                        root.getChildren().add(chain);
                    }

                } else {

                    final int childScore = recursivelyBuildChildren(child, depth - 1, alpha, beta);

                    if (root.isMaximizing()) {
                        bestScore = Math.max(bestScore, childScore);
                        alpha = Math.max(alpha, bestScore);
                    } else {
                        bestScore = Math.min(bestScore, childScore);
                        beta = Math.min(beta, bestScore);
                    }

                    //prune the branch
                    if (beta <= alpha) break;

                    root.getChildren().add(child);
                }


            }

        }

        root.setScore(bestScore);

        return bestScore;
    }

    public static List<Node> captureNodes(final Node root) {
        List<Node> result = new ArrayList<>();
        collectCapturePaths(root, new ArrayList<>(), result, root.getSnapshot());
        return result;
    }

    private static void collectCapturePaths(
            Node node,
            List<Move> currentMoves,
            List<Node> result,
            CheckersBoard rootSnapshot
    ) {
        if (node.getMoveSequence() != null) {
            currentMoves.addAll(node.getMoveSequence().getMoves());
        }

        if (node.getChildren().isEmpty()) {
            // Create new board state with full move sequence and root snapshot
            final Node pathState = new Node();
            final MoveSequence fullSequence = new MoveSequence(node.getMoveSequence().getDestination(), new ArrayList<>(currentMoves));
            pathState.setMoveSequence(fullSequence);
            pathState.setSnapshot(rootSnapshot.deepClone());  // clone to avoid side-effects

            result.add(pathState);
        } else {
            for (Node child : node.getChildren()) {
                collectCapturePaths(child, new ArrayList<>(currentMoves), result, rootSnapshot);
            }
        }
    }

    public static Node recursivelyBuildChainTree(final Node root, final Piece piece) {

        final CheckersBoard originalCheckersBoard = root.getSnapshot().deepClone();

        final List<LandingSpot> landingSpots = originalCheckersBoard.commonLandingSpots(
                piece, piece.getRow(), piece.getCol()
        );

        for (LandingSpot landingSpot : landingSpots) {
            if (!landingSpot.isAfterJump()) continue;  // Only follow jumps

            final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();
            final Node child = new Node();

            final Move move = buildMove(piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y);

            final Piece jumpedPiece = clonedCheckersBoard.findPossibleCapture(
                    piece.getPlayerId(), move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol()
            );
            if (jumpedPiece != null) {
                move.setCapturedPieceId(jumpedPiece.getId());
            }

            final MoveSequence moveSequence = new MoveSequence(originalCheckersBoard.identifyOpponentPlayerId(piece.getPlayerId()), Arrays.asList(move));

            child.setMoveSequence(moveSequence);

            final CheckersBoard updatedBoard = applyMoveSequence(moveSequence, clonedCheckersBoard);
            child.setSnapshot(updatedBoard);

            // Get piece in new location
            final Piece updatedPiece = updatedBoard.findPieceById(piece.getId());

            final Node deeperChild = recursivelyBuildChainTree(child, updatedPiece);
            root.getChildren().add(deeperChild);
        }

        return root;
    }


    public static CheckersBoard applyMoveSequence(final MoveSequence moveSequence, final CheckersBoard checkersBoard){
        for(Move move : moveSequence.getMoves()) {
            final Piece piece = checkersBoard.findPieceById(move.getPieceId());

            if(move.isCapture()) {
                final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
                checkersBoard.getPieces().remove(capturedPiece);
            }

            piece.setKing(checkersBoard.crownKing(checkersBoard.getCreatorId(), piece.getPlayerId(), move.getToRow()));

            //set row and col for the new position
            piece.setRow(move.getToRow());
            piece.setCol(move.getToCol());

        }
        return checkersBoard;
    }

    public static Move buildMove(final String pieceId, final int fromRow, final int toRow,
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
}
