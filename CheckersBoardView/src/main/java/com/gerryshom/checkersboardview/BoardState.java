package com.gerryshom.checkersboardview;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.movement.Move;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;

public class BoardState {
    private CheckersBoard checkersBoard;
    private MoveSequence moveSequence;
    private int score;

    public BoardState(CheckersBoard checkersBoard, MoveSequence moveSequence, int score) {
        this.checkersBoard = checkersBoard;
        this.moveSequence = moveSequence;
        this.score = score;
    }

    public CheckersBoard getCheckersBoard() {
        return checkersBoard;
    }

    public void setCheckersBoard(CheckersBoard checkersBoard) {
        this.checkersBoard = checkersBoard;
    }

    public MoveSequence getMoveSequence() {
        return moveSequence;
    }

    public void setMoveSequence(MoveSequence moveSequence) {
        this.moveSequence = moveSequence;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
