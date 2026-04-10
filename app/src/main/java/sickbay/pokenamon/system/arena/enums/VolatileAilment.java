package sickbay.pokenamon.system.arena.enums;

public enum VolatileAilment {
    NONE, INFATUATION, CONFUSION, FLINCH, POISON, PARALYSIS,
    TRAP, NIGHTMARE, TORMENT, DISABLE, YAWN, HEAL_BLOCK, SILENCE,
    TAR_SHOT, LEECH_SEED, EMBARGO, INGRAIN, TELEKINESIS;


    public static String value(String ailment) {
        return ailment.replaceAll("-", "_").toUpperCase();
    }
}
