package com.gerryshom.checkersboardview.ai.model;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;

import java.util.List;

public class BoardState {
    private CheckersBoard boardSnapshot;
    private MoveSequence moveSequence;
    private int score;
    private List<BoardState> children;

    public BoardState() {
    }

    public BoardState(CheckersBoard boardSnapshot, MoveSequence moveSequence, int score) {
        this.boardSnapshot = boardSnapshot;
        this.moveSequence = moveSequence;
        this.score = score;
    }

    public BoardState deepClone() {
        return new BoardState(
                boardSnapshot.deepClone(),
                moveSequence.deepClone(),
                score
        );
    }

    public CheckersBoard getBoardSnapshot() {
        return boardSnapshot;
    }

    public void setBoardSnapshot(CheckersBoard boardSnapshot) {
        this.boardSnapshot = boardSnapshot;
    }

    public List<BoardState> getChildren() {
        return children;
    }

    public void setChildren(List<BoardState> children) {
        this.children = children;
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
