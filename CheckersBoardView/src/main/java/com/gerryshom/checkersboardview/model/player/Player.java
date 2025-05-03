package com.gerryshom.checkersboardview.model.player;

public class Player {
    private String id;
    private String name;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Player() {
    }

    public static Player human() {
        return new Player("Human", "Human");
    }

    public static Player computer() {
        return new Player("Computer", "Computer");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
