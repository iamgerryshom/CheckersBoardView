package com.gerryshom.checkersboardview.ai.model;

import com.gerryshom.checkersboardview.ai.heuristic.HeuristicFunction;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.movement.model.Move;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.player.Player;
import com.google.gson.FieldNamingPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Node {
    private CheckersBoard snapshot = new CheckersBoard();
    private MoveSequence moveSequence = new MoveSequence();
    private List<Node> children = new ArrayList<>();
    private boolean maximizing;
    private float heuristic;
    private Node optimalNode;

    public Node() {
    }

    public Node(CheckersBoard snapshot, MoveSequence moveSequence, List<Node> children, boolean maximizing, float heuristic) {
        this.snapshot = snapshot;
        this.moveSequence = moveSequence;
        this.children = children;
        this.maximizing = maximizing;
        this.heuristic = heuristic;
    }

    public Node deepClone() {
        return new Node(
                snapshot, moveSequence.deepClone(),
                deepCloneNodes(children),
                maximizing,
                heuristic
        );
    }

    private List<Node> deepCloneNodes(final List<Node> originalNodes) {
        final List<Node> clonedNodes = new ArrayList<>();
        for(Node originalNode : originalNodes) {
            clonedNodes.add(originalNode.deepClone());
        }
        return clonedNodes;
    }

    public Node getOptimalNode() {
        return optimalNode;
    }

    public void setOptimalNode(Node optimalNode) {
        this.optimalNode = optimalNode;
    }

    /**
     * recursively build the children up to a given depth
     * @param depth number of levels to build
     * @param alpha i wish i knew
     *
     */
    public float recursivelyBuildChildren(final int depth, float alpha, float beta) {

        if(depth == 0) {
            setHeuristic(HeuristicFunction.apply(getSnapshot()));
            return getHeuristic();
        }

        final String playerId = isMaximizing() ? Player.computer().getId() : getSnapshot().identifyOpponentPlayerId(Player.computer().getId());

        //first consider pieces that would capture opponents pieces
        List<Piece> pieces = getSnapshot().findCapturesByPlayerId(playerId);

        //use moveable pieces instead if no capture pieces were found
        if(pieces.isEmpty()) {
            pieces = getSnapshot().findMoveablePiecesByPlayerId(playerId);
        }

        float bestScore = isMaximizing() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Node optimalNode = null;

        outer:
        for(Piece piece : pieces) {

            final List<LandingSpot> landingSpots = getSnapshot().findLandingSpots(piece, piece.getRow(), piece.getCol());

            for(LandingSpot landingSpot : landingSpots) {

                final CheckersBoard clonedCheckersBoard = getSnapshot().deepClone();

                final Move move = buildMove(
                        piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y
                );

                final Node child = new Node();
                child.setMaximizing(!isMaximizing());
                child.setMoveSequence(new MoveSequence(playerId, Arrays.asList(move)));
                child.setSnapshot(
                        applyMoveSequence(child.getMoveSequence(), clonedCheckersBoard.deepClone()) // clone board and add it as snapshot
                );

                if(landingSpot.isAfterJump()) {

                    final Node captureRoot = new Node();
                    captureRoot.setSnapshot(getSnapshot());
                    captureRoot.setMoveSequence(getMoveSequence());

                    captureRoot.getChildren().add(child);

                    child.getChildren().addAll(child.deepClone().recursivelyBuildCaptureChainTree(piece).flattenTree());

                    for(Node chainChild : child.getChildren()) {
                        final float chainScore = chainChild.recursivelyBuildChildren(depth - 1, alpha, beta);

                        if (isMaximizing()) {
                            if (chainScore > bestScore) {
                                bestScore = chainScore;
                                optimalNode = chainChild;
                            }
                            alpha = Math.max(alpha, bestScore);
                        } else {
                            if (chainScore < bestScore) {
                                bestScore = chainScore;
                                optimalNode = chainChild;
                            }
                            beta = Math.min(beta, bestScore);
                        }

                        getChildren().add(chainChild);

                        //prune the branch
                        if (beta <= alpha) break outer;

                    }

                } else {

                    final float childScore = child.recursivelyBuildChildren( depth - 1, alpha, beta);

                    // Update best score & child
                    if (isMaximizing()) {
                        if (childScore > bestScore) {
                            bestScore = childScore;
                            optimalNode = child;
                        }
                        alpha = Math.max(alpha, bestScore);
                    } else {
                        if (childScore < bestScore) {
                            bestScore = childScore;
                            optimalNode = child;
                        }
                        beta = Math.min(beta, bestScore);
                    }

                    getChildren().add(child);
                    //prune the branch
                    if (beta <= alpha) break;

                }

            }

        }

        setOptimalNode(optimalNode);

        setHeuristic(bestScore);

        return bestScore;
    }

    private Node recursivelyBuildCaptureChainTree(final Piece piece) {

        final CheckersBoard originalCheckersBoard = getSnapshot().deepClone();

        final List<LandingSpot> landingSpots = originalCheckersBoard.findLandingSpots(
                piece, piece.getRow(), piece.getCol()
        );

        for (LandingSpot landingSpot : landingSpots) {
            if (!landingSpot.isAfterJump()) continue;  // Only follow jumps

            final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();
            final Node child = new Node();

            final Move move = buildMove(piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y);

            final Piece jumpedPiece = clonedCheckersBoard.findCaptureBetweenRowCols(
                    piece.getPlayerId(), move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol()
            );
            if (jumpedPiece != null) {
                move.setCapturedPieceId(jumpedPiece.getId());
            }

            // FIX 2: Assign chain moves to the PIECE's player, not opponent
            final MoveSequence moveSequence = new MoveSequence(piece.getPlayerId(), Arrays.asList(move));

            child.setMoveSequence(moveSequence);

            final CheckersBoard updatedBoard = applyMoveSequence(moveSequence, clonedCheckersBoard);
            child.setSnapshot(updatedBoard);

            // Get piece in new location
            final Piece updatedPiece = updatedBoard.findPieceById(piece.getId());

            final Node deeperChild = child.recursivelyBuildCaptureChainTree(updatedPiece);
            getChildren().add(deeperChild);
        }

        return this;
    }
    /**
     * each node has a moveSequence with a single move in a capture chain
     * this method takes all the nodes that would form all the possible capture chain paths
     * then builds a single node for each path with a moveSequence having all the moves made in that capture chain
     *
     * voila
     */
    /**
     * Converts the jump chain tree into a flat list of complete capture sequences.
     * Each returned node represents one possible complete jump chain from start to finish.
     *
     * @return list of nodes, each containing a complete capture sequence
     */
    private List<Node> flattenTree() {
        List<Node> completeJumpSequences = new ArrayList<>();
        buildCompleteCapturePaths(new ArrayList<>(), completeJumpSequences, getSnapshot().deepClone());
        return completeJumpSequences;
    }

    /**
     * Recursively traverses the jump chain tree to build complete move sequences.
     * Accumulates moves along each path and creates final nodes with complete sequences.
     *
     * @param movesInCurrentPath the moves accumulated so far in this path
     * @param completeSequences the output list to collect complete jump sequences
     * @param originalBoardState the board state before any jumps in this chain
     */
    private void buildCompleteCapturePaths(
            List<Move> movesInCurrentPath,
            List<Node> completeSequences,
            CheckersBoard originalBoardState
    ) {
        // Add this node's moves to the current path
        if (getMoveSequence() != null) {
            movesInCurrentPath.addAll(getMoveSequence().getMoves());
        }

        // If this is a leaf node, we've reached the end of a jump chain
        if (getChildren().isEmpty()) {
            // Create a node representing the complete jump sequence
            final Node completeJumpNode = new Node();
            final MoveSequence fullJumpSequence = new MoveSequence(
                    getMoveSequence().getOpponentPlayerId(),
                    new ArrayList<>(movesInCurrentPath)
            );
            completeJumpNode.setMoveSequence(fullJumpSequence);
            completeJumpNode.setSnapshot(applyMoveSequence(fullJumpSequence, originalBoardState.deepClone()));

            completeSequences.add(completeJumpNode);
        } else {
            // Continue down each possible jump path
            for (Node jumpOption : getChildren()) {
                jumpOption.buildCompleteCapturePaths(
                        new ArrayList<>(movesInCurrentPath),
                        completeSequences,
                        originalBoardState
                );
            }
        }
    }

    private List<Piece> orderPieces(final List<Piece> unorderedPieces) {

        final Map<Piece, Integer> piecesThatCanStartCaptureChain = new HashMap<>();
        final List<Piece> piecesThatCanCapture = new ArrayList<>();
        final List<Piece> piecesThatWillMoveNormally = new ArrayList<>();

        for(Piece piece : unorderedPieces) {
            final List<LandingSpot> landingSpots = getSnapshot().findLandingSpots(piece, piece.getRow(), piece.getCol());
            for(LandingSpot landingSpot : landingSpots) {
                if(landingSpot.isAfterJump()) {

                    final CheckersBoard clonedCheckersBoard = getSnapshot().deepClone();

                    final Move move = buildMove(
                            piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y
                    );

                    final Node child = new Node();
                    child.setMaximizing(!isMaximizing());
                    child.setMoveSequence(new MoveSequence(clonedCheckersBoard.identifyOpponentPlayerId(piece.getPlayerId()), Arrays.asList(move)));
                    child.setSnapshot(
                            applyMoveSequence(child.getMoveSequence(), clonedCheckersBoard.deepClone()) // clone board and add it as snapshot
                    );

                    final Node captureRoot = new Node();
                    captureRoot.setSnapshot(getSnapshot());
                    captureRoot.setMoveSequence(getMoveSequence());

                    captureRoot.getChildren().add(child);
                    child.getChildren().addAll(child.deepClone().recursivelyBuildCaptureChainTree(piece).flattenTree());

                    int moveCount = 0;
                    for (Node chainChild : child.getChildren()) {
                        // Calculate the length of the move sequence for this particular child node
                        final int count = chainChild.getMoveSequence().getMoves().size();

                        // If the current capture chain is longer than the previous longest, update moveCount
                        if (count > moveCount) {
                            moveCount = count;

                            // Check if piece already exists in the map
                            if (piecesThatCanStartCaptureChain.containsKey(piece)) {
                                // Update the value by keeping the maximum between existing value and count
                                piecesThatCanStartCaptureChain.put(piece, Math.max(piecesThatCanStartCaptureChain.get(piece), count));
                            } else {
                                // If the piece is not in the map, add it with the current count
                                piecesThatCanStartCaptureChain.put(piece, count);
                            }
                        }
                    }

                    piecesThatCanCapture.add(piece);
                    continue;
                }

                piecesThatWillMoveNormally.add(piece);

            }

        }

        if (!piecesThatCanStartCaptureChain.isEmpty()) {
            Piece maxPiece = null;
            int maxCount = Integer.MIN_VALUE;

            for (Map.Entry<Piece, Integer> entry : piecesThatCanStartCaptureChain.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    maxPiece = entry.getKey();
                }
            }

            return Collections.singletonList(maxPiece);
        } else if (!piecesThatCanCapture.isEmpty()) {
            // If no pieces can start a capture chain, return pieces that can capture
            return piecesThatCanCapture;
        } else {
            // If no pieces can capture, return pieces that will move normally
            return piecesThatWillMoveNormally;
        }


    }



    /**
     * simulates the moveSequence that was made foe the current checkersBoard
     */
    private CheckersBoard applyMoveSequence(final MoveSequence moveSequence, final CheckersBoard checkersBoard){
        for(Move move : moveSequence.getMoves()) {
            final Piece piece = checkersBoard.findPieceById(move.getPieceId());

            if(move.isCapture()) {
                final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
                checkersBoard.getPieces().remove(capturedPiece);
            }

            //set row and col for the new position
            piece.setRow(move.getToRow());
            piece.setCol(move.getToCol());

            if(!piece.isKing()) {
                piece.setKing(
                        checkersBoard.crownKing(checkersBoard.getCreator().getId(), piece.getPlayerId(), move.getToRow())
                );
            }

        }
        return checkersBoard;
    }



    private Move buildMove(final String pieceId, final int fromRow, final int toRow,
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


    public boolean isMaximizing() {
        return maximizing;
    }

    public void setMaximizing(boolean maximizing) {
        this.maximizing = maximizing;
    }

    public float getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(float heuristic) {
        this.heuristic = heuristic;
    }

    public CheckersBoard getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(CheckersBoard snapshot) {
        this.snapshot = snapshot;
    }

    public MoveSequence getMoveSequence() {
        return moveSequence;
    }

    public void setMoveSequence(MoveSequence moveSequence) {
        this.moveSequence = moveSequence;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }
}
