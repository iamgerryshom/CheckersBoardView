package com.gerryshom.checkersboardview.ai.model;

import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.movement.model.Move;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.player.Player;

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
    private int score;

    public Node() {
    }

    public Node(CheckersBoard snapshot, MoveSequence moveSequence, List<Node> children, boolean maximizing, int score) {
        this.snapshot = snapshot;
        this.moveSequence = moveSequence;
        this.children = children;
        this.maximizing = maximizing;
        this.score = score;
    }

    public Node deepClone() {
        return new Node(
                snapshot, moveSequence.deepClone(),
                deepCloneNodes(children),
                maximizing,
                score
        );
    }

    private List<Node> deepCloneNodes(final List<Node> originalNodes) {
        final List<Node> clonedNodes = new ArrayList<>();
        for(Node originalNode : originalNodes) {
            clonedNodes.add(originalNode.deepClone());
        }
        return clonedNodes;
    }

    private int evaluateBoard(final CheckersBoard board) {

        final String aiPlayerId = Player.computer().getId();
        final String opponentPlayerId = board.identifyOpponentPlayerId(aiPlayerId);

        int score = 0;

        final List<Piece> aiPieces = board.findPiecesByPlayerId(aiPlayerId);
        final List<Piece> opponentPieces = board.findPiecesByPlayerId(opponentPlayerId);

        int aiPieceCount = 0;
        int opponentPieceCount = 0;
        int aiKingCount = 0;
        int opponentKingCount = 0;
        int aiCenterControl = 0;
        int opponentCenterControl = 0;
        int aiAdvance = 0;
        int opponentAdvance = 0;
        int aiPossibleCaptures = 0;
        int opponentPossibleCaptures = 0;

        // Score AI pieces
        for (Piece piece : aiPieces) {
            aiPieceCount++;
            if (piece.isKing()) aiKingCount++;

            int row = piece.getRow();
            int col = piece.getCol();

            // Encourage center control
            if (col >= 2 && col <= 5) aiCenterControl++;

            // Encourage advancement (AI goes from 0 → 7)
            if (!piece.isKing()) aiAdvance += row;

            for(LandingSpot landingSpot : board.findLandingSpots(piece, piece.getRow(), piece.getCol()))
                if(landingSpot.isAfterJump()) aiPossibleCaptures++;


        }

        // Score opponent pieces
        for (Piece piece : opponentPieces) {
            opponentPieceCount++;
            if (piece.isKing()) opponentKingCount++;

            int row = piece.getRow();
            int col = piece.getCol();

            // Encourage center control
            if (col >= 2 && col <= 5) opponentCenterControl++;

            // Opponent goes from 7 → 0
            if (!piece.isKing()) opponentAdvance += (7 - row);

            for(LandingSpot landingSpot : board.findLandingSpots(piece, piece.getRow(), piece.getCol()))
                if(landingSpot.isAfterJump()) opponentPossibleCaptures++;
        }

        // Mobility (number of moveable pieces)
        int aiMobility = board.findMoveablePiecesByPlayerId(aiPlayerId).size();
        int opponentMobility = board.findMoveablePiecesByPlayerId(opponentPlayerId).size();

        // Material + Piece Type
        score += (aiPieceCount - opponentPieceCount) * 100;
        score += (aiKingCount - opponentKingCount) * 50;

        // Center control
        score += (aiCenterControl - opponentCenterControl) * 5;

        // Advancement
        score += (aiAdvance - opponentAdvance) * 2;

        // Mobility
        score += (aiMobility - opponentMobility) * 3;

        score += (aiPossibleCaptures - opponentPossibleCaptures) * 30;

        // Vulnerable pieces (subtract score for how many pieces could be captured)
        score -= opponentPossibleCaptures * 20; // AI's pieces are vulnerable
        score += aiPossibleCaptures * 20;       // Opponent's pieces are vulnerable

        // Endgame bonuses/penalties
        if (opponentPieceCount == 0 || opponentMobility == 0) score += 10000;
        if (aiPieceCount == 0 || aiMobility == 0) score -= 10000;

        return score;
    }

    /**
     * recursively build the children up to a given depth
     * @param depth number of levels to build
     * @param alpha i wish i knew
     *
     */
    public int recursivelyBuildChildren(final int depth, int alpha, int beta) {

        if(depth == 0) {
            setScore(evaluateBoard(getSnapshot()));
            return getScore();
        }

        final String playerId = isMaximizing() ? Player.computer().getId() : getSnapshot().identifyOpponentPlayerId(Player.computer().getId());
        final String opponentPlayerId = getSnapshot().identifyOpponentPlayerId(playerId);

        //first consider pieces that would capture opponents pieces
        List<Piece> pieces = getSnapshot().findCapturesByPlayerId(playerId);

        //use moveable pieces instead if no capture pieces were found
        if(pieces.isEmpty()) {
            pieces = getSnapshot().findMoveablePiecesByPlayerId(playerId);
        }

        int bestScore = isMaximizing() ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for(Piece piece : pieces) {

            final List<LandingSpot> landingSpots = getSnapshot().findLandingSpots(piece, piece.getRow(), piece.getCol());

            for(LandingSpot landingSpot : landingSpots) {

                final CheckersBoard clonedCheckersBoard = getSnapshot().deepClone();

                final Move move = buildMove(
                        piece.getId(), piece.getRow(), landingSpot.getRowCol().x, piece.getCol(), landingSpot.getRowCol().y
                );

                final Node child = new Node();
                child.setMaximizing(!isMaximizing());
                child.setMoveSequence(new MoveSequence(opponentPlayerId, Arrays.asList(move)));
                child.setSnapshot(
                        applyMoveSequence(child.getMoveSequence(), clonedCheckersBoard.deepClone()) // clone board and add it as snapshot
                );

                if(landingSpot.isAfterJump()) {

                    final Node captureRoot = new Node();
                    captureRoot.setSnapshot(getSnapshot());
                    captureRoot.setMoveSequence(getMoveSequence());

                    captureRoot.getChildren().add(child);

                    child.getChildren().addAll(child.deepClone().recursivelyBuildChainTree(piece).captureNodes());

                    for(Node chainChild : child.getChildren()) {
                        final int chainScore = chainChild.recursivelyBuildChildren(depth - 1, alpha, beta);

                        if (isMaximizing()) {
                            bestScore = Math.max(bestScore, chainScore);
                            alpha = Math.max(alpha, bestScore);
                        } else {
                            bestScore = Math.min(bestScore, chainScore);
                            beta = Math.min(beta, bestScore);
                        }

                        //prune the branch
                        if (beta <= alpha) break;

                        getChildren().add(chainChild);
                    }

                } else {

                    final int childScore = child.recursivelyBuildChildren( depth - 1, alpha, beta);

                    if (isMaximizing()) {
                        bestScore = Math.max(bestScore, childScore);
                        alpha = Math.max(alpha, bestScore);
                    } else {
                        bestScore = Math.min(bestScore, childScore);
                        beta = Math.min(beta, bestScore);
                    }

                    //prune the branch
                    if (beta <= alpha) break;

                    getChildren().add(child);
                }


            }

        }

        setScore(bestScore);

        return bestScore;
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
                    child.getChildren().addAll(child.deepClone().recursivelyBuildChainTree(piece).captureNodes());

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
     * each node has a moveSequence with a single move in a capture chain
     * this method takes all the nodes that would form all the possible capture chain paths
     * then builds a single node for each path with a moveSequence having all the moves made in that capture chain
     *
     * voila
     */
    private List<Node> captureNodes() {
        List<Node> result = new ArrayList<>();
        collectCapturePaths(new ArrayList<>(), result, getSnapshot());
        return result;
    }

    private void collectCapturePaths(
            List<Move> currentMoves,
            List<Node> result,
            CheckersBoard rootSnapshot
    ) {
        if (getMoveSequence() != null) {
            currentMoves.addAll(getMoveSequence().getMoves());
        }

        if (getChildren().isEmpty()) {
            // Create new board state with full move sequence and root snapshot
            final Node pathState = new Node();
            final MoveSequence fullSequence = new MoveSequence(getMoveSequence().getDestination(), new ArrayList<>(currentMoves));
            pathState.setMoveSequence(fullSequence);
            pathState.setSnapshot(rootSnapshot.deepClone());  // clone to avoid side-effects

            result.add(pathState);
        } else {
            for (Node child : getChildren()) {
                child.collectCapturePaths(new ArrayList<>(currentMoves), result, rootSnapshot);
            }
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
                        checkersBoard.crownKing(checkersBoard.getCreatorId(), piece.getPlayerId(), move.getToRow())
                );
            }

        }
        return checkersBoard;
    }

    /**
     * builds nodes for all the capture chain paths
     * called only after the first capture has been made and piece has landed in the new rowCol position
     *
     * this means the final tree returned by this method is one move short (ie the first capture move) but its known anyway before calling this method so it can be combined later
     */
    private Node recursivelyBuildChainTree(final Piece piece) {

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

            final MoveSequence moveSequence = new MoveSequence(originalCheckersBoard.identifyOpponentPlayerId(piece.getPlayerId()), Arrays.asList(move));

            child.setMoveSequence(moveSequence);

            final CheckersBoard updatedBoard = applyMoveSequence(moveSequence, clonedCheckersBoard);
            child.setSnapshot(updatedBoard);

            // Get piece in new location
            final Piece updatedPiece = updatedBoard.findPieceById(piece.getId());

            final Node deeperChild = child.recursivelyBuildChainTree(updatedPiece);
            getChildren().add(deeperChild);
        }

        return this;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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
