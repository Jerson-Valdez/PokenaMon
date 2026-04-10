package sickbay.pokenamon.system.arena.model;

public class Ailment extends Infliction {
    sickbay.pokenamon.system.arena.enums.Ailment type;

    public Ailment(sickbay.pokenamon.system.arena.enums.Ailment type, int minimumTurns, int maximumTurns, int accuracy) {
        super(minimumTurns, maximumTurns, accuracy);
        this.type = type;
    }

    public sickbay.pokenamon.system.arena.enums.Ailment getType() {
        return type;
    }

    public void setType(sickbay.pokenamon.system.arena.enums.Ailment type) { this.type = type; }
}
