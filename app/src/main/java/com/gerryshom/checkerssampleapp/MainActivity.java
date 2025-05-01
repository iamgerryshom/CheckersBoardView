package com.gerryshom.checkerssampleapp;

import android.graphics.Color;
import android.os.Bundle;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;
import com.gerryshom.checkersboardview.view.CheckersBoardView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;


import com.gerryshom.checkerssampleapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.checkersBoardView.setMyPlayerId("me");

        new Handler().postDelayed(()->{
            binding.checkersBoardView.setCheckersBoard(CheckersBoard.createCheckersBoard("me", "me", "ai"));

        }, 5000);

        binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
            @Override
            public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {

            }
        });

    }

}