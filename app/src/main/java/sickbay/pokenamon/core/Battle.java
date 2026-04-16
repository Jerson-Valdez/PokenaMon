package sickbay.pokenamon.core;

import android.content.Intent;
import android.content.res.ColorStateList;
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

import java.util.concurrent.TimeUnit;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.home.TimeManager;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.util.Localizer;

public class Battle extends AppCompatActivity {
    LinearLayout noSelectedPokemonDisplay, selectedPokemonDisplay, selectPokemonButton, switchPokemonButton, battleButton;
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

        Log.i("BATTLE_HEALTH", UserManager.getInstance().getSelectedPokemonForBattle().getCurrentHp() + " / " + UserManager.getInstance().getSelectedPokemonForBattle().getTotalHp());

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
        battleButton = findViewById(R.id.battleButton);

        battleButton.setOnClickListener(v -> startActivity(new Intent(this, BattleScene.class)));

        switchPokemonButton.setOnClickListener(v -> startActivity(new Intent(this, Collection.class)));

        playerSprite.setOnClickListener(v -> {
            Intent intent = new Intent(this, PokemonView.class);
            intent.putExtra("pokemon", UserManager.getInstance().getSelectedPokemonForBattle());
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
        PokemonDTO pokemon = UserManager.getInstance().getSelectedPokemonForBattle();

        if (pokemon != null) {
            if (pokemon.getCurrentHp() == 0 && pokemon.getFullHealthCooldown() == 0) {
                pokemon.setCurrentHp(pokemon.getTotalHp());
            }

            noSelectedPokemonDisplay.setVisibility(LinearLayout.GONE);
            selectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);

            playerName.setText(Localizer.formatPokemonName(pokemon.getName()));
            playerLevel.setText(String.format("Lv. %02d", pokemon.getLevel()));

            playerHpBar.setMax(pokemon.getTotalHp());
            playerHpBar.setProgress(pokemon.getCurrentHp());

            playerHp.setText(String.format("%,d / %,d", pokemon.getCurrentHp(), pokemon.getTotalHp()));
            updateHpBarTint(playerHpBar, pokemon.getCurrentHp(), pokemon.getTotalHp());

            playerExpBar.setMax((pokemon.getLevel() > 1 ?  (int) Math.pow(pokemon.getLevel(), 3) : 9));
            playerExpBar.setProgress(pokemon.getExp());

            Glide.with(this)
                    .load(pokemon.getSprite().getFront())
                    .error(pokemon.getSprite().getFrontFallback())
                    .into(playerSprite);

            long duration = pokemon.getFullHealthCooldown();
            long currentTime = TimeManager.getInstance(getApplicationContext()).getCurrentTimeInMs();

            if (duration != 0 && currentTime < duration) {
                battleButton.setVisibility(LinearLayout.GONE);
                battleButton.setEnabled(false);
                battleButton.getBackground().mutate().setTint(ResourcesCompat.getColor(getResources(), R.color.secondary, null));

                new CountDownTimer(duration - currentTime, 1000) {
                    @Override
                    public void onFinish() {
                        DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid()).child(pokemon.getCollectionId()).child("fullHealthCooldown").setValue(0);

                        UserManager.getInstance().updateBattlePokemon(pokemon, () -> {
                            pokemon.setCurrentHp(pokemon.getTotalHp());
                            pokemon.setFullHealthCooldown(0);

                            playerHp.setText(String.format("%,d / %,d", pokemon.getCurrentHp(), pokemon.getTotalHp()));

                            pokemon.setCurrentHp(pokemon.getTotalHp());
                            playerHpBar.setProgress(pokemon.getTotalHp(), true);
                            updateHpBarTint(playerHpBar, pokemon.getCurrentHp(), pokemon.getTotalHp());

                            ((TextView) battleButton.getChildAt(0)).setText("Battle!");
                            battleButton.setEnabled(true);
                            battleButton.getBackground().mutate().setTint(ResourcesCompat.getColor(getResources(), R.color.primary, null));
                        });
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                        battleButton.setVisibility(LinearLayout.VISIBLE);
                        long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                        long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                        String btnTimeLabel = days > 0 ? String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds) : String.format("%02dh %02dm %02ds", hours, minutes, seconds);

                        ((TextView) battleButton.getChildAt(0)).setText(btnTimeLabel);
                    }
                }.start();
            } else {
                pokemon.setCurrentHp(pokemon.getTotalHp());
                pokemon.setFullHealthCooldown(0);

                playerHp.setText(String.format("%,d / %,d", pokemon.getCurrentHp(), pokemon.getTotalHp()));

                pokemon.setCurrentHp(pokemon.getTotalHp());
                playerHpBar.setProgress(pokemon.getTotalHp(), true);
                updateHpBarTint(playerHpBar, pokemon.getCurrentHp(), pokemon.getTotalHp());

                ((TextView) battleButton.getChildAt(0)).setText("Battle!");
                battleButton.setEnabled(true);
                battleButton.getBackground().mutate().setTint(ResourcesCompat.getColor(getResources(), R.color.primary, null));
            }
        } else {
            noSelectedPokemonDisplay.setVisibility(LinearLayout.VISIBLE);
            selectedPokemonDisplay.setVisibility(LinearLayout.GONE);

            selectPokemonButton.setOnClickListener(v -> startActivity(new Intent(this, Collection.class)));
        }
    }

    private void updateHpBarTint(ProgressBar bar, int currentHp, int totalHp) {
        if (totalHp <= 0) return;

        double ratio = (double) currentHp / totalHp;

        int color;
        if (ratio > 0.5) {
            color = getResources().getColor(R.color.grass, null);
        } else if (ratio > 0.2) {
            color = getResources().getColor(R.color.electric, null);
        } else {
            color = getResources().getColor(R.color.fighting, null);
        }

        bar.setProgressTintList(ColorStateList.valueOf(color));
    }
}
