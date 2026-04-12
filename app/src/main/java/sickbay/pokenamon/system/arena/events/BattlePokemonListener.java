package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.core.Battle;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.arena.model.Ailment;
import sickbay.pokenamon.system.arena.model.VolatileAilment;

public interface BattlePokemonListener {
    void onFaint(BattlePokemon pokemon);
    void onBurn(BattlePokemon pokemon);
    void onPoison(BattlePokemon pokemon);
    void onFreeze(BattlePokemon pokemon);
    void onThaw(BattlePokemon pokemon);
    void onSleep(BattlePokemon pokemon);
    void onRest(BattlePokemon pokemon);
    void onWakeUp(BattlePokemon pokemon);
    void onParalyze(BattlePokemon pokemon);
    void onParalyzeSuccess(BattlePokemon pokemon);
    void onConfuse(BattlePokemon pokemon);
    void onConfusion(BattlePokemon pokemon);
    void onConfusionSnap(BattlePokemon pokemon);
    void onFlinch(BattlePokemon pokemon);
    void onHeal(BattlePokemon pokemon);
    void onDrain(BattlePokemon pokemon);
    void onDisabled(BattlePokemon pokemon, BattleMove move);
    void onTorment(BattlePokemon pokemon, BattleMove move);
    void onYawn(BattlePokemon pokemon);
    void onSilence(BattleMove move);
    void onRecoil(BattlePokemon pokemon);
    void onTarShot(BattlePokemon pokemon);
    void onPerishSong(BattlePokemon pokemon, int turns);
    void onStatChange(BattlePokemon pokemon, StatId statId, int stage);

    void onFly(BattlePokemon pokemon);

    void onDig(BattlePokemon pokemon);

    void onDive(BattlePokemon pokemon);

    void onCharge(BattlePokemon pokemon);

    void onCharging(BattlePokemon pokemon);

    void onChargeFinish(BattlePokemon pokemon);

    void onInflict(BattlePokemon pokemon, Ailment ailment);
    void onCure(BattlePokemon pokemon, Ailment ailment);

    void onNightmare(BattlePokemon pokemon);

    void onIngrain(BattlePokemon pokemon);

    void onProtect(BattlePokemon pokemon, BattleMove move);

    void onProtectFail(BattlePokemon pokemon, BattleMove move);

    void onVolatileInflict(BattlePokemon pokemon,VolatileAilment ailment);
    void onVolatileCure(BattlePokemon pokemon, VolatileAilment ailment);
}
