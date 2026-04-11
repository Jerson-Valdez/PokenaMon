package sickbay.pokenamon.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.util.Localizer;

public class Battle extends AppCompatActivity {
    LinearLayout noSelectedPokemonDisplay, selectedPokemonDisplay, selectPokemonButton, battleButtons, battleButton, forfeitButton, continueButton;
    ImageView playerSprite;
    TextView initial, usernameField, dailyStreak, highestFloor, shardsEarned, playerName, playerHp, playerLevel;
    ProgressBar playerExpBar, playerHpBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_battle_dashboard);

        BackgroundMusicManager.getInstance(this).play(R.raw.home_theme);

        init();
        hydrateViews();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hydrateViews();
    }

    private void init() {
        noSelectedPokemonDisplay = findViewById(R.id.noBattlePokemonSelectedDisplay);
        selectedPokemonDisplay = findViewById(R.id.selectedPokemonDisplay);
        selectPokemonButton = findViewById(R.id.selectPokemon);
        initial = findViewById(R.id.initial);
        usernameField = findViewById(R.id.userNameField);
        dailyStreak = findViewById(R.id.dailyStreak);
        highestFloor = findViewById(R.id.highestFloor);
        shardsEarned = findViewById(R.id.earnedShardsByBattling);
        playerSprite = findViewById(R.id.selectedPokemonSprite);
        playerName = findViewById(R.id.selectedPokemonName);
        playerLevel = findViewById(R.id.selectedPokemonLevel);
        playerHp = findViewById(R.id.selectedPokemonMaxHp);
        playerHpBar = findViewById(R.id.selectedPokemonMaxHpBar);
        playerExpBar = findViewById(R.id.selectedPokemonExpBar);
        battleButtons = findViewById(R.id.battleButtons);
        battleButton = findViewById(R.id.battleButton);
        forfeitButton = findViewById(R.id.forfeitButton);
        continueButton = findViewById(R.id.continueButton);

        battleButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BattleScene.class);
            intent.putExtra("player", UserManager.getInstance().getSelectedPokemonForBattle());
            startActivity(intent);
        });

        Navigation.setup(this);
    }

    private void hydrateViews() {
        initial.setText(UserManager.getInstance().getUser().getUsername().substring(0,1).toUpperCase());
        usernameField.setText(Localizer.toTitleCase(UserManager.getInstance().getUser().getUsername()));
        dailyStreak.setText(String.format("%,d", UserManager.getInstance().getUser().getStreak()));
        highestFloor.setText(String.format("%,d", UserManager.getInstance().getUser().getHighestWin()));
        shardsEarned.setText(String.format("%,d", UserManager.getInstance().getUser().getEarnedShardsByBattling()));

        hydrateSelectedPokemon();
    }

    private void hydrateSelectedPokemon() {
        if (UserManager.getInstance().getSelectedPokemonForBattle() != null) {
            BattlePokemon selectedPokemon = new BattlePokemon(UserManager.getInstance().getSelectedPokemonForBattle().toPokemon());

            noSelectedPokemonDisplay.setVisibility(LinearLayout.GONE);
            selectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);

            playerName.setText(Localizer.formatPokemonName(selectedPokemon.getName()));
            playerLevel.setText(String.format("Lv. %02d", selectedPokemon.getLevel()));

            playerHpBar.setMax(selectedPokemon.getTotalHp());
            playerHpBar.setProgress(selectedPokemon.getCurrentHp());

            playerHp.setText(String.format("%,d / %,d", selectedPokemon.getCurrentHp(), selectedPokemon.getTotalHp()));

            playerExpBar.setMax((selectedPokemon.getLevel() > 1 ? selectedPokemon.getLevel() ^ 3 : 9));
            playerExpBar.setProgress(selectedPokemon.getExp());

            Glide.with(this)
                    .load(selectedPokemon.getSprite().getFront())
                    .error(selectedPokemon.getSprite().getFrontFallback())
                    .into(playerSprite);

            battleButtons.setVisibility(LinearLayout.VISIBLE);
        } else {
            noSelectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);
            selectedPokemonDisplay.setVisibility(LinearLayout.GONE);

            selectPokemonButton.setOnClickListener(v -> {
                startActivity(new Intent(this, Collection.class));
            });

            battleButtons.setVisibility(LinearLayout.GONE);
        }
    }
}
