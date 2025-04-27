package com.gerryshom.checkerssampleapp;

import android.os.Bundle;

import com.gerryshom.checkersboardview.view.CheckersBoardView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

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

        binding.checkersBoardView.resetBoard(
                "me","me","other"
        );

        binding.checkersBoardView.addListener(new CheckersBoardView.BoardListener() {
            @Override
            public void onWin(String winnerPlayerId) {

            }
        });

    }

}