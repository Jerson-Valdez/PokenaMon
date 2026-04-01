package sickbay.pokenamon.model;

public class User {
    public String username;
    public String email;
    public int coins;
    public int wins;
    public int pokemon_count;

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.coins = 1000;
        this.wins = 0;
        this.pokemon_count = 0;
    }
}
