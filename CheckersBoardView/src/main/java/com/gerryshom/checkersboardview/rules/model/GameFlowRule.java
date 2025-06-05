package com.gerryshom.checkersboardview.rules.model;

public class GameFlowRule {
    private int maxTurnsWithoutCapture;
    private long maxTurnDurationSeconds;

    public GameFlowRule() {
    }

    public GameFlowRule(int maxTurnsWithoutCapture, long maxTurnDurationSeconds) {
        this.maxTurnsWithoutCapture = maxTurnsWithoutCapture;
        this.maxTurnDurationSeconds = maxTurnDurationSeconds;
    }

    public GameFlowRule clone() {
        return new GameFlowRule(
                maxTurnsWithoutCapture, maxTurnDurationSeconds
        );
    }

    public int getMaxTurnsWithoutCapture() {
        return maxTurnsWithoutCapture;
    }

    public void setMaxTurnsWithoutCapture(int maxTurnsWithoutCapture) {
        this.maxTurnsWithoutCapture = maxTurnsWithoutCapture;
    }

    public long getMaxTurnDurationSeconds() {
        return maxTurnDurationSeconds;
    }

    public void setMaxTurnDurationSeconds(long maxTurnDurationSeconds) {
        this.maxTurnDurationSeconds = maxTurnDurationSeconds;
    }
}
