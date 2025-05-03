package com.gerryshom.checkersboardview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gerryshom.checkersboardview.R;
import com.gerryshom.checkersboardview.ai.algorithm.MiniMax;
import com.gerryshom.checkersboardview.defaults.DefaultPaint;
import com.gerryshom.checkersboardview.defaults.DefaultRule;
import com.gerryshom.checkersboardview.helper.BoardHelper;
import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.board.Piece;
import com.gerryshom.checkersboardview.model.guides.LandingSpot;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.gerryshom.checkersboardview.model.player.Player;
import com.gerryshom.checkersboardview.model.rules.CaptureRule;
import com.gerryshom.checkersboardview.model.rules.GameFlowRule;
import com.gerryshom.checkersboardview.model.rules.KingPieceRule;
import com.gerryshom.checkersboardview.model.rules.NormalPieceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CheckersBoardView extends View {

    private final List<Move> moves = new ArrayList<>();
    private List<BoardListener> listeners = new ArrayList<>();
    private List<LandingSpot> landingSpots = new ArrayList<>();

    private String myPlayerId;
    private String remotePlayerId;
    private String activePlayerId;

    private Paint darkTilePaint;
    private Paint lightTilePaint;

    private CheckersBoard checkersBoard;

    public CheckersBoardView(Context context) {
        super(context);
        init(null);
    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CheckersBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public interface BoardListener {
        default void onPieceCompletedMoveSequence(final MoveSequence moveSequence){}
        default void onActivePlayerSwitched(final String newActivePlayerId){}
        default void onPieceCaptured(final String capturedPiecePlayerId, final int remainingPieceCount){}
        default void onWin(final String winnerPlayerId){}
    }

    /**
     * adds a lister to observe various board activities
     */
    public void addListener(final BoardListener listener) {
        listeners.add(listener);
    }

    public void setRule(final CaptureRule captureRule) {
        checkersBoard.setCaptureRule(captureRule);
    }

    public void setRule(final NormalPieceRule normalPieceRule) {
        checkersBoard.setNormalPieceRule(normalPieceRule);
    }

    public void setRule(final KingPieceRule kingPieceRule) {
        checkersBoard.setKingPieceRule(kingPieceRule);
    }

    public void setRule(final GameFlowRule gameFlowRule) {
        checkersBoard.setGameFlowRule(gameFlowRule);
    }

    public void setDarkTileColor(final int color) {
        darkTilePaint.setColor(color);
        invalidate();
    }

    public void setLightTileColor(final int color) {
        lightTilePaint.setColor(color);
        invalidate();
    }

    private void init(final AttributeSet attrs) {
        //tile paints
        darkTilePaint = DefaultPaint.darkTilePaint();
        lightTilePaint = DefaultPaint.lightTilePaint();

        handleAttrs(attrs);

        listeners.add(new BoardListener() {}); // helps to avoid null pointer exception

    }

    public void playWithComputer() {
        setCheckersBoard(CheckersBoard.createCheckersBoard(Player.human().getId(), Player.human().getId(), Player.computer().getId()));
    }

    /**
     * sets the attributes defined in xml
     */
    private void handleAttrs(final AttributeSet attrs) {
        if (attrs == null) return;
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CheckersBoardView,
                0, 0);
        try {
            darkTilePaint.setColor(a.getColor(R.styleable.CheckersBoardView_darkTileColor, Color.rgb(82, 41, 0)));
            lightTilePaint.setColor(a.getColor(R.styleable.CheckersBoardView_lightTileColor, Color.rgb(192, 144, 105)));
        } finally {
            a.recycle();
        }

    }

    /**
     * sets the id of the player created the board
     * @param myPlayerId the player id
     */
    public void setMyPlayerId(final String myPlayerId) {
        this.myPlayerId = myPlayerId;
    }

    /**
     * Called when a checkers board for a live match is created
     */
    public void setCheckersBoard(final CheckersBoard checkersBoard) {

        this.checkersBoard = checkersBoard;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                getViewTreeObserver().removeOnPreDrawListener(this);

                checkersBoard.setKingPieceRule(DefaultRule.kingPieceRule());
                checkersBoard.setNormalPieceRule(DefaultRule.normalPieceRule());
                checkersBoard.setCaptureRule(DefaultRule.captureRule());
                checkersBoard.setGameFlowRule(DefaultRule.gameFlowRule());
                checkersBoard.setBoardWidth(getWidth());

                switchPlayers(checkersBoard.getActivePlayerId());

                if(myPlayerId.equals(checkersBoard.getCreatorId())) {
                    remotePlayerId = checkersBoard.getOpponentId();
                    setRotation(0);
                } else {
                    remotePlayerId = checkersBoard.getCreatorId();
                    setRotation(180); // rotates the board so that the player at the top can play as if they are at the bottom. This makes it easier to play instead of rotating the whole device/
                }

                activePlayerId = checkersBoard.getActivePlayerId();

                invalidate();
                return true;
            }
        });


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

        final Point newRowCol = calculateRowColByXAndY(touchX, touchY);
        final PointF newCenterXY = calculateCenterXYByRowAndCol(newRowCol.x, newRowCol.y);

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

        final Piece capturedPiece = checkersBoard.findPossibleCapture(
                checkersBoard.findPieceById(move.getPieceId()).getPlayerId(), move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol()
        );

        if(capturedPiece != null) {
            move.setCapturedPieceId(capturedPiece.getId());
            capturing = true;
        }

        moves.add(move);

        // Always find possible captures at the new spot
        final List<Piece> possibleCaptures = checkersBoard.findPossibleCaptures(touchedPiece.getPlayerId(), move.getToRow(), move.getToCol());

        touchedPiece.setInCaptureChain(!possibleCaptures.isEmpty() && capturing);

        playMove(touchedPiece, move, possibleCaptures.isEmpty());

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

        for(BoardListener listener : listeners) {
            listener.onPieceCompletedMoveSequence(new MoveSequence(remotePlayerId, moves));
        }

        if(checkersBoard.getOpponentId().equals(Player.computer().getId())) {
            new Handler().postDelayed(()->{

                MiniMax.search(checkersBoard, 5, new MiniMax.SearchListener() {
                    @Override
                    public void onComplete(MoveSequence moveSequence) {
                        playOpponentMoveSequence(moveSequence);
                    }
                });

            }, 600);
        }

        moves.clear();

        touchedPiece = null;
        landingSpots.clear();

        switchPlayers(remotePlayerId);

    }

    /**
     * clears the previous landing highlights and then
     * adds the new highlights to a highlights list
     */
    private void addLandingSpots(final Piece piece, final int row, final int col) {

        landingSpots.clear();
        landingSpots.addAll(checkersBoard.commonLandingSpots(piece, row, col));

        invalidate();
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

        piece.setRow(move.getToRow());
        piece.setCol(move.getToCol());

        if(!piece.isKing() && isFinalMove)
            piece.setKing(checkersBoard.crownKing(checkersBoard.getCreatorId(), piece.getPlayerId(), move.getToRow()));

        if(move.getCapturedPieceId() != null) {
            final Piece capturedPiece = checkersBoard.findPieceById(move.getCapturedPieceId());
            checkersBoard.getPieces().remove(capturedPiece);

            final int remainingPieces = checkersBoard.getPieceCountByPlayerId(capturedPiece.getPlayerId());

            for(BoardListener listener : listeners)
                listener.onPieceCaptured(capturedPiece.getPlayerId(), remainingPieces);

        }

        animatePieceMovement(piece, move.getToCenterX(), move.getToCenterY(), ()->{

            final String opponentPlayerId = identifyOpponentPlayerId(piece.getPlayerId());

            if(isFinalMove) {
                switchPlayers(opponentPlayerId);
            }

            if(checkersBoard.findMoveablePiecesByPlayerId(opponentPlayerId).isEmpty())
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

            final PointF centerXY = calculateCenterXYByRowAndCol(toRow, toCol);
            move.setToCenterX(centerXY.x);
            move.setToCenterY(centerXY.y);

            final Piece piece = checkersBoard.findPieceById(move.getPieceId());

            playMove(piece, move, i == moveSequence.getMoves().size() - 1);

        }

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

        final Point destinationRowCol = calculateRowColByXAndY(move.getToCenterX(), move.getToCenterY());

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

        final long duration = 300L;

        // Get the new center position
        final PointF pointF = calculateNewCenterXAndY(touchX, touchY);

        // Current position of the piece
        final float startX = piece.getCenterX();
        final float startY = piece.getCenterY();

        // Create two ValueAnimators, one for X and one for Y
        final ValueAnimator animatorX = ValueAnimator.ofFloat(startX, pointF.x);
        animatorX.setDuration(duration); // Duration of the animation (in milliseconds)
        animatorX.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth animation curve

        final ValueAnimator animatorY = ValueAnimator.ofFloat(startY, pointF.y);
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
    private PointF calculateNewCenterXAndY(final float touchX, final float touchY) {
        final Point rowCol = calculateRowColByXAndY(touchX, touchY);
        return calculateCenterXYByRowAndCol(rowCol.x, rowCol.y);
    }

    /**
     * resolves touch co-ordinates on the board into row and col on the board
     * @param touchX touched x coordinate
     * @param touchY touched y coordinate
     */
    private Point calculateRowColByXAndY(
            final float touchX,
            final float touchY) {
        return BoardHelper.calculateRowColByXAndY(getWidth(), touchX, touchY);
    }

    private PointF calculateCenterXYByRowAndCol(
            final int row, final int col) {
        return BoardHelper.calculateCellCenterByRowAndCol(getWidth(), row, col);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);

        if(checkersBoard == null) return;

        drawPieces(canvas);

        drawLandingSpots(canvas);

    }

    private void drawLandingSpots(final Canvas canvas) {
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

        for (LandingSpot landingSpot : landingSpots) {
            final PointF centerXY = calculateCenterXYByRowAndCol(landingSpot.getRowCol().x, landingSpot.getRowCol().y);

            // Draw the outermost border circle (light green)
            canvas.drawCircle(centerXY.x, centerXY.y, outerRadius, outerPaint);

            // Draw the inner border circle (light green)
            canvas.drawCircle(centerXY.x, centerXY.y, innerRadius, innerPaint);

            // Draw the filled inner circle (red)
            canvas.drawCircle(centerXY.x, centerXY.y, fillRadius, middlePaint);
        }
    }

    private void drawBoard(final Canvas canvas) {

        final int cellSize = getWidth() / 8;

        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {

                final Paint paint = CheckersBoard.isDarkCell(row, col) ? darkTilePaint : lightTilePaint;

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

        for (Piece piece : checkersBoard.getPieces()) {
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
            drawLandingSpot(canvas, piece, paint, cellSize);
        }

        // Now draw the actual piece (king or normal)
        if (piece.isKing()) {
            drawKingPiece(canvas, piece, paint, cellSize);
        } else {
            drawNormalPiece(canvas, piece, paint, cellSize);
        }
    }

    private void drawLandingSpot(Canvas canvas, Piece piece, Paint paint, int cellSize) {
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


    private void drawPieceDrawable(Canvas canvas, Piece piece, Paint paint, int cellSize) {
        // Get the vector drawable once and cache it if possible
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.regular_piece_vector);
        if (drawable == null) return;


        // Calculate bounds once
        int size = (int) (cellSize * 0.8f);
        int halfSize = size / 2;
        int centerX = (int) piece.getCenterX();
        int centerY = (int) piece.getCenterY();

        // Set bounds
        drawable.setBounds(
                centerX - halfSize,
                centerY - halfSize,
                centerX + halfSize,
                centerY + halfSize
        );

        // Draw
        drawable.draw(canvas);
    }


}
