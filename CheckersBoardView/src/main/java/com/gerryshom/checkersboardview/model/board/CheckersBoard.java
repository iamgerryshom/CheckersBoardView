package com.gerryshom.checkersboardview.model.board;


import android.graphics.Point;

import androidx.annotation.NonNull;

import com.gerryshom.checkersboardview.enums.Direction;
import com.gerryshom.checkersboardview.helper.PieceHelper;
import com.gerryshom.checkersboardview.model.guides.LandingSpot;
import com.gerryshom.checkersboardview.model.player.Player;
import com.gerryshom.checkersboardview.model.rules.CaptureRule;
import com.gerryshom.checkersboardview.model.rules.GameFlowRule;
import com.gerryshom.checkersboardview.model.rules.KingPieceRule;
import com.gerryshom.checkersboardview.model.rules.NormalPieceRule;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CheckersBoard {
    private String id;
    private List<Piece> pieces;
    private String creatorId;
    private String opponentId;
    private long createdAt;
    private String activePlayerId;
    private Player creator;
    private Player opponent;
    private int boardWidth;
    private NormalPieceRule normalPieceRule;
    private KingPieceRule kingPieceRule;
    private CaptureRule captureRule;
    private GameFlowRule gameFlowRule;

    public CheckersBoard(String id, List<Piece> pieces, String creatorId, String opponentId, long createdAt, String activePlayerId, Player creator, Player opponent, int boardWidth, NormalPieceRule normalPieceRule, KingPieceRule kingPieceRule, CaptureRule captureRule, GameFlowRule gameFlowRule) {
        this.id = id;
        this.pieces = pieces;
        this.creatorId = creatorId;
        this.opponentId = opponentId;
        this.createdAt = createdAt;
        this.activePlayerId = activePlayerId;
        this.creator = creator;
        this.opponent = opponent;
        this.boardWidth = boardWidth;
        this.normalPieceRule = normalPieceRule;
        this.kingPieceRule = kingPieceRule;
        this.captureRule = captureRule;
        this.gameFlowRule = gameFlowRule;
    }

    public CheckersBoard deepClone() {
        return new CheckersBoard(
                id, clonePieces(pieces), creatorId, opponentId, createdAt, activePlayerId, creator, opponent, boardWidth, normalPieceRule, kingPieceRule,
                captureRule, gameFlowRule
        );
    }

    /*
    public CheckersBoard deepClone() {
        final Gson gson = new Gson();
        String json = gson.toJson(this);
        return gson.fromJson(json, CheckersBoard.class);
    }

     */

    /**
     * returns the number of pieces for a player
     * @param playerId - id of the player to count remaining pieces
     * @return an int of the remaining pieces count
     */
    public int getPieceCountByPlayerId(final String playerId) {
        int pieceCount = 0;

        for(Piece p : pieces)
            if(p.getPlayerId().equals(playerId)) pieceCount++;

        return pieceCount;
    }

    public List<Piece> findCapturesByPlayerId(final String playerId) {
        final List<Piece> capturingPeaces = new ArrayList<>();
        for(Piece moveablePiece : findMoveablePiecesByPlayerId(playerId)) {
            if(!findCapturesByRowAndCol(moveablePiece, moveablePiece.getRow(), moveablePiece.getCol()).isEmpty())
                capturingPeaces.add(moveablePiece);
        }
        return capturingPeaces;
    }


    /**
     * returns id of the opponent player
     * @param playerId id of the current player
     * @return id of the enemy player
     */
    public String identifyOpponentPlayerId(final String playerId) {
        if(playerId == null ) throw new RuntimeException("playerId is null");
        return playerId.equals(creatorId)
                ? opponentId
                : creatorId;
    }

    public List<Piece> findPiecesByPlayerId(final String playerId) {
        final List<Piece> playerPieces = new ArrayList<>();
        for(Piece piece : pieces) {
            if(piece.getPlayerId().equals(playerId)) {
                playerPieces.add(piece);
            }
        }
        return playerPieces;
    }

    public List<Piece> clonePieces(final List<Piece> pieces) {
        final List<Piece> clonedPieces = new ArrayList<>();
        for(Piece piece : pieces) {
            clonedPieces.add(piece.clone());
        }
        return clonedPieces;
    }

    public Piece findPieceById(final String id) {
        for(Piece p : pieces) {
            if(p.getId().equals(id)) return p;
        }
        return null;
    }

    /**
     * checks all the player's pieces to see if they have any legal moves left
     * @param playerId id of the player
     * @return a list of the moveable pieces
     */
    public List<Piece> findMoveablePiecesByPlayerId(final String playerId) {
        final List<Piece> moveablePieces = new ArrayList<>();
        for(Piece p : pieces) {
            if(!p.getPlayerId().equals(playerId)) continue;

            if(!commonLandingSpots(p, p.getRow(), p.getCol()).isEmpty())
                moveablePieces.add(p);

        }
        return moveablePieces;
    }

    /**
     * a common method for find landing highlights
     */
    public List<LandingSpot> commonLandingSpots(final Piece p, final int row, final int col) {
        return findLandingSpots(p, row, col,
                p.isKing() || !normalPieceRule.isRestrictToForwardMovement()
                        ? Arrays.asList(Direction.TOP_LEFT, Direction.TOP_RIGHT, Direction.BOTTOM_LEFT, Direction.BOTTOM_RIGHT)
                        : p.getPlayerId().equals(creatorId)
                        ? Arrays.asList(Direction.TOP_LEFT, Direction.TOP_RIGHT)
                        : Arrays.asList(Direction.BOTTOM_LEFT, Direction.BOTTOM_RIGHT),
                normalPieceRule.isAllowBackwardCapture(),
                kingPieceRule.getMaxMoveSteps(),
                kingPieceRule.getMaxLandingDistanceAfterCapture());
    }

    /**
     * finds a piece object in a list using the row and column
     */
    // Helper method to find a piece at a given position
    public Piece findPieceByRowAndCol(final int row, final int col) {
        return PieceHelper.findPieceByRowAndCol(pieces, boardWidth, row, col);
    }

    /**
     * resolves touch co-ordinates into radius field and returns any piece that falls withing that field
     */
    public Piece findTouchedPieceByTouchXAndY(final float touchX, final float touchY) {
        final int cellSize = boardWidth / 8;
        final float pieceRadius = cellSize * 0.4f;  // Calculate the piece radius based on cell size

        for (Piece p : pieces) {
            // Calculate distance from the center of the piece to the touch point
            float deltaX = touchX - p.getCenterX();
            float deltaY = touchY - p.getCenterY();

            // Calculate the Euclidean distance (distance formula)
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // If the distance is less than the piece's radius, it's a match
            if (distance <= pieceRadius) {
                return p;
            }
        }

        return null;
    }

    /**
     * sets the king property to true for a piece
     * @param creatorId userId for player who created the board. This player starts from the 7 - 5 rows
     * @param playerId userId of the player who moved a piece
     * @param toRow the destination row that the piece was moved to
     */
    public boolean crownKing(final String creatorId, final String playerId, final int toRow) {
        if (creatorId.equals(playerId)) {
            // Creator moving upward → Crown at row 0
            return toRow == 0;
        } else {
            // Opponent moving downward → Crown at row 7
            return toRow == 7;
        }
    }

    /**
     * checks that a row or col is within the grid of the board
     */
    public boolean isValidRowCol(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8; // 8x8 grid for standard checkers
    }

    /**
     * returns all the pieces a player can possibly capture in all directions
     * @param piece the id of the player attempting to make a move
     * @return a list of the pieces
     */
    public List<Piece> findCapturesByRowAndCol(final Piece piece, final int row, final int col) {

        final List<Piece> possibleCaptures = new ArrayList<>();

        for(LandingSpot landingSpot : commonLandingSpots(piece, row, col)) {
            if(landingSpot.isAfterJump()) {
                possibleCaptures.add(
                        findCaptureBetweenRowAndCol(
                                piece.getPlayerId(), piece.getRow(), piece.getCol(), landingSpot.getRowCol().x, landingSpot.getRowCol().y
                        )
                );
            }
        }

        /*


        final int[] rowDirections = {-2, -2, 2, 2};
        final int[] colDirections = {-2, 2, -2, 2};

        for(int i = 0; i < 4; i++) {
            final int nextRow = row + rowDirections[i];
            final int nextCol = col + colDirections[i];

            final Piece pieceAtDestination = findPieceByRowAndCol(nextRow, nextCol);

            final int middleRow = row + rowDirections[i] / 2; // Middle piece's row
            final int middleCol = col + colDirections[i] / 2; // Middle piece's column

            final Piece middlePiece = findPieceByRowAndCol(middleRow, middleCol);

            if(middlePiece == null) continue;
            if(middlePiece.getPlayerId().equals(playerId)) continue;
            if(pieceAtDestination != null) continue;
            if(!isValidRowCol(nextRow, nextCol)) continue;

            possibleCaptures.add(middlePiece);

        }

         */

        return possibleCaptures;

    }


    /**
     * attempts to find an enemy piece that was jumped.
     * @return enemy piece object that was jumped or null if no enemy piece was jumped
     */
    public Piece findCaptureBetweenRowAndCol(final String playerId, final int fromRow, final int fromCol, final int toRow, final int toCol) {

        // Determine direction of movement
        int rowDirection = Integer.compare(toRow - fromRow, 0);
        int colDirection = Integer.compare(toCol - fromCol, 0);

        // For a valid capture, start and end must be at least 2 steps apart
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff != colDiff || rowDiff < 2) {
            return null; // Not a diagonal move or not far enough for a capture
        }

        // For regular pieces, check only the middle position
        if (rowDiff == 2) {
            int middleRow = fromRow + rowDirection;
            int middleCol = fromCol + colDirection;

            Piece middlePiece = findPieceByRowAndCol(middleRow, middleCol);
            if (middlePiece != null && !middlePiece.getPlayerId().equals(playerId)) {
                return middlePiece;
            }
        }
        // For king pieces, check all positions between start and end
        else {
            // Start from the position after the starting point
            int currentRow = fromRow + rowDirection;
            int currentCol = fromCol + colDirection;

            Piece enemyPiece = null;

            // Check all positions between start and end (exclusive of end)
            while (currentRow != toRow && currentCol != toCol) {
                Piece pieceAtPosition = findPieceByRowAndCol(currentRow, currentCol);

                if (pieceAtPosition != null) {
                    // If we already found an enemy piece and now found another piece,
                    // this move is invalid (can't jump over two pieces)
                    if (enemyPiece != null) {
                        return null;
                    }

                    // If this is an enemy piece, mark it as the potential capture
                    if (!pieceAtPosition.getPlayerId().equals(playerId)) {
                        enemyPiece = pieceAtPosition;
                    }
                    // If this is a friendly piece, the move is invalid
                    else {
                        return null;
                    }
                }

                // Move to the next position
                currentRow += rowDirection;
                currentCol += colDirection;
            }

            return enemyPiece;
        }

        return null;
    }

    /**
     * Calculates and returns a list of possible landing cells for the given piece.
     *
     * @param piece                              The selected piece
     * @param allowedDirections                  Directions the piece is allowed to move
     * @param allowHighlightsInForbiddenDirections Whether to allow captures in forbidden directions
     * @param maxKingMoveSteps                       Maximum number of steps a king can take in a regular move (0 = no limit)
     * @param maxKingJumpLandingDistance             Maximum distance a king can land after a capture (0 = no limit)
     * @return List of Highlight positions where the piece can land
     */
    public List<LandingSpot> findLandingSpots(final Piece piece,
                                                 final int row,
                                                 final int col,
                                                 final List<Direction> allowedDirections,
                                                 final boolean allowHighlightsInForbiddenDirections,
                                                 final int maxKingMoveSteps,
                                                 final int maxKingJumpLandingDistance) {

        final List<LandingSpot> landingSpots = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            int currentRow = row;
            int currentCol = col;

            final Point rowColDir = dir.toPoint();
            int enemiesFound = 0;
            int moveSteps = 0;
            int jumpLandingSteps = 0;
            boolean justJumped = false;

            while (true) {
                int nextRow = currentRow + rowColDir.x;
                int nextCol = currentCol + rowColDir.y;

                if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) break;

                Piece pieceAtNext = findPieceByRowAndCol(nextRow, nextCol);

                if (pieceAtNext == null) {
                    if (!justJumped) {
                        moveSteps++;
                        if (allowedDirections.contains(dir) && (maxKingMoveSteps == 0 || moveSteps <= maxKingMoveSteps)) {
                            landingSpots.add(new LandingSpot(new Point(nextRow, nextCol), false));
                        }
                        if (!piece.isKing() || (maxKingMoveSteps != 0 && moveSteps >= maxKingMoveSteps)) break;
                    } else {
                        // We've already moved to the capture landing position (step 1)
                        // Now we're considering additional steps after the capture
                        jumpLandingSteps++;

                        // Only add highlights if we're within the allowed landing distance
                        if ((allowedDirections.contains(dir) || allowHighlightsInForbiddenDirections)
                                && (maxKingJumpLandingDistance == 0 || jumpLandingSteps <= maxKingJumpLandingDistance)) {
                            landingSpots.add(new LandingSpot(new Point(nextRow, nextCol), false));
                        }

                        // Break if we've reached the max landing distance
                        if (maxKingJumpLandingDistance != 0 && jumpLandingSteps >= maxKingJumpLandingDistance) break;
                    }
                    currentRow = nextRow;
                    currentCol = nextCol;

                } else if (!pieceAtNext.getPlayerId().equals(piece.getPlayerId())) {
                    enemiesFound++;
                    if (piece.isKing() && enemiesFound >= 2) break;

                    int jumpRow = nextRow + rowColDir.x;
                    int jumpCol = nextCol + rowColDir.y;

                    if (jumpRow < 0 || jumpRow >= 8 || jumpCol < 0 || jumpCol >= 8) break;

                    Piece pieceAtJump = findPieceByRowAndCol(jumpRow, jumpCol);

                    if (pieceAtJump == null) {
                        if (allowedDirections.contains(dir) || allowHighlightsInForbiddenDirections) {
                            // For regular pieces, just add the landing highlight and stop
                            if (!piece.isKing()) {
                                landingSpots.add(new LandingSpot(new Point(jumpRow, jumpCol), true));
                                break;
                            }

                            // For kings with maxJumpLandingDistance = 0, add the landing spot and continue
                            if (maxKingJumpLandingDistance == 0) {
                                landingSpots.add(new LandingSpot(new Point(jumpRow, jumpCol), true));
                                justJumped = true;
                                jumpLandingSteps = 0;
                                currentRow = jumpRow;
                                currentCol = jumpCol;
                            }
                            // For kings with specific maxJumpLandingDistance
                            else {
                                // The immediate landing spot after capture is always allowed
                                if (maxKingJumpLandingDistance >= 1) {
                                    landingSpots.add(new LandingSpot(new Point(jumpRow, jumpCol), true));
                                }

                                // If maxJumpLandingDistance is exactly 1, break here
                                if (maxKingJumpLandingDistance == 1) {
                                    break;
                                }
                                // Otherwise continue processing, but we've already used 1 step
                                else {
                                    justJumped = true;
                                    jumpLandingSteps = 1; // We've already used 1 step by landing right after capture
                                    currentRow = jumpRow;
                                    currentCol = jumpCol;
                                }
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }

                } else {
                    break;
                }

                if (!piece.isKing()) break;
            }
        }

        return landingSpots;
    }

    /**
     * prepares the board for a local match
     * @param activePlayerId the player who will start the game
     * @param myPlayerId the player who created the board
     */
    public static CheckersBoard createCheckersBoard(@NonNull final String activePlayerId,
                                             @NonNull final String myPlayerId,
                                             @NonNull final String opponentPlayerId) {

        final CheckersBoard checkersBoard = new CheckersBoard();

        checkersBoard.setCreatorId(myPlayerId);
        checkersBoard.setActivePlayerId(activePlayerId);
        checkersBoard.setOpponentId(opponentPlayerId);

        checkersBoard.setPieces(createPieces(myPlayerId, opponentPlayerId));

        return checkersBoard;

    }

    private static List<Piece> createPieces(final String myPlayerId, final String opponentPlayerId) {
        final List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                if(isLightCell(row, col) || row > 2 && row < 5) continue;

                final Piece piece = new Piece();
                piece.setId(UUID.randomUUID().toString());
                piece.setRow(row);
                piece.setCol(col);
                piece.setKing(false);

                if(row <= 2) {
                    piece.setPlayerId(opponentPlayerId);
                    piece.setColor("#FFFF99");
                } else if(row >= 5) {
                    piece.setPlayerId(myPlayerId);
                    piece.setColor("#FFFFFFFF");
                }

                pieces.add(piece);
            }
        }
        return pieces;
    }


    public static boolean isDarkCell(final int row, final int col) {
        // A dark box has an odd sum of row + col
        return (row + col) % 2 != 0;
    }

    public static boolean isLightCell(final int row, final int col) {
        // A light box has an even sum of row + col
        return (row + col) % 2 == 0;
    }

    public CheckersBoard() {}

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public String getActivePlayerId() {
        return activePlayerId;
    }

    public void setActivePlayerId(String activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public void setBoardWidth(int boardWidth) {
        this.boardWidth = boardWidth;
    }

    public NormalPieceRule getNormalPieceRule() {
        return normalPieceRule;
    }

    public void setNormalPieceRule(NormalPieceRule normalPieceRule) {
        this.normalPieceRule = normalPieceRule;
    }

    public CaptureRule getCaptureRule() {
        return captureRule;
    }

    public void setCaptureRule(CaptureRule captureRule) {
        this.captureRule = captureRule;
    }

    public GameFlowRule getGameFlowRule() {
        return gameFlowRule;
    }

    public void setGameFlowRule(GameFlowRule gameFlowRule) {
        this.gameFlowRule = gameFlowRule;
    }

    public KingPieceRule getKingPieceRule() {
        return kingPieceRule;
    }

    public void setKingPieceRule(KingPieceRule kingPieceRule) {
        this.kingPieceRule = kingPieceRule;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
