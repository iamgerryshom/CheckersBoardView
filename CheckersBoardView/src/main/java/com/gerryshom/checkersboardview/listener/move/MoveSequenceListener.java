package com.gerryshom.checkersboardview.listener.move;

import com.gerryshom.checkersboardview.movement.model.MoveSequence;

public interface MoveSequenceListener {
    void onPieceCompletedMoveSequence(final MoveSequence moveSequence);
}
