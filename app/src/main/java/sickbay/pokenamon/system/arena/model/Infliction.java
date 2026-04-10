package sickbay.pokenamon.system.arena.model;

public class Infliction {
    int minimumTurns;
    int maximumTurns;

    int turns;
    int accuracy;

    public Infliction(int minimumTurns, int maximumTurns, int accuracy) {
        this.minimumTurns = minimumTurns;
        this.maximumTurns = maximumTurns;
        this.accuracy = accuracy;
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

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }
}
