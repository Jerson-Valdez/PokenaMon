package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.enums.VolatileAilment;

public interface OnVolatileAilmentInflictedListener {
    void onVolatileAilmentInflicted(VolatileAilment ailment);
}
