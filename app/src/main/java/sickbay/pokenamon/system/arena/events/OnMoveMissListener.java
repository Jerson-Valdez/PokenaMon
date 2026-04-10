package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;

public interface OnMoveMissListener {
    void onMiss(BattleMove move);
}
