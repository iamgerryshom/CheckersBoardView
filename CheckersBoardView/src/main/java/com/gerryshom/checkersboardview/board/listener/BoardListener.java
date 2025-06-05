package com.gerryshom.checkersboardview.board.listener;

import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.player.Player;

public interface BoardListener {
        default void onPieceCompletedMoveSequence(final MoveSequence moveSequence){}
        default void onActivePlayerSwitched(final Player newActivePlayer){}
        default void onPieceCaptured(final String capturedPiecePlayerId, final int remainingPieceCount){}
        default void onWin(final Player winnerPlayer){}
    }