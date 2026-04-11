package sickbay.pokenamon.system.arena;

import sickbay.pokenamon.system.arena.events.ActionFinishListener;

public class AttackAction {
    /*
    Represents the 'Attack' action from the game
    Also represents the "Move" each pokemon from either party uses
     */

    BattlePokemon pokemon;
    BattleMove move;

    private ActionFinishListener listener;

    public AttackAction(BattlePokemon pokemon, BattleMove move) {
        this.pokemon = pokemon;
        this.move = move;
    }

    public BattlePokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(BattlePokemon pokemon) {
        this.pokemon = pokemon;
    }

    public BattleMove getMove() {
        return move;
    }

    public void setMove(BattleMove move) {
        this.move = move;
    }

    public void setActionFinishListener(ActionFinishListener listener) {
        this.listener = listener;
    }

    public void notifyFinish() {
        if (listener != null) listener.onFinish();
    }
}
