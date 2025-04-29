package com.gerryshom.checkerssampleapp;

import android.graphics.Color;
import android.os.Bundle;

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

        binding.checkersBoardView.setCheckersBoard(binding.checkersBoardView.createCheckersBoard("me", "me", "ai"));

        binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
            @Override
            public void onPieceCompletedMoveSequence(MoveSequence moveSequence) {
                new Handler().postDelayed(()->{
                    binding.checkersBoardView.triggerMiniMaxAlgorithm();

                },1000);
            }
        });

    }

}