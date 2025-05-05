package com.gerryshom.checkersboardview.movement.model;

import java.util.ArrayList;
import java.util.List;

public class MoveSequence {
    private String destination;
    private List<Move> moves = new ArrayList<>();

    public MoveSequence deepClone() {
        return new MoveSequence(destination, cloneMoves(moves));
    }

    private List<Move> cloneMoves(final List<Move> moves) {
        if(moves == null) return null;
        final List<Move> clonedMoves = new ArrayList<>();
        for(Move move : moves) {
            clonedMoves.add(move.clone());
        }
        return clonedMoves;
    }

    public MoveSequence(String destination, List<Move> moves) {
        this.destination = destination;
        this.moves = moves;
    }

    public MoveSequence() {
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }
}

