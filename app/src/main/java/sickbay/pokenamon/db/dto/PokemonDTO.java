package sickbay.pokenamon.db.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.system.arena.enums.Type;
import sickbay.pokenamon.system.arena.model.PokemonSprite;
import sickbay.pokenamon.system.arena.model.PokemonStat;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.util.Localizer;

public class PokemonDTO implements Parcelable {
    private String collectionId;
    private int pokedexId;
    private String name;
    private int rarity;
    private int level;
    private int exp;
    private double weight;
    private double height;
    private ArrayList<String> types;
    private PokemonSprite sprite;
    private String cry;
    private HashMap<String, Integer> stats;
    private ArrayList<String> moves;
    private long summonedAt;

    public PokemonDTO() {}

    public PokemonDTO(Parcel in) {
        collectionId = in.readString();
        pokedexId = in.readInt();
        name = in.readString();
        rarity = in.readInt();
        level = in.readInt();
        exp = in.readInt();
        weight = in.readDouble();
        height = in.readDouble();
        types = in.createStringArrayList();
        sprite = in.readParcelable(PokemonSprite.class.getClassLoader());
        cry = in.readString();
        stats = new HashMap<>();
        int statsSize = in.readInt();
        for (int i = 0; i < statsSize; i++) {
            String key = in.readString();
            int value  = in.readInt();
            stats.put(key, value);
        }
        moves = in.createStringArrayList();
        summonedAt = in.readLong();
    }

    public PokemonDTO(int pokedexId, String name, int rarity, int level, int exp, double weight, double height, ArrayList<String> types, PokemonSprite sprite, String cry, HashMap<String, Integer> stats, ArrayList<String> moves) {
        this.pokedexId = pokedexId;
        this.name = name;
        this.rarity = rarity;
        this.level = level;
        this.exp = exp;
        this.weight = weight;
        this.height = height;
        this.types = types;
        this.sprite = sprite;
        this.cry = cry;
        this.stats = stats;
        this.moves = moves;
    }

    public PokemonDTO( int pokedexId, String name, int rarity, int level, int exp, double weight, double height, ArrayList<String> types, PokemonSprite sprite, String cry, HashMap<String, Integer> stats, ArrayList<String> moves, long summonedAt) {
        this.pokedexId = pokedexId;
        this.name = name;
        this.rarity = rarity;
        this.level = level;
        this.exp = exp;
        this.weight = weight;
        this.height = height;
        this.types = types;
        this.sprite = sprite;
        this.cry = cry;
        this.stats = stats;
        this.moves = moves;
        this.summonedAt = summonedAt;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

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

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public PokemonSprite getSprite() {
        return sprite;
    }

    public void setSprite(PokemonSprite sprite) {
        this.sprite = sprite;
    }

    public String getCry() { return cry; }

    public void setCry(String cry) { this.cry = cry; }

    public HashMap<String, Integer> getStats() {
        return stats;
    }

    public void setStats(HashMap<String, Integer> stats) {
        this.stats = stats;
    }

    public ArrayList<String> getMoves() {
        return moves;
    }

    public void setMoves(ArrayList<String> moves) {
        this.moves = moves;
    }

    public long getSummonedAt() { return summonedAt; }

    public void setSummonedAt(long summonedAt) { this.summonedAt = summonedAt; }

    public Pokemon toPokemon() {
        Type[] types = this.types
                .stream().map(t -> Type.valueOf(Localizer.formatEnumString(t)))
                .toArray(Type[]::new);

        HashMap<StatId, PokemonStat> stats = new HashMap<>();
        for (Map.Entry<String, Integer> entry : this.stats.entrySet()) {
            StatId id = StatId.valueOf(entry.getKey());
            stats.put(id, new PokemonStat(id, entry.getValue()));
        }

        Pokemon pokemon = new Pokemon(pokedexId, name, rarity, level, exp, types, sprite, cry, weight, stats, moves.toArray(new String[0]));
        pokemon.setCollectionId(collectionId);

        return pokemon;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(collectionId);
        dest.writeInt(pokedexId);
        dest.writeString(name);
        dest.writeInt(rarity);
        dest.writeInt(level);
        dest.writeInt(exp);
        dest.writeDouble(weight);
        dest.writeDouble(height);
        dest.writeStringList(types);
        dest.writeParcelable(sprite, flags);
        dest.writeString(cry);
        dest.writeInt(stats.size());
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue());
        }
        dest.writeStringList(moves);
        dest.writeLong(summonedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PokemonDTO> CREATOR = new Creator<>() {
        @Override
        public PokemonDTO createFromParcel(Parcel in) {
            return new PokemonDTO(in);
        }

        @Override
        public PokemonDTO[] newArray(int size) {
            return new PokemonDTO[size];
        }
    };
}
