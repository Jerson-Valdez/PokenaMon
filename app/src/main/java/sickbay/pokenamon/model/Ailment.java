package sickbay.pokenamon.model;

public class Ailment extends Infliction {
    sickbay.pokenamon.model.enums.Ailment type;

    public Ailment(sickbay.pokenamon.model.enums.Ailment type, int minimumTurns, int maximumTurns, int accuracy) {
        super(minimumTurns, maximumTurns, accuracy);
        this.type = type;
    }

    public sickbay.pokenamon.model.enums.Ailment getType() {
        return type;
    }

    public void setType(sickbay.pokenamon.model.enums.Ailment type) { this.type = type; }
}
