package sickbay.pokenamon.system.arena;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sickbay.pokenamon.system.arena.enums.Ailment;

public class ArenaRegistry {
    public static final int POKEDEX_ENTRY_COUNT = 1025;

    public static final String SINGLE_HP_POKEMON = "shedinja";

    public static final Set<String> BLACKLISTED_MOVES = new HashSet<>(List.of(
            "attract", // causes Infatuation which is permanent until the user is out of field (not that broken since 50% chance of infatuated pokemon missing)
            "baton-pass", // useless
            "helping-hand", // useless
            "trick-room", // too much,
            "destiny-bond" // hassle to implement
    ));

    public static final Set<String> MOVES_WITH_SPECIAL_AILMENT = new HashSet<>(List.of(
       "thousand-arrows", "tri-attack", "smack-down", "perish-song", "telekinesis", "ingrain", "embargo", "tar-shot"
    ));

    public static final Set<String> ALWAYS_HIT = new HashSet<>(List.of(
            "aerial-ace", "aura-sphere", "magical-leaf", "swift", "shock-wave",
            "smart-strike", "feint-attack", "shadow-punch", "magnet-bomb",
            "vital-throw", "toxic"
    ));

    public static final Set<String> OHKO = new HashSet<>(List.of(
            "sheer-cold", "fissure", "horn-drill", "guillotine"
    ));

    public static final Set<String> TWO_TURN = new HashSet<>(List.of(
            "solar-beam", "solar-blade", "skull-bash", "sky-attack", "razor-wind",
            "freeze-shock", "ice-burn", "bounce", "fly", "dig", "dive",
            "phantom-force", "shadow-force", "geomancy", "meteor-beam", "electro-shot"
    ));

    // Invincible state
    public static final Set<String> INVULNERABLE_DURING_CHARGE = new HashSet<>(List.of(
            "bounce", "fly", "dig", "dive", "phantom-force", "shadow-force"
    ));

    // Hit invincible state targets
    public static final Set<String> HITS_DURING_DIG = new HashSet<>(List.of(
            "earthquake", "magnitude", "fissure"
    ));

    public static final Set<String> HITS_DURING_FLY = new HashSet<>(List.of(
            "gust", "twister", "thunder", "hurricane", "sky-uppercut",
            "smack-down", "thousand-arrows"
    ));

    public static final Set<String> HITS_DURING_DIVE = new HashSet<>(List.of(
            "surf", "whirlpool"
    ));

    public static final Set<String> HITS_DURING_PHANTOM_FORCE = new HashSet<>(List.of(
            "shadow-force", "phantom-force"
    ));

    // Recharge moves
    public static final Set<String> RECHARGE = new HashSet<>(List.of(
            "hyper-beam", "giga-impact", "frenzy-plant", "blast-burn", "hydro-cannon",
            "rock-wrecker", "roar-of-time", "eternabeam", "prismatic-laser"
    ));

    // Hit for 2-5 times
    public static final Set<String> MULTI_HIT = new HashSet<>(List.of(
            "double-slap", "comet-punch", "fury-attack", "pin-missile", "spike-cannon",
            "barrage", "fury-swipes", "bone-rush", "bullet-seed", "icicle-spear",
            "rock-blast", "arm-thrust", "water-shuriken", "scale-shot", "surging-strikes"
    ));

    // Exactly 2 turns
    public static final Set<String> DOUBLE_HIT = new HashSet<>(List.of(
            "double-hit", "double-kick", "bonemerang", "dual-chop", "dual-wingbeat",
            "gear-grind", "twineedle", "dragon-darts"
    ));

    // Lock in for 2-3 turns
    public static final Set<String> LOCK_IN = new HashSet<>(List.of(
            "thrash", "outrage", "petal-dance", "uproar"
    ));

    // Higher crit ratio
    public static final Set<String> HIGH_CRIT = new HashSet<>(List.of(
            "slash", "razor-leaf", "crabhammer", "karate-chop", "razor-wind",
            "aeroblast", "cross-chop", "sky-attack", "shadow-claw", "night-slash",
            "psycho-cut", "leaf-blade", "stone-edge", "cross-poison", "spacial-rend",
            "shadow-force", "drill-run", "high-horsepower"
    ));

    // Always crit
    public static final Set<String> ALWAYS_CRIT = new HashSet<>(List.of(
            "frost-breath", "storm-throw"
    ));

    // Stat-ignoring fixed damage
    public static final Set<String> FIXED_DAMAGE = new HashSet<>(List.of(
            "seismic-toss", "night-shade",  // damage = user level
            "dragon-rage",                   // always 40
            "sonic-boom",                    // always 20
            "psywave",                       // 50-150% of user level
            "super-fang",                    // halves target HP
            "nature's-madness"               // halves target HP
    ));

    // Damage equal to user's level
    public static final Set<String> LEVEL_DAMAGE = new HashSet<>(List.of(
            "seismic-toss", "night-shade"
    ));

    // Halves the target's current HP
    public static final Set<String> HALF_HP = new HashSet<>(List.of(
            "super-fang", "natures-madness"
    ));

    // Fixed damage
    public static final Map<String, Integer> FIXED_DAMAGE_VALUES = new HashMap<>() {{
        put("dragon-rage", 40);
        put("sonic-boom", 20);
    }};

    // 100% HP recover
    public static final Set<String> SELF_HEAL = new HashSet<>(List.of(
            "recover", "soft-boiled", "milk-drink", "slack-off", "roost",
            "moonlight", "synthesis", "morning-sun", "shore-up", "jungle-healing",
            "heal-order", "egg-bomb"
    ));

    // 50% HP recover
    public static final Set<String> HEAL_HALF = new HashSet<>(List.of(
            "recover", "soft-boiled", "milk-drink", "slack-off", "roost",
            "heal-order", "shore-up"
    ));

    // 50% of damage drain from target
    public static final Set<String> DRAIN_HALF = new HashSet<>(List.of(
            "absorb", "mega-drain", "giga-drain", "leech-life", "drain-punch",
            "draining-kiss", "oblivion-wing", "bitter-blade"
    ));

    // 75% drain
    public static final Set<String> DRAIN_THREE_QUARTERS = new HashSet<>(List.of(
            "oblivion-wing"
    ));

    // Recoil
    public static final Map<String, Double> RECOIL_RATIO = new HashMap<>() {{
        put("take-down", 0.25);
        put("double-edge", 0.33);
        put("brave-bird", 0.33);
        put("flare-blitz", 0.33);
        put("head-smash", 0.50);
        put("wild-charge", 0.25);
        put("volt-tackle", 0.33);
        put("wood-hammer", 0.33);
        put("high-jump-kick", 0.50);
        put("jump-kick", 0.50);
    }};

    // Fixed priorities for some moves
    public static final Map<String, Integer> PRIORITY_OVERRIDE = new HashMap<>() {{
        put("quick-attack", 1);
        put("mach-punch", 1);
        put("bullet-punch", 1);
        put("ice-shard", 1);
        put("shadow-sneak", 1);
        put("aqua-jet", 1);
        put("accelerock", 1);
        put("sucker-punch", 1);
        put("fake-out", 3);
        put("extreme-speed", 2);
        put("endure", 4);
        put("detect", 4);
        put("protect", 4);
        put("wide-guard", 3);
        put("quick-guard", 3);
        put("pursuit", 0);
        put("trick-room", -7);
        put("magic-room", -7);
        put("wonder-room", -7);
        put("whirlwind", -6);
        put("roar", -6);
        put("dragon-tail", -6);
        put("circle-throw", -6);
    }};

    // Ignore stat stage changes on target (bypass boosts)
    public static final Set<String> BYPASS_STAT_STAGES = new HashSet<>(List.of(
            "chip-away", "sacred-sword", "razor-shell", "storm-throw",
            "frost-breath", "sunsteel-strike", "moongeist-beam", "photon-geyser"
    ));

    // Fail if moves second
    public static final Set<String> FAILS_IF_SECOND = new HashSet<>(List.of(
            "fake-out"  // only works on first turn and if user moves first
    ));

    // Cause the user to faint
    public static final Set<String> USER_FAINTS = new HashSet<>(List.of(
            "explosion", "self-destruct", "misty-explosion", "healing-wish", "lunar-dance"
    ));

    // Variable base power
    public static final Set<String> VARIABLE_POWER = new HashSet<>(List.of(
            "eruption", "water-spout",  // power = (user HP / max HP) * 150
            "flail", "reversal",        // power increases as user HP decreases
            "wring-out", "crush-grip",  // power = (target HP / max HP) * 120
            "gyro-ball",                // power based on speed difference
            "electro-ball",             // power based on speed difference
            "heat-crash", "heavy-slam", // power based on weight difference
            "low-kick", "grass-knot",   // power based on target weight
            "magnitude",                // random power
            "return",                   // power based on friendship
            "frustration",              // power based on friendship
            "stored-power",             // power based on stat boosts
            "punishment"                // power based on target stat boosts
    ));

    // Thaw the user
    public static final Set<String> THAW_USER = new HashSet<>(List.of(
            "flame-wheel", "sacred-fire", "flare-blitz", "fusion-flare",
            "scald", "steam-eruption", "pyro-ball"
    ));

    // Thaw the target while dealing damage
    public static final Set<String> THAW_TARGET = new HashSet<>(List.of(
            "scald", "steam-eruption"
    ));

    // Protection moves
    public static final Set<String> PROTECT = new HashSet<>(List.of(
            "protect", "detect", "quick-guard", "wide-guard",
            "crafty-shield", "mat-block", "kings-shield",
            "spiky-shield", "baneful-bunker", "silk-trap",
            "burning-bulwark", "obstruct"
    ));

    // Bypasses 'Protection'
    public static final Set<String> BYPASS_PROTECT = new HashSet<>(List.of(
            "feint", "phantom-force", "shadow-force", "hyperspace-fury",
            "hyperspace-hole", "menacing-moonraze-maelstrom",
            "light-that-burns-the-sky", "searing-sunraze-smash"
    ));

    // Cure the user's non-volatile ailment
    public static final Set<String> CURE_SELF_AILMENT = new HashSet<>(List.of(
            "rest",          // replaces ailment with sleep for 2 turns, restores full HP
            "refresh",       // cures burn, poison, paralysis
            "heal-bell",     // cures entire party (single pokemon in your case = self)
            "aromatherapy"   // same as heal bell
    ));

    // Cure the target's non-volatile ailment
    public static final Set<String> CURE_TARGET_AILMENT = new HashSet<>(List.of(
            "heal-bell", "aromatherapy", "sparkling-aria" // sparkling-aria also cures burn
    ));

    // Only work when targegt is asleep ---
    public static final Set<String> REQUIRES_SLEEP = new HashSet<>(List.of(
            "dream-eater",
            "nightmare"
    ));

    // Target is asleep
    public static final Set<String> BOOSTED_ON_SLEEP = new HashSet<>(List.of(
            "wake-up-slap", // doubles power if target is asleep, then wakes them
            "smelling-salts" // doubles power if target is paralyzed, then cures it
    ));

    // Cure a specific ailment on hit
    public static final Map<String, Ailment> CURES_AILMENT_ON_HIT;
    static {
        Map<String, Ailment> map = new HashMap<>();
        map.put("wake-up-slap", Ailment.SLEEP);
        map.put("smelling-salts", Ailment.PARALYSIS);
        map.put("sparkling-aria", Ailment.BURN);
        CURES_AILMENT_ON_HIT = Collections.unmodifiableMap(map);
    }

    // Double power under specific ailment on target
    public static final Map<String, Ailment> DOUBLE_POWER_ON_AILMENT;
    static {
        Map<String, Ailment> map = new HashMap<>();
        map.put("wake-up-slap", Ailment.SLEEP);
        map.put("smelling-salts", Ailment.PARALYSIS);
        map.put("hex", Ailment.NONE); // any non-volatile ailment
        map.put("venoshock", Ailment.POISON);
        DOUBLE_POWER_ON_AILMENT = Collections.unmodifiableMap(map);
    }

    // Fail if target doesn't have a specific ailment
    public static final Map<String, Ailment> REQUIRES_AILMENT;
    static {
        Map<String, Ailment> map = new HashMap<>();
        map.put("dream-eater", Ailment.SLEEP);
        map.put("nightmare", Ailment.SLEEP);
        REQUIRES_AILMENT = Collections.unmodifiableMap(map);
    }

    // Increased power based on remaining HP
    public static final Set<String> POWER_INCREASES_LOW_HP = new HashSet<>(List.of(
            "flail", "reversal"
    ));

    // Decreased power based on remaining HP
    public static final Set<String> POWER_DECREASES_LOW_HP = new HashSet<>(List.of(
            "eruption", "water-spout"
    ));

    // Fail if user is not first turn out
    public static final Set<String> FIRST_TURN_ONLY = new HashSet<>(List.of(
            "fake-out", "first-impression"
    ));


    public static boolean isBlacklisted(BattleMove move) { return BLACKLISTED_MOVES.contains(move.getName()); }

    public static boolean isMoveWithSpecialAilment(BattleMove move) { return MOVES_WITH_SPECIAL_AILMENT.contains(move.getName()); }

    public static boolean alwaysHits(BattleMove move) {
        return ALWAYS_HIT.contains(move.getName());
    }

    public static boolean isOhko(BattleMove move) {
        return OHKO.contains(move.getName());
    }

    public static boolean isTwoTurn(BattleMove move) {
        return TWO_TURN.contains(move.getName());
    }

    public static boolean isRecharge(BattleMove move) {
        return RECHARGE.contains(move.getName());
    }

    public static boolean isHighCrit(BattleMove move) {
        return HIGH_CRIT.contains(move.getName());
    }

    public static boolean alwaysCrits(BattleMove move) {
        return ALWAYS_CRIT.contains(move.getName());
    }

    public static boolean isFixedDamage(BattleMove move) {
        return FIXED_DAMAGE.contains(move.getName());
    }

    public static boolean isLevelDamage(BattleMove move) {
        return LEVEL_DAMAGE.contains(move.getName());
    }

    public static boolean isHalfHp(BattleMove move) {
        return HALF_HP.contains(move.getName());
    }

    public static boolean hasDrain(BattleMove move) {
        return DRAIN_HALF.contains(move.getName()) || DRAIN_THREE_QUARTERS.contains(move.getName());
    }

    public static double getDrainRatio(BattleMove move) {
        if (DRAIN_THREE_QUARTERS.contains(move.getName())) return 0.75;
        if (DRAIN_HALF.contains(move.getName())) return 0.50;
        return 0.0;
    }

    public static double getRecoilRatio(BattleMove move) {
        return RECOIL_RATIO.getOrDefault(move.getName(), 0.0);
    }

    public static double getFixedDamageValue(BattleMove move) {
        return FIXED_DAMAGE_VALUES.get(move.getName());
    }

    public static int getPriorityOverride(BattleMove move) {
        return PRIORITY_OVERRIDE.getOrDefault(move.getName(), move.getPriority());
    }

    public static boolean bypassesStatStages(BattleMove move) {
        return BYPASS_STAT_STAGES.contains(move.getName());
    }

    public static boolean causesUserToFaint(BattleMove move) {
        return USER_FAINTS.contains(move.getName());
    }

    public static boolean hasVariablePower(BattleMove move) {
        return VARIABLE_POWER.contains(move.getName());
    }

    public static boolean thawsUser(BattleMove move) {
        return THAW_USER.contains(move.getName());
    }

    public static boolean thawsTarget(BattleMove move) {
        return THAW_TARGET.contains(move.getName());
    }

    public static boolean isProtect(BattleMove move) {
        return PROTECT.contains(move.getName());
    }

    public static boolean isSelfHeal(BattleMove move) {
        return SELF_HEAL.contains(move.getName());
    }

    public static boolean bypassesProtect(BattleMove move) {
        return BYPASS_PROTECT.contains(move.getName());
    }

    public static boolean curesSelfAilment(BattleMove move) {
        return CURE_SELF_AILMENT.contains(move.getName());
    }

    public static boolean requiresSleep(BattleMove move) {
        return REQUIRES_SLEEP.contains(move.getName());
    }

    public static boolean hasCuresAilmentOnHit(BattleMove move) {
        return CURES_AILMENT_ON_HIT.containsKey(move.getName());
    }

    public static Ailment getCuredAilmentOnHit(BattleMove move) {
        return CURES_AILMENT_ON_HIT.get(move.getName());
    }

    public static boolean hasDoublePowerOnAilment(BattleMove move) {
        return DOUBLE_POWER_ON_AILMENT.containsKey(move.getName());
    }

    public static Ailment getDoublePowerAilment(BattleMove move) {
        return DOUBLE_POWER_ON_AILMENT.get(move.getName());
    }

    public static boolean requiresAilment(BattleMove move) {
        return REQUIRES_AILMENT.containsKey(move.getName());
    }

    public static Ailment getRequiredAilment(BattleMove move) {
        return REQUIRES_AILMENT.get(move.getName());
    }

    public static boolean isFirstTurnOnly(BattleMove move) {
        return FIRST_TURN_ONLY.contains(move.getName());
    }

    public static boolean isLockIn(BattleMove move) {
        return LOCK_IN.contains(move.getName());
    }

    public static boolean failsIfSecond(BattleMove move) {
        return FAILS_IF_SECOND.contains(move.getName());
    }

    public static boolean invulnerableDuringCharge(BattleMove move) {
        return INVULNERABLE_DURING_CHARGE.contains(move.getName());
    }

    public static boolean hitsDuringDig(BattleMove move) {
        return HITS_DURING_DIG.contains(move.getName());
    }

    public static boolean hitsDuringFly(BattleMove move) {
        return HITS_DURING_FLY.contains(move.getName());
    }

    public static boolean hitsDuringDive(BattleMove move) {
        return HITS_DURING_DIVE.contains(move.getName());
    }

    public static boolean hitsDuringPhantomForce(BattleMove move) {
        return HITS_DURING_PHANTOM_FORCE.contains(move.getName());
    }

    public static boolean curesTargetAilment(BattleMove move) {
        return CURE_TARGET_AILMENT.contains(move.getName());
    }

    public static boolean powerDecreasesOnLowHp(BattleMove move) {
        return POWER_DECREASES_LOW_HP.contains(move.getName());
    }

    public static boolean powerIncreasesOnLowHp(BattleMove move) {
        return POWER_INCREASES_LOW_HP.contains(move.getName());
    }
}
