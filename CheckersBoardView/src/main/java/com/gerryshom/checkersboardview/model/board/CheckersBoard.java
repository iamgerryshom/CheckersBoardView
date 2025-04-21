package com.gerryshom.checkersboardview.model.board;


import com.gerryshom.checkersboardview.model.player.User;

import java.util.List;

public class CheckersBoard {
    private String id;
    private List<Piece> pieces;
    private String creatorId;
    private String opponentId;
    private long createdAt;
    private String activePlayerId;
    private User creator;
    private User opponent;

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getOpponent() {
        return opponent;
    }

    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    public String getActivePlayerId() {
        return activePlayerId;
    }

    public void setActivePlayerId(String activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }


    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
