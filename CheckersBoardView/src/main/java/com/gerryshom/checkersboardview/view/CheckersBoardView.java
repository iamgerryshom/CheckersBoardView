package com.gerryshom.checkersboardview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gerryshom.checkersboardview.defaults.DefaultPaint;
import com.gerryshom.checkersboardview.defaults.DefaultRule;
import com.gerryshom.checkersboardview.enums.Direction;
import com.gerryshom.checkersboardview.helper.BoardHelper;
import com.gerryshom.checkersboardview.helper.PieceHelper;
import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.board.Piece;
import com.gerryshom.checkersboardview.model.guides.Highlight;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.gerryshom.checkersboardview.model.rules.CaptureRule;
import com.gerryshom.checkersboardview.model.rules.GameFlowRule;
import com.gerryshom.checkersboardview.model.rules.KingPieceRule;
import com.gerryshom.checkersboardview.model.rules.NormalPieceRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class CheckersBoardView extends View {

    private List<Piece> pieces = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();
    private List<BoardListener> listeners = new ArrayList<>();
    private List<Highlight> highlights = new ArrayList<>();

    private String myPlayerId;
    private String remotePlayerId;
    private String activePlayerId;

    private Paint darkTilePaint;
    private Paint lightTilePaint;

    private CaptureRule captureRule;
    private GameFlowRule gameFlowRule;
    private KingPieceRule kingPieceRule;
    private NormalPieceRule normalPieceRule;
    private CheckersBoard checkersBoard;

    public CheckersBoardView(Context context) {
        super(context);
        init();
    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public interface BoardListener {
        default void onPieceCompletedMoveSequence(final MoveSequence moveSequence){}
        default void onActivePlayerSwitched(final String newActivePlayerId){}
        default void onPieceCaptured(final String capturedPiecePlayerId, final int remainingPieceCount){}
        default void onWin(final String winnerPlayerId){}
    }

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

    /**
     * adds a lister to observe various board activities
     */
    public void addListener(final BoardListener listener) {
        listeners.add(listener);
    }

    public void setRule(final CaptureRule captureRule) {
        this.captureRule = captureRule;
    }

    public void setRule(final NormalPieceRule normalPieceRule) {
        this.normalPieceRule = normalPieceRule;
    }

    public void setRule(final KingPieceRule kingPieceRule) {
        this.kingPieceRule = kingPieceRule;
    }

    public void setRule(final GameFlowRule gameFlowRule) {
        this.gameFlowRule = gameFlowRule;
    }

    private void init() {
        //tile paints
        darkTilePaint = DefaultPaint.darkTilePaint();
        lightTilePaint = DefaultPaint.lightTilePaint();

        listeners.add(new BoardListener() {}); // helps to avoid null pointer exception

        setDefaultRules();


    }

    public void setDefaultRules() {
        captureRule = DefaultRule.captureRule();
        gameFlowRule = DefaultRule.gameFlowRule();
        kingPieceRule = DefaultRule.kingPieceRule();
        normalPieceRule = DefaultRule.normalPieceRule();
    }

    /**
     * sets the id of the player created the board
     * @param myPlayerId the player id
     */
    public void setMyPlayerId(final String myPlayerId) {
        this.myPlayerId = myPlayerId;
        checkersBoard = resetBoard(myPlayerId,myPlayerId, "2");
        pieces = checkersBoard.getPieces();
    }

    /**
     * Called when a checkers board for a live match is created
     */
    public void setCheckersBoard(final CheckersBoard checkersBoard) {
        this.checkersBoard = checkersBoard;
        pieces = checkersBoard.getPieces();

        if(myPlayerId.equals(checkersBoard.getCreatorId())) {
            remotePlayerId = checkersBoard.getOpponentId();
            setRotation(0);
        } else {
            remotePlayerId = checkersBoard.getCreatorId();
            setRotation(180); // rotates the board so that the player at the top can play as if they are at the bottom. This makes it easier to play instead of rotating the whole device/
        }

/*
        remotePlayerId = myPlayerId.equals(checkersBoard.getCreatorId()) ?
                checkersBoard.getOpponentId() : checkersBoard.getCreatorId();
 */

        activePlayerId = checkersBoard.getActivePlayerId();

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = getWidth() / 8;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSize, widthSize);
        } else {
            width = desiredSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredSize, heightSize);
        } else {
            height = desiredSize;
        }

        int size = Math.min(width, height); // Make it square

        setMeasuredDimension(size, size);
    }

    private Piece touchedPiece;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float touchX = event.getX();
        final float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(touchX, touchY);
                break;
        }

        return true;
    }

    /**
     * ensures only active player can select a piece and only pieces that belong to the current player can be selected
     */
    private void onActionDown(final float touchX, final float touchY) {

        //checks if the current user is not the active player
        if(!activePlayerId.equals(myPlayerId)) return;

        final Piece piece = findTouchedPieceByTouchXAndY(touchX, touchY);

        if (piece == null) {
            handleMove(touchX, touchY);
        } else {
            //checks if the piece does not belong to the current player
            if(!piece.getPlayerId().equals(myPlayerId)) return;
            handlePieceSelection(piece);
        }

    }

    /**
     * ensures that the move that is about to be made is legal
     */
    private void handleMove(final float touchX, final float touchY) {
        if (touchedPiece == null) return;

        final Point newRowAndCol = calculateRowAndCol(touchX, touchY);
        final Point newCenterXAndY = calculateCellCenter(newRowAndCol.x, newRowAndCol.y);

        final Move move = buildMove(touchedPiece, newRowAndCol, newCenterXAndY);

        if (validateMove(move)) processMove(move, touchedPiece);

    }

    /**
     * highlights possible landing cells for the selected piece and also highlights the selected piece to make it distinguishable from the unselected pieces
     * it also prevents a piece from exiting a capture chain if allowed in the rules
     * @param piece that was touched
     */
    private void handlePieceSelection(final Piece piece) {

        if (touchedPiece != null) {
            touchedPiece.setHighlighted(false);
            if(touchedPiece.isInCaptureChain() && captureRule.isForceCapture()) return;
        }

        touchedPiece = piece;
        piece.setHighlighted(true);

        addHighlights(piece, piece.getCenterX(), piece.getCenterY());

    }

    /**
     * determines if a move that was made would terminate immediately or start a capture chain
     * @param move the object containing movement metadata
     */
    private boolean capturing;
    private void processMove(final Move move, final Piece piece) {

        final Point designatedRowAndCol = calculateRowAndCol(move.getToCenterX(), move.getToCenterY());
        final Point currentRowAndCol = calculateRowAndCol(move.getFromCenterX(), move.getFromCenterY());

        if(piece.isKing()) {

            final Piece capturedPiece = findPossibleCapture(
                    findPieceById(move.getPieceId()).getPlayerId(), move.getFromCenterX(), move.getFromCenterY(), move.getToCenterX(), move.getToCenterY()
            );

            if(capturedPiece != null) {
                move.setCapturedPieceId(capturedPiece.getId());
                capturing = true;
            }

        } else {
            // Check if the move is too far (for a standard move)
            if (Math.abs(designatedRowAndCol.y - currentRowAndCol.y) > 1) {

                final Piece capturedPiece = findPossibleCapture(
                        findPieceById(move.getPieceId()).getPlayerId(), move.getFromCenterX(), move.getFromCenterY(), move.getToCenterX(), move.getToCenterY()
                );

                if(capturedPiece != null) {
                    move.setCapturedPieceId(capturedPiece.getId());
                    capturing = true;
                }


            }
        }

        moves.add(move);

        // Always find possible captures at the new spot
        final List<Piece> possibleCaptures = findPossibleCaptures(touchedPiece.getPlayerId(), move.getToCenterX(), move.getToCenterY());

        touchedPiece.setInCaptureChain(!possibleCaptures.isEmpty() && capturing);

        playMove(touchedPiece, move, possibleCaptures.isEmpty());

        if (move.getCapturedPieceId() != null && !possibleCaptures.isEmpty()) {
            // Was a capture move and more captures possible → continue chain
            addHighlights(findPieceById(move.getPieceId()), move.getToCenterX(), move.getToCenterY());

        } else {
            // Either no capture or no further captures → end move
            endMove();
        }
    }

    /**
     * terminates a move when there can no longer be any more moves
     */
    private void endMove() {
        touchedPiece.setHighlighted(false);
        capturing = false;

        for(BoardListener listener : listeners)
            listener.onPieceCompletedMoveSequence(new MoveSequence(remotePlayerId, moves));

        moves.clear();

        touchedPiece = null;
        highlights.clear();

        switchPlayers(remotePlayerId);

    }

    /**
     * sets the king property to true for a piece
     * @param creatorId userId for player who created the board. This player starts from the 7 - 5 rows
     * @param playerId userId of the player who moved a piece
     * @param toRow the destination row that the piece was moved to
     */
    private boolean crownKing(final String creatorId, final String playerId, final int toRow) {
        if (creatorId.equals(playerId)) {
            // Creator moving upward → Crown at row 0
            return toRow == 0;
        } else {
            // Opponent moving downward → Crown at row 7
            return toRow == 7;
        }
    }

    /**
     * a common method for find landing highlights
     */
    private List<Highlight> commonLandingHighlights(final Piece p, final float currentCenterX, final float currentCenterY) {
        return createLandingHighlights(p, currentCenterX, currentCenterY,
                p.isKing() || !normalPieceRule.isRestrictToForwardMovement()
                        ? Arrays.asList(Direction.TOP_LEFT, Direction.TOP_RIGHT, Direction.BOTTOM_LEFT, Direction.BOTTOM_RIGHT)
                        : p.getPlayerId().equals(checkersBoard.getCreatorId())
                        ? Arrays.asList(Direction.TOP_LEFT, Direction.TOP_RIGHT)
                        : Arrays.asList(Direction.BOTTOM_LEFT, Direction.BOTTOM_RIGHT),
                normalPieceRule.isAllowBackwardCapture(),
                kingPieceRule.getMaxMoveSteps(),
                kingPieceRule.getMaxLandingDistanceAfterCapture());
    }

    /**
     * clears the previous landing highlights and then
     * adds the new highlights to a highlights list
     */
    private void addHighlights(final Piece piece, final float currentCenterX, final float currentCenterY) {

        highlights.clear();
        highlights.addAll(commonLandingHighlights(piece, currentCenterX, currentCenterY));

        invalidate();
    }

    /**
     * Calculates and returns a list of possible landing cells for the given piece.
     *
     * @param piece                              The selected piece
     * @param currentCenterX                     X coordinate of the selected piece
     * @param currentCenterY                     Y coordinate of the selected piece
     * @param allowedDirections                  Directions the piece is allowed to move
     * @param allowHighlightsInForbiddenDirections Whether to allow captures in forbidden directions
     * @param maxKingMoveSteps                       Maximum number of steps a king can take in a regular move (0 = no limit)
     * @param maxKingJumpLandingDistance             Maximum distance a king can land after a capture (0 = no limit)
     * @return List of Highlight positions where the piece can land
     */
    private List<Highlight> createLandingHighlights(final Piece piece,
                                                    final float currentCenterX,
                                                    final float currentCenterY,
                                                    final List<Direction> allowedDirections,
                                                    final boolean allowHighlightsInForbiddenDirections,
                                                    final int maxKingMoveSteps,
                                                    final int maxKingJumpLandingDistance) {

        final List<Highlight> landingHighlights = new ArrayList<>();
        final Point currentRowAndCol = calculateRowAndCol(currentCenterX, currentCenterY);

        for (Direction dir : Direction.values()) {
            int currentRow = currentRowAndCol.x;
            int currentCol = currentRowAndCol.y;

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
                            landingHighlights.add(new Highlight(calculateCellCenter(nextRow, nextCol)));
                        }
                        if (!piece.isKing() || (maxKingMoveSteps != 0 && moveSteps >= maxKingMoveSteps)) break;
                    } else {
                        // We've already moved to the capture landing position (step 1)
                        // Now we're considering additional steps after the capture
                        jumpLandingSteps++;

                        // Only add highlights if we're within the allowed landing distance
                        if ((allowedDirections.contains(dir) || allowHighlightsInForbiddenDirections)
                                && (maxKingJumpLandingDistance == 0 || jumpLandingSteps <= maxKingJumpLandingDistance)) {
                            landingHighlights.add(new Highlight(calculateCellCenter(nextRow, nextCol)));
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
                                landingHighlights.add(new Highlight(calculateCellCenter(jumpRow, jumpCol)));
                                break;
                            }

                            // For kings with maxJumpLandingDistance = 0, add the landing spot and continue
                            if (maxKingJumpLandingDistance == 0) {
                                landingHighlights.add(new Highlight(calculateCellCenter(jumpRow, jumpCol)));
                                justJumped = true;
                                jumpLandingSteps = 0;
                                currentRow = jumpRow;
                                currentCol = jumpCol;
                            }
                            // For kings with specific maxJumpLandingDistance
                            else {
                                // The immediate landing spot after capture is always allowed
                                if (maxKingJumpLandingDistance >= 1) {
                                    landingHighlights.add(new Highlight(calculateCellCenter(jumpRow, jumpCol)));
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

        return landingHighlights;
    }

    /**
     * switched active player to opponent player once the current player has completed a move
     * @param activePlayerId the new active player id
     */
    private void switchPlayers(final String activePlayerId) {
        this.activePlayerId = activePlayerId;
        for(BoardListener listener : listeners) {
            listener.onActivePlayerSwitched(activePlayerId);
        }
    }

    /**
     * returns id of the opponent player
     * @param playerId id of the current player
     * @return id of the enemy player
     */
    private String identifyOpponentPlayerId(final String playerId) {
        if(playerId == null ) throw new RuntimeException("playerId is null");
        return playerId.equals(myPlayerId)
                ? remotePlayerId
                        : myPlayerId;
    }

    /**
     * plays the recorded move
     * @param piece that was involved in the move
     * @param move object containing movement metadata
     * @param isFinalMove ensures that a piece will not be crowned as king if it lands in the opponent's last row while in a capture chain
     */
    private void playMove(final Piece piece, final Move move, final boolean isFinalMove) {

        if(piece == null || move == null) return;

        if(!piece.isKing() && isFinalMove)
            piece.setKing(crownKing(checkersBoard.getCreatorId(), piece.getPlayerId(), move.getToRow()));

        if(move.getCapturedPieceId() != null) {
            final Piece capturedPiece = findPieceById(move.getCapturedPieceId());
            pieces.remove(capturedPiece);

            final int remainingPieces = getPieceCountByPlayerId(capturedPiece.getPlayerId());

            for(BoardListener listener : listeners)
                listener.onPieceCaptured(capturedPiece.getPlayerId(), remainingPieces);

        }

        animatePieceMovement(piece, move.getToCenterX(), move.getToCenterY(), ()->{

            final String opponentPlayerId = identifyOpponentPlayerId(piece.getPlayerId());
            switchPlayers(opponentPlayerId);

            if(findMoveablePiecesByPlayerId(opponentPlayerId).isEmpty())
                for(BoardListener listener : listeners)
                    listener.onWin(piece.getPlayerId());

            invalidate();
        });

    }

    private interface AnimationListener {
        void onAnimationEnd();
    }

    /**
     * plays moves that were made by the opponent/remote player
     * @param moveSequence containing list of step object containing movement metadata
     */
    public void playOpponentMoveSequence(final MoveSequence moveSequence) {

        for(int i = 0; i < moveSequence.getMoves().size(); i++) {

            final Move move = moveSequence.getMoves().get(i);

            final int toRow = move.getToRow();
            final int toCol = move.getToCol();

            final Point centerXAndY = calculateCellCenter(toRow, toCol);
            move.setToCenterX(centerXAndY.x);
            move.setToCenterY(centerXAndY.y);

            final Piece piece = findPieceById(move.getPieceId());

            playMove(piece, move, i == moveSequence.getMoves().size() - 1);

        }

    }


    private Piece findPieceById(final String id) {
        for(Piece p : pieces) {
            if(p.getId().equals(id)) return p;
        }
        return null;
    }

    /**
     * checks that a row or col is within the grid of the board
     */
    private boolean isValidRowCol(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8; // 8x8 grid for standard checkers
    }

    private Move buildMove(final Piece touchedPiece, final Point newRowAndCol, final Point newCenterXAndY) {

        final Move move = new Move();
        move.setId(UUID.randomUUID().toString());
        move.setToCenterX(newCenterXAndY.x);
        move.setToCenterY(newCenterXAndY.y);
        move.setFromCenterX(touchedPiece.getCenterX());
        move.setFromCenterY(touchedPiece.getCenterY());
        move.setPieceId(touchedPiece.getId());
        move.setToRow(newRowAndCol.x);
        move.setToCol(newRowAndCol.y);

        return move;
    }

    /**
     * checks that a piece being moved has valid landing highlight. No landing highlight == invalid move
     */
    private boolean validateMove(final Move move) {

        final Point designatedRowAndCol = calculateRowAndCol(move.getToCenterX(), move.getToCenterY());

        for(Highlight highlight : highlights) {
            final Point highlightRowAndCol = calculateRowAndCol(highlight.getPoint().x, highlight.getPoint().y);
            //if(!piece.isKing() && designatedRowAndCol.x > currentRowAndCol.x) return false;
            if(designatedRowAndCol.x == highlightRowAndCol.x && designatedRowAndCol.y == highlightRowAndCol.y) return true;
        }

        return false;

        /*

        // Check if the destination is a light cell
        if (isLightCell(designatedRowAndCol.x, designatedRowAndCol.y)) return false;

        // Check if the piece is moving to the same row or column
        if (currentRowAndCol.x == designatedRowAndCol.x || currentRowAndCol.y == designatedRowAndCol.y) return false;

        // The move must be diagonal
        if (Math.abs(designatedRowAndCol.x - currentRowAndCol.x) != Math.abs(designatedRowAndCol.y - currentRowAndCol.y)) return false;


        //if (designatedRowAndCol.x > currentRowAndCol.x) return false;

        return true;

         */
    }

    /**
     * checks all the player's pieces to see if they have any legal moves left
     * @param playerId id of the player
     * @return a list of the moveable pieces
     */
    private List<Piece> findMoveablePiecesByPlayerId(final String playerId) {
        final List<Piece> moveablePieces = new ArrayList<>();
        for(Piece p : pieces) {
            if(!p.getPlayerId().equals(playerId)) continue;

            if(!commonLandingHighlights(p, p.getCenterX(), p.getCenterY()).isEmpty())
                moveablePieces.add(p);

        }

        return moveablePieces;
    }

    /**
     * attempts to find an enemy piece that was jumped.
     * @param initialCenterX initial x co-ordinate of the piece that made the jump
     * @param initialCenterY initial y co-ordinate of the piece that made the jump
     * @param finalX final x co-ordinate of the piece that made the jump
     * @param finalY initial final co-ordinate of the piece that made the jump
     * @return enemy piece object that was jumped or null if no enemy piece was jumped
     */
    private Piece findPossibleCapture(final String playerId, final float initialCenterX, final float initialCenterY, final float finalX, final float finalY) {
        // Calculate the current row and column of the selected piece
        final Point currentRowAndCol = calculateRowAndCol(initialCenterX, initialCenterY);
        int startRow = currentRowAndCol.x;
        int startCol = currentRowAndCol.y;

        // Calculate the row and column of the designated (final) destination
        final Point designatedRowAndCol = calculateRowAndCol(finalX, finalY);
        int endRow = designatedRowAndCol.x;
        int endCol = designatedRowAndCol.y;

        // Determine direction of movement
        int rowDirection = Integer.compare(endRow - startRow, 0);
        int colDirection = Integer.compare(endCol - startCol, 0);

        // For a valid capture, start and end must be at least 2 steps apart
        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endCol - startCol);

        if (rowDiff != colDiff || rowDiff < 2) {
            return null; // Not a diagonal move or not far enough for a capture
        }

        // For regular pieces, check only the middle position
        if (rowDiff == 2) {
            int middleRow = startRow + rowDirection;
            int middleCol = startCol + colDirection;

            Piece middlePiece = findPieceByRowAndCol(middleRow, middleCol);
            if (middlePiece != null && !middlePiece.getPlayerId().equals(playerId)) {
                return middlePiece;
            }
        }
        // For king pieces, check all positions between start and end
        else {
            // Start from the position after the starting point
            int currentRow = startRow + rowDirection;
            int currentCol = startCol + colDirection;

            Piece enemyPiece = null;

            // Check all positions between start and end (exclusive of end)
            while (currentRow != endRow && currentCol != endCol) {
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
     * Helper method to check if a position is valid (within bounds)
      */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * returns all the pieces a player can possibly capture in all directions
     * @param playerId the id of the player attempting to make a move
     * @param currentCenterX the current x co-ordinate for the selected piece
     * @param currentCenterY the current y co-ordinate for the selected piece
     * @return a list of the pieces
     */
    private List<Piece> findPossibleCaptures(final String playerId, final float currentCenterX, final float currentCenterY) {

        final List<Piece> possibleCaptures = new ArrayList<>();

        final Point currentRowAndCol = calculateRowAndCol(currentCenterX, currentCenterY);

        final int currentRow = currentRowAndCol.x;
        final int currentCol = currentRowAndCol.y;

        final int[] rowDirections = {-2, -2, 2, 2};
        final int[] colDirections = {-2, 2, -2, 2};

        for(int i = 0; i < 4; i++) {
            final int row = currentRow + rowDirections[i];
            final int col = currentCol + colDirections[i];

            final Piece pieceAtDestination = findPieceByRowAndCol(row, col);

            final int middleRow = currentRow + rowDirections[i] / 2; // Middle piece's row
            final int middleCol = currentCol + colDirections[i] / 2; // Middle piece's column

            final Piece middlePiece = findPieceByRowAndCol(middleRow, middleCol);

            if(middlePiece == null) continue;
            if(middlePiece.getPlayerId().equals(playerId)) continue;
            if(pieceAtDestination != null) continue;
            if(!isValidRowCol(row, col)) continue;

            possibleCaptures.add(middlePiece);

        }

        return possibleCaptures;

    }

    /**
     * finds a piece object in a list using the row and column
     */
    // Helper method to find a piece at a given position
    private Piece findPieceByRowAndCol(int row, int col) {
        return PieceHelper.findPieceByRowAndCol(pieces, getWidth(), row, col);
    }

    /**
     * visually animates the movement of a piece from initial co-ordinates to the final co-ordinates
     * @param piece that was moved
     * @param touchX x co-ordinated of the touched location on the screen
     * @param touchY y co-ordinated of the touched location on the screen
     */
    private void animatePieceMovement(final Piece piece, final float touchX, final float touchY, final AnimationListener listener) {

        final long duration = 300L;

        // Get the new center position
        final Point point = calculateNewCenterXAndY(touchX, touchY);

        // Current position of the piece
        final float startX = piece.getCenterX();
        final float startY = piece.getCenterY();

        // Create two ValueAnimators, one for X and one for Y
        final ValueAnimator animatorX = ValueAnimator.ofFloat(startX, point.x);
        animatorX.setDuration(duration); // Duration of the animation (in milliseconds)
        animatorX.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth animation curve

        final ValueAnimator animatorY = ValueAnimator.ofFloat(startY, point.y);
        animatorY.setDuration(duration); // Duration of the animation (in milliseconds)
        animatorY.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth animation curve

        // Update the position of the piece during the animation
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                piece.setCenterX(animatedValue);
                invalidate();  // Redraw the board with updated piece position
            }
        });

        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                piece.setCenterY(animatedValue);
                invalidate();  // Redraw the board with updated piece position

            }
        });

        // Start both animations
        animatorX.start();
        animatorY.start();

        new Handler().postDelayed(()->{listener.onAnimationEnd();},duration + 50L);
    }

    /**
     * resolves the touch co-ordinates into a perfect center co-ordinates
     */
    private Point calculateNewCenterXAndY(final float touchX, final float touchY) {
        final Point rowCol = calculateRowAndCol(touchX, touchY);
        return calculateCellCenter(rowCol.x, rowCol.y);
    }

    /**
     * resolves touch co-ordinates on the board into row and col on the board
     * @param touchX touched x coordinate
     * @param touchY touched y coordinate
     */
    private Point calculateRowAndCol(
            final float touchX,
            final float touchY) {
        return BoardHelper.calculateRowAndCol(getWidth(), touchX, touchY);
    }

    private Point calculateCellCenter(
            int row, int col) {
        return BoardHelper.calculateCellCenter(getWidth(), row, col);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);

        drawPieces(canvas);

        drawHighlights(canvas);

    }

    private void drawHighlights(final Canvas canvas) {
        // Paint for the outermost border circle (light green)
        final Paint outerPaint = new Paint();
        outerPaint.setColor(Color.parseColor("#81C784")); // Light green
        outerPaint.setStyle(Paint.Style.STROKE);  // Stroke style (borders)
        outerPaint.setAlpha(255);  // Full opacity
        outerPaint.setAntiAlias(true);
        outerPaint.setStrokeWidth(6f);  // Reduced border thickness for outer circle

        // Paint for the inner border circle (light green)
        final Paint innerPaint = new Paint();
        innerPaint.setColor(Color.parseColor("#81C784")); // Light green
        innerPaint.setStyle(Paint.Style.STROKE);  // Stroke style (borders)
        innerPaint.setAlpha(180);  // Full opacity
        innerPaint.setAntiAlias(true);
        innerPaint.setStrokeWidth(3f);  // Reduced border thickness for inner circle (half the outer border thickness)

        // Paint for the filled inner circle (red)
        final Paint middlePaint = new Paint();
        middlePaint.setColor(Color.parseColor("#F44336")); // Red color
        middlePaint.setStyle(Paint.Style.FILL);  // Fill style for the circle
        middlePaint.setAntiAlias(true);
        middlePaint.setAlpha(180);  // Full opacity

        // Define reduced radius values for the circles
        final float outerRadius = (getWidth() / 8) * 0.25f; // Reduced outer circle radius (smaller gap between inner and outer)
        final float innerRadius = outerRadius * 0.75f;    // Reduced inner circle radius
        final float fillRadius = innerRadius * 0.85f;      // Smaller filled circle radius (closer to the inner circle)

        for (Highlight highlight : highlights) {
            final Point point = highlight.getPoint();

            // Draw the outermost border circle (light green)
            canvas.drawCircle(point.x, point.y, outerRadius, outerPaint);

            // Draw the inner border circle (light green)
            canvas.drawCircle(point.x, point.y, innerRadius, innerPaint);

            // Draw the filled inner circle (red)
            canvas.drawCircle(point.x, point.y, fillRadius, middlePaint);
        }
    }

    private void drawBoard(final Canvas canvas) {

        final int cellSize = getWidth() / 8;

        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {

                final Paint paint = isDarkCell(row, col) ? darkTilePaint : lightTilePaint;

                final int left = col * cellSize;
                final int top = row * cellSize;
                final int right = left + cellSize;
                final int bottom = top + cellSize;

                canvas.drawRect(left, top, right, bottom, paint);

            }
        }

    }

    private void drawPieces(final Canvas canvas) {
        int cellSize = getWidth() / 8; // Assuming the board is 8x8

        for (Piece piece : pieces) {
            preparePieceCenter(piece, cellSize);
            drawPiece(canvas, piece, cellSize);
        }
    }

    private void preparePieceCenter(Piece piece, int cellSize) {
        int row = piece.getRow();
        int col = piece.getCol();

        if (piece.getCenterX() == 0) {
            piece.setCenterX(col * cellSize + cellSize / 2f);
        }
        if (piece.getCenterY() == 0) {
            piece.setCenterY(row * cellSize + cellSize / 2f);
        }
    }

    private void drawPiece(Canvas canvas, Piece piece, int cellSize) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw highlight first if the piece is highlighted
        if (piece.isHighlighted()) {
            drawHighlight(canvas, piece, paint, cellSize);
        }

        // Now draw the actual piece (king or normal)
        if (piece.isKing()) {
            drawKingPiece(canvas, piece, paint, cellSize);
        } else {
            drawNormalPiece(canvas, piece, paint, cellSize);
        }
    }

    private void drawHighlight(Canvas canvas, Piece piece, Paint paint, int cellSize) {
        // Calculate the size and position of the highlight square based on the cell size
        float squareSize = cellSize * 0.75f; // Inner square size (75% of cell size to avoid overlap)
        float left = piece.getCenterX() - squareSize / 2;
        float top = piece.getCenterY() - squareSize / 2;
        float right = piece.getCenterX() + squareSize / 2;
        float bottom = piece.getCenterY() + squareSize / 2;

        // Set up the paint for the highlight square (highlight color with toned-down opacity)
        paint.setColor(Color.parseColor("#FF5722")); // Lighter orange (hex: #FFB74D)
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(180); // Reduced opacity
        canvas.drawRect(left, top, right, bottom, paint); // Draw the square

        // Draw the border around the square
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FF5722")); // Darker orange border (hex: #FF5722)
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f); // Thicker border width
        borderPaint.setAlpha(255); // Full opacity for the border
        borderPaint.setAntiAlias(true);

        // Small offset for the border
        float borderOffset = 4f;
        canvas.drawRect(left - borderOffset, top - borderOffset, right + borderOffset, bottom + borderOffset, borderPaint); // Draw the border
    }

    private void drawKingPiece(Canvas canvas, Piece piece, Paint paint, int cellSize) {
        // Draw the king piece
        paint.setColor(Color.parseColor(piece.getColor()));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(piece.getCenterX(), piece.getCenterY(), cellSize * 0.4f, paint);

        // Draw a crown (small circle) inside the king piece
        paint.setColor(Color.RED); // Crown color
        canvas.drawCircle(piece.getCenterX(), piece.getCenterY(), cellSize * 0.2f, paint);
    }

    private void drawNormalPiece(Canvas canvas, Piece piece, Paint paint, int cellSize) {
        paint.setColor(Color.parseColor(piece.getColor()));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(piece.getCenterX(), piece.getCenterY(), cellSize * 0.4f, paint);
    }

/*
    private void d(Canvas canvas, Piece piece, Paint paint, int cellSize) {
        // Load the vector drawable (checker piece)
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.approve_vector);
        int pieceColor = Color.parseColor(piece.getColor());  // This would be the color of the piece (e.g., "red", "black", etc.)
        drawable.setTint(pieceColor);
        // Set the size for the drawable (scale to fit the cell size)
        int size = (int) (cellSize * 0.8); // Adjust size as needed
        drawable.setBounds(
                (int) (piece.getCenterX() - size / 2),
                (int) (piece.getCenterY() - size / 2),
                (int) (piece.getCenterX() + size / 2),
                (int) (piece.getCenterY() + size / 2)
        );

        // Draw the drawable onto the canvas
        drawable.draw(canvas);
    }

 */

    /**
     * resolves touch co-ordinates into radius field and returns any piece that falls withing that field
     */
    private Piece findTouchedPieceByTouchXAndY(final float touchX, final float touchY) {
        final int cellSize = getWidth() / 8;
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
     * prepares the board for a local match
     * @param activePlayerId the player who will start the game
     * @param myPlayerId the player who created the board
     * @param remotePlayerId the player who joined the board
     */
    public CheckersBoard resetBoard(@NonNull final String activePlayerId,
                           @NonNull final String myPlayerId,
                           @NonNull final String remotePlayerId) {

        final CheckersBoard checkersBoard = new CheckersBoard();
        checkersBoard.setPieces(new ArrayList<>());

        this.activePlayerId = activePlayerId;
        this.myPlayerId = myPlayerId;
        this.remotePlayerId = remotePlayerId;

        checkersBoard.setCreatorId(myPlayerId);
        checkersBoard.setActivePlayerId(myPlayerId);
        checkersBoard.setOpponentId(remotePlayerId);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                if(isLightCell(row, col) || row > 2 && row < 5) continue;

                final Piece piece = new Piece();
                piece.setId(UUID.randomUUID().toString());
                piece.setRow(row);
                piece.setCol(col);
                piece.setKing(false);

                if(row <= 2) {
                    piece.setPlayerId(remotePlayerId);
                    piece.setColor("#FFFF99");
                } else if(row >= 5) {
                    piece.setPlayerId(myPlayerId);
                    piece.setColor("#FFFFFFFF");
                }

                checkersBoard.getPieces().add(piece);
            }
        }

        return checkersBoard;

    }


    private boolean isDarkCell(final int row, final int col) {
        // A dark box has an odd sum of row + col
        return (row + col) % 2 != 0;
    }

    private boolean isLightCell(final int row, final int col) {
        // A light box has an even sum of row + col
        return (row + col) % 2 == 0;
    }


}
