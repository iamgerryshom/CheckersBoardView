package com.gerryshom.checkersboardview.board.handler;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gerryshom.checkersboardview.ai.algorithm.MiniMax;
import com.gerryshom.checkersboardview.board.listener.BoardListener;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.movement.model.Move;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.player.Player;
import com.gerryshom.checkersboardview.rules.defaults.DefaultRule;
import com.gerryshom.checkersboardview.rules.model.CaptureRule;
import com.gerryshom.checkersboardview.rules.model.GameFlowRule;
import com.gerryshom.checkersboardview.rules.model.KingPieceRule;
import com.gerryshom.checkersboardview.rules.model.NormalPieceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoardHandler {

    private long movementDurationMillis = 250L;

    private CheckersBoard checkersBoard;
    private final List<LandingSpot> landingSpots = new ArrayList<>();

    private final List<Move> moves = new ArrayList<>();

    private List<com.gerryshom.checkersboardview.board.listener.BoardListener> listeners = new ArrayList<>();

    private String myPlayerId;
    private String remotePlayerId;
    private String activePlayerId;

    private Piece touchedPiece;

    private com.gerryshom.checkersboardview.board.handler.listener.BoardListener boardListener;

    public BoardHandler() {}

    public BoardHandler(CheckersBoard checkersBoard) {
        this.checkersBoard = checkersBoard;
        listeners.add(new com.gerryshom.checkersboardview.board.listener.BoardListener() {}); // helps to avoid null pointer exception
    }

    public void reset() {
        if(checkersBoard == null)
            throw new RuntimeException("No CheckersBoard had been setup");
        setup(checkersBoard.getBoardWidth(), activePlayerId, remotePlayerId);
    }

    public void setup(final int boardWidth, final String activePlayerId, final String opponentPlayerId) {
        final CheckersBoard checkersBoard = CheckersBoard.createCheckersBoard(activePlayerId, myPlayerId, opponentPlayerId);
        checkersBoard.setBoardWidth(boardWidth);
        setCheckersBoard(checkersBoard);
    }

    public String getMyPlayerId() {
        return myPlayerId;
    }

    public void setup(final CheckersBoard checkersBoard) {
        setCheckersBoard(checkersBoard);
    }

    public void setBoardListener(final com.gerryshom.checkersboardview.board.handler.listener.BoardListener boardListener) {
        this.boardListener = boardListener;
    }

    /**
     * ensures only active player can select a piece and only pieces that belong to the current player can be selected
     */
    public void onActionDown(final float touchX, final float touchY) {

        //checks if the current user is not the active player
        if(!activePlayerId.equals(myPlayerId)) return;

        final Piece piece = checkersBoard.findTouchedPieceByTouchXAndY(touchX, touchY);

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

        final Point newRowCol = checkersBoard.calculateRowColByXAndY(touchX, touchY);
        final PointF newCenterXY = checkersBoard.calculateCenterXYByRowAndCol(newRowCol.x, newRowCol.y);

        final Move move = buildMove(
                touchedPiece.getId(),
                touchedPiece.getCenterX(),
                newCenterXY.x,
                touchedPiece.getCenterY(),
                newCenterXY.y,
                touchedPiece.getRow(),
                newRowCol.x,
                touchedPiece.getCol(),
                newRowCol.y
        );

        if (validateMove(move)) processMove(move);

    }

    /**
     * highlights possible landing cells for the selected piece and also highlights the selected piece to make it distinguishable from the unselected pieces
     * it also prevents a piece from exiting a capture chain if allowed in the rules
     * @param piece that was touched
     */
    private void handlePieceSelection(final Piece piece) {

        if (touchedPiece != null) {
            touchedPiece.setHighlighted(false);
            if(touchedPiece.isInCaptureChain() && checkersBoard.getCaptureRule().isForceCapture()) return;
        }

        touchedPiece = piece;
        piece.setHighlighted(true);

        addLandingSpots(piece, piece.getRow(), piece.getCol());

    }

    /**
     * determines if a move that was made would terminate immediately or start a capture chain
     * @param move the object containing movement metadata
     */
    private boolean capturing;
    private void processMove(final Move move) {

        final Piece capturedPiece = checkersBoard.findCaptureBetweenRowCols(
                checkersBoard.findPieceById(move.getPieceId()).getPlayerId(), move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol()
        );

        if(capturedPiece != null) {
            move.setCapturedPieceId(capturedPiece.getId());
            capturing = true;
        }

        moves.add(move);

        // Always find possible captures at the new spot
        final List<Piece> possibleCaptures = checkersBoard.findCapturesByRowAndCol(touchedPiece, move.getToRow(), move.getToCol());

        touchedPiece.setInCaptureChain(!possibleCaptures.isEmpty() && capturing);

        playMove(touchedPiece, move, possibleCaptures.isEmpty(),()->{});

        if (move.getCapturedPieceId() != null && !possibleCaptures.isEmpty()) {
            // Was a capture move and more captures possible → continue chain
            addLandingSpots(checkersBoard.findPieceById(move.getPieceId()), move.getToRow(), move.getToCol());

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

        for(com.gerryshom.checkersboardview.board.listener.BoardListener listener : listeners) {
            listener.onPieceCompletedMoveSequence(new MoveSequence(remotePlayerId, moves));
        }

        if(checkersBoard.getOpponentId().equals(Player.computer().getId())) {
            new Handler().postDelayed(()->{

                MiniMax.searchOptimalMoveSequence(checkersBoard, 5, new MiniMax.SearchListener() {
                    @Override
                    public void onComplete(MoveSequence moveSequence) {
                        playOpponentMoveSequence(moveSequence);
                    }
                });

            }, 350);
        }

        moves.clear();

        touchedPiece = null;
        landingSpots.clear();

        switchPlayers(remotePlayerId);

    }

    /**
     * clears the previous landing spots and then
     * adds the new landing spots to a landingSpots list
     */
    private void addLandingSpots(final Piece piece, final int row, final int col) {

        landingSpots.clear();
        landingSpots.addAll(checkersBoard.findLandingSpots(piece, row, col));

        boardListener.onLandingSpotsAdded(landingSpots);
    }


    /**
     * plays the recorded move
     * @param piece that was involved in the move
     * @param move object containing movement metadata
     * @param isFinalMove ensures that a piece will not be crowned as king if it lands in the opponent's last row while in a capture chain
     */
    private void playMove(final Piece piece, final Move move, final boolean isFinalMove, final AnimationListener animationListener) {

        if(piece == null || move == null) return;

        piece.setRow(move.getToRow());
        piece.setCol(move.getToCol());

        if(!piece.isKing() && isFinalMove)
            piece.setKing(checkersBoard.crownKing(checkersBoard.getCreatorId(), piece.getPlayerId(), move.getToRow()));

        if(move.getCapturedPieceId() != null) {
            final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
            checkersBoard.getPieces().remove(capturedPiece);

            final int remainingPieces = checkersBoard.getPieceCountByPlayerId(capturedPiece.getPlayerId());

            for(com.gerryshom.checkersboardview.board.listener.BoardListener listener : listeners)
                listener.onPieceCaptured(capturedPiece.getPlayerId(), remainingPieces);

        }

        //uses animation to move piece to new position
        animatePieceMovement(piece, move.getToCenterX(), move.getToCenterY(), ()->{

            final String opponentPlayerId = checkersBoard.identifyOpponentPlayerId(piece.getPlayerId());

            if(isFinalMove) {
                switchPlayers(opponentPlayerId);
            }

            if(checkersBoard.findMoveablePiecesByPlayerId(opponentPlayerId).isEmpty())
                for(com.gerryshom.checkersboardview.board.listener.BoardListener listener : listeners)
                    listener.onWin(piece.getPlayerId());

            animationListener.onAnimationEnd();

        });

    }

    public void setMovementDurationMillis(final long movementDurationMillis) {
        this.movementDurationMillis = movementDurationMillis;
    }

    private interface AnimationListener {
        void onAnimationEnd();
    }

    /**
     * plays moves that were made by the opponent/remote player
     * @param moveSequence containing list of step object containing movement metadata
     */
    public void playOpponentMoveSequence(final MoveSequence moveSequence) {
        recursivelyPlayOpponentMoveSequence(moveSequence, 0);
    }

    private void recursivelyPlayOpponentMoveSequence(final MoveSequence moveSequence, final int start) {

        if(start > moveSequence.getMoves().size() - 1) return;

        final Move move = moveSequence.getMoves().get(start);

        final int toRow = move.getToRow();
        final int toCol = move.getToCol();

        final PointF centerXY = checkersBoard.calculateCenterXYByRowAndCol(toRow, toCol);
        move.setToCenterX(centerXY.x);
        move.setToCenterY(centerXY.y);

        final Piece piece = checkersBoard.findPieceById(move.getPieceId());

        playMove(piece, move, start == moveSequence.getMoves().size() - 1, ()->{
            recursivelyPlayOpponentMoveSequence(moveSequence, start + 1);
        });

    }

    private Move buildMove(
            final String pieceId,
            final float fromCenterX,
            final float toCenterX,
            final float fromCenterY,
            final float toCenterY,
            final int fromRow,
            final int toRow,
            final int fromCol,
            final int toCol
    ) {

        final Move move = new Move();
        move.setId(UUID.randomUUID().toString());
        move.setFromCenterX(fromCenterX);
        move.setToCenterX(toCenterX);
        move.setFromCenterY(fromCenterY);
        move.setToCenterY(toCenterY);
        move.setPieceId(pieceId);
        move.setFromCol(fromCol);
        move.setToCol(toCol);
        move.setFromRow(fromRow);
        move.setToRow(toRow);

        return move;
    }

    /**
     * checks that a piece being moved has valid landing highlight. No landing highlight == invalid move
     */
    private boolean validateMove(final Move move) {

        final Point destinationRowCol = checkersBoard.calculateRowColByXAndY(move.getToCenterX(), move.getToCenterY());

        for(LandingSpot landingSpot : landingSpots) {
            final Point landingRowCol = landingSpot.getRowCol();
            if(destinationRowCol.x == landingRowCol.x && destinationRowCol.y == landingRowCol.y) return true;
        }

        return false;

    }

    /**
     * visually animates the movement of a piece from initial co-ordinates to the final co-ordinates
     * @param piece that was moved
     * @param touchX x co-ordinated of the touched location on the screen
     * @param touchY y co-ordinated of the touched location on the screen
     */
    private void animatePieceMovement(final Piece piece, final float touchX, final float touchY, final AnimationListener listener) {

        // Get the new center position
        final PointF pointF = calculateNewCenterXAndY(touchX, touchY);

        // Current position of the piece
        final float startX = piece.getCenterX();
        final float startY = piece.getCenterY();

        // Create two ValueAnimators, one for X and one for Y
        final ValueAnimator animatorX = ValueAnimator.ofFloat(startX, pointF.x);
        animatorX.setDuration(movementDurationMillis); // Duration of the animation (in milliseconds)
        animatorX.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth animation curve

        final ValueAnimator animatorY = ValueAnimator.ofFloat(startY, pointF.y);
        animatorY.setDuration(movementDurationMillis); // Duration of the animation (in milliseconds)
        animatorY.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth animation curve

        // Update the position of the piece during the animation
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                piece.setCenterX(animatedValue);
                boardListener.onAnimating(piece.getId(), piece.getCenterX(), piece.getCenterY());  // Redraw the board with updated piece position
            }
        });

        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                piece.setCenterY(animatedValue);
                boardListener.onAnimating(piece.getId(), piece.getCenterX(), piece.getCenterY());  // Redraw the board with updated piece position

            }
        });

        // Start both animations
        animatorX.start();
        animatorY.start();

        new Handler().postDelayed(()->{listener.onAnimationEnd();},movementDurationMillis);
    }

    public List<LandingSpot> getLandingSpots() {
        return landingSpots;
    }

    /**
     * resolves the touch co-ordinates into a perfect center co-ordinates
     */
    private PointF calculateNewCenterXAndY(final float touchX, final float touchY) {
        final Point rowCol = checkersBoard.calculateRowColByXAndY(touchX, touchY);
        return checkersBoard.calculateCenterXYByRowAndCol(rowCol.x, rowCol.y);
    }

    /**
     * sets the id of the player created the board
     * @param myPlayerId the player id
     */
    public void setMyPlayerId(final String myPlayerId) {
        this.myPlayerId = myPlayerId;
    }

    /**
     * switched active player to opponent player once the current player has completed a move
     * @param activePlayerId the new active player id
     */
    private void switchPlayers(final String activePlayerId) {
        this.activePlayerId = activePlayerId;
        for(com.gerryshom.checkersboardview.board.listener.BoardListener listener : listeners) {
            listener.onActivePlayerSwitched(activePlayerId);
        }
    }

    /**
     * Called when a checkers board for a live match is created
     */
    private void setCheckersBoard(final CheckersBoard checkersBoard) {

        this.checkersBoard = checkersBoard;

        checkersBoard.setKingPieceRule(DefaultRule.kingPieceRule());
        checkersBoard.setNormalPieceRule(DefaultRule.normalPieceRule());
        checkersBoard.setCaptureRule(DefaultRule.captureRule());
        checkersBoard.setGameFlowRule(DefaultRule.gameFlowRule());

        switchPlayers(checkersBoard.getActivePlayerId());

        if(myPlayerId.equals(checkersBoard.getCreatorId())) {
            remotePlayerId = checkersBoard.getOpponentId();
        } else {
            remotePlayerId = checkersBoard.getCreatorId();
        }

        activePlayerId = checkersBoard.getActivePlayerId();

    }

    public CheckersBoard getCheckersBoard() {
        return checkersBoard;
    }

    public void setRule(final CaptureRule captureRule) {
        if(getCheckersBoard() == null) throw new RuntimeException("CheckersBoard has not been set yet");
        checkersBoard.setCaptureRule(captureRule);
    }

    public void setRule(final NormalPieceRule normalPieceRule) {
        if(getCheckersBoard() == null) throw new RuntimeException("CheckersBoard has not been set yet");
        checkersBoard.setNormalPieceRule(normalPieceRule);
    }

    public void setRule(final KingPieceRule kingPieceRule) {
        if(getCheckersBoard() == null) throw new RuntimeException("CheckersBoard has not been set yet");
        checkersBoard.setKingPieceRule(kingPieceRule);
    }

    public void setRule(final GameFlowRule gameFlowRule) {
        if(getCheckersBoard() == null) throw new RuntimeException("CheckersBoard has not been set yet");
        checkersBoard.setGameFlowRule(gameFlowRule);
    }

    /**
     * adds a lister to observe various board activities
     */
    public void addListener(final com.gerryshom.checkersboardview.board.listener.BoardListener listener) {
        listeners.add(listener);
    }

}
