package sickbay.pokenamon.system.arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.model.enums.StatId;
import sickbay.pokenamon.system.arena.events.BattlePokemonListener;
import sickbay.pokenamon.system.arena.events.DamageEffectListener;
import sickbay.pokenamon.system.arena.events.MoveUseListener;
import sickbay.pokenamon.model.Ailment;
import sickbay.pokenamon.model.StatBuff;
import sickbay.pokenamon.model.VolatileAilment;
public class BattlePokemon extends Pokemon {
    BattleMove[] battleMoves;
    Ailment ailment;
    HashSet<VolatileAilment> volatileAilments;
    HashSet<StatBuff> buffs;
    BattleMove lastMoveUsed;
    boolean suspended;
    boolean lockedIn;
    boolean charging;
    int turns;
    private BattlePokemonListener battlePokemonListener;
    private DamageEffectListener damageEffectListener;
    private MoveUseListener moveUseListener;

    public BattlePokemon(Pokemon pokemon) {
        super(pokemon.getCollectionId(),
                pokemon.getPokedexId(),
                pokemon.getName(),
                pokemon.getRarity(),
                pokemon.getLevel(),
                pokemon.getExp(),
                pokemon.getTypes(),
                pokemon.getSprite(),
                pokemon.getCry(),
                pokemon.getWeight(),
                pokemon.getHeight(),
                pokemon.getStats(),
                pokemon.getMoves(),
                pokemon.getSummonedAt(),
                pokemon.getFullHealthCooldown(),
                pokemon.getCurrentHp(),
                pokemon.getTotalHp());
        if (pokemon.getTotalHp() == 0) {
            setTotalHp(getTotalHp());
        }
        if (pokemon.getCurrentHp() == 0) {
            setCurrentHp(getTotalHp());
        }

        battleMoves = Arrays.stream(pokemon.getMoves()).map(BattleMove::new).toArray(BattleMove[]::new);
        buffs = new HashSet<>();
        buffs.add(new StatBuff(StatId.ACCURACY, 0, 0));
        buffs.add(new StatBuff(StatId.EVASION, 0, 0));
        ailment = new Ailment(sickbay.pokenamon.model.enums.Ailment.NONE, 0, 0, 0);
        volatileAilments = new HashSet<>();
    }

    public BattleMove[] getBattleMoves() { return battleMoves; }
    public void setBattleMoves(BattleMove[] battleMoves) { this.battleMoves = battleMoves; }

    public Ailment getAilment() { return ailment; }

    public void setAilment(Ailment ailment) { this.ailment = ailment; }

    public VolatileAilment getVolatileAilment(sickbay.pokenamon.model.enums.VolatileAilment ailment) {
        return volatileAilments.stream().filter(v -> v.getType() == ailment).collect(Collectors.toList()).get(0);
    }

    public HashSet<VolatileAilment> getVolatileAilments() {
        return volatileAilments;
    }

    public void addVolatileAilment(VolatileAilment ailment) { volatileAilments.add(ailment); }
    public void setVolatileAilment(VolatileAilment ailment) { removeVolatileAilment(ailment.getType()); addVolatileAilment(ailment); }
    public void removeVolatileAilment(sickbay.pokenamon.model.enums.VolatileAilment ailment) { volatileAilments.removeIf(v -> v.getType() == ailment); }
    public boolean hasVolatileElement(sickbay.pokenamon.model.enums.VolatileAilment ailment) { return volatileAilments.stream().anyMatch(v -> v.getType() == ailment); }

    public StatBuff getBuff(StatId stat) { return buffs.stream().filter(b -> b.getStat() == stat).collect(Collectors.toList()).get(0); }
    public void addBuff(StatBuff buff) { buffs.add(buff); }
    public void setBuff(StatBuff buff) { removeBuff(buff.getStat()); addBuff(buff); }
    public void removeBuff(StatId stat) { buffs.removeIf(b -> b.getStat() == stat); }
    public boolean hasBuff(StatId stat) { return buffs.stream().anyMatch(b -> b.getStat() == stat); }

    public BattleMove getLastMoveUsed() { return lastMoveUsed; }

    public void setLastMoveUsed(BattleMove lastMoveUsed) { this.lastMoveUsed = lastMoveUsed; }

    public boolean isFainted() { return getCurrentHp() <= 0; }

    public int getEffectiveStat(StatId id) {
        int base = getStats().get(id).getBattleStat();
        int stage = hasBuff(id) ? getBuff(id).getStage() : 0;
        stage = Math.max(-6, Math.min(6, stage));
        double multiplier = (double) Math.max(2, 2 + stage) / Math.max(2, 2 - stage);
        return (int) (base * multiplier);
    }

    public void heal(int amount) {
        setCurrentHp(Math.min(getTotalHp(), getCurrentHp() + amount));
    }

    public void takeDamage(int damage) {
        setCurrentHp(Math.max(0, getCurrentHp() - damage));
    }

    public boolean isSuspended() { return suspended; }

    public void setSuspended(boolean isSuspended) { this.suspended = isSuspended; }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
    }

    public boolean isLockedIn() {
        return lockedIn;
    }

    public void setLockedIn(boolean lockedIn) {
        this.lockedIn = lockedIn;
    }

    public void setBattlePokemonListener(BattlePokemonListener listener) { battlePokemonListener = listener; }
    public void setMoveUseListener(MoveUseListener listener) { moveUseListener = listener; }
    public void setDamageEffectListener(DamageEffectListener listener) { damageEffectListener = listener; }

    public void notifyMoveUse(BattleMove move) {
        if (moveUseListener != null) moveUseListener.onUse(move);
    }

    public void notifyMoveFail(BattleMove move) {
        if (moveUseListener != null) moveUseListener.onFail(move);
    }

    public void notifyMoveMiss(BattleMove move) {
        if (moveUseListener != null) moveUseListener.onMiss(move);
    }

    public void notifyMoveHit(BattleMove move) {
        if (moveUseListener != null) moveUseListener.onHit(move);
    }

    public void notifyMovePpOut(BattleMove move) {
        if (moveUseListener != null) moveUseListener.onPpOut(move);
    }

    public void notifyMoveMultipleHits(BattleMove move, int hits) {
        if (moveUseListener != null) moveUseListener.onMultipleHits(move, hits);
    }

    public void notifyCriticalDamage() {
        if (damageEffectListener != null) damageEffectListener.onCriticalDamage();
    }

    public void notifyEffective() {
        if (damageEffectListener != null) damageEffectListener.onEffective();
    }

    public void notifyResist() {
        if (damageEffectListener != null) damageEffectListener.onResist();
    }

    public void notifyImmune() {
        if (damageEffectListener != null) damageEffectListener.onImmune();
    }


    public void notifyFaint(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onFaint(pokemon);
    }

    public void notifyBurn(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onBurn(pokemon);
    }

    public void notifyPoison(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onPoison(pokemon);
    }

    public void notifyFreeze(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onFreeze(pokemon);
    }

    public void notifyThaw(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onThaw(pokemon);
    }

    public void notifySleep(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onSleep(pokemon);
    }

    public void notifyRest(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onRest(pokemon);
    }

    public void notifyWakeUp(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onWakeUp(pokemon);
    }

    public void notifyParalyze(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onParalyze(pokemon);
    }

    public void notifyParalyzeSuccess(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onParalyzeSuccess(pokemon);
    }

    public void notifyConfuse(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onConfuse(pokemon);
    }

    public void notifyConfusion(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onConfusion(pokemon);
    }

    public void notifyConfusionSnap(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onConfusionSnap(pokemon);
    }

    public void notifyFlinch(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onFlinch(pokemon);
    }

    public void notifyHeal(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onHeal(pokemon);
    }

    public void notifyDrain(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onDrain(pokemon);
    }

    public void notifyDisabled(BattlePokemon pokemon, BattleMove move) {
        if (battlePokemonListener != null) battlePokemonListener.onDisabled(pokemon, move);
    }

    public void notifyTorment(BattlePokemon pokemon, BattleMove move) {
        if (battlePokemonListener != null) battlePokemonListener.onTorment(pokemon, move);
    }

    public void notifyYawn(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onYawn(pokemon);
    }

    public void notifySilence(BattleMove move) {
        if (battlePokemonListener != null) battlePokemonListener.onSilence(move);
    }

    public void notifyRecoil(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onRecoil(pokemon);
    }

    public void notifyTarShot(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onTarShot(pokemon);
    }

    public void notifyPerishSong(BattlePokemon pokemon, int turns) {
        if (battlePokemonListener != null) battlePokemonListener.onPerishSong(pokemon, turns);
    }

    public void notifyStatChange(BattlePokemon pokemon, StatId statId, int previousStage, int stage) {
        if (battlePokemonListener != null) battlePokemonListener.onStatChange(pokemon, statId, previousStage, stage);
    }

    public void notifyInflict(BattlePokemon pokemon, Ailment ailment) {
        if (battlePokemonListener != null) battlePokemonListener.onInflict(pokemon, ailment);
    }

    public void notifyCure(BattlePokemon pokemon, Ailment ailment) {
        if (battlePokemonListener != null) battlePokemonListener.onCure(pokemon, ailment);
    }

    public void notifyNightmare(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onNightmare(pokemon);
    }

    public void notifyIngrain(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onIngrain(pokemon);
    }

    public void notifyVolatileInflict(BattlePokemon pokemon, VolatileAilment ailment) {
        if (battlePokemonListener != null) battlePokemonListener.onVolatileInflict(pokemon, ailment);
    }

    public void notifyVolatileCure(BattlePokemon pokemon, VolatileAilment ailment) {
        if (battlePokemonListener != null) battlePokemonListener.onVolatileCure(pokemon, ailment);
    }

    public void notifyFly(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onFly(pokemon);
    }

    public void notifyDig(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onDig(pokemon);
    }

    public void notifyDive(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onDive(pokemon);
    }

    public void notifyCharge(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onCharge(pokemon);
    }

    public void notifyCharging(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onCharging(pokemon);
    }

    public void notifyChargeFinish(BattlePokemon pokemon) {
        if (battlePokemonListener != null) battlePokemonListener.onChargeFinish(pokemon);
    }

    public void notifyProtect(BattlePokemon pokemon, BattleMove move) {
        if (battlePokemonListener != null) battlePokemonListener.onProtect(pokemon, move);
    }

    public void notifyProtectFail(BattlePokemon pokemon, BattleMove move) {
        if (battlePokemonListener != null) battlePokemonListener.onProtectFail(pokemon, move);
    }

}