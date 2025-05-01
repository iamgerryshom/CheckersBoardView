package com.gerryshom.checkersboardview.model.board;

import com.gerryshom.checkersboardview.model.movement.MoveSequence;

import java.util.List;

public class BoardState {
    private CheckersBoard boardSnapshot;
    private MoveSequence moveSequence;
    private int heuristic;
    private List<BoardState> children;

    public BoardState() {
    }

    public BoardState(CheckersBoard boardSnapshot, MoveSequence moveSequence, int heuristic) {
        this.boardSnapshot = boardSnapshot;
        this.moveSequence = moveSequence;
        this.heuristic = heuristic;
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

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }
}
