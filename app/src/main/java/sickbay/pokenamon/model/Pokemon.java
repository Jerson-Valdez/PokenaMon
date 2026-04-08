package sickbay.pokenamon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pokemon implements Serializable {
    private int id;
    private String name;
    private List<String> types;
    private int level;
    private int stars;
    private List<String> moves;
    private List<Integer> movePower;
    private String imageUrl;
    private String userId;
    private long captureAt;

    public Pokemon() {}

    public Pokemon(int id, String name, List<String> types, int stars, List<String> moves, List<Integer> movePower, String imageUrl, String userId, long captureAt) {
        this.id = id;
        this.name = name;
        this.types = types;
        this.stars = stars;
        this.moves = moves;
        this.movePower = movePower;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.level = 1;
        this.captureAt = captureAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public List<String> getMoves() { return moves; }
    public void setMoves(List<String> moves) { this.moves = moves; }

    public List<Integer> getMovePower() { return movePower; }
    public void setMovePower(List<Integer> movePower) { this.movePower = movePower; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return captureAt; }
    public void setTimestamp(long captureAt) { this.captureAt = captureAt; }
}