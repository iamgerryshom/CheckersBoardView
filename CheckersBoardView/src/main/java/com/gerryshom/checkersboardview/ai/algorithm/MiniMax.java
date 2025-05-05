package com.gerryshom.checkersboardview.ai.algorithm;

import android.os.Handler;
import android.os.Looper;

import com.gerryshom.checkersboardview.ai.model.GameTree;
import com.gerryshom.checkersboardview.board.model.CheckersBoard;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;

import java.util.concurrent.Executors;

public class MiniMax {

    public interface SearchListener {
        void onComplete(final MoveSequence moveSequence);
    }

    public static void searchOptimalMoveSequence(final CheckersBoard originalCheckersBoard, final int depth, final SearchListener listener) {

        Executors.newSingleThreadExecutor().execute(()->{
            final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();

            new Handler(Looper.getMainLooper()).post(
                    ()->listener.onComplete(
                            new GameTree(clonedCheckersBoard, depth).build().getOptimalNode().getMoveSequence()
                    )
            );
        });

    }


}
