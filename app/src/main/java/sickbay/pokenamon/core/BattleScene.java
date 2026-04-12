package sickbay.pokenamon.core;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.ArenaEngine;
import sickbay.pokenamon.system.arena.AttackAction;
import sickbay.pokenamon.system.arena.BattleLogger;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.system.arena.enums.Ailment;
import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.arena.events.BattlePokemonListener;
import sickbay.pokenamon.system.arena.events.DamageEffectListener;
import sickbay.pokenamon.system.arena.events.MoveUseListener;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
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
    private TextView battleLog;
    private TableLayout movePanel;
    private boolean battleOngoing;
    private BattleLogger battleLogger;
    private int floor = 1;
    private int totalShardsEarned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_battle);

        BackgroundMusicManager.getInstance(this).play(R.raw.battle_theme);

        playerPokemon = new BattlePokemon(UserManager.getInstance().getSelectedPokemonForBattle().toPokemon());

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

        battleLog = findViewById(R.id.battleLog);
        movePanel = findViewById(R.id.movePanel);

        battleLogger = new BattleLogger(battleLog) {
            @Override
            public void onLogging() {
                toggleMoves(false);
                movePanel.setVisibility(TableLayout.GONE);
                battleLog.setVisibility(TextView.VISIBLE);
                battleLogger.setIsBattleOngoing(battleOngoing);
            }

            @Override
            public void onLogFinish() {
                movePanel.setVisibility(TableLayout.VISIBLE);
                battleLog.setVisibility(TextView.GONE);
                toggleMoves(true);

                if (ArenaEngine.currentAction != null) {
                    ArenaEngine.currentAction.notifyFinish();
                    ArenaEngine.currentAction = null;
                }
            }
        };

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

    private void refreshHp() {
        ObjectAnimator.ofInt(enemyHpBar, "progress", enemyPokemon.getCurrentHp())
            .setDuration(300)
            .start();
        enemyHpBar.post(() -> updateHpBarTint(enemyHpBar, enemyPokemon.getCurrentHp(), enemyPokemon.getTotalHp()));

        refreshAilmentBadge(enemyAilment, enemyPokemon);

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    ObjectAnimator.ofInt(playerHpBar, "progress", playerPokemon.getCurrentHp())
                            .setDuration(300)
                            .start();
                    playerHpBar.post(() -> {
                        playerHp.setText(String.format("%,d / %,d", playerPokemon.getCurrentHp(), playerPokemon.getTotalHp()));
                        updateHpBarTint(playerHpBar, playerPokemon.getCurrentHp(), playerPokemon.getTotalHp());
                    });
                    refreshAilmentBadge(playerAilment, playerPokemon);
                }, 300
        );
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
            i++;
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

    private void takeAction(BattleMove move) {
        BattleMove[] enemyMoves = enemyPokemon.getBattleMoves();
        BattleMove enemyMove = enemyMoves[rand.nextInt(enemyMoves.length)];

        if (battleLogger.getIsFinished()) {
            ArenaEngine.commence(
                    new AttackAction(playerPokemon, move),
                    new AttackAction(enemyPokemon, enemyMove),
                    () -> {
                        refreshHp();
                        refreshMovesButton(moveMap);
                    }
            );
        }
    }

    private void battle() {
        attachListeners(playerPokemon);
        attachListeners(enemyPokemon);

        refreshHp();
        bindPlayerMovesToButtons(moveMap);
    }

    private void conclude(BattlePokemon pokemon) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (pokemon.getCollectionId() != null) {
                BackgroundMusicManager.getInstance(BattleScene.this).play(R.raw.lose);
                new AlertDialog.Builder(BattleScene.this)
                        .setTitle("You Lost...")
                        .setMessage(String.format("You reached the Floor %,02d before you and your Pokemon fell into defeat.. You have earned %,d shards in this battle.", floor, totalShardsEarned))
                        .setNegativeButton("Return Home", (dialog, which) -> {
                            UserManager.getInstance().updateUserLastBattledPokemon(playerPokemon.toPokemonDTO());
                            finish();
                            overridePendingTransition(0,0);
                        })
                        .show();
                return;
            } else {
                BackgroundMusicManager.getInstance(BattleScene.this).play(R.raw.win);
                int shardsEarned = UserManager.getInstance().valuatePokemon(pokemon.toPokemonDTO());
                totalShardsEarned += shardsEarned;
                int exp = ArenaEngine.gainExp(playerPokemon, pokemon);
                int levelsGained = (int) (
                        (exp / (playerPokemon.getLevel() == 1 ? 9 : Math.pow(playerPokemon.getLevel(), 3)))  >= exp ?
                                (exp / (playerPokemon.getLevel() == 1 ? 9 : Math.pow(playerPokemon.getLevel(), 3))) + 1 :
                                (exp / (playerPokemon.getLevel() == 1 ? 9 : Math.pow(playerPokemon.getLevel(), 3))));

                playerPokemon.setExp(exp);
                playerPokemon.setLevel(playerPokemon.getLevel() + levelsGained);

                UserManager.getInstance().updateShards(shardsEarned);
                UserManager.getInstance().updateUserEarnedShardsByBattling(shardsEarned);
                UserManager.getInstance().updateHighestFloorWin(floor);



                DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid()).child(playerPokemon.getCollectionId()).child("exp").setValue(exp);
                DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid()).child(playerPokemon.getCollectionId()).child("level").setValue(levelsGained);

                new AlertDialog.Builder(BattleScene.this)
                        .setTitle("You won!")
                        .setMessage(String.format("Onwards to Floor %,02d! Your battle in this current floor earned you %,d shards!", floor + 1, shardsEarned))
                        .setNegativeButton("Return Home", (dialog, which) -> {
                            UserManager.getInstance().updateUserLastBattledPokemon(playerPokemon.toPokemonDTO());
                            finish();
                            overridePendingTransition(0,0);
                        }).show();
            }
        }, 3000);
    }

    private void attachListeners(BattlePokemon pokemon) {
        pokemon.setMoveUseListener(new MoveUseListener() {
            @Override
            public void onFail(BattleMove move) {
                battleLogger.displayBattleLog("But it failed!");
            }

            @Override
            public void onMiss(BattleMove move) {
                battleLogger.displayBattleLog("But it missed...");
            }

            @Override
            public void onUse(BattleMove move) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " used " + Localizer.formatPokemonMove(move.getName()));
            }

            @Override
            public void onHit(BattleMove move) {}

            @Override
            public void onPpOut(BattleMove move) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has no more PP!");
            }

            @Override
            public void onMultipleHits(BattleMove move, int hits) {
                battleLogger.displayBattleLog("Hit (x" + hits + ") times!");
            }
        });
        pokemon.setDamageEffectListener(new DamageEffectListener() {
            @Override
            public void onCriticalDamage() {
                battleLogger.displayBattleLog("It was a critical hit!");
            }

            @Override
            public void onEffective() {
                battleLogger.displayBattleLog("It was super effective!");
            }

            @Override
            public void onResist() {
                battleLogger.displayBattleLog("It was not very effective...");
            }

            @Override
            public void onImmune() {
                battleLogger.displayBattleLog("There was no effect.");
            }
        });
        pokemon.setBattlePokemonListener(new BattlePokemonListener() {
            @Override
            public void onFaint(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " fainted.");
                battleLog.postDelayed(() -> refreshHp(), 300);

                conclude(pokemon);
            }

            @Override
            public void onBurn(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been hurt by its burn!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onPoison(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been hurt by its poison!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onFreeze(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is frozen solid!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onThaw(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been thawed!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onSleep(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is fast sleep!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onRest(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has fallen asleep!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onWakeUp(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " woke up!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onParalyze(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been paralyzed and may be unable to move!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }


            @Override
            public void onParalyzeSuccess(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is paralyzed and unable to move!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onConfuse(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is confused!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onConfusion(BattlePokemon pokemon) {
                battleLogger.displayBattleLog("It hurt itself in its confusion.");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onConfusionSnap(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has gotten out of its confusion!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onFlinch(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " flinched!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onHeal(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onDrain(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onDisabled(BattleMove move) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has been disabled!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onTorment(BattleMove move) {
                battleLogger.displayBattleLog("You cannot use the same move twice in a row!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onYawn(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " is about to fall asleep...");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onSilence(BattleMove move) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has been silenced!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onRecoil(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has been damaged by the recoil!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onTarShot(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has been weakened to fire!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onPerishSong(BattlePokemon pokemon, int turns) {
                battleLogger.displayBattleLog("All Pokemon who hears the song will faint in " + turns + "!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onStatChange(BattlePokemon pokemon, StatId statId, int stage) {
                battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + "'s " + Localizer.formatEnum(statId.name()) + " has been raised!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onInflict(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.Ailment ailment) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onCure(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.Ailment ailment) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()) + "!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onNightmare(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is having nightmares!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onIngrain(BattlePokemon pokemon) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has planted its roots!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onVolatileInflict(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.VolatileAilment ailment) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }

            @Override
            public void onVolatileCure(BattlePokemon pokemon, sickbay.pokenamon.system.arena.model.VolatileAilment ailment) {
                battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!");
                battleLog.postDelayed(() -> refreshHp(), 300);
            }
        });
    }
}