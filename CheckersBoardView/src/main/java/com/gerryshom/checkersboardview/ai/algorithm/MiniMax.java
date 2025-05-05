package com.gerryshom.checkersboardview.ai.algorithm;

import android.os.Handler;
import android.os.Looper;
import android.text.Highlights;
import android.util.Log;

import com.gerryshom.checkersboardview.ai.model.Node;
import com.gerryshom.checkersboardview.ai.model.Tree;
import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.board.Piece;
import com.gerryshom.checkersboardview.model.guides.LandingSpot;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.gerryshom.checkersboardview.model.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MiniMax {

    private static final Random random = new Random();

    public interface SearchListener {
        void onComplete(final MoveSequence moveSequence);
    }

    public static void search(final CheckersBoard originalCheckersBoard, final int depth, final SearchListener listener) {

        Executors.newSingleThreadExecutor().execute(()->{
            final CheckersBoard clonedCheckersBoard = originalCheckersBoard.deepClone();

            new Handler(Looper.getMainLooper()).post(
                    ()->listener.onComplete(
                            new Tree(clonedCheckersBoard, depth).getOptimalNode().getMoveSequence()
                    )
            );
        });

    }


}
