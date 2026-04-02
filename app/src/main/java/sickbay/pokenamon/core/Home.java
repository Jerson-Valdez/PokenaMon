package sickbay.pokenamon.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.helper.BottomNavHelper;
import sickbay.pokenamon.controller.PokemonAdapter;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.model.User;

public class Home extends AppCompatActivity {

    TextView txtUsername, txtCoins;
    RecyclerView recyclerViewRecent;
    Button startBattle, changePokemon;

    PokemonAdapter adapter;
    List<Pokemon> recentPokemonList;

    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        init();
        fetchUserData();
        fetchRecentCaptures();
    }

    private void init() {
        startBattle = findViewById(R.id.startBattle);
        changePokemon = findViewById(R.id.changePokemon);
        txtUsername = findViewById(R.id.homeUsername);
        txtCoins = findViewById(R.id.homeCoins);
        recyclerViewRecent = findViewById(R.id.recyclerViewRecent);

        recyclerViewRecent.setLayoutManager(new GridLayoutManager(this, 2));

        LinearLayout btnSummon = findViewById(R.id.btnQuickSummon);
        LinearLayout btnCollection = findViewById(R.id.btnQuickCollection);

        btnSummon.setOnClickListener(v -> {
            startActivity(new Intent(this, Gacha.class));
            overridePendingTransition(0, 0);
            finish();
        });

        btnCollection.setOnClickListener(v -> {
            Toast.makeText(this, "Collection coming soon!", Toast.LENGTH_SHORT).show();
        });

        BottomNavHelper.setup(this);
    }

    private void fetchUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        DatabaseReference userRef = database.getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    Integer coins = snapshot.child("coins").getValue(Integer.class);

                    if (username != null) txtUsername.setText(username);
                    if (coins != null) txtCoins.setText(String.format("%,d", coins));

                    if (UserManager.getInstance().getUser() != null) {
                        UserManager.getInstance().getUser().coins = (coins != null) ? coins : 0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeBackend", "Failed to fetch user data: " + error.getMessage());
            }
        });
    }

    private void fetchRecentCaptures() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        DatabaseReference inventoryRef = database.getReference("user_inventory").child(uid);

        Query recentQuery = inventoryRef.orderByChild("timestamp").limitToLast(2);

        recentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentPokemonList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Pokemon p = child.getValue(Pokemon.class);
                        if (p != null) {
                            recentPokemonList.add(p);
                        }
                    } catch (Exception e) {
                        Log.e("HomeBackend", "Failed to parse Pokemon: " + e.getMessage());
                    }
                }

                Collections.reverse(recentPokemonList);

                adapter = new PokemonAdapter(recentPokemonList);
                recyclerViewRecent.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeBackend", "Failed to fetch inventory: " + error.getMessage());
                Toast.makeText(Home.this, "Failed to load recent captures.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}