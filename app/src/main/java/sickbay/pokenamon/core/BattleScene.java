package sickbay.pokenamon.core;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.ArenaEngine;
import sickbay.pokenamon.system.arena.AttackAction;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.system.arena.enums.Ailment;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.arena.events.BattlePokemonListener;
import sickbay.pokenamon.system.arena.events.DamageEffectListener;
import sickbay.pokenamon.system.arena.events.MoveUseListener;
import sickbay.pokenamon.system.gacha.BackgroundMusicManager;
import sickbay.pokenamon.system.gacha.GetBattlePokemonListener;
import sickbay.pokenamon.system.gacha.GetGachaPokemonListener;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.util.Localizer;

public class BattleScene extends AppCompatActivity {
    private static final Random rand = new Random();
    private ImageView playerSprite, enemySprite;
    private ProgressBar playerHpBar, enemyHpBar;
    private TextView playerHp;
    private TextView playerName, playerLevel, enemyName, enemyLevel;
    private LinearLayout move1, move2, move3, move4;
    private TextView move1Name, move1Pp, move2Name, move2Pp, move3Name, move3Pp, move4Name, move4Pp;
    private TextView move1Type, move2Type, move3Type, move4Type;
    private TextView playerAilment, enemyAilment;
    private BattlePokemon playerPokemon, enemyPokemon;
    private Map<LinearLayout, List<TextView>> moveMap;
    private LinearLayout dock;
    private TextView battleLog;
    private TableLayout movePanel;
    private boolean battleOngoing;

    private final List<String> logQueue = new ArrayList<>();
    private boolean isDisplayingLog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_battle);

        BackgroundMusicManager.getInstance(this).play(R.raw.battle_theme);

        playerPokemon = new BattlePokemon(((PokemonDTO) Objects.requireNonNull(getIntent().getExtras()).getParcelable("player")).toPokemon());
        UserManager.getInstance().getUser().setLastBattledPokemon(playerPokemon);

        init();
        hydrate();
    }

    private void init() {
        playerSprite = findViewById(R.id.playerPokemonSprite);
        enemySprite = findViewById(R.id.enemyPokemonSprite);
        playerHpBar = findViewById(R.id.playerPokemonHpBar);
        enemyHpBar = findViewById(R.id.enemyPokemonHpBar);
        playerHp = findViewById(R.id.playerPokemonHp);
        playerName = findViewById(R.id.playerPokemonName);
        playerLevel = findViewById(R.id.playerPokemonLevel);
        enemyName = findViewById(R.id.enemyPokemonName);
        enemyLevel = findViewById(R.id.enemyPokemonLevel);
        move1 = findViewById(R.id.move1);
        move1Name = findViewById(R.id.move1Name);
        move1Pp = findViewById(R.id.move1Pp);
        move1Type = findViewById(R.id.move1Type);
        move2 = findViewById(R.id.move2);
        move2Name = findViewById(R.id.move2Name);
        move2Pp = findViewById(R.id.move2Pp);
        move2Type = findViewById(R.id.move2Type);
        move3 = findViewById(R.id.move3);
        move3Name = findViewById(R.id.move3Name);
        move3Pp = findViewById(R.id.move3Pp);
        move3Type = findViewById(R.id.move3Type);
        move4 = findViewById(R.id.move4);
        move4Name = findViewById(R.id.move4Name);
        move4Pp = findViewById(R.id.move4Pp);
        move4Type = findViewById(R.id.move4Type);

        playerAilment = findViewById(R.id.playerPokemonAilment);
        enemyAilment = findViewById(R.id.enemyPokemonAilment);

        moveMap = new HashMap<>();
        moveMap.put(move1, List.of(move1Name, move1Pp, move1Type));
        moveMap.put(move2, List.of(move2Name, move2Pp, move2Type));
        moveMap.put(move3, List.of(move3Name, move3Pp, move3Type));
        moveMap.put(move4, List.of(move4Name, move4Pp, move4Type));

        dock = findViewById(R.id.dock);
        battleLog = findViewById(R.id.battleLog);
        movePanel = findViewById(R.id.movePanel);

        battleOngoing = true;
    }

    private void toggleMoves(boolean enabled) {
        move1.setEnabled(enabled);
        move2.setEnabled(enabled);
        move3.setEnabled(enabled);
        move4.setEnabled(enabled);
    }

    private void hydrate() {
        loadPlayerPokemon();
        generateEnemyPokemon(this::battle);
    }

    private void hydratePlayer() {
        Glide.with(this)
                .load(playerPokemon.getSprite().getBack())
                .error(playerPokemon.getSprite().getBackFallback())
                .into(playerSprite);

        playerHpBar.setMax(playerPokemon.getTotalHp());
        playerHpBar.setProgress(playerPokemon.getCurrentHp());

        playerHp.setText(String.format("%,d / %,d", playerPokemon.getCurrentHp(), playerPokemon.getTotalHp()));

        playerName.setText(Localizer.formatPokemonName(playerPokemon.getName()));
        playerLevel.setText(String.format("Lv. %02d", playerPokemon.getLevel()));
    }

    private void hydrateEnemy() {
        Glide.with(this)
                .load(enemyPokemon.getSprite().getFront())
                .error(enemyPokemon.getSprite().getFrontFallback())
                .into(enemySprite);

        enemyHpBar.setMax(enemyPokemon.getTotalHp());
        enemyHpBar.setProgress(enemyPokemon.getCurrentHp());

        enemyName.setText(Localizer.formatPokemonName(enemyPokemon.getName()));
        enemyLevel.setText(String.format("Lv. %02d", enemyPokemon.getLevel()));
    }

    private void loadPlayerPokemon() {
        PokeAPIManager.getInstance(getApplicationContext()).getPokemonDetails(playerPokemon, new GetBattlePokemonListener() {
            @Override
            public void onComplete(BattlePokemon pokemon) {
                playerPokemon = pokemon;
                hydratePlayer();
            }

            @Override
            public void onError(String message) {
                Log.e("BattleScenePlayerPopulate", message);
            }
        }, false);
    }

    private void generateEnemyPokemon(Runnable listener) {
        PokeAPIManager.getInstance(getApplicationContext()).getGachaEnemyPokemon(playerPokemon.getLevel(), new GetGachaPokemonListener() {
            @Override
            public void onComplete(Pokemon pokemon) {
                BattlePokemon enemy = new BattlePokemon(pokemon);

                PokeAPIManager.getInstance(getApplicationContext()).getPokemonDetails(enemy, new GetBattlePokemonListener() {
                    @Override
                    public void onComplete(BattlePokemon pokemon) {
                        enemyPokemon = pokemon;
                        hydrateEnemy();
                        listener.run();
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("BattleSceneEnemyPopulate", message);
                    }
                }, true);
            }

            @Override
            public void onError(String message) {
                Log.e("BattleSceneEnemyPopulate", message);
            }
        });
    }

    private void bindPlayerMovesToButtons(Map<LinearLayout, List<TextView>> moveMap) {
        BattleMove[] playerMoves = playerPokemon.getBattleMoves();

        int i = 0;
        for (Map.Entry<LinearLayout, List<TextView>> move : moveMap.entrySet()) {
            BattleMove playerMove = playerMoves[i];
            move.getValue().get(0).setText(Localizer.formatPokemonMove(playerMove.getName()));
            move.getValue().get(1).setText(String.format("%d/%d", playerMove.getCurrentPp(), playerMove.getTotalPp()));
            move.getValue().get(2).setText(Localizer.toTitleCase(playerMove.getType().toString()));
            PokemonListAdapter.setTypeStrokeColor(move.getValue().get(2), playerMove.getType().toString());

            int j = i;
            move.getKey().setOnClickListener(v -> {
                if (!battleOngoing) return;
                takeAction(playerMoves[j]);
            });

            i++;
        }
    }

    private void takeAction(BattleMove move) {
        toggleMoves(false);

        BattleMove[] enemyMoves = enemyPokemon.getBattleMoves();
        BattleMove enemyMove = enemyMoves[rand.nextInt(enemyMoves.length)];


        ArenaEngine.commence(
                new AttackAction(playerPokemon, move),
                new AttackAction(enemyPokemon, enemyMove),
                () -> {
                    refreshHp();
                    refreshMovesButton(moveMap);
                }
        );
    }

    private void refreshHp() {
        enemyHpBar.setProgress(enemyPokemon.getCurrentHp(), true);
        updateHpBarTint(enemyHpBar, enemyPokemon.getCurrentHp(), enemyPokemon.getTotalHp());
        refreshAilmentBadge(enemyAilment, enemyPokemon);

        playerHpBar.setProgress(playerPokemon.getCurrentHp(), true);
        playerHp.setText(String.format("%,d / %,d", playerPokemon.getCurrentHp(), playerPokemon.getTotalHp()));
        updateHpBarTint(playerHpBar, playerPokemon.getCurrentHp(), playerPokemon.getTotalHp());
        refreshAilmentBadge(playerAilment, playerPokemon);
    }

    private void updateHpBarTint(ProgressBar bar, int currentHp, int totalHp) {
        double percent = (double) totalHp / currentHp;

        int color = percent > 0.5 ? getResources().getColor(R.color.grass, null) : percent > 0.2 ? getResources().getColor(R.color.electric, null) : getResources().getColor(R.color.fighting, null);
        bar.getProgressDrawable().setTint(color);
    }

    private void refreshMovesButton(Map<LinearLayout, List<TextView>> moveMap) {
        BattleMove[] playerMoves = playerPokemon.getBattleMoves();

        int i = 0;
        for (Map.Entry<LinearLayout, List<TextView>> move : moveMap.entrySet()) {
            BattleMove playerMove = playerMoves[i];
            move.getValue().get(1).setText(String.format("%d/%d", playerMove.getCurrentPp(), playerMove.getTotalPp()));
            move.getKey().setEnabled(playerMove.getCurrentPp() > 0);
        }
    }

    private void refreshAilmentBadge(TextView ailmentView, BattlePokemon pokemon) {
        Ailment ailment = pokemon.getAilment() != null
                ? pokemon.getAilment().getType()
                : sickbay.pokenamon.system.arena.enums.Ailment.NONE;

        if (ailment != sickbay.pokenamon.system.arena.enums.Ailment.NONE) {
            ailmentView.setVisibility(TextView.VISIBLE);
            ailmentView.setText("[" + Localizer.toTitleCase(ailment.name()) + "]");
            ailmentView.setTextColor(ailmentColor(ailment));
        } else {
            ailmentView.setVisibility(TextView.GONE);
        }
    }

    private int ailmentColor(Ailment ailment) {
        switch (ailment) {
            case BURN:      return getResources().getColor(R.color.fire, null);
            case FREEZE:    return getResources().getColor(R.color.ice, null);
            case PARALYSIS: return getResources().getColor(R.color.electric, null);
            case POISON:    return getResources().getColor(R.color.poison, null);
            default:        return getResources().getColor(R.color.normal, null);
        }
    }

    private void displayBattleLog(String message) {
        logQueue.add(message);
        if (!isDisplayingLog) {
            showNextMessage();
        }
    }

    private void showNextMessage() {
        if (logQueue.isEmpty()) {
            isDisplayingLog = false;

            if (battleOngoing && !playerPokemon.isFainted() && !enemyPokemon.isFainted()) {
                movePanel.setVisibility(TableLayout.VISIBLE);
                battleLog.setVisibility(TextView.GONE);
                toggleMoves(true);
            }
            return;
        }

        isDisplayingLog = true;
        String text = logQueue.remove(0);

        movePanel.setVisibility(TableLayout.GONE);
        battleLog.setVisibility(TextView.VISIBLE);

        long totalWait = Math.min((text.length() * 45) + 1200, 2500);

        ValueAnimator animator = ValueAnimator.ofInt(0, text.length());
        animator.setDuration(totalWait);
        animator.addUpdateListener(animation -> {
            battleLog.setText(text.substring(0, (int) animation.getAnimatedValue()));
        });
        animator.start();


        dock.postDelayed(this::showNextMessage, totalWait + 500);
    }

    private void battle() {
        attachListeners(playerPokemon);
        attachListeners(enemyPokemon);

        refreshHp();
        bindPlayerMovesToButtons(moveMap);
    }

    private void attachListeners(BattlePokemon pokemon) {
        String pkmnName = Localizer.formatPokemonName(pokemon.getName());

        pokemon.setMoveUseListener(new MoveUseListener() {
            @Override
            public void onFail(BattleMove move) {
                displayBattleLog("But it failed!");
            }

            @Override
            public void onMiss(BattleMove move) {
                displayBattleLog("But it missed...");
            }

            @Override
            public void onUse(BattleMove move) {
                displayBattleLog(pkmnName + " used " + Localizer.formatPokemonMove(move.getName()));
            }

            @Override
            public void onHit(BattleMove move) {

            }

            @Override
            public void onPpOut(BattleMove move) {
                displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has no more PP!");
            }

            @Override
            public void onMultipleHits(BattleMove move, int hits) {
                displayBattleLog("Hit (x" + hits + ") times!");
            }
        });
        pokemon.setDamageEffectListener(new DamageEffectListener() {
            @Override
            public void onCriticalDamage() {
                displayBattleLog("It was a critical hit!");
            }

            @Override
            public void onEffective() {
                displayBattleLog("It was super effective!");
            }

            @Override
            public void onResist() {
                displayBattleLog("It was not very effective...");
            }

            @Override
            public void onImmune() {
                displayBattleLog("There was no effect.");
            }
        });
        pokemon.setBattlePokemonListener(new BattlePokemonListener() {
            @Override
            public void onFaint(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " fainted.");
                refreshHp();
            }

            @Override
            public void onBurn(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " was burned!");
                refreshHp();
            }

            @Override
            public void onPoison(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " was poisoned!");
                refreshHp();
            }

            @Override
            public void onFreeze(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " was frozen solid!");
                refreshHp();
            }


            @Override
            public void onThaw(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " was thawed!");
                refreshHp();
            }


            @Override
            public void onSleep(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " is fast sleep!");
                refreshHp();
            }


            @Override
            public void onRest(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " is resting!");
                refreshHp();
            }


            @Override
            public void onWakeUp(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " woke up!");
                refreshHp();
            }


            @Override
            public void onParalyze(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " has been paralyzed and may be unable to move!");
                refreshHp();
            }


            @Override
            public void onParalyzeSuccess(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " is paralyzed and unable to move!");
                refreshHp();
            }

            @Override
            public void onConfuse(BattlePokemon pokemon) {
                displayBattleLog(pokemon.getName() + " is confused!");
                refreshHp();
            }

            @Override
            public void onConfusion(BattlePokemon pokemon) {
                displayBattleLog("It hurt itself in its confusion.");
                refreshHp();
            }

            @Override
            public void onConfusionSnap(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has gotten out of its confusion!");
                refreshHp();
            }

            @Override
            public void onFlinch(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " flinched!");
                refreshHp();
            }

            @Override
            public void onHeal(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!");
                refreshHp();
            }

            @Override
            public void onDrain(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!");
                refreshHp();
            }

            @Override
            public void onDisabled(BattleMove move) {
                displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has been disabled!");
                refreshHp();
            }

            @Override
            public void onTorment(BattleMove move) {
                displayBattleLog("You cannot use the same move twice in a row!");
                refreshHp();
            }

            @Override
            public void onYawn(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " is about to fall asleep!");
                refreshHp();
            }

            @Override
            public void onSilence(BattleMove move) {
                displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has been silenced!");
                refreshHp();
            }

            @Override
            public void onRecoil(BattlePokemon pokemon) {
                displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has been damaged by the recoil!");
                refreshHp();
            }

            @Override
            public void onStatChange(BattlePokemon pokemon, StatId statId, int stage) {
                displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + "'s " + Localizer.formatEnum(statId.name()) + " has been raised!");
                refreshHp();
            }

            @Override
            public void onInflict(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.Ailment ailment) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()) + "!");
                refreshHp();
            }

            @Override
            public void onCure(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.Ailment ailment) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()) + "!");
                refreshHp();
            }

            @Override
            public void onVolatileInflict(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.VolatileAilment ailment) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()) + "!");
                refreshHp();
            }

            @Override
            public void onVolatileCure(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.VolatileAilment ailment) {
                displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()) + "!");
                refreshHp();
            }
        });
    }
}