package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.BattleMove;

public interface OnMoveFailListener {
    void onMoveFail(BattleMove move);
}
