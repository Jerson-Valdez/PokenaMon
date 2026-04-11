package sickbay.pokenamon.system.arena;

import java.util.ArrayList;
import java.util.List;

import sickbay.pokenamon.system.arena.enums.Ailment;
import sickbay.pokenamon.system.arena.enums.DamageClass;
import sickbay.pokenamon.system.arena.enums.TargetType;
import sickbay.pokenamon.system.arena.enums.Type;
import sickbay.pokenamon.system.arena.model.StatBuff;

public class BattleMove {
    private String name;
    private int power;
    private DamageClass damageClass;
    private Type type;
    private TargetType targetType;
    private int accuracy;
    private int totalPp;
    private int currentPp;
    private int priority;
    private int minimumHits;
    private int maximumHits;
    private int minimumTurns;
    private int maximumTurns;
    private String rawAilment;
    private Ailment ailment;
    private int ailmentChance;
    private int flinchChance; // flinch TO the target, not the user
    private int statChance;
    private boolean disabled;
    private int disabledTurns;

    private List<StatBuff> statBuffs = new ArrayList<>();

    public BattleMove(String move) {
        this.name = move;
    }

    public BattleMove(String name, DamageClass damageClass, Type type, TargetType targetType, int power, int accuracy) {
        this.name = name;
        this.power = power;
        this.damageClass = damageClass;
        this.type = type;
        this.targetType = targetType;
        this.accuracy = accuracy;
    }

    public BattleMove(String name, DamageClass damageClass, Type type, TargetType targetType, int power, int pp, int priority, int minimumTurns, int maximumTurns, int minimumHits, int maximumHits,  int accuracy) {
        this.name = name;
        this.power = power;
        this.damageClass = damageClass;
        this.type = type;
        this.totalPp = pp;
        this.currentPp = pp;
        this.priority = priority;
        this.minimumTurns = minimumTurns;
        this.maximumTurns = maximumTurns;
        this.minimumHits = minimumHits;
        this.maximumHits = maximumHits;
        this.targetType = targetType;
        this.accuracy = accuracy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public DamageClass getDamageClass() {
        return damageClass;
    }

    public void setDamageClass(DamageClass damageClass) {
        this.damageClass = damageClass;
    }

    public Type getType() { return type; }

    public void setType(Type type) { this.type = type; }

    public TargetType getTargetType() { return targetType; }

    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getTotalPp() {
        return totalPp;
    }

    public void setTotalPp(int totalPp) { this.totalPp = totalPp; }

    public int getCurrentPp() {
        return currentPp;
    }

    public void setCurrentPp(int currentPp) { this.currentPp = currentPp; }

    public void reducePp() {
        setCurrentPp(getCurrentPp() - 1);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMinimumHits() {
        return minimumHits;
    }

    public void setMinimumHits(int minimumHits) {
        this.minimumHits = minimumHits;
    }

    public int getMaximumHits() {
        return maximumHits;
    }

    public void setMaximumHits(int maximumHits) {
        this.maximumHits = maximumHits;
    }

    public int getMinimumTurns() {
        return minimumTurns;
    }

    public void setMinimumTurns(int minimumTurns) {
        this.minimumTurns = minimumTurns;
    }

    public int getMaximumTurns() {
        return maximumTurns;
    }

    public void setMaximumTurns(int maximumTurns) {
        this.maximumTurns = maximumTurns;
    }

    public String getRawAilment() {
        return rawAilment;
    }

    public void setRawAilment(String rawAilment) {
        this.rawAilment = rawAilment;
    }

    public Ailment getAilment() {
        return ailment;
    }

    public void setAilment(Ailment ailment) {
        this.ailment = ailment;
    }

    public int getAilmentChance() {
        return ailmentChance;
    }

    public void setAilmentChance(int ailmentChance) {
        this.ailmentChance = ailmentChance;
    }

    public int getFlinchChance() {
        return flinchChance;
    }

    public void setFlinchChance(int flinchChance) {
        this.flinchChance = flinchChance;
    }

    public int getStatChance() {
        return statChance;
    }

    public void setStatChance(int statChance) {
        this.statChance = statChance;
    }

    public List<StatBuff> getStatBuffs() { return statBuffs; }
    public void setStatBuffs(List<StatBuff> statBuffs) { this.statBuffs = statBuffs; }

    public boolean isDisabled() { return disabled; }

    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    public int getDisabledTurns() { return disabledTurns; }

    public void setDisabledTurns(int turns) { this.disabledTurns = turns; }
}
