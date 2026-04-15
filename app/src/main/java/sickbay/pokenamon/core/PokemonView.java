package sickbay.pokenamon.core;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.system.home.TimeManager;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.model.enums.StatId;
import sickbay.pokenamon.system.gacha.GetBattleMoveListener;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;

public class PokemonView extends AppCompatActivity {
    private final String STAR = "★";
    private TextView backButton, sellButton, entry, name, rarity, level, exp, type1, type2, totalBaseStats, statHp, statAttack, statDefense, statSpAttack, statSpDefense, statSpeed, obtainedAt;
    private ProgressBar levelBar, hpBar, attackBar, defenseBar, spAttackBar, spDefenseBar, speedBar;
    private ImageView sprite;

    private TextView move1Name, move1Type, move1Pp, move1Class,  move2Name, move2Type, move2Pp, move2Class, move3Name, move3Type, move3Pp, move3Class, move4Name, move4Type, move4Pp, move4Class;
    private LinearLayout move1, move2, move3, move4, selectPokemon;
    private PokemonDTO pokemon;
    private List<BattleMove> queriedMoves;

    MediaPlayer cryPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_pokemon_view);

        pokemon = Objects.requireNonNull(getIntent().getExtras()).getParcelable("pokemon");
        queriedMoves = new ArrayList<>();

        init();
    }

    private void init() {
        backButton = findViewById(R.id.pokemon_view_backButton);
        sellButton = findViewById(R.id.pokemon_view_sellButton);
        entry = findViewById(R.id.pokemon_view_entry);
        name = findViewById(R.id.pokemon_view_name);
        rarity = findViewById(R.id.pokemon_view_rarity);
        level = findViewById(R.id.pokemon_view_level);
        exp = findViewById(R.id.pokemon_view_exp);
        type1 = findViewById(R.id.pokemon_view_type1);
        type2 = findViewById(R.id.pokemon_view_type2);
        sprite = findViewById(R.id.pokemon_view_sprite);
        totalBaseStats = findViewById(R.id.totalBaseStats);
        move1 = findViewById(R.id.pokemon_view_move1);
        move2 = findViewById(R.id.pokemon_view_move2);
        move3 = findViewById(R.id.pokemon_view_move3);
        move4 = findViewById(R.id.pokemon_view_move4);
        move1Name = findViewById(R.id.pokemon_view_move1Name);
        move2Name = findViewById(R.id.pokemon_view_move2Name);
        move3Name = findViewById(R.id.pokemon_view_move3Name);
        move4Name = findViewById(R.id.pokemon_view_move4Name);
        move1Type = findViewById(R.id.pokemon_view_move1Type);
        move2Type = findViewById(R.id.pokemon_view_move2Type);
        move3Type = findViewById(R.id.pokemon_view_move3Type);
        move4Type = findViewById(R.id.pokemon_view_move4Type);
        move1Pp = findViewById(R.id.pokemon_view_move1Pp);
        move2Pp = findViewById(R.id.pokemon_view_move2Pp);
        move3Pp = findViewById(R.id.pokemon_view_move3Pp);
        move4Pp = findViewById(R.id.pokemon_view_move4Pp);
        move1Class = findViewById(R.id.pokemon_view_move1Class);
        move2Class = findViewById(R.id.pokemon_view_move2Class);
        move3Class = findViewById(R.id.pokemon_view_move3Class);
        move4Class = findViewById(R.id.pokemon_view_move4Class);
        levelBar = findViewById(R.id.pokemon_view_expBar);
        hpBar = findViewById(R.id.pokemon_view_hpBar);
        attackBar = findViewById(R.id.pokemon_view_attackBar);
        defenseBar = findViewById(R.id.pokemon_view_defenseBar);
        spAttackBar = findViewById(R.id.pokemon_view_spAttackBar);
        spDefenseBar = findViewById(R.id.pokemon_view_spDefenseBar);
        speedBar = findViewById(R.id.pokemon_view_speedBar);
        statHp = findViewById(R.id.pokemon_view_hp);
        statAttack = findViewById(R.id.pokemon_view_attack);
        statDefense = findViewById(R.id.pokemon_view_defense);
        statSpAttack = findViewById(R.id.pokemon_view_spAttack);
        statSpDefense = findViewById(R.id.pokemon_view_spDefense);
        statSpeed = findViewById(R.id.pokemon_view_speed);
        selectPokemon = findViewById(R.id.pokemon_view_selectPokemon);
        cryPlayer = new MediaPlayer();
        cryPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        obtainedAt = findViewById(R.id.pokemon_view_summonedAt);

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        sellButton.setOnClickListener(v -> {
            UserManager.getInstance().sellPokemon(this, pokemon, () -> { finish(); overridePendingTransition(0, 0);});
        });

        long duration = pokemon.getFullHealthCooldown();
        long currentTime = TimeManager.getInstance(getApplicationContext()).getCurrentTimeInMs();

        if (duration != 0 && currentTime < duration) {
            selectPokemon.setVisibility(LinearLayout.GONE);
            selectPokemon.setEnabled(false);

            new CountDownTimer(duration - currentTime, 1000) {
                @Override
                public void onFinish() {
                    DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid()).child(pokemon.getCollectionId()).child("fullHealthCooldown").setValue(0);
                    UserManager.getInstance().getUser().getLastBattledPokemon().setFullHealthCooldown(0);

                    ((TextView) selectPokemon.getChildAt(0)).setText("Battle with " + Localizer.formatPokemonName(pokemon.getName()) + "!");
                    selectPokemon.setEnabled(true);
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    selectPokemon.setVisibility(LinearLayout.VISIBLE);

                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);

                    String btnTimeLabel = days > 0 ? String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds) : String.format("%02dh %02dm %02ds", hours, minutes, seconds);

                    ((TextView) selectPokemon.getChildAt(0)).setText(btnTimeLabel);
                }
            }.start();
        } else {
            selectPokemon.setVisibility(LinearLayout.VISIBLE);

            ((TextView) selectPokemon.getChildAt(0)).setText("Battle with " + Localizer.formatPokemonName(pokemon.getName()) + "!");
            selectPokemon.setEnabled(true);
        }

        selectPokemon.setOnClickListener(v -> {
            Log.i("VIEW_HEALTH", pokemon.getCurrentHp() + " / " + pokemon.getTotalHp());
            UserManager.getInstance().setSelectedPokemonForBattle(pokemon);

            Log.i("VIEW_HEALTH", pokemon.getCurrentHp() + " / " + pokemon.getTotalHp());

            startActivity(new Intent(this, Battle.class));
            finish();
            overridePendingTransition(0,0);
        });

        sprite.setOnClickListener(v -> {
            try {
                cryPlayer.reset();
                cryPlayer.setDataSource(pokemon.getCry());
                cryPlayer.prepare();
                cryPlayer.start();
            } catch (IOException e) {
                Log.e("PokemonViewCry", e.getMessage(), e);
            }
        });

        for (String stringMove: pokemon.getMoves()) {
            PokeAPIManager.getInstance(getApplicationContext()).getPokemonMove(stringMove, new GetBattleMoveListener() {
                @Override
                public void onComplete(BattleMove move) {
                    queriedMoves.add(move);
                    hydrateViews(pokemon, queriedMoves);
                }

                @Override
                public void onError(String message) {
                    Log.e("PokemonViewMoveHydration", message);
                }
            });
        }
    }

    private void hydrateViews(PokemonDTO pokemon, List<BattleMove> queriedMoves) {
        String pokedexId = String.format("#%04d", pokemon.getPokedexId());
        String pokemonName = Localizer.formatPokemonName(pokemon.getName());
        String pokemonLevel = String.format("Lv. %02d", pokemon.getLevel());
        String pokemonType1 = Localizer.toTitleCase(pokemon.getTypes().get(0));
        String pokemonType2 = null;
        long pokemonSummonedAt = pokemon.getSummonedAt();

        try {
            pokemonType2 = Localizer.toTitleCase(pokemon.getTypes().get(1));
        } catch (IndexOutOfBoundsException e) {
            type2.setVisibility(TextView.GONE);
        }

        int pokemonRarity = pokemon.getRarity();
        int pokemonExp = pokemon.getExp();
        int pokemonRequirementExp = pokemon.getLevel() > 1 ? (int) Math.pow(pokemon.getLevel(), 3) : 9;

        entry.setText(pokedexId);
        rarity.setText(STAR.repeat(pokemonRarity));
        name.setText(pokemonName);
        level.setText(pokemonLevel);
        exp.setText(Math.max(0, pokemonExp) + " / " + pokemonRequirementExp);
        type1.setText(pokemonType1);
        PokemonListAdapter.setTypeStrokeColor(type1, pokemonType1);

        Glide.with(this)
                .load(pokemon.getSprite().getFront())
                .error(pokemon.getSprite().getFrontFallback())
                .into(sprite);

        spriteScale(pokemon.getHeight());

        if (pokemonType2 != null) { type2.setText(pokemonType2); PokemonListAdapter.setTypeStrokeColor(type2, pokemonType2); }

        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");

        obtainedAt.setText("Obtained at: " + format.format(new Date(pokemonSummonedAt)));

        for (BattleMove move: queriedMoves) {
            int i = queriedMoves.indexOf(move);
            TextView currMoveName = i == 0 ? move1Name: i == 1 ? move2Name: i == 2 ? move3Name : move4Name;
            TextView currMovePp = i == 0 ? move1Pp: i == 1 ? move2Pp: i == 2 ? move3Pp : move4Pp;
            TextView currMoveType = i == 0 ? move1Type: i == 1 ? move2Type: i == 2 ? move3Type : move4Type;
            TextView currMoveClass = i == 0 ? move1Class: i == 1 ? move2Class: i == 2 ? move3Class : move4Class;

            currMoveName.setText(Localizer.formatPokemonMove(queriedMoves.get(i).getName()));
            currMovePp.setText(queriedMoves.get(i).getTotalPp() + " / " + queriedMoves.get(i).getTotalPp());
            currMoveType.setText(Localizer.toTitleCase(Localizer.formatEnumString(queriedMoves.get(i).getType().name())));
            currMoveClass.setText(Localizer.formatEnumString(queriedMoves.get(i).getDamageClass().name()));

            PokemonListAdapter.setTypeStrokeColor(currMoveType, Localizer.formatEnumString(queriedMoves.get(i).getType().name()));
            setMoveClassBackgroundColor(currMoveClass, Localizer.formatEnumString(queriedMoves.get(i).getDamageClass().name()));
        }

        levelBar.post(() -> {
            levelBar.setMax(pokemonRequirementExp);
            levelBar.setProgress(Math.max(pokemonExp, 0), true);
        });

        int totalStats = 0;

        for (Map.Entry<String, Integer> stat: pokemon.getStats().entrySet()) {
            StatId statId = StatId.valueOf(Localizer.formatEnumString(stat.getKey()));

            ProgressBar bar;
            TextView label;

            if (statId == StatId.HP) {
                bar = hpBar;
                label = statHp;
            } else if (statId == StatId.ATTACK) {
                bar = attackBar;
                label = statAttack;
            } else if (statId == StatId.DEFENSE) {
                bar = defenseBar;
                label = statDefense;
            } else if (statId == StatId.SPECIAL_ATTACK) {
                bar = spAttackBar;
                label = statSpAttack;
            } else if (statId == StatId.SPECIAL_DEFENSE) {
                bar = spDefenseBar;
                label = statSpDefense;
            } else {
                bar = speedBar;
                label = statSpeed;
            }

            totalStats+=stat.getValue();
            bar.post(() -> {bar.setMax(255); bar.setProgress(stat.getValue(), true);});
            label.setText(String.valueOf(stat.getValue()));
        }

        totalBaseStats.setText(String.format("(%,d)", totalStats));
        ((TextView) selectPokemon.getChildAt(0)).setText("Battle with " + Localizer.formatPokemonName(pokemonName));
    }

    private void setMoveClassBackgroundColor(TextView view, String moveClass) {
        if (moveClass.equalsIgnoreCase("physical")) {
            view.setBackgroundColor(getResources().getColor(R.color.fire, null));
        } else if (moveClass.equalsIgnoreCase("special")) {
            view.setBackgroundColor(getResources().getColor(R.color.dragon, null));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.normal, null));
        }
    }

    private void spriteScale(double speciesHeightInMeters) {
        int basePadding = 10;
        int dynamicPadding;

        if (speciesHeightInMeters < 1.0) {
            dynamicPadding = 100;
        } else if (speciesHeightInMeters < 3.0) {
            dynamicPadding = 40;
        } else {
            dynamicPadding = basePadding;
        }

        sprite.setPadding(dynamicPadding, dynamicPadding, dynamicPadding, dynamicPadding);
        sprite.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}