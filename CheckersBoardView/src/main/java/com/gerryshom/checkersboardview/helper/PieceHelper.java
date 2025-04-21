package com.gerryshom.checkersboardview.helper;

import android.graphics.Point;

import com.gerryshom.checkersboardview.model.board.Piece;

import java.util.List;

public class PieceHelper {
    /**
     * finds a piece object in a list using the row and column
     */
    public static Piece findPieceByRowAndCol(final List<Piece> pieces, final int boardWidth, final int row, final int col) {
        for (Piece p : pieces) {
            Point piecePosition = BoardHelper.calculateRowAndCol(boardWidth, p.getCenterX(), p.getCenterY());
            if (piecePosition.x == row && piecePosition.y == col) {
                return p;
            }
        }
        return null;
    }


}
