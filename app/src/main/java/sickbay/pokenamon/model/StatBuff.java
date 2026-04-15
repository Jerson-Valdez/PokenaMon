package sickbay.pokenamon.model;

import sickbay.pokenamon.model.enums.StatId;

public class StatBuff extends Infliction {
    StatId stat;
    int stage;

    public StatBuff(StatId stat, int stage, int accuracy) {
        super(0, 0, accuracy);
        this.stat = stat;
        this.stage = stage;
    }

    public StatId getStat() {
        return stat;
    }

    public void setStat(StatId stat) {
        this.stat = stat;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
