package sickbay.pokenamon.system.home;

public interface HealthCooldownListener {
    void onDecrement(long time);
    void onFinish(long time);
}
