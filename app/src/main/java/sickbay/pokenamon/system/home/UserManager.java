package sickbay.pokenamon.system.home;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;

import java.util.Objects;

import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.User;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.util.Localizer;

public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private PokemonDTO selectedPokemonForBattle;

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getUser() { return currentUser; }

    public void setUser(User user) { this.currentUser = user; }

    public PokemonDTO getSelectedPokemonForBattle() {
        return selectedPokemonForBattle;
    }

    public void setSelectedPokemonForBattle(PokemonDTO selectedPokemonForBattle) {
        this.selectedPokemonForBattle = selectedPokemonForBattle;
    }

    public Task<Void> updateCoins(int amount) {
        currentUser.setCoins(currentUser.getCoins() + amount);
        return DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("coins").setValue(currentUser.getCoins());
    }

    public void updatePokemonCount(int count) {
        currentUser.setPokemonCount(currentUser.getPokemonCount() + count);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("pokemonCount").setValue(currentUser.getPokemonCount());
    }

    public void updatePokemonSold(int count) {
        currentUser.setPokemonSold(currentUser.getPokemonSold() + count);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("pokemonSold").setValue(currentUser.getPokemonSold());
    }

    public void updateHighestFloorWin(int floor) {
        if (floor > currentUser.getHighestWin()) {
            currentUser.setHighestWin(floor);
            DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("highestWin").setValue(currentUser.getHighestWin());
        }
    }

    public void updateUserEarnedShardsBySelling(int amount) {
        currentUser.setEarnedShardsBySelling(currentUser.getEarnedShardsBySelling() + amount);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("earnedShardsBySelling").setValue(currentUser.getPokemonCount());
    }

    public void updateUserEarnedShardsByBattling(int amount) {
        currentUser.setEarnedShardsByBattling(currentUser.getEarnedShardsByBattling() + amount);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("earnedShardsByBattling").setValue(currentUser.getPokemonCount());
    }

    public void updateUserLastBattledPokemon(PokemonDTO pokemon) {
        setSelectedPokemonForBattle(pokemon);
        currentUser.setLastBattledPokemon(pokemon);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("lastBattledPokemon").setValue(pokemon);
    }

    public void sellPokemon(Context context, PokemonDTO pokemon, Runnable listener) {
        int pokemonValue = valuatePokemon(pokemon);
        new AlertDialog.Builder(context)
                .setTitle("Sell Pokemon")
                .setMessage("You are about to sell " + Localizer.formatPokemonName(pokemon.getName()) + " for " + pokemonValue + " shards. Do you want to continue?")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    updateCoins(pokemonValue)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (selectedPokemonForBattle != null && pokemon.getCollectionId().equals(selectedPokemonForBattle.getCollectionId())) {
                                        selectedPokemonForBattle = null;
                                    }

                                    DB.getDatabaseInstance().deleteUserPokemon(currentUser.getUid(), pokemon.getCollectionId());
                                    updatePokemonCount(-1);
                                    updatePokemonSold(1);
                                    updateUserEarnedShardsBySelling(pokemonValue);
                                    listener.run();
                                }
                            });
                    dialog.cancel();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()).show();
    }

    public int valuatePokemon(PokemonDTO pokemon) {
        return (int) Math.floor(pokemon.getRarity() * 3 + pokemon.getTypes().size() * .5 + pokemon.getLevel() * 2.5 + pokemon.getStats().values().stream().mapToInt(Integer::valueOf).sum() * .085 - pokemon.getExp() * .30);
    }
}
