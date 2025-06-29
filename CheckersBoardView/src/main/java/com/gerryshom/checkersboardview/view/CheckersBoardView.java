package com.gerryshom.checkersboardview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gerryshom.checkersboardview.R;
import com.gerryshom.checkersboardview.board.handler.BoardHandler;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.highlights.Highlight;
import com.gerryshom.checkersboardview.listener.move.MoveSequenceListener;
import com.gerryshom.checkersboardview.listener.capture.PieceCapturedListener;
import com.gerryshom.checkersboardview.listener.playerswitch.PlayerSwitchedListener;
import com.gerryshom.checkersboardview.listener.win.WinListener;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.paint.DefaultPaint;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.player.Player;
import com.gerryshom.checkersboardview.rules.model.CaptureRule;
import com.gerryshom.checkersboardview.rules.model.GameFlowRule;
import com.gerryshom.checkersboardview.rules.model.KingPieceRule;
import com.gerryshom.checkersboardview.rules.model.NormalPieceRule;

import java.util.List;


public class CheckersBoardView extends View {

    private Paint darkTilePaint;
    private Paint lightTilePaint;

    private BoardHandler boardHandler = new BoardHandler();
    private Drawable localPlayerRegularPieceDrawable;
    private Drawable localPlayerKingPieceDrawable;
    private Drawable opponentPlayerRegularPieceDrawable;
    private Drawable opponentPlayerKingPieceDrawable;

    private int landingSpotColor;
    private float cornerRadius;

    private boolean touchDisabled;

    private int defaultRadiusInDp = 6;
    private int defaultLadingSpotColor = Color.RED;

    private int rotation = 0;

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

    public void setDarkTileColor(final int color) {
        darkTilePaint.setColor(color);
        invalidate();
    }

    public void setLightTileColor(final int color) {
        lightTilePaint.setColor(color);
        invalidate();
    }

    public CheckersBoardView setLocalPlayerRegularPieceDrawable(final Drawable localPlayerRegularPieceDrawable) {
        this.localPlayerRegularPieceDrawable = localPlayerRegularPieceDrawable;
        return this;
    }

    public CheckersBoardView setLocalPlayerKingPieceDrawable(final Drawable localPlayerKingPieceDrawable) {
        this.localPlayerKingPieceDrawable = localPlayerKingPieceDrawable;
        return this;
    }

    public CheckersBoardView setOpponentPlayerRegularPieceDrawable(final Drawable opponentPlayerRegularPieceDrawable) {
        this.opponentPlayerRegularPieceDrawable = opponentPlayerRegularPieceDrawable;
        return this;
    }

    public CheckersBoardView setOpponentPlayerKingPieceDrawable(final Drawable opponentPlayerKingPieceDrawable) {
        this.opponentPlayerKingPieceDrawable = opponentPlayerKingPieceDrawable;
        return this;
    }

    public CheckersBoardView setTouchDisabled(final boolean touchDisabled) {
        this.touchDisabled = touchDisabled;
        return this;
    }

    private void init(final AttributeSet attrs) {
        //tile paints
        darkTilePaint = DefaultPaint.darkTilePaint();
        lightTilePaint = DefaultPaint.lightTilePaint();

        localPlayerRegularPieceDrawable = ContextCompat.getDrawable(getContext(), R.drawable.local_player_normal_piece_vector);
        opponentPlayerRegularPieceDrawable = ContextCompat.getDrawable(getContext(), R.drawable.opponent_player_normal_piece_vector);

        localPlayerKingPieceDrawable = ContextCompat.getDrawable(getContext(), R.drawable.local_player_king_piece_vector);
        opponentPlayerKingPieceDrawable = ContextCompat.getDrawable(getContext(), R.drawable.opponent_player_king_piece_vector);

        setCornerRadius(defaultRadiusInDp); //default corner radius

        setLandingSpotColor(defaultLadingSpotColor); //default landing spot color

        handleAttrs(attrs);

        boardHandler.setBoardListener(new com.gerryshom.checkersboardview.board.handler.listener.BoardListener() {
            @Override
            public void onLandingSpotsAdded(List<LandingSpot> landingSpots) {
                invalidate();
            }

            @Override
            public void onAnimating(String pieceId, float centerX, float centerY) {
                invalidate();
            }

            @Override
            public void onHighlightsAdded(List<Highlight> highlights) {
                invalidate();
            }
        });

    }

    public CheckersBoardView setLocalPlayer(final Player localPlayer) {
        boardHandler.setLocalPlayer(localPlayer);
        return this;
    }

    public CheckersBoardView addMoveSequenceListener(final MoveSequenceListener moveSequenceListener) {
        boardHandler.addMoveSequenceListener(moveSequenceListener);
        return this;
    }

    public CheckersBoardView addPieceCapturedListener(final PieceCapturedListener pieceCapturedListener) {
        boardHandler.addPieceCapturedListener(pieceCapturedListener);
        return this;
    }

    public CheckersBoardView addPlayerSwitchedListener(final PlayerSwitchedListener playerSwitchedListener) {
        boardHandler.addPlayerSwitchedListener(playerSwitchedListener);
        return this;
    }

    public CheckersBoardView addWinListener(final WinListener winListener) {
        boardHandler.addWinListener(winListener);
        return this;
    }

    public void setup(final String activePlayerId, final Player opponentPlayer) {
        getDimensions((width, height)->{
            boardHandler.setup((int) width, activePlayerId, opponentPlayer);
        });
    }

    public void setup(final CheckersBoard checkersBoard) {

        if(checkersBoard.getCreator().getId().equals(boardHandler.getLocalPlayer().getId())) {
            rotation = 0;
            setRotation(0);
        } else {
            rotation = 180;
            setRotation(180);
        }

        getDimensions((width, height)->{
            checkersBoard.setBoardWidth((int) width);
            boardHandler.setup(checkersBoard);
            invalidate();
        });
    }

    public void playOpponentMoveSequence(final MoveSequence moveSequence) {
        boardHandler.playOpponentMoveSequence(moveSequence);
    }

    public void reset() {
        boardHandler.reset();
    }

    public CheckersBoard getCheckersBoard() {
        return boardHandler.getCheckersBoard();
    }

    public void setRule(final CaptureRule captureRule) {
        boardHandler.setRule(captureRule);
    }

    public void setRule(final NormalPieceRule normalPieceRule) {
        boardHandler.setRule(normalPieceRule);
    }

    public void setRule(final KingPieceRule kingPieceRule) {
        boardHandler.setRule(kingPieceRule);
    }

    public void setRule(final GameFlowRule gameFlowRule) {
        boardHandler.setRule(gameFlowRule);
    }

    public void setCornerRadius(final int dp) {
        float scale = getResources().getDisplayMetrics().density;
        this.cornerRadius = dp * scale;
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
            setLandingSpotColor(a.getColor(R.styleable.CheckersBoardView_landingSpotColor, defaultLadingSpotColor));
            setCornerRadius(a.getDimensionPixelSize(R.styleable.CheckersBoardView_cornerRadius, defaultRadiusInDp));

            final Drawable localRegular = a.getDrawable(R.styleable.CheckersBoardView_localPlayerRegularPieceDrawable);
            if (localRegular != null) setLocalPlayerRegularPieceDrawable(localRegular);

            final Drawable localKing = a.getDrawable(R.styleable.CheckersBoardView_localPlayerKingPieceDrawable);
            if (localKing != null) setLocalPlayerKingPieceDrawable(localKing);

            final Drawable opponentRegular = a.getDrawable(R.styleable.CheckersBoardView_opponentPlayerRegularPieceDrawable);
            if (opponentRegular != null) setOpponentPlayerRegularPieceDrawable(opponentRegular);

            final Drawable opponentKing = a.getDrawable(R.styleable.CheckersBoardView_opponentPlayerKingPieceDrawable);
            if (opponentKing != null) setOpponentPlayerKingPieceDrawable(opponentKing);

            touchDisabled = a.getBoolean(R.styleable.CheckersBoardView_touchDisabled, false);

        } finally {
            a.recycle();
        }

    }

    /**
     * Called when a checkers board for a live match is created
     */
    private void setCheckersBoard(final CheckersBoard checkersBoard) {
        getDimensions((width, height)->{
            checkersBoard.setBoardWidth((int) width);

                if(boardHandler.getLocalPlayer().equals(checkersBoard.getCreator().getId())) {
                    rotation = 0;
                    setRotation(0);
                } else {
                    rotation = 180;
                    setRotation(180); // rotates the board so that the player at the top can play as if they are at the bottom. This makes it easier to play instead of rotating the whole device/
                }

            boardHandler.setup(checkersBoard);

            invalidate();
        });
    }

    public void clearListeners() {
        boardHandler.clearListeners();
    }

    public void clearMoveSequenceListeners() {
        boardHandler.clearMoveSequenceListeners();
    }

    public void clearWinListeners() {
        boardHandler.clearWinListeners();
    }

    public void clearPieceCapturedListeners() {
        boardHandler.clearPieceCapturedListeners();
    }

    public void clearPlayerSwitchedListeners() {
        boardHandler.clearPlayerSwitchedListeners();
    }

    public void removeMoveSequenceListener(final MoveSequenceListener moveSequenceListener) {
        boardHandler.removeMoveSequenceListener(moveSequenceListener);
    }

    public void removeWinListener(final WinListener winListener) {
        boardHandler.removeWinListener(winListener);
    }

    public void removePieceCapturedListener(final PieceCapturedListener pieceCapturedListener) {
        boardHandler.removePieceCapturedListener(pieceCapturedListener);
    }

    public void removePlayerSwitchedListener(final PlayerSwitchedListener playerSwitchedListener) {
        boardHandler.removePlayerSwitchedListener(playerSwitchedListener);
    }

    private void getDimensions(final DimensionsListener listener) {
        if (getWidth() > 0 && getHeight() > 0) {
            listener.onAvailable(getWidth(), getHeight());
            return;
        }

        post(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                listener.onAvailable(getWidth(), getHeight());
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (getWidth() > 0 && getHeight() > 0) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            listener.onAvailable(getWidth(), getHeight());
                        }
                    }
                });
            }
        });
    }


    private interface DimensionsListener {
        void onAvailable(final float width, final float height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;

        if (widthMode == MeasureSpec.EXACTLY) {
            // Width fixed (e.g. match_parent or exact)
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            // Width not fixed, but height fixed, use height as size
            size = heightSize;
        } else {
            // Neither fixed, choose default size
            size = 400; // or whatever default size you want
        }

        setMeasuredDimension(size, size); // force square
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(touchDisabled) return true;

        final float touchX = event.getX();
        final float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boardHandler.onActionDown(touchX, touchY);
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);

        if(boardHandler.getCheckersBoard() == null) return;

        for(Highlight highlight : boardHandler.getHighlights()) {
            drawHighlight(canvas, highlight.getCenterX(), highlight.getCenterY(), new Paint(), getWidth() / 8 , "#2EB32E");
        }

        drawPieces(canvas);

        drawLandingSpots(canvas);


    }

    public CheckersBoardView setLandingSpotColor(final int landingSpotColor) {
        this.landingSpotColor = landingSpotColor;
        return this;
    }

    private void drawLandingSpots(final Canvas canvas) {
        final Paint paint = new Paint();
        paint.setColor(landingSpotColor); // Bright red
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setAlpha(255); // Full opacity

        final float radius = (getWidth() / 8f) * 0.15f; // Small radius

        for (LandingSpot landingSpot : boardHandler.getLandingSpots()) {
            final PointF centerXY = boardHandler.getCheckersBoard()
                    .calculateCenterXYByRowAndCol(landingSpot.getRowCol().x, landingSpot.getRowCol().y);

            canvas.drawCircle(centerXY.x, centerXY.y, radius, paint);
        }
    }

    private void drawBoard(final Canvas canvas) {
        final float boardWidth = getWidth();
        final float boardHeight = getHeight();

        // cell size based on board size, can handle non-square if needed
        final float cellWidth = boardWidth / 8f;
        final float cellHeight = boardHeight / 8f;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final Paint paint = CheckersBoard.isDarkCell(row, col) ? darkTilePaint : lightTilePaint;

                // Calculate left, top, right, bottom using floats
                final float left = col * cellWidth;
                final float top = row * cellHeight;

                // For the last column, force right edge to boardWidth to avoid gap
                final float right = (col == 7) ? boardWidth : left + cellWidth;

                // For the last row, force bottom edge to boardHeight to avoid gap
                final float bottom = (row == 7) ? boardHeight : top + cellHeight;

                boolean topLeft = (row == 0 && col == 0);
                boolean topRight = (row == 0 && col == 7);
                boolean bottomLeft = (row == 7 && col == 0);
                boolean bottomRight = (row == 7 && col == 7);

                if (topLeft || topRight || bottomLeft || bottomRight) {
                    float[] radii = new float[8];

                    if (topLeft)
                        radii = new float[] {cornerRadius, cornerRadius, 0, 0, 0, 0, 0, 0};
                    else if (topRight)
                        radii = new float[] {0, 0, cornerRadius, cornerRadius, 0, 0, 0, 0};
                    else if (bottomRight)
                        radii = new float[] {0, 0, 0, 0, cornerRadius, cornerRadius, 0, 0};
                    else if (bottomLeft)
                        radii = new float[] {0, 0, 0, 0, 0, 0, cornerRadius, cornerRadius};

                    Path path = new Path();
                    path.addRoundRect(new RectF(left, top, right, bottom), radii, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else {
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
    }


    private void drawPieces(final Canvas canvas) {

        int cellSize = getWidth() / 8; // Assuming the board is 8x8

        for (Piece piece : boardHandler.getCheckersBoard().getPieces()) {
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
        if (piece.isSelected()) {
            drawHighlight(canvas, piece.getCenterX(), piece.getCenterY(), paint, cellSize, "#FF5722");
        }

        drawPieceDrawable(canvas, piece, cellSize, rotation);
    }

    private void drawHighlight(final Canvas canvas, final float centerX, final float centerY, final Paint paint, final int cellSize, final String colorHex) {

        // Calculate the size and position of the highlight square based on the cell size
        float squareSize = cellSize * 0.75f; // Inner square size (75% of cell size to avoid overlap)
        float left = centerX - squareSize / 2;
        float top = centerY - squareSize / 2;
        float right = centerX + squareSize / 2;
        float bottom = centerY + squareSize / 2;

        // Set up the paint for the highlight square (highlight color with toned-down opacity)
        paint.setColor(Color.parseColor(colorHex)); // Lighter orange (hex: #FFB74D)
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(180); // Reduced opacity
        paint.setAntiAlias(true);
        canvas.drawRect(left, top, right, bottom, paint); // Draw the square

        // Draw the border around the square
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor(colorHex)); // Darker orange border (hex: #FF5722)
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f); // Thicker border width
        borderPaint.setAlpha(255); // Full opacity for the border
        borderPaint.setAntiAlias(true);

        // Small offset for the border
        float borderOffset = 4f;
        canvas.drawRect(left - borderOffset, top - borderOffset, right + borderOffset, bottom + borderOffset, borderPaint); // Draw the border
    }

    private void drawPieceDrawable(final Canvas canvas, final Piece piece, final int cellSize, final int degree) {

        // Calculate bounds once
        int size = (int) (cellSize * 0.8f);
        int halfSize = size / 2;
        int centerX = (int) piece.getCenterX();
        int centerY = (int) piece.getCenterY();

        if(piece.isKing()) {
            final Drawable drawable = piece.getPlayerId().equals(getLocalPlayer().getId())
                    ? localPlayerKingPieceDrawable : opponentPlayerKingPieceDrawable;
                    // Set bounds
            drawable.setBounds(
                    centerX - halfSize,
                    centerY - halfSize,
                    centerX + halfSize,
                    centerY + halfSize
            );

            // Draw
            drawable.draw(canvas);

        } else {
            final Drawable drawable = piece.getPlayerId().equals(getLocalPlayer().getId())
                    ? localPlayerRegularPieceDrawable : opponentPlayerRegularPieceDrawable;
            // Set bounds
            drawable.setBounds(
                    centerX - halfSize,
                    centerY - halfSize,
                    centerX + halfSize,
                    centerY + halfSize
            );
            // Always match save/restore
            canvas.save();
            try {
                canvas.rotate(degree, centerX, centerY);
                drawable.draw(canvas);
            } finally {
                canvas.restore(); // Ensure this always happens
            }
        }

    }

    public Player getLocalPlayer() {
        return boardHandler.getLocalPlayer();
    }

    public Player getOpponentPlayer() {
        return boardHandler.getOpponentPlayer();
    }


}
