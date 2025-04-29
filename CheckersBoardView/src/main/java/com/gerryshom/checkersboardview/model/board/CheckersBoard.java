package com.gerryshom.checkersboardview.model.board;


import com.gerryshom.checkersboardview.model.player.Player;

import java.util.ArrayList;
import java.util.List;

public class CheckersBoard {
    private String id;
    private List<Piece> pieces;
    private String creatorId;
    private String opponentId;
    private long createdAt;
    private String activePlayerId;
    private Player creator;
    private Player opponent;

    public CheckersBoard(String id, List<Piece> pieces, String creatorId, String opponentId, long createdAt, String activePlayerId, Player creator, Player opponent) {
        this.id = id;
        this.pieces = pieces;
        this.creatorId = creatorId;
        this.opponentId = opponentId;
        this.createdAt = createdAt;
        this.activePlayerId = activePlayerId;
        this.creator = creator;
        this.opponent = opponent;
    }

    public CheckersBoard deepClone() {
        return new CheckersBoard(
                id,
                clonePieces(pieces),
                creatorId,
                opponentId,
                createdAt,
                activePlayerId,
                creator,
                opponent
        );
    }

    public List<Piece> clonePieces(final List<Piece> pieces) {
        final List<Piece> clonedPieces = new ArrayList<>();
        for(Piece piece : pieces) {
            clonedPieces.add(piece.clone());
        }
        return clonedPieces;
    }

    public CheckersBoard() {}

    public Player getCreator() {
        return creator;
    }

    public void setCreator(Player creator) {
        this.creator = creator;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
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
