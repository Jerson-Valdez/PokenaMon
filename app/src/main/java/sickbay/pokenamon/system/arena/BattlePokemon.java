package sickbay.pokenamon.system.arena;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.arena.events.OnAilmentCuredListener;
import sickbay.pokenamon.system.arena.events.OnAilmentInflictedListener;
import sickbay.pokenamon.system.arena.events.OnCriticalHitListener;
import sickbay.pokenamon.system.arena.events.OnDamageListener;
import sickbay.pokenamon.system.arena.events.OnFaintListener;
import sickbay.pokenamon.system.arena.events.OnFlinchListener;
import sickbay.pokenamon.system.arena.events.OnFrozenListener;
import sickbay.pokenamon.system.arena.events.OnHealListener;
import sickbay.pokenamon.system.arena.events.OnImmuneListener;
import sickbay.pokenamon.system.arena.events.OnMoveFailListener;
import sickbay.pokenamon.system.arena.events.OnMoveHitListener;
import sickbay.pokenamon.system.arena.events.OnMoveMissListener;
import sickbay.pokenamon.system.arena.events.OnParalyzeSuccessListener;
import sickbay.pokenamon.system.arena.events.OnRestListener;
import sickbay.pokenamon.system.arena.events.OnSleepingListener;
import sickbay.pokenamon.system.arena.events.OnSnappedOutOfConfusion;
import sickbay.pokenamon.system.arena.events.OnStatChangeListener;
import sickbay.pokenamon.system.arena.events.OnVolatileAilmentInflictedListener;
import sickbay.pokenamon.system.arena.model.Ailment;
import sickbay.pokenamon.system.arena.model.StatBuff;
import sickbay.pokenamon.system.arena.model.VolatileAilment;

public class BattlePokemon extends Pokemon {
    int totalHp;
    int currentHp;
    BattleMove[] battleMoves;
    Ailment ailment;
    HashSet<VolatileAilment> volatileAilments;
    HashSet<StatBuff> buffs;

    private OnSnappedOutOfConfusion confusionListener;
    private OnFlinchListener flinchListener;
    private OnMoveMissListener moveMissListener;
    private OnMoveHitListener moveHitListener;
    private OnMoveFailListener moveFailListener;
    private OnImmuneListener immuneListener;
    private OnAilmentCuredListener ailmentCuredListener;
    private OnCriticalHitListener criticalHitListener;
    private OnRestListener restListener;
    private OnSleepingListener sleepingListener;
    private OnStatChangeListener statChangeListener;
    private OnAilmentInflictedListener ailmentInflictedListener;
    private OnVolatileAilmentInflictedListener volatileAilmentInflictedListener;
    private OnHealListener healListener;
    private OnParalyzeSuccessListener paralyzeSuccessListener;
    private OnFrozenListener frozenListener;
    private OnDamageListener damageListener;
    private OnFaintListener faintListener;

    public BattlePokemon(Pokemon pokemon) {
        super(pokemon.getPokedexId(),
                pokemon.getName(),
                pokemon.getLevel(),
                pokemon.getExp(),
                pokemon.getTypes(),
                pokemon.getSprite(),
                pokemon.getCry(),
                pokemon.getWeight(),
                pokemon.getHeight(),
                pokemon.getStats(),
                pokemon.getMoves());
        totalHp = ArenaEngine.calculatePokemonTotalHp(pokemon.getStats().get(StatId.HP).getBaseStat(), pokemon.getLevel());
        battleMoves = Arrays.stream(pokemon.getMoves()).map(BattleMove::new).toArray(BattleMove[]::new);
        buffs = new HashSet<>();
        buffs.add(new StatBuff(StatId.ACCURACY, 0, 0));
        buffs.add(new StatBuff(StatId.EVASION, 0, 0));
        ailment = new Ailment(sickbay.pokenamon.system.arena.enums.Ailment.NONE,0,0,0);
        volatileAilments = new HashSet<>();
    }

    public int getTotalHp() { return totalHp; }

    public void setTotalHp(int totalHp) { this.totalHp = totalHp; }

    public int getCurrentHp() { return currentHp; }

    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public BattleMove[] getBattleMoves() {
        return this.battleMoves;
    }

    public void setBattleMoves(BattleMove[] battleMoves) {
        this.battleMoves = battleMoves;
    }

    public Ailment getAilment() {
        return ailment;
    }

    public void setAilment(Ailment ailment) {
        this.ailment = ailment;
    }

    public VolatileAilment getVolatileAilment(sickbay.pokenamon.system.arena.enums.VolatileAilment ailment) {
        return volatileAilments.stream().filter(v -> v.getType() == ailment).collect(Collectors.toList()).get(0);
    }

    public void addVolatileAilment(VolatileAilment ailment) {
        volatileAilments.add(ailment);
    }

    public void setVolatileAilment(VolatileAilment ailment) {
        removeVolatileAilment(ailment.getType());
        addVolatileAilment(ailment);
    }

    public void removeVolatileAilment(sickbay.pokenamon.system.arena.enums.VolatileAilment ailment) {
        volatileAilments.removeIf(v -> v.getType() == ailment);
    }

    public boolean hasVolatileElement(sickbay.pokenamon.system.arena.enums.VolatileAilment ailment) {
        return volatileAilments.stream().anyMatch(v -> v.getType() == ailment);
    }

    public StatBuff getBuff(StatId stat) {
        return buffs.stream().filter(b -> b.getStat() == stat).collect(Collectors.toList()).get(0);
    }

    public void addBuff(StatBuff buff) {
        buffs.add(buff);
    }

    public void setBuff(StatBuff buff) {
        removeBuff(buff.getStat());
        addBuff(buff);
    }

    public void removeBuff(StatId stat) {
        buffs.removeIf(b -> b.getStat() == stat);
    }

    public boolean hasBuff(StatId stat) {
        return buffs.stream().anyMatch(b -> b.getStat() == stat);
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

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

    public void setOnSnappedOutOfConfusionListener(OnSnappedOutOfConfusion listener) { this.confusionListener = listener; }

    public void setOnFlinchListener(OnFlinchListener listener) { this.flinchListener = listener; }

    public void setOnMoveMissListener(OnMoveMissListener listener) { this.moveMissListener = listener; }

    public void setOnMoveHitListener(OnMoveHitListener listener) { this.moveHitListener = listener; }

    public void setOnImmuneListener(OnImmuneListener listener) { this.immuneListener = listener; }

    public void setOnMoveFailListener(OnMoveFailListener listener) { this.moveFailListener = listener; }

    public void setOnAilmentCuredListener(OnAilmentCuredListener listener) { this.ailmentCuredListener = listener; }

    public void setOnCriticalHitListener(OnCriticalHitListener listener) { this.criticalHitListener = listener; }

    public void setOnRestListener(OnRestListener listener) { this.restListener = listener; }

    public void setOnFrozenListener(OnFrozenListener listener) { this.frozenListener = listener; }

    public void setOnAilmentInflictedListener(OnAilmentInflictedListener listener) { this.ailmentInflictedListener = listener; }

    public void setOnVolatileAilmentInflictedListener(OnVolatileAilmentInflictedListener listener) { this.volatileAilmentInflictedListener = listener; }

    public void setOnSleepingListener(OnSleepingListener listener) { this.sleepingListener = listener; }

    public void setOnHealListener(OnHealListener listener) { this.healListener = listener; }

    public void setOnStatChangeListener(OnStatChangeListener listener) { this.statChangeListener = listener; }

    public void setOnParalyzeSuccessListener(OnParalyzeSuccessListener listener) { this.paralyzeSuccessListener = listener; }

    public void setOnDamageListener(OnDamageListener listener) {
        this.damageListener = listener;
    }

    public void setOnFaintListener(OnFaintListener listener) {
        this.faintListener = listener;
    }

    public void onSnappedOutOfConfusion() {
        confusionListener.onSnappedOutOfConfusion();
    }

    public void onMoveMiss(BattleMove move) {
        moveMissListener.onMiss(move);
    }

    public void onMoveHit() {
        moveHitListener.onHit();
    }

    public void onMoveFail(BattleMove move) {
        moveFailListener.onMoveFail(move);
    }

    public void onImmune(BattleMove move) {
        immuneListener.onImmune(move);
    }

    public void onAilmentCured(sickbay.pokenamon.system.arena.enums.Ailment ailment) {
        ailmentCuredListener.onAilmentCured(ailment);
    }

    public void onCriticalHit() {
        criticalHitListener.onCriticalHit();
    }

    public void onFlinch() {
        flinchListener.onFlinch();
    }

    public void onAilmentInflicted(sickbay.pokenamon.system.arena.enums.Ailment ailment) {
        ailmentInflictedListener.onAilmentInflicted(ailment);
    }

    public void onVolatileAilmentInflicted(sickbay.pokenamon.system.arena.enums.VolatileAilment ailment) {
        volatileAilmentInflictedListener.onVolatileAilmentInflicted(ailment);
    }

    public void onRest() {
        restListener.onRest();
    }

    public void onSleeping() {
        sleepingListener.onSleeping();
    }

    public void onHeal(int amount) {
        Log.d("NOW", currentHp + "");
        healListener.onHeal(amount);
    }

    public void onStatChange(StatId stat, int stage) {
        statChangeListener.onStatChange(stat, stage);
    }

    public void onFrozen() {
        frozenListener.onFrozen();
    }

    public void onParalyzeSuccess() {
        paralyzeSuccessListener.onParalyzeSuccess();
    }

    public void takeDamage(int damage) {
        setCurrentHp(Math.max(0, getCurrentHp() - damage));

        if (getCurrentHp() == 0 && faintListener != null) {
            faintListener.onFaint(this);
            return;
        }

        damageListener.onDamage(damage);
    }
}
