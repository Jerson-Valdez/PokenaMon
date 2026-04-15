package sickbay.pokenamon.system.arena;

import android.animation.ValueAnimator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sickbay.pokenamon.system.arena.events.BattleLogFinishListener;

public class BattleLogger implements BattleLogFinishListener {
    private final List<String> logQueue;
    private boolean isFinished;
    private boolean isBattleOngoing;
    private final TextView battleLog;

    public BattleLogger(TextView battleLog) {
        logQueue = new ArrayList<>();
        isFinished = true;
        this.battleLog = battleLog;
    }

    public void setIsBattleOngoing(boolean isBattleOngoing) { this.isBattleOngoing = isBattleOngoing; }

    public boolean getIsFinished() { return isFinished; }

    private void handleBattleLog() {
        if (logQueue.isEmpty()) {
            isFinished = true;
            onLogFinish();
            return;
        }

        isFinished = false;
        animateLogDisplay(logQueue.remove(0));
        onLogging();
    }

    public void displayBattleLog(String message) {
        logQueue.add(message);

        if (isFinished) {
            handleBattleLog();
        }
    }

    public void animateLogDisplay(String text) {
        long totalWait = Math.min((text.length() * 45), 2500);

        ValueAnimator animator = ValueAnimator.ofInt(0, text.length());
        animator.setDuration(totalWait);
        animator.addUpdateListener(animation -> {
            battleLog.setText(text.substring(0, (int) animation.getAnimatedValue()));
        });
        animator.start();
        battleLog.postDelayed(this::handleBattleLog, totalWait + 500);
    }

    @Override
    public void onLogging() {
    }

    @Override
    public void onLogFinish() {
    }
}
