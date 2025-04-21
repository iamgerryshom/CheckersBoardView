package com.gerryshom.checkersboardview.model.movement;

import java.util.List;

public class MoveSequence {
    private String destination;
    private List<Move> moves;

    public MoveSequence(String destination, List<Move> moves) {
        this.destination = destination;
        this.moves = moves;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<Move> getSteps() {
        return moves;
    }

    public void setSteps(List<Move> moves) {
        this.moves = moves;
    }
}
