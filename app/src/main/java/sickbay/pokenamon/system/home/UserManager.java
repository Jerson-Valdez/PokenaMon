package sickbay.pokenamon.system.home;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;

import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.User;
import sickbay.pokenamon.util.Localizer;

public class UserManager {
    private static UserManager instance;
    private User currentUser;

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getUser() { return currentUser; }
    public void setUser(User user) { this.currentUser = user; }

    public Task<Void> updateCoins(int amount) {
        currentUser.setCoins(currentUser.getCoins() + amount);
        return DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("coins").setValue(currentUser.getCoins());
    }

    public void updatePokemonCount(int count) {
        currentUser.setPokemonCount(currentUser.getPokemonCount() + count);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("pokemonCount").setValue(currentUser.getPokemonCount());
    }

    public void sellPokemon(Context context, PokemonDTO pokemon, Runnable listener) {
        new AlertDialog.Builder(context)
                .setTitle("Sell Pokemon")
                .setMessage("You are about to sell " + Localizer.formatPokemonName(pokemon.getName()) + " for " + valuatePokemon(pokemon) + " shards. Do you want to continue?")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    updateCoins(valuatePokemon(pokemon))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DB.getDatabaseInstance().deleteUserPokemon(currentUser.getUid(), pokemon.getCollectionId());
                                    updatePokemonCount(-1);
                                    listener.run();
                                }
                            });
                    dialog.cancel();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                }).show();
    }

    private int valuatePokemon(PokemonDTO pokemon) {
        return (int) Math.floor(pokemon.getRarity() * 3 + pokemon.getTypes().size() * .5 + pokemon.getLevel() * 2.5 + pokemon.getStats().values().stream().mapToInt(Integer::valueOf).sum() * .085 - pokemon.getExp() * .30);
    }

}
