package sickbay.pokenamon.system.gacha;

import sickbay.pokenamon.system.arena.BattleMove;

public interface GetBattleMoveListener {
    void onComplete(BattleMove move);
    void onError(String message);
}
