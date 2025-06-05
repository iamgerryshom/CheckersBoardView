package com.gerryshom.checkersboardview.listener.playerswitch;

import com.gerryshom.checkersboardview.player.Player;

public interface PlayerSwitchedListener {
    void onActivePlayerSwitched(final Player newActivePlayer);
}
