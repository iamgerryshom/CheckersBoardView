package com.gerryshom.checkersboardview.piece.model;

import com.gerryshom.checkersboardview.player.Player;

public class Piece {
    private String id;
    private int row;
    private int col;
    private String playerId;
    private Player player;
    private transient boolean king;
    private transient float centerX;
    private transient float centerY;
    private transient boolean selected;
    private String color;
    private boolean inCaptureChain;

    public Piece(String id, int row, int col, String playerId, Player player, boolean king, float centerX, float centerY, boolean selected, String color, boolean inCaptureChain) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.playerId = playerId;
        this.player = player;
        this.king = king;
        this.centerX = centerX;
        this.centerY = centerY;
        this.selected = selected;
        this.color = color;
        this.inCaptureChain = inCaptureChain;
    }

    public Piece clone() {
        return new Piece(
                id,
                row,
                col,
                playerId,
                player,
                king,
                centerX,
                centerY,
                selected,
                color,
                inCaptureChain
        );
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Piece() {}

    public boolean isInCaptureChain() {
        return inCaptureChain;
    }

    public void setInCaptureChain(boolean inCaptureChain) {
        this.inCaptureChain = inCaptureChain;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isKing() {
        return king;
    }

    public void setKing(boolean king) {
        this.king = king;
    }
}
