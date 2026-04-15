package sickbay.pokenamon.system.home;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;


import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.User;
import sickbay.pokenamon.system.arena.BattlePokemon;
import sickbay.pokenamon.util.Localizer;

public class UserManager {
    private static UserManager instance;
    private static User currentUser;
    private static PokemonDTO selectedPokemonForBattle;
    private static boolean hasBattledToday;

    private UserManager() {}

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User getUser() { return currentUser; }

    public void setUser(User user) { UserManager.currentUser = user; }

    public PokemonDTO getSelectedPokemonForBattle() {
        return selectedPokemonForBattle;
    }

    public void setSelectedPokemonForBattle(PokemonDTO selectedPokemonForBattle) {
        UserManager.selectedPokemonForBattle = selectedPokemonForBattle;
    }

    public boolean isHasBattledToday() { return hasBattledToday; }

    public void setHasBattledToday(boolean hasBattledToday) { UserManager.hasBattledToday = hasBattledToday; }

    public void updateStreak(int amount, String date) {
        if (!hasBattledToday) {
            currentUser.setStreak(currentUser.getStreak() + amount);
            DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("lastLogin").setValue(date);
            DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("streak").setValue(currentUser.getStreak());
            setHasBattledToday(true);
        }
    }

    public Task<Void> updateShards(int amount) {
        currentUser.setShards(currentUser.getShards() + amount);
        return DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("shards").setValue(currentUser.getShards());
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
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("earnedShardsBySelling").setValue(currentUser.getEarnedShardsBySelling());
    }

    public void updateUserEarnedShardsByBattling(int amount) {
        currentUser.setEarnedShardsByBattling(currentUser.getEarnedShardsByBattling() + amount);
        DB.getDatabaseInstance().getUserReference(currentUser.getUid()).child("earnedShardsByBattling").setValue(currentUser.getEarnedShardsByBattling());
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
                    updateShards(pokemonValue)
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
        return (int) Math.max(Math.floor(pokemon.getRarity() * 1.5 + pokemon.getTypes().size() * 1.5 + pokemon.getLevel() * .3 + pokemon.getStats().values().stream().mapToInt(Integer::valueOf).sum() * .085 - pokemon.getExp() * .30 / 500), 20);
    }

    public void updateBattlePokemon(PokemonDTO oldPokemon) {
        DB.getDatabaseInstance().getUserInventoryReference(currentUser.getUid()).child(oldPokemon.getCollectionId()).setValue(oldPokemon).addOnCompleteListener(
                (task) -> {
                    if (task.isSuccessful()) {
                        updateUserLastBattledPokemon(oldPokemon);
                        selectedPokemonForBattle = oldPokemon;
                    }
                }
        );
    }
}
