package sickbay.pokenamon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.enums.Type;
import sickbay.pokenamon.model.enums.StatId;

public class Pokemon {
    private String collectionId;
    private int pokedexId;
    private String name;
    private int rarity;
    private int level;
    private int exp;
    private Type[] types;
    private PokemonSprite sprite;
    private String cry;
    private double weight;
    private double height;
    private HashMap<StatId, PokemonStat> stats;
    private String[] moves;
    private long summonedAt;

    public Pokemon(int pokedexId, String name, int level, int exp, Type[] types, PokemonSprite sprite, String cry, double weight, double height, HashMap<StatId, PokemonStat> stats, String[] moves) {
        this.pokedexId = pokedexId;
        this.name = name;
        this.level = level;
        this.exp = exp;
        this.types = types;
        this.sprite = sprite;
        this.cry = cry;
        this.weight = weight;
        this.height = height;
        this.stats = stats;
        this.moves = moves;
    }


    public Pokemon(String collectionId, int pokedexId, String name, int rarity, int level, int exp, Type[] types, PokemonSprite sprite, String cry, double weight, double height, HashMap<StatId, PokemonStat> stats, String[] moves, long summonedAt) {
        this.collectionId = collectionId;
        this.pokedexId = pokedexId;
        this.name = name;
        this.rarity = rarity;
        this.level = level;
        this.exp = exp;
        this.types = types;
        this.sprite = sprite;
        this.cry = cry;
        this.weight = weight;
        this.height = height;
        this.stats = stats;
        this.moves = moves;
        this.summonedAt = summonedAt;
    }

    public String getCollectionId() { return collectionId; }

    public void setCollectionId(String collectionId) { this.collectionId = collectionId; }

    public int getPokedexId() {
        return pokedexId;
    }

    public void setPokedexId(int pokedexId) {
        this.pokedexId = pokedexId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public Type[] getTypes() {
        return types;
    }

    public void setTypes(Type[] types) {
        this.types = types;
    }

    public PokemonSprite getSprite() {
        return sprite;
    }

    public void setSprite(PokemonSprite sprite) {
        this.sprite = sprite;
    }

    public String getCry() {
        return cry;
    }

    public void setCry(String cry) {
        this.cry = cry;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


    public double getHeight() {
        return height;
    }


    public void setHeight(double height) {
        this.height = height;
    }

    public HashMap<StatId, PokemonStat> getStats() {
        return stats;
    }

    public void setStats(HashMap<StatId, PokemonStat> stats) {
        this.stats = stats;
    }

    public String[] getMoves() {
        return moves;
    }

    public void setMoves(String[] moves) {
        this.moves = moves;
    }

    public long getSummonedAt() { return summonedAt; }

    public void setSummonedAt(long summonedAt) { this.summonedAt = summonedAt; }

    public PokemonDTO toPokemonDTO() {
        ArrayList<String> types = Arrays.stream(this.types).map(t -> t.name().toLowerCase()).collect(Collectors.toCollection(ArrayList::new));

        HashMap<String, Integer> stats = new HashMap<>();
        for (Map.Entry<StatId, PokemonStat> stat: this.stats.entrySet()) {
            stats.put(stat.getKey().name(), stat.getValue().getBattleStat());
        }

        PokemonDTO dto = new PokemonDTO(pokedexId, name, rarity, level, exp, weight, height, types, sprite, cry, stats, new ArrayList<>(Arrays.asList(moves)), summonedAt);
        dto.setCollectionId(getCollectionId());

        return dto;
    }
}
