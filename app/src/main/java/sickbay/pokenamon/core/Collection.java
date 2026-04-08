package sickbay.pokenamon.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.auth.Login;
import sickbay.pokenamon.helper.BottomNavHelper;
import sickbay.pokenamon.controller.PokemonAdapter;
import sickbay.pokenamon.model.Pokemon;

public class Collection extends AppCompatActivity {

    TextView txtInitial, txtUsername, txtPokeCount, txtWins;
    Button btnLogout;
    Spinner lvlSpnr, raritySpnr;
    RecyclerView recyclerViewCollection;

    List<Pokemon> myPokemonList;
    PokemonAdapter adapter;
    int selectedLevelPos = 0;
    int selectedRarityPos = 0;

    FirebaseAuth auth;
    DatabaseReference userRef, inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_collection);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        inventoryRef = FirebaseDatabase.getInstance().getReference("user_inventory").child(uid);

        init();
        BottomNavHelper.setup(this);

        fetchUserProfile();
        loadAllMyPokemon();
    }

    private void init() {
        // Bind UI Elements
        txtInitial = findViewById(R.id.username_initial);
        txtUsername = findViewById(R.id.username);
        txtPokeCount = findViewById(R.id.pokemon_count);
        txtWins = findViewById(R.id.wins);
        btnLogout = findViewById(R.id.logout);
        lvlSpnr = findViewById(R.id.lvlSpnr);
        raritySpnr = findViewById(R.id.raritySpnr);
        recyclerViewCollection = findViewById(R.id.recyclerViewCollection);

        recyclerViewCollection.setLayoutManager(new GridLayoutManager(this, 2));

        setupSpinners();

        lvlSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLevelPos = position;
                applySorting();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        raritySpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRarityPos = position;
                applySorting();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Logout Logic
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Collection.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupSpinners() {

        try {
            ArrayAdapter<CharSequence> lvlAdapter = ArrayAdapter.createFromResource(
                    this, R.array.lvl, R.layout.spinner_item);
            lvlAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            lvlSpnr.setAdapter(lvlAdapter);

            ArrayAdapter<CharSequence> rarityAdapter = ArrayAdapter.createFromResource(
                    this, R.array.rarity, R.layout.spinner_item);
            rarityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            raritySpnr.setAdapter(rarityAdapter);
        } catch (Exception e) {
            Log.e("CollectionUI", "Failed to load custom spinners. Using defaults.");
            ArrayAdapter<CharSequence> defAdapter1 = ArrayAdapter.createFromResource(this, R.array.lvl, android.R.layout.simple_spinner_item);
            ArrayAdapter<CharSequence> defAdapter2 = ArrayAdapter.createFromResource(this, R.array.rarity, android.R.layout.simple_spinner_item);
            lvlSpnr.setAdapter(defAdapter1);
            raritySpnr.setAdapter(defAdapter2);
        }
    }

    private void fetchUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    Object count = snapshot.child("pokemonCount").getValue();
                    Object wins = snapshot.child("wins").getValue();

                    if (name != null && !name.isEmpty()) {
                        txtUsername.setText(name);
                        txtInitial.setText(name.substring(0, 1).toUpperCase());
                    }

                    txtPokeCount.setText(String.valueOf(count != null ? count : 0));
                    txtWins.setText(String.valueOf(wins != null ? wins : 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CollectionBackend", "Profile fetch failed: " + error.getMessage());
            }
        });
    }

    private void loadAllMyPokemon() {
        inventoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPokemonList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Pokemon p = child.getValue(Pokemon.class);
                        if (p != null) myPokemonList.add(p);
                    } catch (Exception e) {
                        Log.e("CollectionBackend", "Failed to parse Pokemon: " + e.getMessage());
                    }
                }

                applySorting();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CollectionBackend", "Inventory fetch failed: " + error.getMessage());
            }
        });
    }

    private void applySorting() {
        if (myPokemonList == null || myPokemonList.isEmpty()) return;

        List<Pokemon> sortedList = new ArrayList<>(myPokemonList);

        Collections.sort(sortedList, (p1, p2) -> {
            if (selectedLevelPos == 0) {
                return Integer.compare(p1.getLevel(), p2.getLevel());
            } else {
                return Integer.compare(p2.getLevel(), p1.getLevel());
            }
        });

        Collections.sort(sortedList, (p1, p2) -> {
            if (selectedRarityPos == 0) {
                return Integer.compare(p1.getStars(), p2.getStars());
            } else {
                return Integer.compare(p2.getStars(), p1.getStars());
            }
        });

        adapter = new PokemonAdapter(sortedList);
        recyclerViewCollection.setAdapter(adapter);
    }
}