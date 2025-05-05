package com.gerryshom.checkersboardview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gerryshom.checkersboardview.R;
import com.gerryshom.checkersboardview.board.handler.BoardHandler;
import com.gerryshom.checkersboardview.board.listener.BoardListener;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.paint.DefaultPaint;
import com.gerryshom.checkersboardview.piece.model.Piece;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;
import com.gerryshom.checkersboardview.rules.model.CaptureRule;
import com.gerryshom.checkersboardview.rules.model.GameFlowRule;
import com.gerryshom.checkersboardview.rules.model.KingPieceRule;
import com.gerryshom.checkersboardview.rules.model.NormalPieceRule;

import java.util.ArrayList;
import java.util.List;


public class CheckersBoardView extends View {

    private Paint darkTilePaint;
    private Paint lightTilePaint;

    private BoardHandler boardHandler = new BoardHandler();

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

    private void init(final AttributeSet attrs) {
        //tile paints
        darkTilePaint = DefaultPaint.darkTilePaint();
        lightTilePaint = DefaultPaint.lightTilePaint();

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

        });

    }

    public CheckersBoardView setMyPlayerId(final String myPlayerId) {
        boardHandler.setMyPlayerId(myPlayerId);
        return this;
    }

    public CheckersBoardView addBoardListener(final BoardListener gameListener) {
        boardHandler.addListener(gameListener);
        return this;
    }

    public void setup(final String activePlayerId, final String opponentPlayerId) {
        getDimensions((width, height)->{
            boardHandler.setup((int) width, activePlayerId, opponentPlayerId);
        });
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
     * Called when a checkers board for a live match is created
     */
    private void setCheckersBoard(final CheckersBoard checkersBoard) {
        getDimensions((width, height)->{
            checkersBoard.setBoardWidth((int) width);

                if(boardHandler.getMyPlayerId().equals(checkersBoard.getCreatorId())) {
                    setRotation(0);
                } else {
                    setRotation(180); // rotates the board so that the player at the top can play as if they are at the bottom. This makes it easier to play instead of rotating the whole device/
                }

            boardHandler.setup(checkersBoard);

            invalidate();
        });
    }

    private void getDimensions(final DimensionsListener listener) {
        if(getWidth() != 0 || getHeight() != 0) {
            listener.onAvailable(getWidth(), getHeight());
            return;
        }
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                listener.onAvailable(getWidth(), getHeight());
                return true;
            }
        });
    }

    private interface DimensionsListener {
        void onAvailable(final float width, final float height);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

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

        for (LandingSpot landingSpot : boardHandler.getLandingSpots()) {
            final PointF centerXY = boardHandler.getCheckersBoard().calculateCenterXYByRowAndCol(landingSpot.getRowCol().x, landingSpot.getRowCol().y);

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
