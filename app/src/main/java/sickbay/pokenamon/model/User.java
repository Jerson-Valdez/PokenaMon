package sickbay.pokenamon.model;

import sickbay.pokenamon.db.dto.PokemonDTO;

public class User {
    private String uid;
    private String username;
    private String email;
    private int shards;
    private int wins;
    private int pokemonCount;
    private int pokemonSold;
    private int streak;
    private int highestWin;
    private PokemonDTO lastBattledPokemon;
    private int earnedShardsBySelling;
    private int earnedShardsByBattling;

    public User(){}

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.shards = 1000;
        this.wins = 0;
        this.pokemonCount = 0;
        this.streak = 1;
        this.highestWin = 0;
        this.lastBattledPokemon = null;
    }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getShards() {
        return shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getPokemonCount() {
        return pokemonCount;
    }

    public void setPokemonCount(int pokemonCount) {
        this.pokemonCount = pokemonCount;
    }

    public int getPokemonSold() {
        return pokemonSold;
    }

    public void setPokemonSold(int pokemonSold) {
        this.pokemonSold = pokemonSold;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getHighestWin() {
        return highestWin;
    }

    public void setHighestWin(int highestWin) {
        this.highestWin = highestWin;
    }

    public PokemonDTO getLastBattledPokemon() { return lastBattledPokemon; }

    public void setLastBattledPokemon(PokemonDTO lastBattledPokemon) { this.lastBattledPokemon = lastBattledPokemon; }

    public int getEarnedShardsByBattling() {
        return earnedShardsByBattling;
    }

    public void setEarnedShardsByBattling(int earnedShardsByBattling) {
        this.earnedShardsByBattling = earnedShardsByBattling;
    }

    public int getEarnedShardsBySelling() {
        return earnedShardsBySelling;
    }

    public void setEarnedShardsBySelling(int earnedShardsBySelling) {
        this.earnedShardsBySelling = earnedShardsBySelling;
    }
}
