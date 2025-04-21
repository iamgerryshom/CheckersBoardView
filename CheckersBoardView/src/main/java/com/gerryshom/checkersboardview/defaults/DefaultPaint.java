package com.gerryshom.checkersboardview.defaults;

import android.graphics.Color;
import android.graphics.Paint;

public class DefaultPaint {
    /**
     * paint is used for dark tiles only
     * @return the paint instance
     */
    public static Paint darkTilePaint() {
        final Paint paint = new Paint();
        paint.setColor(Color.rgb(82, 41, 0)); // Darker brown
        return paint;
    }

    /**
     * paint is used for light tiles only
     * @return the paint instance
     */
    public static Paint lightTilePaint() {
        final Paint paint = new Paint();
        paint.setColor(Color.rgb(192, 144, 105)); // Darker light brown
        return paint;
    }
}
