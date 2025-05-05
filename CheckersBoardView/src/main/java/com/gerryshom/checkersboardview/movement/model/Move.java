package com.gerryshom.checkersboardview.movement.model;

import java.io.Serializable;

public class Move implements Serializable {
    private String id;
    private String pieceId;

    private String capturedPieceId;
    private String destination;

    private int fromRow;
    private int toRow;
    private int fromCol;
    private int toCol;

    private transient float fromCenterX;
    private transient float toCenterX;
    private transient float fromCenterY;
    private transient float toCenterY;

    public boolean isCapture() {
        return capturedPieceId != null;
    }

    public Move clone() {
        return new Move(id,
                pieceId,
                capturedPieceId,
                destination,
                fromRow,
                toRow,
                fromCol,
                toCol,
                fromCenterX,
                toCenterX,
                fromCenterY,
                toCenterY
        );
    }

    public Move(String id, String pieceId, String capturedPieceId, String destination, int fromRow, int toRow, int fromCol, int toCol, float fromCenterX, float toCenterX, float fromCenterY, float toCenterY) {
        this.id = id;
        this.pieceId = pieceId;
        this.capturedPieceId = capturedPieceId;
        this.destination = destination;
        this.fromRow = fromRow;
        this.toRow = toRow;
        this.fromCol = fromCol;
        this.toCol = toCol;
        this.fromCenterX = fromCenterX;
        this.toCenterX = toCenterX;
        this.fromCenterY = fromCenterY;
        this.toCenterY = toCenterY;
    }

    public Move() {}

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public float getFromCenterX() {
        return fromCenterX;
    }

    public void setFromCenterX(float fromCenterX) {
        this.fromCenterX = fromCenterX;
    }

    public float getToCenterX() {
        return toCenterX;
    }

    public void setToCenterX(float toCenterX) {
        this.toCenterX = toCenterX;
    }

    public float getFromCenterY() {
        return fromCenterY;
    }

    public void setFromCenterY(float fromCenterY) {
        this.fromCenterY = fromCenterY;
    }

    public float getToCenterY() {
        return toCenterY;
    }

    public void setToCenterY(float toCenterY) {
        this.toCenterY = toCenterY;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPieceId() {
        return pieceId;
    }

    public void setPieceId(String pieceId) {
        this.pieceId = pieceId;
    }

    public String getCapturedPieceId() {
        return capturedPieceId;
    }

    public void setCapturedPieceId(String capturedPieceId) {
        this.capturedPieceId = capturedPieceId;
    }

}
