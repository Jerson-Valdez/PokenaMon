package sickbay.pokenamon.network;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.ArenaRegistry;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.model.enums.Ailment;
import sickbay.pokenamon.model.enums.DamageClass;
import sickbay.pokenamon.model.enums.TargetType;
import sickbay.pokenamon.model.enums.Type;
import sickbay.pokenamon.model.enums.VolatileAilment;
import sickbay.pokenamon.model.PokemonSprite;
import sickbay.pokenamon.model.PokemonStat;
import sickbay.pokenamon.model.StatBuff;
import sickbay.pokenamon.model.enums.StatId;
import sickbay.pokenamon.system.gacha.GetBattleMoveListener;
import sickbay.pokenamon.system.gacha.GetBattlePokemonListener;
import sickbay.pokenamon.system.gacha.GetGachaPokemonListener;
import sickbay.pokenamon.util.Localizer;

public class PokeAPIManager {
    private static final Random rand = new Random();
    private static final String API_URL = "https://pokeapi.co/api/v2/";
    private static final String POKEMON_ENDPOINT = "pokemon/";
    private static final String GROWTH_RATE_ENDPOINT = "growth-rate/";
    private static final String MOVE_ENDPOINT = "move/";
    private static final String MOVE_CATEGORY_ENDPOINT = "move-category/";
    private static final String MOVE_AILMENT_ENDPOINT = "move-ailment/";
    private static final String MOVE_DAMAGE_CLASS_ENDPOINT = "move-damage-class/";
    private static final String SHOWDOWN_SPRITES_URL = "https://play.pokemonshowdown.com/sprites/";
    private static final String SHOWDOWN_SPRITES_BACK_ENDPOINT = "ani-back/";
    private static final String SHOWDOWN_SPRITES_ENDPOINT = "ani/";

    private static final String SHOWDOWN_SPRITES_BACK_FALLBACK_ENDPOINT = "gen5ani-back/";
    private static final String SHOWDOWN_SPRITES_FALLBACK_ENDPOINT = "gen5ani/";

    private static final String SHOWDOWN_SPRITES_LAST_FALLBACK_ENDPOINT = "gen5/";
    private static final String SHOWDOWN_SPRITES_LAST_BACK_FALLBACK_ENDPOINT = "gen5-back/";

    static PokeAPIManager instance;
    static Context context;

    PokeAPIManager(Context context) {
        PokeAPIManager.context = context;
    }

    public static synchronized PokeAPIManager getInstance(Context context) {
        if (instance == null) {
            instance = new PokeAPIManager(context);
        }

        return instance;
    }

    public void getGachaPokemon(int pokemonId, GetGachaPokemonListener listener, int multiplier) throws RuntimeException {
        String query = buildEndpoint(Endpoint.POKEMON, String.valueOf(pokemonId));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query, null, (response) -> {
            try {
                int pokedexId = response.getInt("id");
                String name = response.getString("name");
                double weight = hectogramToKilogram(response.getInt("weight"));
                weight += weight * (rand.nextInt(101) > multiplier ? .02 : .05);
                double height = decimeterToMeter(response.getInt("height"));
                height += height * (rand.nextInt(101) > multiplier ? .02 : .05);
                String cry = response.getJSONObject("cries").getString("latest");

                JSONObject sprites = response.getJSONObject("sprites");

                PokemonSprite sprite = new PokemonSprite(
                        sprites.getString("front_default"),
                        sprites.getString("back_default")
                );

                HashMap<StatId, PokemonStat> statsMap = new HashMap<>();

                JSONArray types = response.getJSONArray("types");
                Type[] typeSet = new Type[types.length()];

                typeSet[0] = Type.valueOf(types.getJSONObject(0).getJSONObject("type").getString("name").toUpperCase());

                if (typeSet.length > 1) {
                    typeSet[1] = Type.valueOf(types.getJSONObject(1).getJSONObject("type").getString("name").toUpperCase());
                }

                JSONArray stats = response.getJSONArray("stats");

                int hpStat = stats.getJSONObject(0).getInt("base_stat");
                int atkStat = stats.getJSONObject(1).getInt("base_stat");
                int defStat = stats.getJSONObject(2).getInt("base_stat");
                int spAtkStat = stats.getJSONObject(3).getInt("base_stat");
                int spDefStat = stats.getJSONObject(4).getInt("base_stat");
                int spdStat = stats.getJSONObject(5).getInt("base_stat");

                statsMap.put(StatId.HP, new PokemonStat(StatId.HP, (int) (hpStat + (hpStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.ATTACK, new PokemonStat(StatId.ATTACK, (int) (atkStat + (atkStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.DEFENSE, new PokemonStat(StatId.DEFENSE, (int) (defStat + (defStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPECIAL_ATTACK, new PokemonStat(StatId.SPECIAL_ATTACK, (int) (spAtkStat + (spAtkStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPECIAL_DEFENSE, new PokemonStat(StatId.SPECIAL_DEFENSE, (int) (spDefStat + (spDefStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPEED, new PokemonStat(StatId.SPEED, (int) (spdStat + (spdStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));

                JSONArray moves = response.getJSONArray("moves");
                Set<String> chosenMoves = new HashSet<>();

                while (chosenMoves.size() < Math.min(moves.length() > 3 ? moves.length() : 4, 4)) {
                    String move = moves.getJSONObject(rand.nextInt(moves.length())).getJSONObject("move").getString("name");

                    if (ArenaRegistry.isBlacklisted(new BattleMove(move))) {
                        continue;
                    }

                    chosenMoves.add(move);
                }

                List<String> spriteUrls = new ArrayList<>();
                spriteUrls.add(buildEndpoint(Endpoint.FRONT, sanitizePokemonNameForShowdown(name)));
                spriteUrls.add(buildEndpoint(Endpoint.FRONT_FALLBACK, sanitizePokemonNameForShowdown(name)));
                spriteUrls.add(buildFallbackEndpoint(Endpoint.FRONT_FALLBACK, sanitizePokemonNameForShowdown(name)));

                double finalWeight = weight;
                double finalHeight = height;
                getFrontSpriteFallback(spriteUrls, 0, sprite, () -> {
                    Pokemon pokemon = new Pokemon(pokedexId, name, 1, 0, typeSet, sprite, cry, finalWeight, finalHeight, statsMap, chosenMoves.toArray(new String[0]));
                    listener.onComplete(pokemon);
                });

            } catch (JSONException e) {
                listener.onError(e.getMessage());
            }
        }, (error) -> listener.onError(error.getMessage()));

        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getGachaEnemyPokemon(int playerLevel, GetGachaPokemonListener listener, int multiplier) throws RuntimeException {
        int pokemonId = rand.nextInt(ArenaRegistry.POKEDEX_ENTRY_COUNT) + 1;
        int baseLevel = Math.max(1, playerLevel);
        int variance = rand.nextInt(6) < 3
                ? rand.nextInt(playerLevel < 16 ? 2 : 5) + 1
                : rand.nextInt(playerLevel < 16 ? 3 : 6) + 1;
        int level = Math.max(1, Math.min(100, baseLevel + (rand.nextBoolean() ? variance : -variance)));

        String query = buildEndpoint(Endpoint.POKEMON, String.valueOf(pokemonId));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query, null, (response) -> {
            try {
                int pokedexId = response.getInt("id");
                String name = response.getString("name");
                double weight = hectogramToKilogram(response.getInt("weight"));
                weight += weight * (rand.nextInt(101) > multiplier ? .02 : .05);
                double height = decimeterToMeter(response.getInt("height"));
                height += height * (rand.nextInt(101) > multiplier ? .02 : .05);
                String cry = response.getJSONObject("cries").getString("latest");

                JSONObject sprites = response.getJSONObject("sprites");

                PokemonSprite sprite = new PokemonSprite(
                        sprites.getString("front_default"),
                        sprites.getString("back_default")
                );

                HashMap<StatId, PokemonStat> statsMap = new HashMap<>();

                JSONArray types = response.getJSONArray("types");
                Type[] typeSet = new Type[types.length()];

                typeSet[0] = Type.valueOf(types.getJSONObject(0).getJSONObject("type").getString("name").toUpperCase());

                if (typeSet.length > 1) {
                    typeSet[1] = Type.valueOf(types.getJSONObject(1).getJSONObject("type").getString("name").toUpperCase());
                }

                JSONArray stats = response.getJSONArray("stats");

                int hpStat = stats.getJSONObject(0).getInt("base_stat");
                int atkStat = stats.getJSONObject(1).getInt("base_stat");
                int defStat = stats.getJSONObject(2).getInt("base_stat");
                int spAtkStat = stats.getJSONObject(3).getInt("base_stat");
                int spDefStat = stats.getJSONObject(4).getInt("base_stat");
                int spdStat = stats.getJSONObject(5).getInt("base_stat");

                statsMap.put(StatId.HP, new PokemonStat(StatId.HP, (int) (hpStat + (hpStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.ATTACK, new PokemonStat(StatId.ATTACK, (int) (atkStat + (atkStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.DEFENSE, new PokemonStat(StatId.DEFENSE, (int) (defStat + (defStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPECIAL_ATTACK, new PokemonStat(StatId.SPECIAL_ATTACK, (int) (spAtkStat + (spAtkStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPECIAL_DEFENSE, new PokemonStat(StatId.SPECIAL_DEFENSE, (int) (spDefStat + (spDefStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));
                statsMap.put(StatId.SPEED, new PokemonStat(StatId.SPEED, (int) (spdStat + (spdStat * (rand.nextInt(101) > multiplier ? -.02 : .04)))));


                JSONArray moves = response.getJSONArray("moves");
                Set<String> chosenMoves = new HashSet<>();

                while (chosenMoves.size() < Math.min(moves.length() > 3 ? moves.length() : 4, 4)) {
                    String move = moves.getJSONObject(rand.nextInt(moves.length())).getJSONObject("move").getString("name");

                    if (ArenaRegistry.isBlacklisted(new BattleMove(move))) {
                        continue;
                    }

                    chosenMoves.add(move);
                }

                List<String> spriteUrls = new ArrayList<>();
                spriteUrls.add(buildEndpoint(Endpoint.FRONT, sanitizePokemonNameForShowdown(name)));
                spriteUrls.add(buildEndpoint(Endpoint.FRONT_FALLBACK, sanitizePokemonNameForShowdown(name)));
                spriteUrls.add(buildFallbackEndpoint(Endpoint.FRONT_FALLBACK, sanitizePokemonNameForShowdown(name)));

                double finalWeight = weight;
                double finalHeight = height;
                getFrontSpriteFallback(spriteUrls, 0, sprite, () -> {
                    Pokemon pokemon = new Pokemon(pokedexId, name, level, 0, typeSet, sprite, cry, finalWeight, finalHeight, statsMap, chosenMoves.toArray(new String[0]));
                    listener.onComplete(pokemon);
                });


            } catch (JSONException e) {
                listener.onError(e.getMessage());
            }
        }, (error) -> listener.onError(error.getMessage()));

        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public void getPokemonDetails(BattlePokemon pokemon, GetBattlePokemonListener listener, boolean enemy) {
        BattleMove[] originalMoves = pokemon.getBattleMoves();
        List<BattleMove> detailedMoves = new ArrayList<>();

        final int[] movesLoaded = {0};

        for (BattleMove move : originalMoves) {
            Log.d("REQUEST_INITIAL", "Looping");
            getPokemonMove(move.getName(), new GetBattleMoveListener() {
                @Override
                public void onComplete(BattleMove hydratedMove) {
                    detailedMoves.add(hydratedMove);
                    movesLoaded[0]++;
                    Log.d("REQUEST_INITIAL", "Moved");

                    if (movesLoaded[0] == originalMoves.length) {
                        Log.d("REQUEST_INITIAL", "Finished");
                        pokemon.setBattleMoves(detailedMoves.toArray(new BattleMove[0]));
                        startSpriteLoading(pokemon, listener, enemy);
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e("MoveHydration", message);
                    movesLoaded[0]++;
                    if (movesLoaded[0] == originalMoves.length) {
                        startSpriteLoading(pokemon, listener, enemy);
                    }
                }
            });
        }
    }

    private void startSpriteLoading(BattlePokemon pokemon, GetBattlePokemonListener listener, boolean enemy) {
        List<String> spriteUrls = new ArrayList<>();

        if (enemy) {
            spriteUrls.add(buildEndpoint(Endpoint.FRONT, sanitizePokemonNameForShowdown(pokemon.getName())));
            spriteUrls.add(buildEndpoint(Endpoint.FRONT_FALLBACK, sanitizePokemonNameForShowdown(pokemon.getName())));
            spriteUrls.add(buildFallbackEndpoint(Endpoint.FRONT_FALLBACK, pokemon.getName()));
            getFrontSpriteFallback(spriteUrls, 0, pokemon.getSprite(), () -> {
                Log.d("REQUEST_INITIAL", "Complete");
                listener.onComplete(pokemon);
            });
        } else {
            spriteUrls.add(buildEndpoint(Endpoint.BACK, sanitizePokemonNameForShowdown(pokemon.getName())));
            spriteUrls.add(buildEndpoint(Endpoint.BACK_FALLBACK, sanitizePokemonNameForShowdown(pokemon.getName())));
            spriteUrls.add(buildFallbackEndpoint(Endpoint.BACK_FALLBACK, pokemon.getName()));
            getBackSpriteFallback(spriteUrls, 0, pokemon.getSprite(), () -> {
                Log.d("REQUEST_INITIAL", "Complete");
                listener.onComplete(pokemon);
            });
        }
    }

    private void getFrontSpriteFallback( List<String> urls, int index, PokemonSprite spriteObj, Runnable listener) {
        if (index >= urls.size()) { Log.d("REQUEST_INITIAL", "wow"); listener.run(); return; }

        String currentUrl = urls.get(index);

        Glide.with(context)
                .load(currentUrl)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        getFrontSpriteFallback(urls, (index < urls.size() ? index + 1 : index), spriteObj, listener);
                        Log.d("REQUEST_INITIAL", "Trying again");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        spriteObj.setFront(currentUrl);
                        listener.run();
                        Log.d("REQUEST_INITIAL", "Finally");
                        return false;
                    }
                })
                .preload();
    }

    private void getBackSpriteFallback( List<String> urls, int index, PokemonSprite spriteObj, Runnable listener) {
        if (index >= urls.size()) { Log.d("REQUEST_INITIAL", "wow"); listener.run(); return; }

        String currentUrl = urls.get(index);

        Glide.with(context)
                .load(currentUrl)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        getBackSpriteFallback(urls, (index < urls.size() ? index + 1 : index), spriteObj, listener);
                        Log.d("REQUEST_INITIAL", "Trying again");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        spriteObj.setBack(currentUrl);
                        listener.run();
                        Log.d("REQUEST_INITIAL", "Finally");
                        return false;
                    }
                })
                .preload();
    }

    public void getPokemonMove(String moveName, GetBattleMoveListener listener) throws RuntimeException {
        String query = buildEndpoint(Endpoint.MOVE, moveName);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query, null, (response) -> {
            try {
                Log.d("REQUEST_INITIAL", "Test");
                String name = response.getString("name");
                int accuracy = response.get("accuracy") instanceof Integer ? response.getInt("accuracy") : 0;
                DamageClass damageClass = DamageClass.valueOf(response.getJSONObject("damage_class").getString("name").toUpperCase());
                Type type = Type.valueOf(response.getJSONObject("type").getString("name").toUpperCase());
                int power = response.get("power") instanceof Integer ? response.getInt("power") : 0;
                int priority = response.getInt("priority");
                int pp = response.getInt("pp");
                TargetType targetType = TargetType.valueOf(Localizer.formatEnumString(response.getJSONObject("target").getString("name")));
                List<StatBuff> buffs = new ArrayList<>();

                Log.d("REQUEST_INITIAL", "Test1");

                JSONObject meta = null;

                try {
                    meta = response.getJSONObject("meta");
                } catch (JSONException e) {
                    Log.e("META", e.getMessage(), e);
                }

                JSONObject ailmentObj = meta != null ? meta.getJSONObject("ailment") : null;

                String ailmentName = Localizer.formatEnumString(ailmentObj != null ? ailmentObj.getString("name") : "none");
                int ailmentChance = meta != null ? meta.getInt("ailment_chance") : 0;
                int minimumTurns = meta != null && meta.get("min_turns") instanceof Integer ? meta.getInt("min_turns") : 0;
                int maximumTurns = meta != null && meta.get("max_turns") instanceof Integer ? meta.getInt("max_turns") : 0;
                int minimumHits = meta != null && meta.get("min_hits") instanceof Integer ? meta.getInt("min_hits") : 0;
                int maximumHits = meta != null && meta.get("max_hits") instanceof Integer ? meta.getInt("max_hits") : 0;
                int statChance = meta != null && meta.get("stat_chance") instanceof Integer ? meta.getInt("stat_chance") : 0;

                JSONArray statChanges = response.getJSONArray("stat_changes");

                for (int i = 0; i < statChanges.length(); i++) {
                    JSONObject stat = statChanges.getJSONObject(i);
                    String statName = Localizer.formatEnumString(stat.getJSONObject("stat").getString("name"));
                    int stages = stat.getInt("change");

                    buffs.add(new StatBuff(StatId.valueOf(statName), stages, statChance));
                }

                BattleMove battleMove = new BattleMove(name, damageClass, type, targetType, power, pp, priority, minimumTurns, maximumTurns, minimumHits, maximumHits, accuracy);
                battleMove.setAilmentChance(ailmentChance);

                try {
                    VolatileAilment.valueOf(ailmentName);
                    battleMove.setRawAilment(ailmentName);
                } catch (IllegalArgumentException e) {
                    Log.e("MOVE_FETCH", e.getMessage(), e);
                    battleMove.setAilment(Ailment.valueOf(ailmentName));
                }

                battleMove.setStatBuffs(buffs);

                listener.onComplete(battleMove);
            } catch (JSONException e) {
                listener.onError(e.getMessage());
                throw new RuntimeException(e);
            }
        }, (error) -> {
            listener.onError(error.getMessage());
            throw new RuntimeException(error);
        });

        RequestSingleton.getInstance(context).addToRequestQueue(request);
    }

    private String sanitizePokemonNameForShowdown(String pokemonName) {
        if (pokemonName.contains("mega")) {
            pokemonName = pokemonName.replace("mega-", "mega");
        }
        if (pokemonName.contains("mr-")) {
            pokemonName = pokemonName.replace("mr-", "mr.");
        }
        if (pokemonName.contains("-jr")) {
            pokemonName = pokemonName.replace("-", "");
        }
        if (pokemonName.contains("tapu")) {
            pokemonName = pokemonName.replace("-", "");
        }
        if (pokemonName.contains("incarnate")) {
            pokemonName = pokemonName.replace("-incarnate", "");
        }
        if (pokemonName.contains("male")) {
            pokemonName = pokemonName.replace("-male", "");
        }
        if (pokemonName.contains("female")) {
            pokemonName = pokemonName.replace("female", "f");
        }
        if (pokemonName.contains("family-of-three")) {
            pokemonName = pokemonName.replace("-family-of-three", "");
        }
        if (pokemonName.contains("family-of-four")) {
            pokemonName = pokemonName.replace("family-of-", "");
        }
        if (pokemonName.contains("solo")) {
            pokemonName = pokemonName.replace("-solo", "");
        }
        if (pokemonName.contains("zero")) {
            pokemonName = pokemonName.replace("-zero", "");
        }
        if (pokemonName.contains("ordinary")) {
            pokemonName = pokemonName.replace("-ordinary", "");
        }
        if (pokemonName.contains("-50")) {
            pokemonName = pokemonName.replace("-50", "");
        }
        if (pokemonName.contains("-curly")) {
            pokemonName = pokemonName.replace("-curly", "");
        }
        if (pokemonName.contains("-o")) {
            pokemonName = pokemonName.replace("-", "");
        }
        if (pokemonName.contains("average")) {
            pokemonName = pokemonName.replace("-average", "");
        }
        if (pokemonName.contains("red-meteor")) {
            pokemonName = pokemonName.replace("-red", "");
        }
        if (pokemonName.contains("blue-meteor") || pokemonName.contains("yellow-meteor") || pokemonName.contains("indigo-meteor") || pokemonName.contains("orange-meteor") || pokemonName.contains("violet-meteor") || pokemonName.contains("green-meteor")) {
            pokemonName = pokemonName.replace("-meteor", "");
        }

        return pokemonName;
    }

    private String buildEndpoint(Endpoint endpoint, String query) {
        String au = "";
        String ep = "";

        switch (endpoint) {
            case POKEMON:
                ep = POKEMON_ENDPOINT;
                break;
            case GROWTH_RATE:
                ep = GROWTH_RATE_ENDPOINT;
                break;
            case MOVE:
                ep = MOVE_ENDPOINT;
                break;
            case MOVE_CATEGORY:
                ep = MOVE_CATEGORY_ENDPOINT;
                break;
            case MOVE_AILMENT:
                ep = MOVE_AILMENT_ENDPOINT;
                break;
            case MOVE_DAMAGE_CLASS:
                ep = MOVE_DAMAGE_CLASS_ENDPOINT;
                break;
            case BACK:
                ep = SHOWDOWN_SPRITES_BACK_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
            case BACK_FALLBACK:
                ep = SHOWDOWN_SPRITES_BACK_FALLBACK_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
            case FRONT:
                ep = SHOWDOWN_SPRITES_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
            case FRONT_FALLBACK:
                ep = SHOWDOWN_SPRITES_FALLBACK_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
        }

        return (au.isEmpty() ? API_URL : au) + ep + query + (au.isEmpty() ? "" : ".gif");
    }

    private String buildFallbackEndpoint(Endpoint endpoint, String query) {
        String au = "";
        String ep = "";

        switch (endpoint) {
            case BACK_FALLBACK:
                ep = SHOWDOWN_SPRITES_LAST_BACK_FALLBACK_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
            case FRONT_FALLBACK:
                ep = SHOWDOWN_SPRITES_LAST_FALLBACK_ENDPOINT;
                au = SHOWDOWN_SPRITES_URL;
                break;
        }

        return au + ep + query + ".png";
    }

    private double hectogramToKilogram(int value) {
        return value * 0.1;
    }

    private double decimeterToMeter(int value) { return value * 0.1; }
}
