package sickbay.pokenamon.system.arena.events;

public interface DamageEffectListener {
    void onCriticalDamage();
    void onEffective();
    void onResist();
    void onImmune();
}
