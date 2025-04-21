package com.gerryshom.checkersboardview.model.rules;

public class KingPieceRule {
    private int maxMoveSteps;
    private int maxLandingDistanceAfterCapture;
    private boolean canChangeDirectionDuringMultiJump;
    private boolean canMoveImmediatelyAfterPromotion;

    public KingPieceRule(int maxMoveSteps, int maxLandingDistanceAfterCapture, boolean canChangeDirectionDuringMultiJump, boolean canMoveImmediatelyAfterPromotion) {
        this.maxMoveSteps = maxMoveSteps;
        this.maxLandingDistanceAfterCapture = maxLandingDistanceAfterCapture;
        this.canChangeDirectionDuringMultiJump = canChangeDirectionDuringMultiJump;
        this.canMoveImmediatelyAfterPromotion = canMoveImmediatelyAfterPromotion;
    }

    public int getMaxMoveSteps() {
        return maxMoveSteps;
    }

    public void setMaxMoveSteps(int maxMoveSteps) {
        this.maxMoveSteps = maxMoveSteps;
    }

    public int getMaxLandingDistanceAfterCapture() {
        return maxLandingDistanceAfterCapture;
    }

    public void setMaxLandingDistanceAfterCapture(int maxLandingDistanceAfterCapture) {
        this.maxLandingDistanceAfterCapture = maxLandingDistanceAfterCapture;
    }

    public boolean isCanChangeDirectionDuringMultiJump() {
        return canChangeDirectionDuringMultiJump;
    }

    public void setCanChangeDirectionDuringMultiJump(boolean canChangeDirectionDuringMultiJump) {
        this.canChangeDirectionDuringMultiJump = canChangeDirectionDuringMultiJump;
    }

    public boolean isCanMoveImmediatelyAfterPromotion() {
        return canMoveImmediatelyAfterPromotion;
    }

    public void setCanMoveImmediatelyAfterPromotion(boolean canMoveImmediatelyAfterPromotion) {
        this.canMoveImmediatelyAfterPromotion = canMoveImmediatelyAfterPromotion;
    }
}
