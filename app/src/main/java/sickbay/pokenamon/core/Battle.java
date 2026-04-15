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

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.util.Localizer;

public class Battle extends AppCompatActivity {
    LinearLayout noSelectedPokemonDisplay, selectedPokemonDisplay, selectPokemonButton, switchPokemonButton, battleButtons, battleButton, forfeitButton, continueButton;
    ImageView playerSprite;
    TextView initial, usernameField, dailyStreak, highestFloor, shardsEarned, playerName, playerHp, playerLevel;
    ProgressBar playerExpBar, playerHpBar;
    PokemonDTO selectedPokemon;

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

        BackgroundMusicManager.getInstance(this).play(R.raw.home_theme);

        hydrateViews();
    }

    private void init() {
        noSelectedPokemonDisplay = findViewById(R.id.noBattlePokemonSelectedDisplay);
        selectedPokemonDisplay = findViewById(R.id.selectedPokemonDisplay);
        selectPokemonButton = findViewById(R.id.selectPokemon);
        switchPokemonButton = findViewById(R.id.switchPokemon);
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
            startActivity(new Intent(this, BattleScene.class));
        });

        switchPokemonButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Collection.class));
        });

        playerSprite.setOnClickListener(v -> {
            Intent intent = new Intent(this, PokemonView.class);
            intent.putExtra("pokemon", selectedPokemon);
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
        if (UserManager.getInstance().getUser().getLastBattledPokemon() != null) {
            selectedPokemon = UserManager.getInstance().getUser().getLastBattledPokemon();
        } if (UserManager.getInstance().getSelectedPokemonForBattle() != null) {
            selectedPokemon = UserManager.getInstance().getSelectedPokemonForBattle();
        }

        if (selectedPokemon != null) {
            BattlePokemon battlePokemon = new BattlePokemon(selectedPokemon.toPokemon());

            noSelectedPokemonDisplay.setVisibility(LinearLayout.GONE);
            selectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);

            playerName.setText(Localizer.formatPokemonName(battlePokemon.getName()));
            playerLevel.setText(String.format("Lv. %02d", battlePokemon.getLevel()));

            playerHpBar.setMax(battlePokemon.getTotalHp());
            playerHpBar.setProgress(battlePokemon.getCurrentHp());

            playerHp.setText(String.format("%,d / %,d", battlePokemon.getCurrentHp(), battlePokemon.getTotalHp()));

            playerExpBar.setMax((battlePokemon.getLevel() > 1 ?  (int) Math.pow(battlePokemon.getLevel(), 3) : 9));
            playerExpBar.setProgress(battlePokemon.getExp());

            Glide.with(this)
                    .load(battlePokemon.getSprite().getFront())
                    .error(battlePokemon.getSprite().getFrontFallback())
                    .into(playerSprite);

            battleButtons.setVisibility(LinearLayout.VISIBLE);
        } else {
            noSelectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);
            selectedPokemonDisplay.setVisibility(LinearLayout.GONE);

            selectPokemonButton.setOnClickListener(v -> startActivity(new Intent(this, Collection.class)));

            battleButtons.setVisibility(LinearLayout.GONE);
        }
    }
}
