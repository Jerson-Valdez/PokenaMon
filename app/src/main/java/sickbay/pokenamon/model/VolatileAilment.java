package sickbay.pokenamon.model;

public class VolatileAilment extends Infliction {
    sickbay.pokenamon.model.enums.VolatileAilment type;
    public VolatileAilment(sickbay.pokenamon.model.enums.VolatileAilment type, int minimumTurns, int maximumTurns, int accuracy) {
        super(minimumTurns, maximumTurns, accuracy);
        this.type = type;
    }

    public sickbay.pokenamon.model.enums.VolatileAilment getType() {
        return this.type;
    }

    public void setType(sickbay.pokenamon.model.enums.VolatileAilment type) {
        this.type = type;
    }
}
