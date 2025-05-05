package com.gerryshom.checkerssampleapp;

import android.os.Bundle;

import com.gerryshom.checkersboardview.board.listener.BoardListener;
import com.gerryshom.checkersboardview.movement.model.MoveSequence;
import com.gerryshom.checkersboardview.player.Player;
import com.gerryshom.checkersboardview.rules.model.CaptureRule;
import com.gerryshom.checkersboardview.rules.model.GameFlowRule;
import com.gerryshom.checkersboardview.rules.model.KingPieceRule;
import com.gerryshom.checkersboardview.rules.model.NormalPieceRule;

import androidx.appcompat.app.AppCompatActivity;


import com.gerryshom.checkersboardview.view.CheckersBoardView;
import com.gerryshom.checkerssampleapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        setContentView(binding.getRoot());

        setClickListener();

        // Define the rules for piece capturing
        final CaptureRule captureRule = new CaptureRule(
                true, //forceCapture
                true, //allowMultiCapture
                false //mustTakeLongestJumpPath
        );

// Define the rules for overall game flow
        final GameFlowRule gameFlowRule = new GameFlowRule(
                12, //maxTurnsWithoutCapture
                60 //maxTurnDurationSeconds
        );

// Define the rules for King pieces
        final KingPieceRule kingPieceRule = new KingPieceRule(
                0, //maxMoveSteps (0 = infinity)
                0, //maxLandingStepsAfterCapture (0 == infinity)
                false, //canChangeDirectionDuringMultiJump
                false //canMoveImmediatelyAfterPromotion
        );

// Define the rules for Normal pieces
        final NormalPieceRule normalPieceRule = new NormalPieceRule(
                true, //restrictToForwardMovement
                true, //allowBackwardCapture
                true //promoteOnlyAtLastRow
        );


        /**
         * Opponent can be computer or real human,
         * Use inbuilt computer playerId if playing with computer
         * or real humanId if playing with another human
         */
        final String opponentPlayerId = Player.computer().getId(); //Inbuilt computer id,
        final String humanPlayerId = "Human"; // your human playerId

        binding.checkersBoardView.setMyPlayerId(humanPlayerId)
                .addBoardListener(new BoardListener() {
                    @Override
                    public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {
                        /**
                         * Triggered as soon as a player's piece lands
                         * in the final tile
                         *
                         * moveSequence has a list of all moves that were made
                         */
                    }

                    @Override
                    public void onActivePlayerSwitched(String newActivePlayerId) {
                        /**
                         * Triggered as soon as a player makes a complete moveSequence and
                         * active player is switched to opponent
                         */
                        binding.tvActivePlayer.setText(
                                newActivePlayerId.equals(Player.computer().getId()) ? "Computer's turn" : "Your turn"
                        );
                    }

                    @Override
                    public void onWin(String winnerPlayerId) {
                        /**
                         * Triggered as soon as you or the opponent cannot make any more moves
                         */
                        binding.tvActivePlayer.setText(
                                winnerPlayerId.equals(Player.computer().getId()) ? "Computer Won" : "You turn"
                        );
                    }
                    @Override
                    public void onPieceCaptured(String capturedPiecePlayerId, int remainingPieceCount) {
                        /**
                         * Triggered as soon as a piece is jumped either in a single capture or a captured chain
                         */
                        if(capturedPiecePlayerId.equals(Player.computer().getId())) {
                            binding.tvOpponentPieceCount.setText("Computer : " + remainingPieceCount);
                        } else {
                            binding.tvMyPlayerPieceCount.setText("You : " + remainingPieceCount);
                        }
                    }
                })
                /**
                 * best used for for multiplayer games where you need them to share the same board with exactly the same data
                 */
                //.setup(checkersBoard)

                /**
                 * best used for single device player game
                 * playing with opponent on the same device eg playing with computer
                 */
                .setup(humanPlayerId, opponentPlayerId);

    }

    private void init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
    }

    private void reset() {
        binding.checkersBoardView.reset();
        binding.tvOpponentPieceCount.setText("Computer : 12");
        binding.tvMyPlayerPieceCount.setText("You : 12");
        binding.checkersBoardView.getCheckersBoard();
    }

    private void setClickListener() {
        binding.btnReset.setOnClickListener(v->reset());
    }

}