package com.gerryshom.checkersboardview.defaults;

import com.gerryshom.checkersboardview.model.rules.CaptureRule;
import com.gerryshom.checkersboardview.model.rules.GameFlowRule;
import com.gerryshom.checkersboardview.model.rules.KingPieceRule;
import com.gerryshom.checkersboardview.model.rules.NormalPieceRule;

public class DefaultRule {
    public static CaptureRule captureRule() {
        return new CaptureRule(
                true,
                true,
                false
        );
    }

    public static GameFlowRule gameFlowRule() {
        return new GameFlowRule(
                12,
                60
        );
    }

    public static KingPieceRule kingPieceRule() {
        return new KingPieceRule(
                0,
                0,
                false,
                false
        );
    }

    public static NormalPieceRule normalPieceRule() {
        return new NormalPieceRule(
                true,
                true,
                true
        );
    }
}
