package com.gerryshom.checkersboardview.board.listener;

import com.gerryshom.checkersboardview.movement.model.MoveSequence;

public interface BoardListener {
        default void onPieceCompletedMoveSequence(final MoveSequence moveSequence){}
        default void onActivePlayerSwitched(final String newActivePlayerId){}
        default void onPieceCaptured(final String capturedPiecePlayerId, final int remainingPieceCount){}
        default void onWin(final String winnerPlayerId){}
    }