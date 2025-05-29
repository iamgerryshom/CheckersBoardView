package com.gerryshom.checkersboardview.rules.model;

public class NormalPieceRule {
    private boolean restrictToForwardMovement;
    private boolean allowBackwardCapture;
    private boolean promoteOnlyAtLastRow;

    public NormalPieceRule() {
    }

    public NormalPieceRule(boolean restrictToForwardMovement, boolean allowBackwardCapture, boolean promoteOnlyAtLastRow) {
        this.restrictToForwardMovement = restrictToForwardMovement;
        this.allowBackwardCapture = allowBackwardCapture;
        this.promoteOnlyAtLastRow = promoteOnlyAtLastRow;
    }

    public boolean isRestrictToForwardMovement() {
        return restrictToForwardMovement;
    }

    public void setRestrictToForwardMovement(boolean restrictToForwardMovement) {
        this.restrictToForwardMovement = restrictToForwardMovement;
    }

    public boolean isAllowBackwardCapture() {
        return allowBackwardCapture;
    }

    public void setAllowBackwardCapture(boolean allowBackwardCapture) {
        this.allowBackwardCapture = allowBackwardCapture;
    }

    public boolean isPromoteOnlyAtLastRow() {
        return promoteOnlyAtLastRow;
    }

    public void setPromoteOnlyAtLastRow(boolean promoteOnlyAtLastRow) {
        this.promoteOnlyAtLastRow = promoteOnlyAtLastRow;
    }
}
