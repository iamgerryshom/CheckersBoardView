package com.gerryshom.checkerssampleapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Tree {
    private Node root = new Node();
    private AtomicInteger counter = new AtomicInteger(0);

    public Tree buildTree(final int depth) {

        root.setNum(counter.getAndIncrement());

        buildNumberSubTree(root, depth, counter);

        return this;
    }

    private Node buildNumberSubTree(final Node node, final int depth, final AtomicInteger counter) {

        if(depth == 0) return node;

        for(int i = 0; i < 2; i++) {

            final Node child = new Node();
            child.setNum(counter.incrementAndGet());

            node.getChildren().add(child);

            buildNumberSubTree(child, depth - 1, counter);
        }

        return node;

    }

    public void printTree() {
        printNode(root, 0);
    }

    private void printNode(Node node, int level) {
        System.out.println("  ".repeat(level) + node.getNum());
        for (Node child : node.getChildren()) {
            printNode(child, level + 1);
        }
    }


}
