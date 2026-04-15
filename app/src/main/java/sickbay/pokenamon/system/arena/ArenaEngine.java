package sickbay.pokenamon.system.arena;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sickbay.pokenamon.model.enums.Ailment;
import sickbay.pokenamon.model.enums.StatId;
import sickbay.pokenamon.model.enums.DamageClass;
import sickbay.pokenamon.model.enums.TargetType;
import sickbay.pokenamon.model.enums.Type;
import sickbay.pokenamon.model.enums.VolatileAilment;
import sickbay.pokenamon.model.StatBuff;
import sickbay.pokenamon.system.arena.states.BattleState;
import sickbay.pokenamon.util.Localizer;

public class ArenaEngine {
    private static final Random rand = new Random();
    public static AttackAction[] actions;
    public static AttackAction currentAction;
    public static int currentActionIndex = 0;

    public static int gainExp(BattlePokemon player) {
        return (int) ((double) ((25 * player.getLevel() / 7) * 1 * 1) / 1 * 1.5 * 1);
    }

    public static void prepareTurn(AttackAction playerAction, AttackAction enemyAction) {
        actions = sortActions(playerAction, enemyAction);
        currentActionIndex = 0;
        currentAction = null;
    }

    public static boolean executeNextAction() {
        if (currentActionIndex < actions.length) {
            currentAction = actions[currentActionIndex++];

            if (currentAction.getPokemon().isFainted()) {
                return executeNextAction();
            }

            applyMove(currentAction.getPokemon(), currentAction.getMove(), getTarget(currentAction, actions));
            return true;
        }

        currentAction = null;
        return false;
    }

    public static void applyMove(BattlePokemon user, BattleMove move,
                                 BattlePokemon target) {
        Log.d("Moving", user.getName() + " is now moving");

        if (ArenaRegistry.isRecharge(move)) {
            if (user.isCharging() && user.getTurns() > 0) {
                user.setTurns(user.getTurns() - 1);
                user.notifyCharging(user);
                return;
            } else if (user.isCharging() && user.getTurns() == 0) {
                user.setCharging(false);
                user.notifyChargeFinish(user);
            }
        }

        if (!canMove(user, move)) {
            return;
        };

        if (move.getCurrentPp() == 0) {
            user.notifyMovePpOut(move);
            return;
        }

        user.notifyMoveUse(move);
        user.setLastMoveUsed(move);

        Log.d("Moving", user.getName() + " has successfully moved");

        if (user.hasVolatileElement(VolatileAilment.TORMENT) && user.getLastMoveUsed() != null && user.getLastMoveUsed().getName().equals(move.getName())) {
            user.notifyTorment(user, move);
            return;
        }

        if (move.isDisabled()) {
            user.getVolatileAilment(VolatileAilment.DISABLE);

            if (user.getVolatileAilment(VolatileAilment.DISABLE).getTurns() > 0) {
                user.getVolatileAilment(VolatileAilment.DISABLE).setTurns(user.getVolatileAilment(VolatileAilment.DISABLE).getTurns() - 1);
                user.notifyDisabled(user, move);
                return;
            }

            user.removeVolatileAilment(VolatileAilment.DISABLE);
            move.setDisabled(false);
        }

        if (user.hasVolatileElement(VolatileAilment.HEAL_BLOCK)) {
            if (user.getVolatileAilment(VolatileAilment.HEAL_BLOCK).getTurns() > 0) {
                user.getVolatileAilment(VolatileAilment.HEAL_BLOCK).setTurns(user.getVolatileAilment(VolatileAilment.HEAL_BLOCK).getTurns() - 1);
                user.notifyDisabled(user, move);
                return;
            }

            user.removeVolatileAilment(VolatileAilment.HEAL_BLOCK);
        }

        if (ArenaRegistry.isLockIn(move) || ArenaRegistry.isTwoTurn(move)) {
            int turns = 1;

            if (rand.nextBoolean() && !ArenaRegistry.isTwoTurn(move)) {
                turns = 2;
            }

            user.setTurns(turns);
        }

        if (ArenaRegistry.isTwoTurn(move)) {
            Log.d("Moving", user.getName() + "'s move is a two turn");

            if (user.isSuspended() && user.getTurns() > 0) {
                user.setSuspended(false);
                user.setTurns(0);
                Log.d("Moving", user.getName() + " is about to exit suspension");
            } else {
                user.setSuspended(true);
                Log.d("Moving", user.getName() + " has been suspended. Forwarding to next action...");
                switch (move.getName()) {
                    case "fly":
                        user.notifyFly(user);
                        return;
                    case "dig":
                        user.notifyDig(user);
                        return;
                    case "dive":
                        user.notifyDive(user);
                        return;
                };
                return;
            }
        }

        if (ArenaRegistry.isLockIn(move)) {
            Log.d("Moving", user.getName() + "'s move is locked in");

            if (user.isLockedIn() && user.getTurns() > 0) {
                user.setTurns(user.getTurns() - 1);
                Log.d("Moving", user.getName() + "'s locked in turns is now " + user.getTurns());
            } else if (user.isLockedIn() && user.getTurns() == 0) {
                if (!move.getName().equalsIgnoreCase("uproar")) {
                    inflictAilment(Ailment.NONE, "confusion", 0, user, false);
                }
            }
        }

        move.reducePp();

        if (actions[1] == currentAction && ArenaRegistry.isFirstTurnOnly(move)) {
            user.notifyMoveFail(move);
            return;
        }


        if (target.getAilment().getType() == Ailment.SLEEP) {
            if (move.getName().equalsIgnoreCase("uproar")) {
                target.setAilment(new sickbay.pokenamon.model.Ailment(Ailment.NONE, 0, 0,0));
                target.notifyWakeUp(target);
            }
        }

        if (user.hasVolatileElement(VolatileAilment.CONFUSION)) {
            sickbay.pokenamon.model.VolatileAilment confusion =
                    user.getVolatileAilment(VolatileAilment.CONFUSION);
            confusion.setTurns(confusion.getTurns() - 1);

            if (confusion.getTurns() <= 0) {
                user.removeVolatileAilment(VolatileAilment.CONFUSION);
                user.notifyConfusionSnap(user);
            } else if (rand.nextBoolean()) {
                BattleMove confusionHit = new BattleMove("confusion-hit", DamageClass.PHYSICAL,
                        Type.NORMAL, TargetType.USER, 40, 0);
                user.takeDamage(computeDamage(user, confusionHit, user));
                user.notifyConfusion(user);
                return;
            }
        }

        if (user.hasVolatileElement(VolatileAilment.FLINCH)) {
            user.removeVolatileAilment(VolatileAilment.FLINCH);
            user.notifyFlinch(user);
            return;
        }

        if (move.getAccuracy() > 0 && !ArenaRegistry.alwaysHits(move) && canHit(user, move, target)) {
            if (move.getName().equals("high-jump-kick") || move.getName().equals("jump-kick")) {
                int recoil = (int) Math.floor(user.getTotalHp() / 2.0);
                user.takeDamage(recoil);
                user.notifyRecoil(user);
            }
            user.notifyMoveMiss(move);
            return;
        }

        if (ArenaRegistry.isMoveWithSpecialAilment(move)) {
            inflictAilment(null, move.getName(), 0, target, false);

            if (move.getName().equals("tar-shot")) {
                if (rand.nextBoolean()) {
                    applyPrimaryBuffs(move, user, target);
                }
            }
        }

        if (target.getLastMoveUsed() != null) {
            if (!ArenaRegistry.bypassesProtect(move) && ArenaRegistry.isProtect(target.getLastMoveUsed())) {
                user.notifyProtect(target, move);
                return;
            } else if (ArenaRegistry.bypassesProtect(move)) {
                user.notifyProtectFail(target, move);
            }
        }

        double typeEffectiveness = TypeChart.getEffectiveness(move.getType(), target.getTypes());

        if (move.getDamageClass() != DamageClass.STATUS) {
            if (typeEffectiveness == 0.0) {
                user.notifyImmune();
                return;
            } else if (typeEffectiveness == 0.5) {
                user.notifyResist();
            } else if (typeEffectiveness == 2) {
                user.notifyEffective();
            }
        }

        if (ArenaRegistry.requiresAilment(move)) {
            if (target.getAilment().getType() != ArenaRegistry.getRequiredAilment(move)) {
                user.notifyMoveFail(move);
                return;
            }
        }

        if (ArenaRegistry.isOhko(move)) { applyOhko(user, move, target); return; }
        if (ArenaRegistry.isFixedDamage(move)) { applyFixedDamage(user, move, target); return; }
        if (move.getDamageClass() == DamageClass.STATUS) {
            applyStatusMove(user, move, target); return; }

        applyDamageMove(user, move, target);

        resolveAilment(user, target);

        Log.d("Moving", user.getName() + "'s move is successful");

    }

    private static void applyDamageMove(BattlePokemon user, BattleMove move,
                                        BattlePokemon target) {
        int hits = resolveHits(move);
        user.notifyMoveHit(move);

        for (int i = 0; i < hits; i++) {
            if (target.isFainted()) { target.notifyFaint(target); return; }

            if (ArenaRegistry.thawsUser(move) && user.getAilment().getType() ==
                    Ailment.FREEZE) {
                user.getAilment().setType(Ailment.NONE);
                user.notifyThaw(user);
            }

            int damage = applyDamageModifiers(move, target,
                    computeDamage(user, move, target));
            target.takeDamage(damage);

            if (target.isFainted()) { target.notifyFaint(target); return; }

            if (ArenaRegistry.thawsTarget(move) && target.getAilment().getType() ==
                    Ailment.FREEZE) {
                target.getAilment().setType(Ailment.NONE);
                target.notifyThaw(user);
            }

            double drainRatio = ArenaRegistry.getDrainRatio(move);
            if (drainRatio > 0) {
                user.heal((int) Math.floor(damage * drainRatio));
                target.takeDamage((int) Math.floor(damage * drainRatio));
                user.notifyDrain(user);
            }

            double recoilRatio = ArenaRegistry.getRecoilRatio(move);
            if (recoilRatio > 0) {
                user.takeDamage((int) Math.floor(damage * recoilRatio));
                user.notifyRecoil(user);
            }

            if (hits > 1) user.notifyMoveMultipleHits(move, i + 1);
        }

        if (ArenaRegistry.causesUserToFaint(move)) {
            user.takeDamage(user.getCurrentHp());
            user.notifyFaint(user);
            return;
        }

        if (move.getName().equalsIgnoreCase("wake-up-slap")) {
            target.notifyWakeUp(target);
        }

        if (ArenaRegistry.isRecharge(move)) {
            user.setCharging(true);
            user.notifyCharge(user);
        }

        if (!target.isFainted()) {
            applySecondaryAilment(move, target);
            applySecondaryBuffs(move, user, ArenaRegistry.debuffsUser(move) ? user : target);
            applySecondaryFlinch(move, target);
        }
    }

    private static int computeDamage(BattlePokemon user, BattleMove move,
                                     BattlePokemon target) {
        if (move.getDamageClass() == DamageClass.STATUS) return 0;

        boolean isPhysical = move.getDamageClass() == DamageClass.PHYSICAL;
        boolean isCrit = resolveCrit(move);

        int atkStat = isPhysical ? user.getEffectiveStat(StatId.ATTACK) : user.getEffectiveStat(StatId.SPECIAL_ATTACK);
        int defStat = isPhysical ? target.getEffectiveStat(StatId.DEFENSE) : target.getEffectiveStat(StatId.SPECIAL_DEFENSE);

        if (isCrit) {
            if (isPhysical) {
                atkStat = Math.max(atkStat, user.getStats().get(StatId.ATTACK).getBattleStat());
                defStat = Math.min(defStat, target.getStats().get(StatId.DEFENSE).getBattleStat());
            } else {
                atkStat = Math.max(atkStat, user.getStats().get(StatId.SPECIAL_ATTACK).getBattleStat());
                defStat = Math.min(defStat, target.getStats().get(StatId.SPECIAL_DEFENSE).getBattleStat());
            }
            user.notifyCriticalDamage();
        }

        int power = resolvePower(user, move, target);
        int level = user.getLevel();
        double base = Math.floor(
                (Math.floor(2.0 * level / 5 + 2) * power * atkStat / (double) defStat) / 50.0) + 2;

        double critMulti   = isCrit ? 1.5 : 1.0;
        double randomMulti = (rand.nextInt(16) + 85) / 100.0;
        double stab        = Arrays.asList(user.getTypes()).contains(move.getType()) ? 1.5 : 1.0;
        double typeMulti   = TypeChart.getEffectiveness(move.getType(), target.getTypes());
        double burnMulti   = (!move.getName().equals("facade") && isPhysical && user.getAilment().getType() ==
                Ailment.BURN) ? 0.5 : target.hasVolatileElement(VolatileAilment.TAR_SHOT) ? 2.0 : 1.0;

        return Math.max(1, (int) Math.floor(base * critMulti * randomMulti * stab * typeMulti * burnMulti));
    }

    private static void resolveAilment(BattlePokemon user, BattlePokemon target) {
        if (user.getAilment().getType() == Ailment.NONE && user.getVolatileAilments().isEmpty()) return;

        switch (user.getAilment().getType()) {
            case BURN:
                user.takeDamage((int) Math.floor(user.getTotalHp() * .0625)); // 1/16th of max HP
                user.notifyBurn(user);
                break;
            case POISON:
                user.takeDamage((int) Math.floor(user.getTotalHp() * .125)); // 1/8th of max HP
                user.notifyPoison(user);
                break;
        }

        for (sickbay.pokenamon.model.VolatileAilment volatileAilment: user.getVolatileAilments()) {
            switch (volatileAilment.getType()) {
                case INGRAIN:
                    user.setCurrentHp(user.getCurrentHp() + (int) Math.floor(user.getTotalHp() * .0625));
                    user.notifyIngrain(user);
                    break;
                case NIGHTMARE:
                    user.setCurrentHp(user.getCurrentHp() + (int) Math.floor(user.getTotalHp() * .25)); // 1/4th of max hp
                    user.notifyNightmare(user);
                    break;
                case PERISH_SONG:
                    if (user.getVolatileAilment(VolatileAilment.PERISH_SONG).getTurns() > 0) {
                        user.getVolatileAilment(VolatileAilment.PERISH_SONG).setTurns(user.getVolatileAilment(VolatileAilment.PERISH_SONG).getTurns() - 1);
                        user.notifyPerishSong(user, user.getVolatileAilment(VolatileAilment.PERISH_SONG).getTurns());
                    } else {
                        user.takeDamage(user.getCurrentHp());
                        target.takeDamage(target.getCurrentHp());
                        user.notifyFaint(user);
                        target.notifyFaint(target);
                        return;
                    }
                    break;
                case LEECH_SEED:
                    user.setCurrentHp(user.getCurrentHp() + (int) Math.floor(user.getTotalHp() * .125));
                    target.takeDamage((int) Math.floor(user.getTotalHp() * .125));
                    user.notifyDrain(user);
                    break;
                case YAWN:
                    if (user.getVolatileAilment(VolatileAilment.YAWN).getTurns() > 0) {
                        user.getVolatileAilment(VolatileAilment.YAWN).setTurns(user.getVolatileAilment(VolatileAilment.YAWN).getTurns() - 1);
                        user.notifyYawn(user);
                    } else {
                        user.notifySleep(user);
                    }
                    break;
            }
        }
    }

    private static int applyDamageModifiers(BattleMove move, BattlePokemon target, int damage) {
        if (ArenaRegistry.hasDoublePowerOnAilment(move)) {
            Ailment required = ArenaRegistry.getDoublePowerAilment(move);
            boolean applies = required == Ailment.NONE
                    ? target.getAilment().getType() != Ailment.NONE
                    : target.getAilment().getType() == required;
            if (applies) damage *= 2;
        }

        if (ArenaRegistry.hasCuresAilmentOnHit(move)) {
            Ailment cured = ArenaRegistry.getCuredAilmentOnHit(move);
            if (target.getAilment().getType() == cured) {
                target.notifyCure(target, target.getAilment());
                target.getAilment().setType(Ailment.NONE);
            }
        }

        return damage;
    }

    private static void applyOhko(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (target.getLevel() > user.getLevel()) { user.notifyMoveFail(move); return; }
        if (canHit(user, move, target)) { user.notifyMoveMiss(move); return; }
        target.takeDamage(target.getCurrentHp());
        target.notifyFaint(target);
    }

    private static void applyFixedDamage(BattlePokemon user, BattleMove move, BattlePokemon target) {
        int damage;
        switch (move.getName()) {
            case "seismic-toss": case "night-shade": damage = user.getLevel(); break;
            case "dragon-rage":  damage = 40; break;
            case "sonic-boom":   damage = 20; break;
            case "super-fang": case "natures-madness":
                damage = (int) Math.max(1, Math.floor(target.getCurrentHp() / 2.0)); break;
            case "psywave":
                damage = (int) Math.max(1, Math.floor(user.getLevel() * (rand.nextInt(101) + 50) / 100.0)); break;
            default: damage = move.getPower(); break;
        }
        target.takeDamage(damage);
        if (target.isFainted()) target.notifyFaint(target);
    }

    private static void applyStatusMove(BattlePokemon user, BattleMove move,
                                        BattlePokemon target) {
        if (ArenaRegistry.isSelfHeal(move) || ArenaRegistry.curesSelfAilment(move)) {
            applySelfHeal(user, move);
            return;
        }
        applyPrimaryAilment(move, target);
        applyPrimaryBuffs(move, user, ArenaRegistry.debuffsUser(move) ? user : target);
    }

    private static void applySelfHeal(BattlePokemon user, BattleMove move) {
        switch (move.getName()) {
            case "rest":
                user.setAilment(new sickbay.pokenamon.model.Ailment(
                        Ailment.SLEEP, 2, 2, 0));
                user.setCurrentHp(user.getTotalHp());
                user.notifyRest(user);
                break;
            case "refresh":
                Ailment current = user.getAilment().getType();
                if (current == Ailment.BURN
                        || current == Ailment.POISON
                        || current == Ailment.PARALYSIS) {
                    user.notifyCure(user, user.getAilment());
                    user.setAilment(new sickbay.pokenamon.model.Ailment(
                            Ailment.NONE, 0, 0, 0));
                } else {
                    user.notifyMoveFail(move);
                }
                break;
            default:
                if (ArenaRegistry.HEAL_HALF.contains(move.getName())) {
                    int amount = (int) Math.floor(user.getTotalHp() / 2.0);
                    user.heal(amount);
                    user.notifyHeal(user);
                }
                break;
        }
    }

    private static void applyPrimaryAilment(BattleMove move, BattlePokemon target) {
        if (move.getAilment() == null || move.getAilment() ==
                Ailment.NONE) return;
        inflictAilment(move.getAilment(), move.getRawAilment(), move.getAilmentChance(), target, true);
    }

    private static void applySecondaryAilment(BattleMove move, BattlePokemon target) {
        if (move.getAilment() == null || move.getAilment() ==
                Ailment.NONE) return;
        inflictAilment(move.getAilment(), move.getRawAilment(), move.getAilmentChance(), target, false);
    }

    private static void inflictAilment(Ailment ailment,
                                       String rawAilment, int chance,
                                       BattlePokemon target, boolean isPrimary) {
        if (!isPrimary && chance > 0 && rand.nextInt(100) >= chance) return;

        boolean isNonVolatile = ailment == Ailment.BURN
                || ailment == Ailment.FREEZE
                || ailment == Ailment.PARALYSIS
                || ailment == Ailment.POISON
                || ailment == Ailment.SLEEP;

        if (isNonVolatile) {
            if (target.getAilment().getType() != Ailment.NONE) return;
            target.getAilment().setType(ailment);

            if (ailment == Ailment.SLEEP) {
                int turns = rand.nextInt(3) + 1;
                target.getAilment().setTurns(turns);
                target.getAilment().setMaximumTurns(turns);
                target.getAilment().setMinimumTurns(1);
                target.notifySleep(target);
            } else if (ailment == Ailment.FREEZE) {
                target.notifyFreeze(target);
            }
            else {
                target.notifyInflict(target, target.getAilment());
            }
        } else {
            VolatileAilment volatileType = VolatileAilment.valueOf(Localizer.formatEnumString(rawAilment));
            if (volatileType == VolatileAilment.NONE) return;
            if (target.hasVolatileElement(volatileType)) return;

            sickbay.pokenamon.model.VolatileAilment va =
                    new sickbay.pokenamon.model.VolatileAilment(volatileType, 0, 0, 0);

            if (volatileType == VolatileAilment.CONFUSION) {
                int turns = rand.nextInt(4) + 1;
                va.setTurns(turns);
                va.setMaximumTurns(4);
                va.setMinimumTurns(1);
                target.notifyConfuse(target);
            } else if (volatileType == VolatileAilment.YAWN) {
                va.setTurns(2);
                va.setMaximumTurns(2);
                va.setMinimumTurns(2);
                target.notifyYawn(target);
            } else if (volatileType == VolatileAilment.TORMENT) {
                target.notifyTorment(target, target.getLastMoveUsed());
            } else if (volatileType == VolatileAilment.DISABLE) {
                int turns = rand.nextInt(8);
                va.setTurns(turns);
                va.setMaximumTurns(7);
                va.setMinimumTurns(0);
                target.notifyDisabled(target, target.getLastMoveUsed());
            } else if (volatileType == VolatileAilment.TAR_SHOT) {
                target.notifyTarShot(target);
            } else if (volatileType == VolatileAilment.PERISH_SONG) {
                va.setMaximumTurns(3);
                va.setTurns(va.getMaximumTurns());
                target.notifyPerishSong(target, va.getTurns());
            } else if (volatileType == VolatileAilment.TRI_ATTACK) {
                int randomAilment = rand.nextInt(3);

                if (randomAilment == 0) {
                    inflictAilment(Ailment.PARALYSIS, "", 20, target, true);
                } else if (randomAilment == 1) {
                    inflictAilment(Ailment.FREEZE, "", 20, target, true);
                } else {
                    inflictAilment(Ailment.BURN, "", 20, target, true);
                }

                return;
            }

            target.addVolatileAilment(va);
        }
    }

    private static void applySecondaryFlinch(BattleMove move, BattlePokemon target) {
        if (move.getFlinchChance() <= 0) return;
        if (rand.nextInt(100) >= move.getFlinchChance()) return;
        if (target.hasVolatileElement(VolatileAilment.FLINCH)) return;
        target.addVolatileAilment(
                new sickbay.pokenamon.model.VolatileAilment(VolatileAilment.FLINCH, 1, 1, 0));
    }

    private static void applyPrimaryBuffs(BattleMove move, BattlePokemon user, BattlePokemon target) {
        if (move.getStatBuffs() == null || move.getStatBuffs().isEmpty()) return;
        applyBuffList(move.getStatBuffs(), user, target);
    }

    private static void applySecondaryBuffs(BattleMove move, BattlePokemon user, BattlePokemon target) {
        if (move.getStatBuffs() == null || move.getStatBuffs().isEmpty()) return;
        if (move.getStatChance() > 0 && rand.nextInt(100) >= move.getStatChance()) return;
        applyBuffList(move.getStatBuffs(), user, target);
    }

    private static void applyBuffList(List<StatBuff> buffs, BattlePokemon user, BattlePokemon target) {
        for (StatBuff buff : buffs) {
            if (buff.getAccuracy() > 0 && rand.nextInt(100) >= buff.getAccuracy()) continue;
            BattlePokemon affected = buff.getStage() < 0 ? target : user;
            int currentStage = affected.hasBuff(buff.getStat()) ? affected.getBuff(buff.getStat()).getStage() : 0;
            int newStage = Math.max(-6, Math.min(6, currentStage + buff.getStage()));
            if (newStage == currentStage) continue;
            affected.setBuff(new StatBuff(buff.getStat(), newStage, 0));
            affected.notifyStatChange(affected, buff.getStat(), currentStage, buff.getStage());
        }
    }

    public static boolean canMove(BattlePokemon user, BattleMove move) {
        Ailment ailment = user.getAilment().getType();

        if (ailment == Ailment.SLEEP) {
            if (user.getAilment().getTurns() > 0) {
                user.getAilment().setTurns(user.getAilment().getTurns() - 1);
                user.notifySleep(user);
                return false;
            }

            user.getAilment().setType(Ailment.NONE);
            user.notifyWakeUp(user);
            return true;
        }

        if (ailment == Ailment.FREEZE) {
            if (ArenaRegistry.thawsUser(move)) {
                user.getAilment().setType(Ailment.NONE);
                user.notifyThaw(user);
                return true;
            }
            if (rand.nextInt(5) == 0) {
                user.getAilment().setType(Ailment.NONE);
                user.notifyThaw(user);
                return true;
            }
            user.notifyFreeze(user);
            return false;
        }

        if (ailment == Ailment.PARALYSIS) {
            user.notifyParalyze(user);

            if (rand.nextInt(4) == 0) {
                user.notifyParalyzeSuccess(user);
                return false;
            }
        }

        return true;
    }

    private static boolean canHit(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (ArenaRegistry.isOhko(move)) {
            return rand.nextInt(100) + 1 > 30 + (user.getLevel() - target.getLevel());
        }
        double stageMulti = (double) Math.max(3, 3 + user.getBuff(StatId.ACCURACY).getStage())
                / Math.max(3, 3 - target.getBuff(StatId.EVASION).getStage());
        return !(rand.nextInt(100) + 1 <= move.getAccuracy() * stageMulti);
    }

    private static boolean resolveCrit(BattleMove move) {
        if (ArenaRegistry.alwaysCrits(move)) return true;
        int stage = ArenaRegistry.isHighCrit(move) ? 1 : 0;
        return stage == 0 ? rand.nextInt(24) == 0 : rand.nextInt(8) == 0;
    }

    private static int resolveHits(BattleMove move) {
        if (ArenaRegistry.MULTI_HIT.contains(move.getName())) {
            int roll = rand.nextInt(100);
            if (roll < 35) return 2;
            if (roll < 70) return 3;
            if (roll < 85) return 4;
            return 5;
        }
        return 1;
    }

    private static int resolvePower(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (!ArenaRegistry.hasVariablePower(move)) return move.getPower();
        double hpRatio = (double) user.getCurrentHp() / user.getTotalHp();
        switch (move.getName()) {
            case "wake-up-slap":
                if (target.getAilment().getType() == Ailment.SLEEP) return move.getPower() * 2;
            case "smelling-salts":
                if (target.getAilment().getType() == Ailment.PARALYSIS) return move.getPower() * 2;
            case "eruption": case "water-spout":
                return (int) Math.max(1, Math.floor(150 * hpRatio));
            case "flail": case "reversal":
                if (hpRatio > 0.6875) return 20;
                if (hpRatio > 0.3542) return 40;
                if (hpRatio > 0.2083) return 80;
                if (hpRatio > 0.1042) return 100;
                if (hpRatio > 0.0417) return 150;
                return 200;
            case "wring-out": case "crush-grip":
                return (int) Math.max(1, Math.floor(120 * ((double) target.getCurrentHp() / target.getTotalHp())));
            case "gyro-ball":
                return (int) Math.min(150, Math.floor(25.0 * target.getEffectiveStat(StatId.SPEED) / user.getEffectiveStat(StatId.SPEED)));
            case "electro-ball":
                double speedRatio = (double) user.getEffectiveStat(StatId.SPEED) / target.getEffectiveStat(StatId.SPEED);
                if (speedRatio >= 4) return 150;
                if (speedRatio >= 3) return 120;
                if (speedRatio >= 2) return 80;
                if (speedRatio >= 1) return 60;
                return 40;
            case "stored-power": return 20 + 20 * user.buffs.size();
            case "punishment":   return Math.min(200, 60 + 20 * target.buffs.size());
            case "magnitude":
                int roll = rand.nextInt(100);
                if (roll < 5)  return 10;
                if (roll < 15) return 30;
                if (roll < 35) return 50;
                if (roll < 65) return 70;
                if (roll < 85) return 90;
                if (roll < 95) return 110;
                return 150;
            case "low-kick": case "grass-knot":
                double weight = target.getWeight();
                if (weight < 10) return 20;
                if (weight < 25) return 40;
                if (weight < 50) return 60;
                if (weight < 100) return 80;
                if (weight < 200) return 100;
                return 120;
            case "heat-crash": case "heavy-slam":
                double userWeight = user.getWeight();
                double targetWeight = target.getWeight();
                double percent = ((userWeight - targetWeight) / userWeight) * 100;
                if (percent > 50)   return 40;
                if (percent >= 33.35) return 60;
                if (percent >= 25.01) return 80;
                if (percent >= 20.01) return 100;
                return 120;
            default: return move.getPower();
        }
    }

    private static BattlePokemon getOpponent(AttackAction action, AttackAction[] sortedActions) {
        return action == sortedActions[0] ? sortedActions[1].getPokemon() : sortedActions[0].getPokemon();
    }

    public static BattlePokemon getTarget(AttackAction action, AttackAction[] sortedActions) {
        return action.getMove().getTargetType() == TargetType.USER
                ? action.getPokemon()
                : getOpponent(action, sortedActions);
    }

    private static AttackAction[] sortActions(AttackAction playerAction, AttackAction enemyAction) {
        List<AttackAction> actions = new ArrayList<>(List.of(playerAction, enemyAction));
        actions.sort((a1, a2) -> {
            int p1 = ArenaRegistry.getPriorityOverride(a1.getMove());
            int p2 = ArenaRegistry.getPriorityOverride(a2.getMove());
            if (p1 != p2) return Integer.compare(p2, p1);
            double a1Speed = a1.getPokemon().getEffectiveStat(StatId.SPEED);
            double a2Speed = a2.getPokemon().getEffectiveStat(StatId.SPEED);
            if (a1.getPokemon().getAilment().getType() == Ailment.PARALYSIS) a1Speed *= 0.75;
            if (a2.getPokemon().getAilment().getType() == Ailment.PARALYSIS) a2Speed *= 0.75;
            if (a1Speed != a2Speed) return Double.compare(a2Speed, a1Speed);
            return rand.nextBoolean() ? -1 : 1;
        });
        return actions.toArray(new AttackAction[0]);
    }
}