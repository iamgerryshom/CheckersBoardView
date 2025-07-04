package com.gerryshom.checkersboardview.board.handler;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gerryshom.checkersboardview.ai.algorithm.MiniMax;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.highlights.Highlight;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.listener.move.MoveSequenceListener;
import com.gerryshom.checkersboardview.listener.capture.PieceCapturedListener;
import com.gerryshom.checkersboardview.listener.playerswitch.PlayerSwitchedListener;
import com.gerryshom.checkersboardview.listener.win.WinListener;
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
    private final List<Highlight> highlights = new ArrayList<>();

    private final List<Move> moves = new ArrayList<>();

    private final List<MoveSequenceListener> moveSequenceListeners = new ArrayList<>();
    private final List<PieceCapturedListener> pieceCapturedListeners = new ArrayList<>();
    private final List<PlayerSwitchedListener> playerSwitchedListeners = new ArrayList<>();
    private final List<WinListener> winListeners = new ArrayList<>();

    private Player localPlayer;
    private Player opponentPlayer;

    private String activePlayerId;

    private Piece touchedPiece;

    private com.gerryshom.checkersboardview.board.handler.listener.BoardListener boardListener;

    public BoardHandler() {}

    public BoardHandler(CheckersBoard checkersBoard) {
        this.checkersBoard = checkersBoard;
    }

    public void reset() {
        if(checkersBoard == null)
            throw new RuntimeException("No CheckersBoard had been setup");
        setup(checkersBoard.getBoardWidth(), activePlayerId, opponentPlayer);
    }

    public void setup(final int boardWidth, final String activePlayerId, final Player opponentPlayer) {
        final CheckersBoard checkersBoard = CheckersBoard.createCheckersBoard(activePlayerId, localPlayer, opponentPlayer);
        checkersBoard.setBoardWidth(boardWidth);
        setCheckersBoard(checkersBoard);
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public Player getOpponentPlayer() {
        return opponentPlayer;
    }

    public void setup(final CheckersBoard checkersBoard) {

        setCheckersBoard(checkersBoard);
    }

    public void setBoardListener(final com.gerryshom.checkersboardview.board.handler.listener.BoardListener boardListener) {
        this.boardListener = boardListener;
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }

    /**
     * ensures only active player can select a piece and only pieces that belong to the current player can be selected
     */
    public void onActionDown(final float touchX, final float touchY) {

        highlights.clear();
        //trigger with empty list to clear the previous highlights
        boardListener.onHighlightsAdded(highlights);

        //checks if the current user is not the active player
        if(!activePlayerId.equals(localPlayer.getId())) return;

        final Piece piece = checkersBoard.findTouchedPieceByTouchXAndY(touchX, touchY);

        if (piece == null) {
            handleMove(touchX, touchY);
        } else {
            //checks if the piece does not belong to the current player
            if(!piece.getPlayerId().equals(localPlayer.getId())) return;
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

        if(checkersBoard.getCaptureRule().isForceCapture() &&
                checkersBoard.isTherePossibleCaptures(piece.getPlayerId()) &&
                !checkersBoard.canPieceCapture(piece)) {

            highlights.addAll(buildHighlights(checkersBoard.findPiecesWithPossibleCapturesByPlayerId(piece.getPlayerId())));

            //now trigger with the new list of highlights
            boardListener.onHighlightsAdded(highlights);

            return;
        }

        if (touchedPiece != null) {
            touchedPiece.setSelected(false);
            if(touchedPiece.isInCaptureChain() && checkersBoard.getCaptureRule().isForceCapture()) return;
        }

        touchedPiece = piece;
        piece.setSelected(true);

        addLandingSpots(piece, piece.getRow(), piece.getCol());

    }

    private List<Highlight> buildHighlights(final List<Piece> pieces) {
        final List<Highlight> highlights = new ArrayList<>();
        for(Piece piece : pieces) {
            highlights.add(buildHighlight(piece.getCenterX(), piece.getCenterY(), piece.getRow(), piece.getCol()));
        }
        return highlights;
    }

    private Highlight buildHighlight(final float centerX, final float centerY, final int row, final int col) {

        final Highlight highlight = new Highlight();
        highlight.setRow(row);
        highlight.setCol(col);
        highlight.setCenterX(centerX);
        highlight.setCenterY(centerY);

        return highlight;
    }

    /**
     * determines if a move that was made would terminate immediately or start a capture chain
     * @param move the object containing movement metadata
     */
    private boolean capturing;
    private void processMove(final Move move) {

        moves.add(move);

        touchedPiece.setRow(move.getToRow());
        touchedPiece.setCol(move.getToCol());

        final List<Piece> possibleCaptures = new ArrayList<>();

        if(move.isCapture()) {

            final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
            checkersBoard.getPieces().remove(capturedPiece);

            if(!pieceCapturedListeners.isEmpty()) {
                for(PieceCapturedListener pieceCapturedListener : pieceCapturedListeners) {
                    pieceCapturedListener.onPieceCaptured(capturedPiece.getPlayerId(), checkersBoard.getPieceCountByPlayerId(capturedPiece.getPlayerId()));
                }
            }

            // Always find possible captures at the new spot
            possibleCaptures.addAll(checkersBoard.findCapturesByRowAndCol(touchedPiece, move.getToRow(), move.getToCol()));
            touchedPiece.setInCaptureChain(!possibleCaptures.isEmpty() && capturing);
        }

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
        touchedPiece.setSelected(false);
        capturing = false;

        if(!moveSequenceListeners.isEmpty()) {
            for(MoveSequenceListener moveSequenceListener : moveSequenceListeners) {
                moveSequenceListener.onPieceCompletedMoveSequence(
                        new MoveSequence(
                                opponentPlayer.getId(),
                                localPlayer.getId(),
                                moves,
                                checkersBoard.findMoveablePiecesByPlayerId(localPlayer.getId()).size(),
                                checkersBoard.findMoveablePiecesByPlayerId(opponentPlayer.getId()).size(),
                                System.currentTimeMillis(),
                                1000L * 45
                        )
                );
            }
        }

        if(checkersBoard.getOpponent().getId().equals(Player.computer().getId())) {
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

        switchPlayers(opponentPlayer.getId());

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

        if(!piece.isKing() && isFinalMove)
            piece.setKing(checkersBoard.crownKing(checkersBoard.getCreator().getId(), piece.getPlayerId(), move.getToRow()));

        //uses animation to move piece to new position
        animatePieceMovement(piece, move.getToCenterX(), move.getToCenterY(), ()->{

            final String opponentPlayerId = checkersBoard.identifyOpponentPlayerId(piece.getPlayerId());

            if(isFinalMove) {
                switchPlayers(opponentPlayerId);
            }

            if(checkersBoard.findMoveablePiecesByPlayerId(opponentPlayerId).isEmpty()) {

                final Player myLocalPlayer = checkersBoard.getCreator().getId().equals(localPlayer.getId())
                        ? checkersBoard.getCreator() : checkersBoard.getOpponent();

                final Player opponentPlayer = checkersBoard.getCreator().getId().equals(myLocalPlayer.getId())
                        ? checkersBoard.getOpponent() : checkersBoard.getCreator();

                if(!winListeners.isEmpty()) {
                    for(WinListener winListener : winListeners) {
                        final Player winnerPlayer = piece.getPlayerId().equals(myLocalPlayer.getId())
                                ? myLocalPlayer : opponentPlayer;

                        winListener.onWin(winnerPlayer);
                    }
                }

            }

            animationListener.onAnimationEnd();

        });

    }

    public void setMovementDurationMillis(final long movementDurationMillis) {
        this.movementDurationMillis = movementDurationMillis;
    }

    public void clearListeners() {
        clearMoveSequenceListeners();
        clearWinListeners();
        clearPieceCapturedListeners();
        clearPlayerSwitchedListeners();
    }

    public void clearMoveSequenceListeners() {
        moveSequenceListeners.clear();
    }

    public void clearWinListeners() {
        winListeners.clear();
    }

    public void clearPieceCapturedListeners() {
        pieceCapturedListeners.clear();
    }

    public void clearPlayerSwitchedListeners() {
        playerSwitchedListeners.clear();
    }

    public void removeMoveSequenceListener(final MoveSequenceListener moveSequenceListener) {
        moveSequenceListeners.remove(moveSequenceListener);
    }

    public void removeWinListener(final WinListener winListener) {
        winListeners.remove(winListener);
    }

    public void removePieceCapturedListener(final PieceCapturedListener pieceCapturedListener) {
        pieceCapturedListeners.remove(pieceCapturedListener);
    }

    public void removePlayerSwitchedListener(final PlayerSwitchedListener playerSwitchedListener) {
        playerSwitchedListeners.remove(playerSwitchedListener);
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

        piece.setRow(move.getToRow());
        piece.setCol(move.getToCol());

        if(move.isCapture()) {
            final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
            checkersBoard.getPieces().remove(capturedPiece);

            if(!pieceCapturedListeners.isEmpty()) {
                for(PieceCapturedListener pieceCapturedListener : pieceCapturedListeners) {
                    pieceCapturedListener.onPieceCaptured(capturedPiece.getPlayerId(), checkersBoard.getPieceCountByPlayerId(capturedPiece.getPlayerId()));
                }
            }

        }

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

        final Piece capturedPiece = checkersBoard.findCaptureBetweenRowCols(
                checkersBoard.findPieceById(move.getPieceId()).getPlayerId(), move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol()
        );

        if(capturedPiece != null) {
            move.setCapturedPieceId(capturedPiece.getId());
            capturing = true;
        }

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
     */
    public void setLocalPlayer(final Player localPlayer) {
        this.localPlayer = localPlayer;
    }

    /**
     * switched active player to opponent player once the current player has completed a move
     * @param activePlayerId the new active player id
     */
    private void switchPlayers(final String activePlayerId) {
        this.activePlayerId = activePlayerId;

        final Player myLocalPlayer = checkersBoard.getCreator().getId().equals(localPlayer.getId())
                ? checkersBoard.getCreator() : checkersBoard.getOpponent();

        final Player opponentPlayer = checkersBoard.getCreator().getId().equals(myLocalPlayer.getId())
                ? checkersBoard.getOpponent() : checkersBoard.getCreator();

        final Player activePlayer = activePlayerId.equals(myLocalPlayer.getId())
                ? myLocalPlayer : opponentPlayer;

        if(!playerSwitchedListeners.isEmpty()) {
            for(PlayerSwitchedListener playerSwitchedListener : playerSwitchedListeners) {
                playerSwitchedListener.onActivePlayerSwitched(activePlayer);
            }
        }

    }

    /**
     * Called when a checkers board for a live match is created
     */
    private void setCheckersBoard(final CheckersBoard checkersBoard) {

        if(checkersBoard.getCreator() == null)
            throw new RuntimeException("creatorPlayer cannot be null");

        if(checkersBoard.getOpponent() == null)
            throw new RuntimeException("opponentPlayer cannot be null");

        if(localPlayer == null)
            throw new RuntimeException("localPlayer cannot be null");

        if(checkersBoard.getActivePlayerId() == null)
            throw new RuntimeException("activePlayerId cannot be null");

        if (!checkersBoard.getCreator().getId().equals(checkersBoard.getActivePlayerId())
                && !checkersBoard.getOpponent().getId().equals(checkersBoard.getActivePlayerId()))
            throw new RuntimeException("activePlayerId must be the id either players");

        this.checkersBoard = checkersBoard;

        checkersBoard.setKingPieceRule(checkersBoard.getKingPieceRule() == null ? DefaultRule.kingPieceRule() : checkersBoard.getKingPieceRule());
        checkersBoard.setNormalPieceRule(checkersBoard.getNormalPieceRule() == null ? DefaultRule.normalPieceRule() : checkersBoard.getNormalPieceRule());
        checkersBoard.setCaptureRule(checkersBoard.getCaptureRule() == null ? DefaultRule.captureRule() : checkersBoard.getCaptureRule());
        checkersBoard.setGameFlowRule(checkersBoard.getGameFlowRule() == null ? DefaultRule.gameFlowRule() : checkersBoard.getGameFlowRule());

        switchPlayers(checkersBoard.getActivePlayerId());

        if(localPlayer.getId().equals(checkersBoard.getCreator().getId())) {
            opponentPlayer = checkersBoard.getOpponent();
        } else {
            opponentPlayer = checkersBoard.getCreator();
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

    public void addMoveSequenceListener(final MoveSequenceListener moveSequenceListener) {
        moveSequenceListeners.add(moveSequenceListener);
    }

    public void addPieceCapturedListener(final PieceCapturedListener pieceCapturedListener) {
        pieceCapturedListeners.add(pieceCapturedListener);
    }

    public void addPlayerSwitchedListener(final PlayerSwitchedListener playerSwitchedListener) {
        playerSwitchedListeners.add(playerSwitchedListener);
    }

    public void addWinListener(final WinListener winListener) {
        winListeners.add(winListener);
    }

}
