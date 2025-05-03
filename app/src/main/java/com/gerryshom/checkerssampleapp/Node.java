package com.gerryshom.checkerssampleapp;

import java.util.List;

public class Node {
    private int num;
    private List<Node> children;

    public Node() {
    }

    public Node(int num, List<Node> children) {
        this.num = num;
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
