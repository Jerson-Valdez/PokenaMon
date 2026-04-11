package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.BattleMove;

public interface MoveUseListener {
    void onFail(BattleMove move);
    void onMiss(BattleMove move);
    void onUse(BattleMove move);
    void onHit(BattleMove move);
    void onPpOut(BattleMove move);
    void onMultipleHits(BattleMove move, int hits);
}
