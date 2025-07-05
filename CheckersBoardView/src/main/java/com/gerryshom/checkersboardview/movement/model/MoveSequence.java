package com.gerryshom.checkersboardview.movement.model;

import java.util.ArrayList;
import java.util.List;


public class MoveSequence {
    private String opponentPlayerId;
    private String currentPlayerId;
    private List<Move> moves = new ArrayList<>();
    private int opponentPlayerMoveablePieceCount;
    private int currentPlayerMoveablePieceCount;
    private Object createdAt;
    private long expiresIn;

    public MoveSequence deepClone() {
        return new MoveSequence(opponentPlayerId, currentPlayerId, cloneMoves(moves), currentPlayerMoveablePieceCount, opponentPlayerMoveablePieceCount, createdAt, expiresIn);
    }

    private List<Move> cloneMoves(final List<Move> moves) {
        if(moves == null) return null;
        final List<Move> clonedMoves = new ArrayList<>();
        for(Move move : moves) {
            clonedMoves.add(move.clone());
        }
        return clonedMoves;
    }

    public MoveSequence(String opponentPlayerId,
                        String currentPlayerId,
                        List<Move> moves,
                        int currentPlayerMoveablePieceCount,
                        int opponentPlayerMoveablePieceCount,
                        Object createdAt,
                        long expiresIn
    ) {
        this.opponentPlayerId = opponentPlayerId;
        this.moves = moves;
        this.currentPlayerId = currentPlayerId;
        this.opponentPlayerMoveablePieceCount = opponentPlayerMoveablePieceCount;
        this.currentPlayerMoveablePieceCount = currentPlayerMoveablePieceCount;
        this.createdAt = createdAt;
        this.expiresIn = expiresIn;
    }

    public MoveSequence(String opponentPlayerId, List<Move> moves) {
        this.opponentPlayerId = opponentPlayerId;
        this.moves = moves;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public int getCurrentPlayerMoveablePieceCount() {
        return currentPlayerMoveablePieceCount;
    }

    public void setCurrentPlayerMoveablePieceCount(int currentPlayerMoveablePieceCount) {
        this.currentPlayerMoveablePieceCount = currentPlayerMoveablePieceCount;
    }

    public int getOpponentPlayerMoveablePieceCount() {
        return opponentPlayerMoveablePieceCount;
    }

    public void setOpponentPlayerMoveablePieceCount(int opponentPlayerMoveablePieceCount) {
        this.opponentPlayerMoveablePieceCount = opponentPlayerMoveablePieceCount;
    }

    public MoveSequence() {
    }

    public String getOpponentPlayerId() {
        return opponentPlayerId;
    }

    public void setOpponentPlayerId(String opponentPlayerId) {
        this.opponentPlayerId = opponentPlayerId;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }
}

