package com.gerryshom.checkersboardview.listener.capture;

public interface PieceCapturedListener {
    void onPieceCaptured(final String capturedPiecePlayerId, final int remainingPieceCount);
}
