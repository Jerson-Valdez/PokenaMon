package sickbay.pokenamon.system.arena;

public class AttackAction {
    /*
    Represents the 'Attack' action from the game
    Also represents the "Move" each pokemon from either party uses
     */

    BattlePokemon pokemon;
    BattleMove move;


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
}
