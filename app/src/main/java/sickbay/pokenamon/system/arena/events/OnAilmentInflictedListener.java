package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.enums.Ailment;

public interface OnAilmentInflictedListener {
    void onAilmentInflicted(Ailment ailment);
}
