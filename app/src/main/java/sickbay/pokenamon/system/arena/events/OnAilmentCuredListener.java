package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.enums.Ailment;

public interface OnAilmentCuredListener {
    void onAilmentCured(Ailment ailment);
}
