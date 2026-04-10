package sickbay.pokenamon.system.arena.model;

public class VolatileAilment extends Infliction {
    sickbay.pokenamon.system.arena.enums.VolatileAilment type;
    public VolatileAilment(sickbay.pokenamon.system.arena.enums.VolatileAilment type, int minimumTurns, int maximumTurns, int accuracy) {
        super(minimumTurns, maximumTurns, accuracy);
        this.type = type;
    }

    public sickbay.pokenamon.system.arena.enums.VolatileAilment getType() {
        return this.type;
    }

    public void setType(sickbay.pokenamon.system.arena.enums.VolatileAilment type) {
        this.type = type;
    }
}
