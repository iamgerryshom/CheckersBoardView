package com.gerryshom.checkersboardview.helper;

import android.graphics.Point;
import android.graphics.PointF;

public class BoardHelper {
    public static Point calculateRowColByXAndY(final int boardWidth, final float touchX, final float touchY) {
        // Get the width and height of the entire view
        int cellSize = boardWidth / 8; // Assuming the board is 8x8

        // Calculate the row and column based on the touch coordinates
        int row = (int) (touchY / cellSize);
        int col = (int) (touchX / cellSize);

        // Ensure the row and col are within valid bounds (0 to 7 for an 8x8 board)
        row = Math.max(0, Math.min(7, row));
        col = Math.max(0, Math.min(7, col));

        // Return the row and column as a Point
        return new Point(row, col);
    }

    public static PointF calculateCellCenterByRowAndCol(final int boardWidth, final int row, final int col) {
        // Get the width and height of the entire view
        int cellSize = boardWidth / 8; // Assuming the board is 8x8

        // Calculate the center of the cell
        float centerX = col * cellSize + cellSize / 2f;
        float centerY = row * cellSize + cellSize / 2f;

        // Return the center as a Point
        return new PointF((int) centerX, (int) centerY);
    }

}
