package com.gerryshom.checkersboardview.movement.enums;

import android.graphics.Point;

public enum Direction {
    TOP_LEFT(-1, -1),
    TOP_RIGHT(-1, 1),
    BOTTOM_LEFT(1, -1),
    BOTTOM_RIGHT(1, 1);

    public final int rowOffset;
    public final int colOffset;

    Direction(int rowOffset, int colOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    public Point toPoint() {
        return new Point(rowOffset, colOffset);
    }
}
