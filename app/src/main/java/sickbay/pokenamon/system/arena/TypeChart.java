package sickbay.pokenamon.system.arena;

import java.util.HashMap;
import java.util.Map;

import sickbay.pokenamon.system.arena.enums.Type;

public class TypeChart {
    private static final Map<Type, Map<Type, Double>> chart = new HashMap<>();

    static {
        set(Type.NORMAL,   Type.ROCK, 0.5);
        set(Type.NORMAL,   Type.GHOST, 0.0);
        set(Type.NORMAL,   Type.STEEL, 0.5);

        set(Type.FIRE,     Type.FIRE, 0.5);
        set(Type.FIRE,     Type.WATER, 0.5);
        set(Type.FIRE,     Type.GRASS, 2.0);
        set(Type.FIRE,     Type.ICE, 2.0);
        set(Type.FIRE,     Type.BUG, 2.0);
        set(Type.FIRE,     Type.ROCK, 0.5);
        set(Type.FIRE,     Type.DRAGON, 0.5);
        set(Type.FIRE,     Type.STEEL, 2.0);

        set(Type.WATER,    Type.FIRE, 2.0);
        set(Type.WATER,    Type.WATER, 0.5);
        set(Type.WATER,    Type.GRASS, 0.5);
        set(Type.WATER,    Type.GROUND, 2.0);
        set(Type.WATER,    Type.ROCK, 2.0);
        set(Type.WATER,    Type.DRAGON, 0.5);

        set(Type.ELECTRIC, Type.WATER, 2.0);
        set(Type.ELECTRIC, Type.ELECTRIC, 0.5);
        set(Type.ELECTRIC, Type.GRASS, 0.5);
        set(Type.ELECTRIC, Type.GROUND, 0.0);
        set(Type.ELECTRIC, Type.FLYING, 2.0);
        set(Type.ELECTRIC, Type.DRAGON, 0.5);

        set(Type.GRASS,    Type.FIRE, 0.5);
        set(Type.GRASS,    Type.WATER, 2.0);
        set(Type.GRASS,    Type.GRASS, 0.5);
        set(Type.GRASS,    Type.POISON, 0.5);
        set(Type.GRASS,    Type.GROUND, 2.0);
        set(Type.GRASS,    Type.FLYING, 0.5);
        set(Type.GRASS,    Type.BUG, 0.5);
        set(Type.GRASS,    Type.ROCK, 2.0);
        set(Type.GRASS,    Type.DRAGON, 0.5);
        set(Type.GRASS,    Type.STEEL, 0.5);

        set(Type.ICE,      Type.FIRE, 0.5);
        set(Type.ICE,      Type.WATER, 0.5);
        set(Type.ICE,      Type.GRASS, 2.0);
        set(Type.ICE,      Type.ICE, 0.5);
        set(Type.ICE,      Type.GROUND, 2.0);
        set(Type.ICE,      Type.FLYING, 2.0);
        set(Type.ICE,      Type.DRAGON, 2.0);
        set(Type.ICE,      Type.STEEL, 0.5);

        set(Type.FIGHTING, Type.NORMAL, 2.0);
        set(Type.FIGHTING, Type.ICE, 2.0);
        set(Type.FIGHTING, Type.POISON, 0.5);
        set(Type.FIGHTING, Type.FLYING, 0.5);
        set(Type.FIGHTING, Type.PSYCHIC, 0.5);
        set(Type.FIGHTING, Type.BUG, 0.5);
        set(Type.FIGHTING, Type.ROCK, 2.0);
        set(Type.FIGHTING, Type.GHOST, 0.0);
        set(Type.FIGHTING, Type.DARK, 2.0);
        set(Type.FIGHTING, Type.STEEL, 2.0);
        set(Type.FIGHTING, Type.FAIRY, 0.5);

        set(Type.POISON,   Type.GRASS, 2.0);
        set(Type.POISON,   Type.POISON, 0.5);
        set(Type.POISON,   Type.GROUND, 0.5);
        set(Type.POISON,   Type.ROCK, 0.5);
        set(Type.POISON,   Type.GHOST, 0.5);
        set(Type.POISON,   Type.STEEL, 0.0);
        set(Type.POISON,   Type.FAIRY, 2.0);

        set(Type.GROUND,   Type.FIRE, 2.0);
        set(Type.GROUND,   Type.ELECTRIC, 2.0);
        set(Type.GROUND,   Type.GRASS, 0.5);
        set(Type.GROUND,   Type.POISON, 2.0);
        set(Type.GROUND,   Type.FLYING, 0.0);
        set(Type.GROUND,   Type.BUG, 0.5);
        set(Type.GROUND,   Type.ROCK, 2.0);
        set(Type.GROUND,   Type.STEEL, 2.0);

        set(Type.FLYING,   Type.ELECTRIC, 0.5);
        set(Type.FLYING,   Type.GRASS, 2.0);
        set(Type.FLYING,   Type.FIGHTING, 2.0);
        set(Type.FLYING,   Type.BUG, 2.0);
        set(Type.FLYING,   Type.ROCK, 0.5);
        set(Type.FLYING,   Type.STEEL, 0.5);

        set(Type.PSYCHIC,  Type.FIGHTING, 2.0);
        set(Type.PSYCHIC,  Type.POISON, 2.0);
        set(Type.PSYCHIC,  Type.PSYCHIC, 0.5);
        set(Type.PSYCHIC,  Type.DARK, 0.0);
        set(Type.PSYCHIC,  Type.STEEL, 0.5);

        set(Type.BUG,      Type.FIRE, 0.5);
        set(Type.BUG,      Type.GRASS, 2.0);
        set(Type.BUG,      Type.FIGHTING, 0.5);
        set(Type.BUG,      Type.FLYING, 0.5);
        set(Type.BUG,      Type.PSYCHIC, 2.0);
        set(Type.BUG,      Type.GHOST, 0.5);
        set(Type.BUG,      Type.DARK, 2.0);
        set(Type.BUG,      Type.STEEL, 0.5);
        set(Type.BUG,      Type.FAIRY, 0.5);

        set(Type.ROCK,     Type.FIRE, 2.0);
        set(Type.ROCK,     Type.ICE, 2.0);
        set(Type.ROCK,     Type.FIGHTING, 0.5);
        set(Type.ROCK,     Type.GROUND, 0.5);
        set(Type.ROCK,     Type.FLYING, 2.0);
        set(Type.ROCK,     Type.BUG, 2.0);
        set(Type.ROCK,     Type.STEEL, 0.5);

        set(Type.GHOST,    Type.NORMAL, 0.0);
        set(Type.GHOST,    Type.PSYCHIC, 2.0);
        set(Type.GHOST,    Type.GHOST, 2.0);
        set(Type.GHOST,    Type.DARK, 0.5);

        set(Type.DRAGON,   Type.DRAGON, 2.0);
        set(Type.DRAGON,   Type.STEEL, 0.5);
        set(Type.DRAGON,   Type.FAIRY, 0.0);

        set(Type.DARK,     Type.FIGHTING, 0.5);
        set(Type.DARK,     Type.PSYCHIC, 2.0);
        set(Type.DARK,     Type.GHOST, 2.0);
        set(Type.DARK,     Type.DARK, 0.5);
        set(Type.DARK,     Type.FAIRY, 0.5);

        set(Type.STEEL,    Type.FIRE, 0.5);
        set(Type.STEEL,    Type.WATER, 0.5);
        set(Type.STEEL,    Type.ELECTRIC, 0.5);
        set(Type.STEEL,    Type.ICE, 2.0);
        set(Type.STEEL,    Type.ROCK, 2.0);
        set(Type.STEEL,    Type.STEEL, 0.5);
        set(Type.STEEL,    Type.FAIRY, 2.0);

        set(Type.FAIRY,    Type.FIRE, 0.5);
        set(Type.FAIRY,    Type.FIGHTING, 2.0);
        set(Type.FAIRY,    Type.POISON, 0.5);
        set(Type.FAIRY,    Type.DRAGON, 2.0);
        set(Type.FAIRY,    Type.DARK, 2.0);
        set(Type.FAIRY,    Type.STEEL, 0.5);
    }

    private static void set(Type atk, Type def, double multiplier) {
        chart.computeIfAbsent(atk, k -> new HashMap<>()).put(def, multiplier);
    }

    public static double getEffectiveness(Type moveType, Type[] defenderTypes) {
        double multiplier = 1.0;
        for (Type defType : defenderTypes) {
            multiplier *= chart.get(moveType).getOrDefault(defType, 1.0);
        }
        return multiplier;
    }
}