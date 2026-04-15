package sickbay.pokenamon.core;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import sickbay.pokenamon.R;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.model.VolatileAilment;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.arena.ArenaEngine;
import sickbay.pokenamon.system.arena.AttackAction;
import sickbay.pokenamon.system.arena.BattleLogger;
import sickbay.pokenamon.system.arena.BattleMove;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.model.enums.Ailment;
import sickbay.pokenamon.model.enums.StatId;
import sickbay.pokenamon.system.arena.events.BattlePokemonListener;
import sickbay.pokenamon.system.arena.events.DamageEffectListener;
import sickbay.pokenamon.system.arena.events.MoveUseListener;
import sickbay.pokenamon.system.arena.states.BattleState;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.gacha.GetBattlePokemonListener;
import sickbay.pokenamon.system.gacha.GetGachaPokemonListener;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.system.home.TimeManager;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.util.Localizer;

public class BattleScene extends AppCompatActivity {
    private static final Random rand = new Random();

    private LinearLayout quitButton;
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
    private BattleLogger battleLogger;
    private int floor = 1;
    private int totalShardsEarned = 0;
    private boolean battleOngoing;
    private BattleState currentState = BattleState.IDLE;

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
        quitButton = findViewById(R.id.quitButton);
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

        moveMap = new LinkedHashMap<>();
        moveMap.put(move1, List.of(move1Name, move1Pp, move1Type));
        moveMap.put(move2, List.of(move2Name, move2Pp, move2Type));
        moveMap.put(move3, List.of(move3Name, move3Pp, move3Type));
        moveMap.put(move4, List.of(move4Name, move4Pp, move4Type));

        battleLog = findViewById(R.id.battleLog);
        movePanel = findViewById(R.id.movePanel);

        quitButton.setOnClickListener(v -> {
            conclude(playerPokemon);
        });

        battleLogger = new BattleLogger(battleLog) {
            @Override
            public void onLogging() {
                movePanel.setVisibility(TableLayout.GONE);
                battleLog.setVisibility(TextView.VISIBLE);
                battleLogger.setIsBattleOngoing(battleOngoing);
            }

            @Override
            public void onLogFinish() {
                if (ArenaEngine.currentAction != null) {
                    ArenaEngine.currentAction.notifyFinish();
                }

                new Handler(Looper.getMainLooper()).post(() -> setState(BattleState.CHECK_FAINT));
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

        spriteScale(playerSprite, playerPokemon.getHeight());

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

        spriteScale(enemySprite, enemyPokemon.getHeight());

        Log.i("ENEMY", enemyPokemon.getCurrentHp() + " / " + enemyPokemon.getTotalHp());

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
        int random = (int) (0.1 + (101 - 0.1) * rand.nextDouble());
        int multiplier = random <= .5 ? 60 : random <= 8 ? 35: random <= 30 ? 20 : 10;

        PokeAPIManager.getInstance(getApplicationContext()).getGachaEnemyPokemon(playerPokemon.getLevel(), new GetGachaPokemonListener() {
            @Override
            public void onComplete(Pokemon pokemon) {
                BattlePokemon enemy = new BattlePokemon(pokemon);

                Log.i("ENEMY", enemy.getCurrentHp() + " / " + enemy.getTotalHp());

                enemy.setTotalHp(enemy.getTotalHp());
                enemy.setCurrentHp(enemy.getTotalHp());

                Log.i("ENEMY", enemy.getCurrentHp() + " / " + enemy.getTotalHp());


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
        }, multiplier);
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
                takeAction(playerMoves[j]);
            });

            i++;
        }
    }

    private void refreshHp(Runnable listener) {
        ObjectAnimator.ofInt(enemyHpBar, "progress", enemyPokemon.getCurrentHp())
            .setDuration(600)
            .start();
        enemyHpBar.post(() -> {
            updateHpBarTint(enemyHpBar, enemyPokemon.getCurrentHp(), enemyPokemon.getTotalHp());
            refreshAilmentBadge(enemyAilment, enemyPokemon);
            ObjectAnimator.ofInt(playerHpBar, "progress", playerPokemon.getCurrentHp())
                    .setDuration(600)
                    .start();
            playerHpBar.post(() -> {
                playerHp.setText(String.format("%,d / %,d", playerPokemon.getCurrentHp(), playerPokemon.getTotalHp()));
                updateHpBarTint(playerHpBar, playerPokemon.getCurrentHp(), playerPokemon.getTotalHp());
                refreshAilmentBadge(playerAilment, playerPokemon);

                if (listener != null) {
                    listener.run();
                }
            });
        });
    }

    private void updateHpBarTint(ProgressBar bar, int currentHp, int totalHp) {
        double percent = (double) totalHp / currentHp * 100;

        int color = percent > 0.5 ? getResources().getColor(R.color.grass, null) : percent > 0.2 ? getResources().getColor(R.color.electric, null) : getResources().getColor(R.color.fighting, null);
        bar.setProgressTintList(ColorStateList.valueOf(color));
    }

    private void refreshMovesButton(Map<LinearLayout, List<TextView>> moveMap) {
        BattleMove[] playerMoves = playerPokemon.getBattleMoves();
        boolean lockedIn = playerPokemon.isSuspended()
                || playerPokemon.isCharging()
                || playerPokemon.isLockedIn();
        BattleMove lastMove = playerPokemon.getLastMoveUsed();

        int i = 0;
        for (Map.Entry<LinearLayout, List<TextView>> entry : moveMap.entrySet()) {
            BattleMove playerMove = playerMoves[i];

            if (lockedIn && lastMove != null) {
                boolean isLockedMove = playerMove.getName().equalsIgnoreCase(lastMove.getName());
                entry.getKey().setVisibility(isLockedMove ? LinearLayout.VISIBLE : LinearLayout.INVISIBLE);
            } else {
                entry.getKey().setVisibility(LinearLayout.VISIBLE);
            }

            entry.getValue().get(1).setText(String.format("%d/%d",
                    playerMove.getCurrentPp(), playerMove.getTotalPp()));
            i++;
        }
    }

    private void refreshAilmentBadge(TextView ailmentView, BattlePokemon pokemon) {
        Ailment ailment = pokemon.getAilment() != null
                ? pokemon.getAilment().getType()
                : Ailment.NONE;

        if (ailment != Ailment.NONE) {
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
        if (!battleLogger.getIsFinished() || currentState != BattleState.IDLE) return;

        BattleMove playerMove = (playerPokemon.isSuspended()
                || playerPokemon.isCharging()
                || playerPokemon.isLockedIn())
                ? playerPokemon.getLastMoveUsed()
                : move;

        BattleMove[] enemyMoves = enemyPokemon.getBattleMoves();
        BattleMove enemyMove = (enemyPokemon.isSuspended()
                || enemyPokemon.getTurns() > 0) && enemyPokemon.getLastMoveUsed() != null
                ? enemyPokemon.getLastMoveUsed()
                : enemyMoves[rand.nextInt(enemyMoves.length)];

        ArenaEngine.prepareTurn(
                new AttackAction(playerPokemon, playerMove),
                new AttackAction(enemyPokemon, enemyMove)
        );

        setState(BattleState.PREPARE_TURN);
    }

    private void battle() {
        attachListeners(playerPokemon);
        attachListeners(enemyPokemon);

        refreshHp(null);
        bindPlayerMovesToButtons(moveMap);
    }

    private void conclude(BattlePokemon pokemon) {
        if (!UserManager.getInstance().isHasBattledToday()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date lastLogin = format.parse(UserManager.getInstance().getUser().getLastLogin());
                Date today = format.parse(format.format(new Date()));

                if (lastLogin == null || TimeUnit.DAYS.convert(today.getTime() - lastLogin.getTime(), TimeUnit.MILLISECONDS) == 1) {
                    UserManager.getInstance().updateStreak(1, format.format(today));
                } else {
                    UserManager.getInstance().updateStreak(-(UserManager.getInstance().getUser().getStreak() - 1), format.format(today));
                }
            } catch (ParseException e) {
                Log.e("Streak", e.getMessage());
            }
        }


        if (pokemon.getCollectionId() != null) {
            BackgroundMusicManager.getInstance(BattleScene.this.peekAvailableContext()).play(R.raw.lose);

            long startTime = TimeManager.getInstance(getApplicationContext()).getCurrentTimeInMs();
            long fullHealthCooldown = ArenaEngine.generateRestoreCooldown(startTime, pokemon);

            playerPokemon.setFullHealthCooldown(fullHealthCooldown);

            new AlertDialog.Builder(BattleScene.this)
                    .setTitle("You Have Been Defeated")
                    .setMessage(String.format("You've reached Floor %,02d before you and your Pokemon fell into defeat.. You have earned %,d shards in total.", floor, totalShardsEarned))
                    .setNegativeButton("Return Home", (dialog, which) -> {
                        UserManager.getInstance().updateBattlePokemon(playerPokemon.toPokemonDTO(), () -> {
                            finish();
                            overridePendingTransition(0,0);
                        });
                    })
                    .setCancelable(false)
                    .show();
        } else {
            BackgroundMusicManager.getInstance(BattleScene.this).play(R.raw.win);

            int shardsEarned = UserManager.getInstance().valuatePokemon(pokemon.toPokemonDTO());
            totalShardsEarned += shardsEarned;
            int expGained = ArenaEngine.gainExp(playerPokemon);
            double expRatio =  expGained / (playerPokemon.getLevel() == 1 ? 9 : Math.pow(playerPokemon.getLevel(), 3));
            int levelsGained = (int) Math.min(1, Math.floor(expRatio));


            playerPokemon.setExp(playerPokemon.getExp() + expGained);
            playerPokemon.setLevel(playerPokemon.getLevel() + levelsGained);

            UserManager.getInstance().updateShards(shardsEarned);
            UserManager.getInstance().updateUserEarnedShardsByBattling(shardsEarned);
            UserManager.getInstance().updateHighestFloorWin(floor);

            boolean levelUp = false;
            while (true) {
                int currLevel = playerPokemon.getLevel();
                double expThreshold = currLevel == 1 ? 9 : Math.pow(currLevel, 3);

                if (playerPokemon.getExp() > expThreshold) {
                    playerPokemon.setLevel(currLevel + 1);

                    playerPokemon.setExp(playerPokemon.getExp() - (int) expThreshold);
                    levelUp = true;

                    new AlertDialog.Builder(BattleScene.this)
                            .setTitle("Level Up!")
                            .setMessage(String.format("%s has leveled up to %d!", Localizer.formatPokemonName(playerPokemon.getName()), playerPokemon.getLevel()))
                            .setPositiveButton("Continue", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true)
                            .show();
                } else {
                    break;
                }
            }

            if (levelUp) {
                playerPokemon.setCurrentHp(playerPokemon.getTotalHp());

                new AlertDialog.Builder(BattleScene.this)
                        .setTitle("Level Up!")
                        .setMessage(String.format("%s has restored all its health!", Localizer.formatPokemonName(playerPokemon.getName())))
                        .setPositiveButton("Continue", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }

            new AlertDialog.Builder(BattleScene.this)
                    .setTitle(String.format("Onwards to Floor %,02d!", floor + 1))
                    .setMessage(String.format(" Your battle in this current floor earned you %,d shards!", shardsEarned))
                    .setNegativeButton("Return Home", (dialog, which) -> {
                        UserManager.getInstance().updateBattlePokemon(playerPokemon.toPokemonDTO(), () -> {
                            Log.i("UPDATE_HEALTH", playerPokemon.getCurrentHp() + " / " + playerPokemon.getTotalHp());
                            dialog.dismiss();
                            finish();
                            overridePendingTransition(0,0);
                        });
                    })
                    .setPositiveButton("Continue", (dialog, which) -> {
                        UserManager.getInstance().updateBattlePokemon(playerPokemon.toPokemonDTO(), () -> {
                            floor += 1;

                            setState(BattleState.IDLE);
                            generateEnemyPokemon(() -> {
                                loadPlayerPokemon();
                                refreshHp(this::battle);
                                BackgroundMusicManager.getInstance(BattleScene.this).play(R.raw.battle_theme);
                            });
                            dialog.dismiss();
                        });
                    })
                    .setCancelable(false)
                    .show();
        }
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
                refreshHp(() -> {
                    battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " fainted.");
                });
            }

            @Override
            public void onBurn(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been hurt by its burn!"));
            }

            @Override
            public void onPoison(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been hurt by its poison!"));
            }

            @Override
            public void onFreeze(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is frozen solid!"));
            }


            @Override
            public void onThaw(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been thawed!"));
            }


            @Override
            public void onSleep(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is fast sleep!"));
            }


            @Override
            public void onRest(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has fallen asleep!"));
            }


            @Override
            public void onWakeUp(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " woke up!"));
            }

            @Override
            public void onParalyze(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been paralyzed and may be unable to move!"));
            }

            @Override
            public void onParalyzeSuccess(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is paralyzed and is unable to move!"));
            }

            @Override
            public void onConfuse(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is confused!"));
            }

            @Override
            public void onConfusion(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog("It hurt itself in its confusion."));
            }

            @Override
            public void onConfusionSnap(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has gotten out of its confusion!"));
            }

            @Override
            public void onFlinch(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " flinched!"));
            }

            @Override
            public void onHeal(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!"));
            }

            @Override
            public void onDrain(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has restored some of its health!"));
            }

            @Override
            public void onDisabled(BattlePokemon pokemon, BattleMove move) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + "'s " + Localizer.formatPokemonMove(move.getName()) + " has been disabled!"));
            }

            @Override
            public void onTorment(BattlePokemon pokemon, BattleMove move) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " cannot use the same move twice in a row!"));
            }

            @Override
            public void onYawn(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " is about to fall asleep..."));
            }

            @Override
            public void onSilence(BattleMove move) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(move.getName()) + " has been silenced!"));
            }

            @Override
            public void onRecoil(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has been damaged by the recoil!"));
            }

            @Override
            public void onTarShot(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has been weakened to fire!"));
            }

            @Override
            public void onPerishSong(BattlePokemon pokemon, int turns) {
                refreshHp(() -> battleLogger.displayBattleLog("All Pokemon who hears the song will faint in " + turns + " turns!"));
            }

            @Override
            public void onStatChange(BattlePokemon pokemon, StatId statId, int previousStage, int stage) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + "'s " + Localizer.formatEnum(statId.name()) + " has been" + (previousStage > stage ? " lowered!" : " raised!")));
            }

            @Override
            public void onFly(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " flew to the sky!"));
            }

            @Override
            public void onDig(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " dug into the ground!"));
            }

            @Override
            public void onDive(BattlePokemon pokemon) {
               refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " dove into the water!"));
            }

            @Override
            public void onCharge(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has started to charge!"));
            }

            @Override
            public void onCharging(BattlePokemon pokemon) {
               refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " is charging..."));
            }

            @Override
            public void onChargeFinish(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " has finished charging!"));
            }

            @Override
            public void onInflict(BattlePokemon pokemon, sickbay.pokenamon.model.Ailment ailment) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!"));

            }

            @Override
            public void onCure(BattlePokemon pokemon, sickbay.pokenamon.model.Ailment ailment) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()) + "!"));
            }

            @Override
            public void onNightmare(BattlePokemon pokemon) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " is having nightmares!"));
            }

            @Override
            public void onIngrain(BattlePokemon pokemon) {
               refreshHp(() ->  battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has planted its roots!"));
            }

            @Override
            public void onProtect(BattlePokemon pokemon, BattleMove move) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + " protected itself from the move!"));
            }

            @Override
            public void onProtectFail(BattlePokemon pokemon, BattleMove move) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonMove(pokemon.getName()) + "'s protection failed!"));
            }

            @Override
            public void onVolatileInflict(BattlePokemon pokemon, VolatileAilment ailment) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been inflicted with " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!"));
            }

            @Override
            public void onVolatileCure(BattlePokemon pokemon, VolatileAilment ailment) {
                refreshHp(() -> battleLogger.displayBattleLog(Localizer.formatPokemonName(pokemon.getName()) + " has been cured of " + Localizer.formatEnum(ailment.getType().name()).toLowerCase() + "!"));
            }
        });
    }

    private void setState(BattleState newState) {
        this.currentState = newState;
        Log.d("BATTLE_FSM", "Transitioning to: " + newState.name());

        switch (currentState) {
            case IDLE:
                movePanel.setVisibility(TableLayout.VISIBLE);
                battleLog.setVisibility(TextView.GONE);

                boolean playerLocked = playerPokemon.isCharging() || playerPokemon.isLockedIn() || playerPokemon.isSuspended();

                refreshMovesButton(moveMap);

                if (playerLocked) {
                    toggleMoves(false);
                    takeAction(playerPokemon.getLastMoveUsed());
                } else {
                    toggleMoves(true);
                }
                break;

            case PREPARE_TURN:
                toggleMoves(false);
                setState(BattleState.EXECUTE_ACTION);
                break;

            case EXECUTE_ACTION:
                boolean hasMoreActions = ArenaEngine.executeNextAction();

                if (!hasMoreActions) {
                    setState(BattleState.TURN_END);
                }
                break;

            case CHECK_FAINT:
                refreshHp(() -> {
                    if (playerPokemon.isFainted() || enemyPokemon.isFainted()) {
                        setState(BattleState.CONCLUDE);

                    } else {
                        setState(BattleState.EXECUTE_ACTION);
                    }
                });
                break;

            case TURN_END:
                refreshHp(() -> setState(BattleState.IDLE));
                break;

            case CONCLUDE:
                conclude(playerPokemon.isFainted() ? playerPokemon : enemyPokemon);
                break;
        }
    }

    private void spriteScale(ImageView sprite, double speciesHeightInMeters) {
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