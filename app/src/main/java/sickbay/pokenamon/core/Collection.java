package sickbay.pokenamon.core;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.helper.BottomNavHelper;
import sickbay.pokenamon.system.home.GridSpacingItemDecoration;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;

public class Collection extends AppCompatActivity {
    TextView txtInitial, txtUsername, txtPokeCount, txtWins, txtStreak;
    CheckBox nameSort, levelSort, raritySort, dateSort;
    Button btnLogout;
    RecyclerView recyclerViewCollection;
    List<PokemonDTO> myPokemonListOld;
    List<PokemonDTO> myPokemonList;
    private CheckBox activeFilter;

    private int primaryFilterId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_collection);

        init();
        fetchUserProfile();
        loadAllMyPokemon();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        loadAllMyPokemon();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        txtInitial = findViewById(R.id.username_initial);
        txtUsername = findViewById(R.id.username);
        txtPokeCount = findViewById(R.id.pokemon_count);
        txtWins = findViewById(R.id.wins);
        txtStreak = findViewById(R.id.streak);
        btnLogout = findViewById(R.id.logout);
        nameSort = findViewById(R.id.sortByAlphabet);
        levelSort = findViewById(R.id.sortByLevel);
        raritySort = findViewById(R.id.sortByRarity);
        dateSort = findViewById(R.id.sortByDate);

        recyclerViewCollection = findViewById(R.id.recyclerViewCollection);

        recyclerViewCollection.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewCollection.addItemDecoration(new GridSpacingItemDecoration(2, 16, false));

        nameSort.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                if (primaryFilterId == -1) primaryFilterId = button.getId();
                nameSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                nameSort.setTextColor(getResources().getColor(R.color.background, null));
                nameSort.setText("Name: ↓");
            } else {
                if (primaryFilterId == button.getId()) primaryFilterId = -1;
                nameSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                nameSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                nameSort.setText("Name: ↑");
            }
            sort();
            snapRecyclerViewToStart();
        });

        levelSort.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                if (primaryFilterId == -1) primaryFilterId = button.getId();
                levelSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                levelSort.setTextColor(getResources().getColor(R.color.background, null));
                levelSort.setText("Level: ↓");
            } else {
                if (primaryFilterId == button.getId()) primaryFilterId = -1;
                levelSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                levelSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                levelSort.setText("Level: ↑");
            }
            sort();
            snapRecyclerViewToStart();
        });

        raritySort.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                if (primaryFilterId == -1) primaryFilterId = button.getId();
                raritySort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                raritySort.setTextColor(getResources().getColor(R.color.background, null));
                raritySort.setText("Rarity: ↓");
            } else {
                if (primaryFilterId == button.getId()) primaryFilterId = -1;
                raritySort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                raritySort.setTextColor(getResources().getColor(R.color.textVariant, null));
                raritySort.setText("Rarity: ↑");
            }
            sort();
            snapRecyclerViewToStart();
        });

        dateSort.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                if (primaryFilterId == -1) primaryFilterId = button.getId();
                dateSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                dateSort.setTextColor(getResources().getColor(R.color.background, null));
                dateSort.setText("Date: ↓");
            } else {
                if (primaryFilterId == button.getId()) primaryFilterId = -1;
                dateSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                dateSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                dateSort.setText("Date: ↑");
            }
            sort();
            snapRecyclerViewToStart();
        });

        BottomNavHelper.setup(this);
    }

    private void snapRecyclerViewToStart() {
        RecyclerView.SmoothScroller scroller = new LinearSmoothScroller(this) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        scroller.setTargetPosition(0);

        recyclerViewCollection.getLayoutManager().startSmoothScroll(scroller);
    }

    private void sort() {
        if (myPokemonList == null || myPokemonList.isEmpty()) return;

        myPokemonList.sort((p1, p2) -> {
            int result = 0;

            if (primaryFilterId == R.id.sortByLevel) {
                result = levelSort.isChecked() ? Integer.compare(p2.getLevel(), p1.getLevel()) : Integer.compare(p1.getLevel(), p2.getLevel());
            } else if (primaryFilterId == R.id.sortByRarity) {
                result = raritySort.isChecked() ? Integer.compare(p2.getRarity(), p1.getRarity()) : Integer.compare(p1.getRarity(), p2.getRarity());
            } else if (primaryFilterId == R.id.sortByDate) {
                result = dateSort.isChecked() ? Long.compare(p2.getSummonedAt(), p1.getSummonedAt()) : Long.compare(p1.getSummonedAt(), p2.getSummonedAt());
            } else if (primaryFilterId == R.id.sortByAlphabet) {
                result = nameSort.isChecked() ? p2.getName().compareToIgnoreCase(p1.getName()) : p1.getName().compareToIgnoreCase(p2.getName());
            }

            if (result != 0) return result;

            if (p1.getLevel() != p2.getLevel()) {
                return levelSort.isChecked() ? Integer.compare(p2.getLevel(), p1.getLevel()) : Integer.compare(p1.getLevel(), p2.getLevel());
            }

            if (p1.getRarity() != p2.getRarity()) {
                return raritySort.isChecked() ? Integer.compare(p2.getRarity(), p1.getRarity()) : Integer.compare(p1.getRarity(), p2.getRarity());
            }

            if (p1.getSummonedAt() != p2.getSummonedAt()) {
                return dateSort.isChecked() ? Long.compare(p2.getSummonedAt(), p1.getSummonedAt()) : Long.compare(p1.getSummonedAt(), p2.getSummonedAt());
            }
            return nameSort.isChecked() ? p2.getName().compareToIgnoreCase(p1.getName()) : p1.getName().compareToIgnoreCase(p2.getName());
        });

        if (recyclerViewCollection.getAdapter() != null) {
            recyclerViewCollection.getAdapter().notifyDataSetChanged();
        }
    }
    private int sortByName (PokemonDTO p1, PokemonDTO p2) {
        return nameSort.isChecked() ?
                p2.getName().compareToIgnoreCase(p1.getName()) :
                p1.getName().compareToIgnoreCase(p2.getName());
    }

    private int sortByLevel (PokemonDTO p1, PokemonDTO p2) {
        return levelSort.isChecked() ?
                Integer.compare(p2.getLevel(), p1.getLevel()) :
                Integer.compare(p1.getLevel(), p2.getLevel());
    }

    private int sortByRarity (PokemonDTO p1, PokemonDTO p2) {
        return raritySort.isChecked() ?
                Integer.compare(p2.getRarity(), p1.getRarity()) :
                Integer.compare(p1.getRarity(), p2.getRarity());
    }

    private int sortByDate (PokemonDTO p1, PokemonDTO p2) {
        return dateSort.isChecked() ?
                Long.compare(p2.getSummonedAt(), p1.getSummonedAt()) :
                Long.compare(p1.getSummonedAt(), p2.getSummonedAt());
    }



    private void fetchUserProfile() {
        int pokemonCount = UserManager.getInstance().getUser().getPokemonCount();
        int wins = UserManager.getInstance().getUser().getWins();
        int streak = UserManager.getInstance().getUser().getStreak();
        String username = UserManager.getInstance().getUser().getUsername();

        txtUsername.setText(Localizer.toTitleCase(username));
        txtInitial.setText(Localizer.toTitleCase(username).substring(0,1));
        txtPokeCount.setText(String.format("%,d", pokemonCount));
        txtWins.setText(String.format("%,d", wins));
        txtStreak.setText(String.format("%,d", streak));
    }

    private void loadAllMyPokemon() {
        DB.getDatabaseInstance().getUserInventoryReference(UserManager.getInstance().getUser().getUid())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myPokemonList = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            PokemonDTO p = child.getValue(PokemonDTO.class);
                            if (p != null) myPokemonList.add(p);
                        } catch (Exception e) {
                            Log.e("Collection", e.getMessage(), e);
                        }
                    }

                    myPokemonListOld = myPokemonList;
                    recyclerViewCollection.setAdapter(new PokemonListAdapter(Collection.this, myPokemonList));
                    sort();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Collection", error.getMessage(), error.toException());
                }
            });
    }
}