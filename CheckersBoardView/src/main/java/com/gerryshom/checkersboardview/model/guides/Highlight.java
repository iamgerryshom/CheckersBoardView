package com.gerryshom.checkersboardview.model.guides;

import android.graphics.Point;

public class Highlight {
    private String id;
    private Point point;

    public Highlight(Point point) {
        this.point = point;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
