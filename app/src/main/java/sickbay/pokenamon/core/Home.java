package sickbay.pokenamon.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.auth.Login;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.model.User;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.home.HorizontalSpacingItemDecoration;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;
import sickbay.pokenamon.util.SecurePreferences;

public class Home extends AppCompatActivity {
    ImageView lastBattledPokemon;
    LinearLayout battleCard;
    TextView greeting, txtUsername, lastBattledPokemonName, noRecentSummonsDisplay;
    RecyclerView recyclerViewRecent;
    Button startBattle, changePokemon, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);

        BackgroundMusicManager.getInstance(this).play(R.raw.home_theme);

        init();
        fetchUser();
        fetchUserRecentSummons();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        fetchUserRecentSummons();
    }

    private void init() {
        battleCard = findViewById(R.id.battleCard);
        greeting = findViewById(R.id.greeting);
        startBattle = findViewById(R.id.startBattle);
        lastBattledPokemon = findViewById(R.id.lastBattledPokemonSprite);
        changePokemon = findViewById(R.id.changePokemon);
        txtUsername = findViewById(R.id.homeUsername);
        lastBattledPokemonName = findViewById(R.id.lastBattledPokemonName);
        noRecentSummonsDisplay = findViewById(R.id.noRecentDisplay);
        recyclerViewRecent = findViewById(R.id.recyclerViewRecent);
        btnLogout = findViewById(R.id.logout);

        greeting.setText(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12 ? "Good morning, " : Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 17 ? "Good afternoon, " : Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 19 ? "Good evening, " : "Good night, ");

        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRecent.addItemDecoration(new HorizontalSpacingItemDecoration(16));

        LinearLayout btnSummon = findViewById(R.id.btnQuickSummon);
        LinearLayout btnCollection = findViewById(R.id.btnQuickCollection);

        if (UserManager.getInstance().getUser().getLastBattledPokemon() == null) {
            battleCard.setVisibility(LinearLayout.GONE);
        } else {
            battleCard.setVisibility(LinearLayout.VISIBLE);
        }

        btnLogout.setOnClickListener(v -> {
            SecurePreferences prefs = new SecurePreferences(this, "credentials", true);
            prefs.clear();

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            DB.getAuthInstance(this).signOutAuthUser();
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });

        startBattle.setOnClickListener(v -> {
            startActivity(new Intent(this, Battle.class));
            overridePendingTransition(0, 0);
        });

        changePokemon.setOnClickListener(v -> {
            startActivity(new Intent(this, Collection.class));
            overridePendingTransition(0, 0);
        });

        btnSummon.setOnClickListener(v -> {
            startActivity(new Intent(this, Gacha.class));
            finish();
            overridePendingTransition(0, 0);
        });

        btnCollection.setOnClickListener(v -> {
            startActivity(new Intent(this, Collection.class));
            overridePendingTransition(0, 0);
        });

        Navigation.setup(this);
    }

    private void fetchUser() {
        if (UserManager.getInstance().getUser() == null) {
            String uid = DB.getAuthInstance(this).getAuthUser().getUid();
            DB.getDatabaseInstance().getUserReference(uid)
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        User user = snapshot.getValue(User.class);
                                        user.setUid(uid);

                                        UserManager.getInstance().setUser(user);
                                        fetchUser();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("Home", error.getMessage(), error.toException());
                                }
                            });

            return;
        }

        String username = UserManager.getInstance().getUser().getUsername();
        PokemonDTO pokemon = UserManager.getInstance().getUser().getLastBattledPokemon();

        UserManager.getInstance().setSelectedPokemonForBattle(pokemon);

        txtUsername.setText(Localizer.toTitleCase(username));

        if (pokemon != null) {
            lastBattledPokemonName.setText(Localizer.formatPokemonName(pokemon.getName()));
            Glide.with(this)
                    .load(pokemon.getSprite().getFront())
                    .load(pokemon.getSprite().getFrontFallback())
                    .into(lastBattledPokemon);
        }
    }

    private void fetchUserRecentSummons() {
        DatabaseReference inventory = DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid());

        inventory.get()
            .addOnCompleteListener(this, task -> {
                if (task.getResult().getChildrenCount() == 0) {
                    recyclerViewRecent.setVisibility(RecyclerView.GONE);
                    noRecentSummonsDisplay.setVisibility(RecyclerView.VISIBLE);
                    return;
                }

                Query recentPokemons = inventory.orderByChild("summonedAt").limitToLast(10);
                recentPokemons.get()
                        .addOnCompleteListener(this, childTask -> {
                        if (childTask.isSuccessful()) {
                            List<PokemonDTO> recentPokemonsList = new ArrayList<>();

                            for (DataSnapshot child: childTask.getResult().getChildren()) {
                                PokemonDTO pokemon = child.getValue(PokemonDTO.class);
                                recentPokemonsList.add(pokemon);
                            }

                            Collections.reverse(recentPokemonsList);

                            recyclerViewRecent.setAdapter(new PokemonListAdapter(this, recentPokemonsList));
                        }
                    })
                    .addOnFailureListener(this, error -> Log.e("RecentSummons", error.getMessage(), error));
            })
            .addOnFailureListener(this, error -> Log.e("RecentSummons", error.getMessage(), error));
    }
}