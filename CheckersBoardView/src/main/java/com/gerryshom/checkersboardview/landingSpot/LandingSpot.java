package com.gerryshom.checkersboardview.landingSpot;

import android.graphics.Point;

public class LandingSpot {
    private String id;

    private Point rowCol;
    private boolean afterJump;

    public LandingSpot(Point rowCol, boolean afterJump) {
        this.rowCol = rowCol;
        this.afterJump = afterJump;
    }

    public void setRowCol(Point rowCol) {
        this.rowCol = rowCol;
    }

    public Point getRowCol() {
        return rowCol;
    }

    public boolean isAfterJump() {
        return afterJump;
    }

    public void setAfterJump(boolean afterJump) {
        this.afterJump = afterJump;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
