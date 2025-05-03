package com.gerryshom.checkersboardview.ai.model;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;
import com.gerryshom.checkersboardview.model.movement.MoveSequence;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private CheckersBoard snapshot = new CheckersBoard();
    private MoveSequence moveSequence = new MoveSequence();
    private List<Node> children = new ArrayList<>();
    private boolean maximizing;
    private int score;

    public Node() {
    }

    public Node(CheckersBoard snapshot, MoveSequence moveSequence, List<Node> children, boolean maximizing, int score) {
        this.snapshot = snapshot;
        this.moveSequence = moveSequence;
        this.children = children;
        this.maximizing = maximizing;
        this.score = score;
    }

    public Node deepClone() {
        return new Node(
                snapshot, moveSequence.deepClone(),
                deepCloneNodes(children),
                maximizing,
                score
        );
    }

    private List<Node> deepCloneNodes(final List<Node> originalNodes) {
        final List<Node> clonedNodes = new ArrayList<>();
        for(Node originalNode : originalNodes) {
            clonedNodes.add(originalNode.deepClone());
        }
        return clonedNodes;
    }

    public boolean isMaximizing() {
        return maximizing;
    }

    public void setMaximizing(boolean maximizing) {
        this.maximizing = maximizing;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public CheckersBoard getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(CheckersBoard snapshot) {
        this.snapshot = snapshot;
    }

    public MoveSequence getMoveSequence() {
        return moveSequence;
    }

    public void setMoveSequence(MoveSequence moveSequence) {
        this.moveSequence = moveSequence;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }
}
