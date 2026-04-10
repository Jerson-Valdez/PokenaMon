package sickbay.pokenamon.system.arena.events;

import sickbay.pokenamon.system.arena.enums.StatId;

public interface OnStatChangeListener {
    void onStatChange(StatId stat, int stage);
}
