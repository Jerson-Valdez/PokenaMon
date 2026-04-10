package sickbay.pokenamon.system.arena.model;

import sickbay.pokenamon.system.arena.enums.StatId;

public class PokemonStat {
    public StatId stat;
    public int baseStat;
    public int stage = 0;

    public PokemonStat(StatId stat, int baseStat) {
        this.stat = stat;
        this.baseStat = baseStat;
    }
    public double getStageMultiplier() {
        if (stat ==StatId.ACCURACY || stat == StatId.EVASION) {
            int[] num = {33, 36, 43, 50, 60, 75, 100, 133, 166, 200, 250, 266, 300};
            int idx = stage + 6;
            return (double) num[idx] / 100;
        }
        return (double) Math.max(2, 2 + stage) / Math.max(2, 2 - stage);
    }

    public int getBaseStat() {
        return baseStat;
    }

    public void setBaseStat(int baseStat) {
        this.baseStat = baseStat;
    }

    public int getBattleStat() {
        return (int) (baseStat * getStageMultiplier());
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
