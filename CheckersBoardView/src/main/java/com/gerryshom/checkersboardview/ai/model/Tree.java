package com.gerryshom.checkersboardview.ai.model;

import com.gerryshom.checkersboardview.model.board.CheckersBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tree {
    private Node root = new Node();
    private static final Random random = new Random();
    private Node optimalNode;

    public Tree() {
    }

    public Tree(final CheckersBoard checkersBoard, final int depth) {
        build(checkersBoard, depth);
        optimalNode = findOptimalNode();
    }

    private Tree build(final CheckersBoard checkersBoard, final int depth) {

        final Node root = new Node();
        root.setSnapshot(checkersBoard);
        root.setMaximizing(true);

        final Tree tree = new Tree();
        tree.setRoot(root);

        root.recursivelyBuildChildren(depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        return tree;

    }

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
