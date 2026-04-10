package sickbay.pokenamon.system.gacha;

import sickbay.pokenamon.system.arena.BattlePokemon;

public interface GetBattlePokemonListener {
    void onComplete(BattlePokemon pokemon);
    void onError(String message);
}
