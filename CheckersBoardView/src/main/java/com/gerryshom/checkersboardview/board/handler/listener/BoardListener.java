package com.gerryshom.checkersboardview.board.handler.listener;

import com.gerryshom.checkersboardview.highlights.Highlight;
import com.gerryshom.checkersboardview.landingSpot.LandingSpot;

import java.util.List;

public interface BoardListener {
    default void onLandingSpotsAdded(final List<LandingSpot> landingSpots) {
    }

    default void onAnimating(final String pieceId, final float centerX, final float centerY) {
    }

    default void onHighlightsAdded(final List<Highlight> highlights) {}
}