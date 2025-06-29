package com.gerryshom.checkerssampleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

    private CaptureRule captureRule;
    private GameFlowRule gameFlowRule;
    private KingPieceRule kingPieceRule;
    private NormalPieceRule normalPieceRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        setContentView(binding.getRoot());

        setClickListener();

        /**
         * Opponent can be computer or real human,
         * Use inbuilt computer playerId if playing with computer
         * or real humanId if playing with another human
         */
        final Player opponentPlayer = Player.computer(); //Inbuilt computer id,

        final Player humanPlayer = new Player("Human", "Human");

        binding.checkersBoardView.setLocalPlayer(humanPlayer)
                .addMoveSequenceListener(moveSequence ->{
                    System.out.println(moveSequence);
                    /**
                     * Triggered as soon as a player's piece lands
                     * in the final tile
                     *
                     * moveSequence has a list of all moves that were made
                     */
                }).addPlayerSwitchedListener(newActivePlayer -> {

                    /**
                     * Triggered as soon as a player makes a complete moveSequence and
                     * active player is switched to opponent
                     */
                    binding.tvActivePlayer.setText(
                            newActivePlayer.getId().equals(Player.computer().getId()) ? "Computer's turn" : "Your turn"
                    );

                }).addWinListener(winnerPlayer -> {
                    /**
                     * Triggered as soon as you or the opponent cannot make any more moves
                     */
                    binding.tvActivePlayer.setText(
                            winnerPlayer.getId().equals(Player.computer().getId()) ? "Computer Won" : "You turn"
                    );
                }).addPieceCapturedListener((capturedPiecePlayerId, remainingPieceCount)->{
                    /**
                     * Triggered as soon as a piece is jumped either in a single capture or a captured chain
                     */
                    if(capturedPiecePlayerId.equals(Player.computer().getId())) {
                        binding.tvOpponentPieceCount.setText("Computer : " + remainingPieceCount);
                    } else {
                        binding.tvMyPlayerPieceCount.setText("You : " + remainingPieceCount);
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
                .setup(humanPlayer.getId(), opponentPlayer);

    }

    private void init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Define the rules for piece capturing
        captureRule = new CaptureRule(
                true, //forceCapture
                true, //allowMultiCapture
                false //mustTakeLongestJumpPath
        );

        // Define the rules for overall game flow
        gameFlowRule = new GameFlowRule(
                12, //maxTurnsWithoutCapture
                60 //maxTurnDurationSeconds
        );

        // Define the rules for King pieces
        kingPieceRule = new KingPieceRule(
                0, //maxMoveSteps (0 = infinity)
                0, //maxLandingStepsAfterCapture (0 == infinity)
                false, //canChangeDirectionDuringMultiJump
                false //canMoveImmediatelyAfterPromotion
        );

        // Define the rules for Normal pieces
        normalPieceRule = new NormalPieceRule(
                true, //restrictToForwardMovement
                true, //allowBackwardCapture
                true, //promoteOnlyAtLastRow
                false //kingDuringMultiCapture
        );

    }

    private void reset() {
        binding.checkersBoardView.reset();
        binding.tvOpponentPieceCount.setText("Computer : 12");
        binding.tvMyPlayerPieceCount.setText("You : 12");
        binding.checkersBoardView.getCheckersBoard();
    }

    private void setClickListener() {
        binding.btnReset.setOnClickListener(v->reset());

        binding.btnStartGame.setOnClickListener(v->{

            binding.ruleLayout.setVisibility(View.GONE);
            binding.boardLayout.setVisibility(View.VISIBLE);

            captureRule.setForceCapture(binding.forceCapture.isChecked());
            normalPieceRule.setAllowBackwardCapture(binding.captureBackwards.isChecked());

            kingPieceRule.setMaxMoveSteps(Integer.parseInt(binding.inputMaxNormalSteps.getText().toString().trim()));
            kingPieceRule.setMaxLandingDistanceAfterCapture(Integer.parseInt(binding.inputMaxStepsAfterCapture.getText().toString().trim()));

            binding.checkersBoardView.setRule(captureRule);
            binding.checkersBoardView.setRule(normalPieceRule);
            binding.checkersBoardView.setRule(kingPieceRule);


        });
    }

}