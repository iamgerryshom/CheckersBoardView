package com.gerryshom.checkersboardview.rules.model;

public class NormalPieceRule {
    private boolean restrictToForwardMovement;
    private boolean allowBackwardCapture;
    private boolean promoteOnlyAtLastRow;
    private boolean kingDuringCaptureChain;

    public NormalPieceRule() {
    }

    public NormalPieceRule(boolean restrictToForwardMovement, boolean allowBackwardCapture, boolean promoteOnlyAtLastRow, boolean kingDuringCaptureChain) {
        this.restrictToForwardMovement = restrictToForwardMovement;
        this.allowBackwardCapture = allowBackwardCapture;
        this.promoteOnlyAtLastRow = promoteOnlyAtLastRow;
        this.kingDuringCaptureChain = kingDuringCaptureChain;
    }

    public NormalPieceRule clone() {
        return new NormalPieceRule(
                restrictToForwardMovement,
                allowBackwardCapture,
                promoteOnlyAtLastRow,
                kingDuringCaptureChain
        );
    }


    public boolean isKingDuringCaptureChain() {
        return kingDuringCaptureChain;
    }

    public void setKingDuringCaptureChain(boolean kingDuringCaptureChain) {
        this.kingDuringCaptureChain = kingDuringCaptureChain;
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
