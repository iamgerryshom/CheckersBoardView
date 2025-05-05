package com.gerryshom.checkersboardview.rules.defaults;

import com.gerryshom.checkersboardview.rules.model.CaptureRule;
import com.gerryshom.checkersboardview.rules.model.GameFlowRule;
import com.gerryshom.checkersboardview.rules.model.KingPieceRule;
import com.gerryshom.checkersboardview.rules.model.NormalPieceRule;

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
