package sickbay.pokenamon.system.arena;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sickbay.pokenamon.system.arena.enums.StatId;
import sickbay.pokenamon.system.arena.enums.Ailment;
import sickbay.pokenamon.system.arena.enums.DamageClass;
import sickbay.pokenamon.system.arena.enums.TargetType;
import sickbay.pokenamon.system.arena.enums.Type;
import sickbay.pokenamon.system.arena.enums.VolatileAilment;
import sickbay.pokenamon.system.arena.model.StatBuff;

public class ArenaEngine {
    private static final Random rand = new Random();

    public static int calculatePokemonTotalHp(int baseHp, int level) {
        return (int) Math.floor(0.01 * (2 * baseHp) * level) + level + 10;
    }

    public static void commence(AttackAction playerAction, AttackAction enemyAction) {
        AttackAction[] sortedActions = sortActions(playerAction, enemyAction);

        for (AttackAction action : sortedActions) {
            if (action.getPokemon().isFainted()) return;
            applyMove(action.getPokemon(), action.getMove(), getTarget(action, sortedActions));
        }
    }

    public static void applyMove(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (move.getCurrentPp() == 0) return;
        if (!canMove(user, move)) return;

        move.reducePp();

        if (user.hasVolatileElement(VolatileAilment.CONFUSION)) {
            sickbay.pokenamon.system.arena.model.VolatileAilment confusion = user.getVolatileAilment(VolatileAilment.CONFUSION);
            confusion.setTurns(confusion.getTurns() - 1);

            if (confusion.getTurns() <= 0) {
                user.removeVolatileAilment(VolatileAilment.CONFUSION);
                user.onSnappedOutOfConfusion();
            } else if (rand.nextBoolean()) {
                BattleMove confusionHit = new BattleMove("confusion-hit", DamageClass.PHYSICAL, Type.NORMAL, TargetType.USER, 40, 0);
                user.takeDamage(computeDamage(user, confusionHit, user));
                return;
            }
        }

        if (user.hasVolatileElement(VolatileAilment.FLINCH)) {
            user.removeVolatileAilment(VolatileAilment.FLINCH);
            user.onFlinch();
            return;
        }

        if (move.accuracy > 0 && !ArenaRegistry.alwaysHits(move) && canHit(user, move, target)) {
            if (move.name.equals("high-jump-kick") || move.name.equals("jump-kick")) {
                int recoil = (int) Math.floor(user.getTotalHp() / 2.0);
                user.takeDamage(recoil);
            }
            user.onMoveMiss(move);
            return;
        }

        if (ArenaRegistry.requiresAilment(move)) {
            if (target.getAilment().getType() != ArenaRegistry.getRequiredAilment(move)) {
                user.onMoveFail(move);
                return;
            }
        }

        if (ArenaRegistry.isOhko(move)) {
            applyOhko(user, move, target);
            return;
        }

        if (ArenaRegistry.isFixedDamage(move)) {
            applyFixedDamage(user, move, target);
            return;
        }

        if (move.damageClass == DamageClass.STATUS) {
            applyStatusMove(user, move, target);
            return;
        }

        applyDamageMove(user, move, target);
    }

    private static void applyDamageMove(BattlePokemon user, BattleMove move,
                                        BattlePokemon target) {
        int hits = resolveHits(move);

        for (int i = 0; i < hits; i++) {
            if (target.isFainted()) break;

            if (ArenaRegistry.thawsUser(move) && user.getAilment().getType() == Ailment.FREEZE) {
                user.getAilment().setType(Ailment.NONE);
                user.onAilmentCured(Ailment.FREEZE);
            }

            int damage = applyDamageModifiers(move, target, computeDamage(user, move, target));

            target.takeDamage(damage);

            if (ArenaRegistry.thawsTarget(move) && target.getAilment().getType() == Ailment.FREEZE) {
                target.getAilment().setType(Ailment.NONE);
                target.onAilmentCured(Ailment.FREEZE);
            }


            double drainRatio = ArenaRegistry.getDrainRatio(move);
            if (drainRatio > 0) {
                user.heal((int) Math.floor(damage * drainRatio));
            }

            double recoilRatio = ArenaRegistry.getRecoilRatio(move);
            if (recoilRatio > 0) {
                user.takeDamage((int) Math.floor(damage * recoilRatio));
            }
        }

        if (ArenaRegistry.causesUserToFaint(move)) {
            user.takeDamage(user.getCurrentHp());
            return;
        }

        if (!target.isFainted()) {
            applySecondaryAilment(move, target);
            applySecondaryBuffs(move, user, target);
            applySecondaryFlinch(move, target);
        }
    }

    private static int computeDamage(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (move.damageClass == DamageClass.STATUS) return 0;

        boolean isPhysical = move.damageClass == DamageClass.PHYSICAL;
        boolean isCrit = resolveCrit(move);

        int atkStat = isPhysical
                ? user.getEffectiveStat(StatId.ATTACK)
                : user.getEffectiveStat(StatId.SPECIAL_ATTACK);
        int defStat = isPhysical
                ? target.getEffectiveStat(StatId.DEFENSE)
                : target.getEffectiveStat(StatId.SPECIAL_DEFENSE);

        if (isCrit) {
            if (isPhysical) {
                atkStat = Math.max(atkStat, user.getStats().get(StatId.ATTACK).getBattleStat());
                defStat = Math.min(defStat, target.getStats().get(StatId.DEFENSE).getBattleStat());
            } else {
                atkStat = Math.max(atkStat, user.getStats().get(StatId.SPECIAL_ATTACK).getBattleStat());
                defStat = Math.min(defStat, target.getStats().get(StatId.SPECIAL_DEFENSE).getBattleStat());
            }
            user.onCriticalHit();
        }

        int power = resolvePower(user, move, target);
        int level = user.getLevel();

        double base = Math.floor(
                (Math.floor(2.0 * level / 5 + 2) * power * atkStat / (double) defStat) / 50.0
        ) + 2;

        double critMulti   = isCrit ? 1.5 : 1.0;
        double randomMulti = (rand.nextInt(16) + 85) / 100.0;
        double stab        = Arrays.asList(user.getTypes()).contains(move.type) ? 1.5 : 1.0;
        double typeMulti   = TypeChart.getEffectiveness(move.type, target.getTypes());
        double burnMulti   = (isPhysical && user.getAilment().getType() == Ailment.BURN) ? 0.5 : 1.0;

        return Math.max(1, (int) Math.floor(base * critMulti * randomMulti * stab * typeMulti * burnMulti));
    }

    private static int applyDamageModifiers(BattleMove move,
                                            BattlePokemon target, int damage) {
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
                target.getAilment().setType(Ailment.NONE);
                target.onAilmentCured(cured);
            }
        }

        return damage;
    }

    private static int resolvePower(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (!ArenaRegistry.hasVariablePower(move)) return move.power;

        double hpRatio = (double) user.getCurrentHp() / user.getTotalHp();

        switch (move.name) {
            case "eruption":
            case "water-spout":
                return (int) Math.max(1, Math.floor(150 * hpRatio));

            case "flail":
            case "reversal":
                if (hpRatio > 0.6875) return 20;
                if (hpRatio > 0.3542) return 40;
                if (hpRatio > 0.2083) return 80;
                if (hpRatio > 0.1042) return 100;
                if (hpRatio > 0.0417) return 150;
                return 200;

            case "wring-out":
            case "crush-grip":
                return (int) Math.max(1, Math.floor(120 * ((double) target.getCurrentHp() / target.getTotalHp())));

            case "gyro-ball":
                return (int) Math.min(150, Math.floor(
                        25.0 * target.getEffectiveStat(StatId.SPEED) / user.getEffectiveStat(StatId.SPEED)));

            case "electro-ball":
                double speedRatio = (double) user.getEffectiveStat(StatId.SPEED) / target.getEffectiveStat(StatId.SPEED);
                if (speedRatio >= 4) return 150;
                if (speedRatio >= 3) return 120;
                if (speedRatio >= 2) return 80;
                if (speedRatio >= 1) return 60;
                return 40;

            case "stored-power":
                return 20 + 20 * user.buffs.size();

            case "punishment":
                return Math.min(200, 60 + 20 * target.buffs.size());

            case "magnitude":
                int roll = rand.nextInt(100);
                if (roll < 5)  return 10;
                if (roll < 15) return 30;
                if (roll < 35) return 50;
                if (roll < 65) return 70;
                if (roll < 85) return 90;
                if (roll < 95) return 110;
                return 150;
            case "low-kick":
            case "grass-knot":
                double weight = target.getWeight();
                if (weight < 10) return 20;
                if (weight >= 10 && weight < 25) return 40;
                if (weight >= 25 && weight < 50) return  60;
                if (weight >= 50 && weight < 100) return 80;
                if (weight >= 100 && weight < 200) return 100;
                if (weight >= 200) return 120;
            case "heat-crash":
            case "heavy-slam":
                double userWeight = user.getWeight();
                double targetWeight = target.getWeight();
                double percent = (userWeight - targetWeight / userWeight) * 100;
                if (percent > 50) return 40;
                if (percent >= 33.35) return 60;
                if (percent >= 25.01 && percent <= 33.34) return 80;
                if (percent >= 20.01 && percent <= 25) return 100;
                else return 120;
            default:
                return move.power;
        }
    }

    private static void applyOhko(BattlePokemon user, BattleMove move, BattlePokemon target) {
        // Fails if target is higher level
        if (target.getLevel() > user.getLevel()) {
            user.onMoveFail(move);
            return;
        }
        // Sheer Cold has reduced accuracy against non-Ice types (handled in canHit)
        if (canHit(user, move, target)) {
            user.onMoveMiss(move);
            return;
        }
        target.takeDamage(target.getCurrentHp());
    }

    private static void applyFixedDamage(BattlePokemon user, BattleMove move, BattlePokemon target) {
        int damage;
        switch (move.name) {
            case "seismic-toss":
            case "night-shade":
                damage = user.getLevel();
                break;
            case "dragon-rage":
                damage = 40;
                break;
            case "sonic-boom":
                damage = 20;
                break;
            case "super-fang":
            case "natures-madness":
                damage = (int) Math.max(1, Math.floor(target.getCurrentHp() / 2.0));
                break;
            case "psywave":
                damage = (int) Math.max(1, Math.floor(user.getLevel() * (rand.nextInt(101) + 50) / 100.0));
                break;
            default:
                damage = move.power;
                break;
        }
        target.takeDamage(damage);
    }

    private static void applyStatusMove(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (ArenaRegistry.isSelfHeal(move) || ArenaRegistry.curesSelfAilment(move)) {
            applySelfHeal(user, move);
            return;
        }

        applyPrimaryAilment(move, target);
        applyPrimaryBuffs(move, user, target);
    }

    private static void applyPrimaryAilment(BattleMove move, BattlePokemon target) {
        if (move.getAilment() == null || move.getAilment() == Ailment.NONE) return;
        inflictAilment(move.getAilment(), move.getRawAilment(), move.getAilmentChance(), target, true);
    }

    private static void applySecondaryAilment(BattleMove move, BattlePokemon target) {
        if (move.getAilment() == null || move.getAilment() == Ailment.NONE) return;
        inflictAilment(move.getAilment(), move.getRawAilment(), move.getAilmentChance(), target, false);
    }

    private static void inflictAilment(Ailment ailment, String rawAilment, int chance,
                                       BattlePokemon target, boolean isPrimary) {
        // Primary ailments (status moves) always attempt while secondary use chance roll
        if (!isPrimary && chance > 0 && rand.nextInt(100) >= chance) return;

        boolean isNonVolatile = (ailment == Ailment.BURN || ailment == Ailment.FREEZE
                || ailment == Ailment.PARALYSIS || ailment == Ailment.POISON
                || ailment == Ailment.SLEEP);

        if (isNonVolatile) {
            if (target.getAilment().getType() != Ailment.NONE) return;
            target.getAilment().setType(ailment);
            if (ailment == Ailment.SLEEP) {
                int turns = rand.nextInt(3) + 1;
                target.getAilment().setTurns(turns);
                target.getAilment().setMaximumTurns(turns);
                target.getAilment().setMinimumTurns(1);
            }
            target.onAilmentInflicted(ailment);
        } else {
            VolatileAilment volatileType = VolatileAilment.valueOf(VolatileAilment.value(rawAilment));
            if (volatileType == VolatileAilment.NONE) return;
            if (target.hasVolatileElement(volatileType)) return;

            sickbay.pokenamon.system.arena.model.VolatileAilment va =
                    new sickbay.pokenamon.system.arena.model.VolatileAilment(volatileType, 0, 0, 0);

            if (volatileType == VolatileAilment.CONFUSION) {
                int turns = rand.nextInt(4) + 1;
                va.setTurns(turns);
                va.setMaximumTurns(4);
                va.setMinimumTurns(1);
            } else if (volatileType == VolatileAilment.YAWN) {
                va.setTurns(2);
                va.setMaximumTurns(2);
                va.setMinimumTurns(2);
            } else if (volatileType == VolatileAilment.DISABLE) {
                int turns = rand.nextInt(8);
                va.setTurns(turns);
                va.setMaximumTurns(7);
                va.setMinimumTurns(0);
            }

            target.addVolatileAilment(va);
            target.onVolatileAilmentInflicted(volatileType);
        }
    }

    private static void applySecondaryFlinch(BattleMove move, BattlePokemon target) {
        if (move.getFlinchChance() <= 0) return;
        if (rand.nextInt(100) >= move.getFlinchChance()) return;
        if (target.hasVolatileElement(VolatileAilment.FLINCH)) return;

        target.addVolatileAilment(
                new sickbay.pokenamon.system.arena.model.VolatileAilment(VolatileAilment.FLINCH, 1, 1, 0));
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

    private static void applyBuffList(List<StatBuff> buffs, BattlePokemon user,
                                      BattlePokemon target) {
        for (StatBuff buff : buffs) {
            // Accuracy check per buff where 0 means it always applies
            if (buff.getAccuracy() > 0 && rand.nextInt(100) >= buff.getAccuracy()) continue;

            // Negative stage = debuff on target | positive = buff on user
            BattlePokemon affected = buff.getStage() < 0 ? target : user;

            int currentStage = affected.hasBuff(buff.getStat())
                    ? affected.getBuff(buff.getStat()).getStage()
                    : 0;

            int newStage = Math.max(-6, Math.min(6, currentStage + buff.getStage()));
            if (newStage == currentStage) continue; // limit reached

            StatBuff applied = new StatBuff(buff.getStat(), newStage, 0);
            affected.setBuff(applied);
            affected.onStatChange(buff.getStat(), buff.getStage());
        }
    }

    private static void applySelfHeal(BattlePokemon user, BattleMove move) {
        Log.d("WORKING", "Function is working");
        switch (move.name) {
            case "rest":
                user.setAilment(new sickbay.pokenamon.system.arena.model.Ailment(Ailment.SLEEP, 2, 2, 0));
                user.setCurrentHp(user.getTotalHp());
                user.onRest();
                break;
            case "refresh":
                Ailment current = user.getAilment().getType();
                if (current == Ailment.BURN || current == Ailment.POISON || current == Ailment.PARALYSIS) {
                    user.setAilment(new sickbay.pokenamon.system.arena.model.Ailment(Ailment.NONE, 0, 0, 0));
                    user.onAilmentCured(current);
                } else {
                    user.onMoveFail(move);
                }
                break;
            default:
                Log.d("WORKING", "Switch working");
                // recover, slack-off, roost, etc.
                if (ArenaRegistry.HEAL_HALF.contains(move.name)) {
                    Log.d("FOUND", "Move found");
                    int amount = (int) Math.floor(user.getTotalHp() / 2.0);
                    Log.d("HEAL", amount + "");
                    user.heal(amount);
                    user.onHeal(amount);
                }
                break;
        }
    }

    public static boolean canMove(BattlePokemon user, BattleMove move) {
        Ailment ailment = user.getAilment().getType();

        if (ailment == Ailment.SLEEP) {
            if (user.getAilment().getTurns() > 0) {
                user.getAilment().setTurns(user.getAilment().getTurns() - 1);
                user.onSleeping();
                return false;
            }
            user.getAilment().setType(Ailment.NONE);
            user.onAilmentCured(Ailment.SLEEP);
            return true;
        }

        if (ailment == Ailment.FREEZE) {
            if (ArenaRegistry.thawsUser(move)) {
                user.getAilment().setType(Ailment.NONE);
                user.onAilmentCured(Ailment.FREEZE);
                return true;
            }
            if (rand.nextInt(5) == 0) {
                user.getAilment().setType(Ailment.NONE);
                user.onAilmentCured(Ailment.FREEZE);
                return true;
            }
            user.onFrozen();
            return false;
        }

        if (ailment == Ailment.PARALYSIS && rand.nextInt(4) == 0) {
            user.onParalyzeSuccess();
            return false;
        }

        return true;
    }

    private static boolean canHit(BattlePokemon user, BattleMove move, BattlePokemon target) {
        if (ArenaRegistry.isOhko(move)) {
            return rand.nextInt(100) + 1 > 30 + (user.getLevel() - target.getLevel());
        }
        double stageMulti = (double) Math.max(3, 3 + user.getBuff(StatId.ACCURACY).getStage())
                / Math.max(3, 3 - target.getBuff(StatId.EVASION).getStage());
        return !(rand.nextInt(100) + 1 <= move.accuracy * stageMulti);
    }

    private static boolean resolveCrit(BattleMove move) {
        if (ArenaRegistry.alwaysCrits(move)) return true;
        int stage = ArenaRegistry.isHighCrit(move) ? 1 : 0;
        switch (stage) {
            case 0:  return rand.nextInt(24) == 0;
            case 1:  return rand.nextInt(8) == 0;
            case 2:  return rand.nextInt(2) == 0;
            default: return true;
        }
    }

    private static int resolveHits(BattleMove move) {
        if (ArenaRegistry.DOUBLE_HIT.contains(move.name)) return 2;
        if (ArenaRegistry.MULTI_HIT.contains(move.name)) {
            int roll = rand.nextInt(100);
            if (roll < 35) return 2;
            if (roll < 70) return 3;
            if (roll < 85) return 4;
            return 5;
        }
        return 1;
    }

    private static BattlePokemon getOpponent(AttackAction action, AttackAction[] sortedActions) {
        return action == sortedActions[0]
                ? sortedActions[1].getPokemon()
                : sortedActions[0].getPokemon();
    }

    private static BattlePokemon getTarget(AttackAction action, AttackAction[] sortedActions) {
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