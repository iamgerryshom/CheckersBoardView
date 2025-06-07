package com.gerryshom.checkersboardview.rules.model;

public class CaptureRule {

    private boolean forceCapture;
    private boolean allowMultiCapture;
    private boolean mustTakeLongestJumpPath;

    public CaptureRule(boolean forceCapture, boolean allowMultiCapture, boolean mustTakeLongestJumpPath) {
        this.forceCapture = forceCapture;
        this.allowMultiCapture = allowMultiCapture;
        this.mustTakeLongestJumpPath = mustTakeLongestJumpPath;
    }

    public CaptureRule() {
    }

    public CaptureRule clone() {
        return new CaptureRule(
                forceCapture,
                allowMultiCapture,
                mustTakeLongestJumpPath
        );
    }


    public boolean isForceCapture() {
        return forceCapture;
    }

    public void setForceCapture(boolean forceCapture) {
        this.forceCapture = forceCapture;
    }

    public boolean isAllowMultiCapture() {
        return allowMultiCapture;
    }

    public void setAllowMultiCapture(boolean allowMultiCapture) {
        this.allowMultiCapture = allowMultiCapture;
    }

    public boolean isMustTakeLongestJumpPath() {
        return mustTakeLongestJumpPath;
    }

    public void setMustTakeLongestJumpPath(boolean mustTakeLongestJumpPath) {
        this.mustTakeLongestJumpPath = mustTakeLongestJumpPath;
    }
}
