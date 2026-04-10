package sickbay.pokenamon.core;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.gacha.GetBattleMoveListener;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;

public class PokemonView extends AppCompatActivity {
    private final String STAR = "★";
    private TextView backButton, sellButton, entry, name, rarity, level, exp, type1, type2, move1Name, move1Pp, move2Name, move2Pp, move3Name, move3Pp, move4Name, move4Pp, statHp, statAttack, statDefense, statSpAttack, statSpDefense, statSpeed;
    private ProgressBar levelBar, hpBar, attackBar, defenseBar, spAttackBar, spDefenseBar, speedBar;
    private ImageView sprite, move1Type, move2Type, move3Type, move4Type;
    private LinearLayout move1, move2, move3, move4;
    private boolean disableReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_pokemon_view);

        PokemonDTO pokemon = Objects.requireNonNull(getIntent().getExtras()).getParcelable("pokemon");

        List<BattleMove> queriedMoves = new ArrayList<>();

        init();

        backButton.setOnClickListener(v -> {
            finish();
        });

        sellButton.setOnClickListener(v -> {
            UserManager.getInstance().sellPokemon(this, pokemon, this::finish);
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
                    Log.e("PokemonView", message);
                }
            });
        }
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
        move1 = findViewById(R.id.pokemon_view_move1);
        move2 = findViewById(R.id.pokemon_view_move2);
        move3 = findViewById(R.id.pokemon_view_move3);
        move4 = findViewById(R.id.pokemon_view_move4);
        move1Name = findViewById(R.id.pokemon_view_move1Name);
        move2Name = findViewById(R.id.pokemon_view_move2Name);
        move3Name = findViewById(R.id.pokemon_view_move3Name);
        move4Name = findViewById(R.id.pokemon_view_move4Name);
        move1Pp = findViewById(R.id.pokemon_view_move1Pp);
        move2Pp = findViewById(R.id.pokemon_view_move2Pp);
        move3Pp = findViewById(R.id.pokemon_view_move3Pp);
        move4Pp = findViewById(R.id.pokemon_view_move4Pp);
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
    }

    private void hydrateViews(PokemonDTO pokemon, List<BattleMove> queriedMoves) {
        String pokedexId = String.format("#%04d", pokemon.getPokedexId());
        String pokemonName = Localizer.formatPokemonName(pokemon.getName());
        String pokemonLevel = String.valueOf(pokemon.getLevel());
        String pokemonType1 = Localizer.toTitleCase(pokemon.getTypes().get(0));
        String pokemonType2 = null;

        try {
            pokemonType2 = Localizer.toTitleCase(pokemon.getTypes().get(1));
        } catch (IndexOutOfBoundsException e) {
            type2.setVisibility(TextView.GONE);
        }

        int pokemonRarity = pokemon.getRarity();
        int pokemonExp = pokemon.getExp();
        int pokemonRequirementExp = pokemon.getLevel() > 1 ? pokemonExp^3 : 9;

        entry.setText(pokedexId);
        rarity.setText(STAR.repeat(pokemonRarity));
        name.setText(pokemonName);
        level.setText("Lv. " + pokemonLevel);
        exp.setText(pokemonExp + " / " + pokemonRequirementExp);
        type1.setText(pokemonType1);
        PokemonListAdapter.setTypeStrokeColor(type1, pokemonType1);

        Glide.with(this)
                .load(pokemon.getSprite().getFrontFallback())
                .into(sprite);

        if (pokemonType2 != null) { type2.setText(pokemonType2); PokemonListAdapter.setTypeStrokeColor(type2, pokemonType2); }


        for (BattleMove move: queriedMoves) {
            int i = queriedMoves.indexOf(move);
            TextView currMoveName = i == 0 ? move1Name: i == 1 ? move2Name: i == 2 ? move3Name : move4Name;
            TextView currMovePp = i == 0 ? move1Pp: i == 1 ? move2Pp: i == 2 ? move3Pp : move4Pp;

            currMoveName.setText(Localizer.formatPokemonMove(queriedMoves.get(i).getName()));
            currMovePp.setText(String.valueOf(queriedMoves.get(i).getTotalPp()));
        }

        levelBar.post(() -> {
            levelBar.setMax(pokemonRequirementExp);
            levelBar.setProgress(pokemonExp / pokemonRequirementExp * 100, true);
        });

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

            bar.post(() -> {bar.setMax(255); bar.setProgress(stat.getValue(), true);});
            label.setText(String.valueOf(stat.getValue()));
        }
    }
}