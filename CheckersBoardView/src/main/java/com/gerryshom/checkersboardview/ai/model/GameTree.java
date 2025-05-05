package com.gerryshom.checkersboardview.ai.model;


import com.gerryshom.checkersboardview.board.model.CheckersBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameTree {
    private Node root = new Node();
    private static final Random random = new Random();
    private Node optimalNode;
    private CheckersBoard checkersBoard;
    private int depth;

    public GameTree() {
    }

    public GameTree(final CheckersBoard checkersBoard, final int depth) {
        this.checkersBoard = checkersBoard;
        this.depth = depth;
    }

    /**
     * this generates the complete game tree
     * some optimizations done using alpha beta pruning
     */
    public GameTree build() {

        final Node root = new Node();
        root.setSnapshot(checkersBoard);
        root.setMaximizing(true);

        root.recursivelyBuildChildren(depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        setRoot(root);

        setOptimalNode(findOptimalNode());

        return this;

    }

    /**
     * randomly picks any first level node with a score that matches
     * that of the root node
     *
     * i know its stupid
     */
    public Node findOptimalNode() {
        final List<Node> children = new ArrayList<>();
        for(Node child : root.getChildren()) {
            if(child.getScore() == root.getScore()) children.add(child);
        }
        return children.get(random.nextInt(children.size()));
    }

    public Node getOptimalNode() {
        return optimalNode;
    }

    public void setOptimalNode(Node optimalNode) {
        this.optimalNode = optimalNode;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
