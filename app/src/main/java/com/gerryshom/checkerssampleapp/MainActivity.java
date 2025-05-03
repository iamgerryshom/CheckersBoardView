package com.gerryshom.checkerssampleapp;

import android.os.Bundle;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.gerryshom.checkersboardview.model.player.Player;
import com.gerryshom.checkersboardview.view.CheckersBoardView;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;


import com.gerryshom.checkerssampleapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        setContentView(binding.getRoot());

        setClickListener();

        reset();

        binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
            @Override
            public void onActivePlayerSwitched(String newActivePlayerId) {
                binding.tvActivePlayer.setText(
                        newActivePlayerId.equals(Player.computer().getId()) ? "Computer's turn" : "Your turn"
                );
            }

            @Override
            public void onWin(String winnerPlayerId) {
                binding.tvActivePlayer.setText(
                        winnerPlayerId.equals(Player.computer().getId()) ? "Computer Won" : "You turn"
                );
            }


            @Override
            public void onPieceCaptured(String capturedPiecePlayerId, int remainingPieceCount) {
                if(capturedPiecePlayerId.equals(Player.computer().getId())) {
                    binding.tvOpponentPieceCount.setText("Computer : " + remainingPieceCount);
                } else {
                    binding.tvMyPlayerPieceCount.setText("You : " + remainingPieceCount);
                }
            }
        });

    }

    private void init() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.checkersBoardView.setMyPlayerId(Player.human().getId());
    }

    private void reset() {
        binding.checkersBoardView.playWithComputer();
        binding.tvOpponentPieceCount.setText("Computer : 12");
        binding.tvMyPlayerPieceCount.setText("You : 12");
    }

    private void setClickListener() {
        binding.btnReset.setOnClickListener(v->reset());
    }

}