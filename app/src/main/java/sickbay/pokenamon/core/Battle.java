package sickbay.pokenamon.core;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.Random;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.system.gacha.BackgroundMusicManager;
import sickbay.pokenamon.system.gacha.GetBattlePokemonListener;
import sickbay.pokenamon.system.gacha.GetGachaPokemonListener;
import sickbay.pokenamon.util.Localizer;

public class Battle extends AppCompatActivity {
    ImageView playerSprite, enemySprite;
    TextView playerName, enemyName, playerLevel, enemyLevel, playerHp, enemyHp, move1, move2, move3, move4, move1pp, move2pp, move3pp, move4pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_battle);

        playerSprite = findViewById(R.id.playerPokemon);
        enemySprite = findViewById(R.id.enemyPokemon);
        playerName = findViewById(R.id.playerPokemonName);
        enemyName = findViewById(R.id.enemyPokemonName);
        enemyLevel = findViewById(R.id.enemyPokemonLevel);
        playerLevel = findViewById(R.id.playerPokemonLevel);
        move1 = findViewById(R.id.move1Name);
        move2 = findViewById(R.id.move2Name);
        move3 = findViewById(R.id.move3Name);
        move4 = findViewById(R.id.move4Name);
        move1pp = findViewById(R.id.move1Pp);
        move2pp = findViewById(R.id.move2Pp);
        move3pp = findViewById(R.id.move3Pp);
        move4pp = findViewById(R.id.move4Pp);

        BackgroundMusicManager.getInstance(this).play(R.raw.battle_theme);

        PokemonDTO pokemonDTO = getIntent().getParcelableExtra("pokemon");

        PokeAPIManager.getInstance(this).getPokemonDetails(new BattlePokemon(pokemonDTO.toPokemon()), new GetBattlePokemonListener() {
            @Override
            public void onComplete(BattlePokemon pokemon) {
                Log.d("TRY", pokemon.getBattleMoves().toString() + " " + Arrays.toString(pokemon.getBattleMoves()) + " " + String.valueOf(pokemon.getSprite()));

                Glide.with(Battle.this)
                        .asGif()
                        .load(pokemon.getSprite().getBack())
                        .into(playerSprite);

                playerName.setText(Localizer.formatPokemonName(pokemon.getName()));
                playerLevel.setText("Lv. " + pokemon.getLevel());
                ;

                BattleMove[] moves = pokemon.getBattleMoves();
                if (moves.length > 0) move1.setText(Localizer.toTitleCase(moves[0].getName()));
                if (moves.length > 1) move2.setText(Localizer.toTitleCase(moves[1].getName()));
                if (moves.length > 2) move3.setText(Localizer.toTitleCase(moves[2].getName()));
                if (moves.length > 3) move4.setText(Localizer.toTitleCase(moves[3].getName()));
            }

            @Override
            public void onError(String message) {
                Log.e("BattlePokemonHydration", message);
            }
        }, false);

        engageEnemy();
    }

    private void engageEnemy() {
        PokeAPIManager.getInstance(this).getGachaPokemon(new Random().nextInt(1025) + 1,
                new GetGachaPokemonListener() {
                    @Override
                    public void onComplete(Pokemon pokemon) {
                        BattlePokemon enemyPokemon = new BattlePokemon(pokemon);
                        enemyPokemon.setLevel(new Random().nextInt(99) + 1);

                        PokeAPIManager.getInstance(Battle.this).getPokemonDetails(enemyPokemon, new GetBattlePokemonListener() {
                            @Override
                            public void onComplete(BattlePokemon pokemon) {
                                Log.d("POKEMON NULL", String.valueOf(pokemon == null));

                                Glide.with(Battle.this)
                                        .asGif()
                                        .load(pokemon.getSprite().getFront())
                                        .error(pokemon.getSprite().getFrontFallback())
                                        .into(enemySprite);

                                enemyName.setText(Localizer.formatPokemonName(pokemon.getName()));
                                enemyLevel.setText("Lv. " + pokemon.getLevel());
                            }

                            @Override
                            public void onError(String message) {
                                Log.e("BattlePokemonHydration", message);
                            }
                        }, true);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("EnemyCreation", message);
                    }
                }
        );
    }
}
