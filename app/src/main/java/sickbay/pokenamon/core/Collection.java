package sickbay.pokenamon.core;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
import java.util.LinkedList;
import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.system.home.GridSpacingItemDecoration;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;

public class Collection extends AppCompatActivity {
    TextView txtInitial, txtUsername, txtPokeCount, txtPokeSold, txtShardsEarned;
    CheckBox nameSort, levelSort, raritySort, dateSort;
    RecyclerView recyclerViewCollection;
    List<PokemonDTO> myPokemonListOld;
    List<PokemonDTO> myPokemonList;

    private int nameSortCount = 0;
    private int levelSortCount = 0;
    private int raritySortCount = 0;
    private int dateSortCount = 0;

    LinkedList<CheckBox> activeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_collection);

        BackgroundMusicManager.getInstance(this).play(R.raw.home_theme);

        init();
        fetchUserProfile();
        loadAllMyPokemon();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        fetchUserProfile();
        loadAllMyPokemon();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        txtInitial = findViewById(R.id.username_initial);
        txtUsername = findViewById(R.id.username);
        txtPokeCount = findViewById(R.id.pokemon_count);
        txtPokeSold = findViewById(R.id.soldPokemons);
        txtShardsEarned = findViewById(R.id.earnedShardsBySelling);
        nameSort = findViewById(R.id.sortByAlphabet);
        levelSort = findViewById(R.id.sortByLevel);
        raritySort = findViewById(R.id.sortByRarity);
        dateSort = findViewById(R.id.sortByDate);

        recyclerViewCollection = findViewById(R.id.recyclerViewCollection);

        recyclerViewCollection.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewCollection.addItemDecoration(new GridSpacingItemDecoration(2, 16, false));

        activeFilter = new LinkedList<>();

        nameSort.setOnCheckedChangeListener((button, isChecked) -> {
            nameSortCount = nameSortCount + 1 < 3 ? nameSortCount + 1 : 0;
            if (nameSortCount > 0) {
                nameSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                nameSort.setTextColor(getResources().getColor(R.color.background, null));

                if (!activeFilter.contains(nameSort)) {
                    if (activeFilter.size() >= 2) activeFilter.poll();
                    activeFilter.add(nameSort);
                }

            }
            if (nameSortCount == 1) {
                nameSort.setText("Name: ↑");
            } else if (nameSortCount == 2) {
                nameSort.setText("Name: ↓");
            } else {
                nameSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                nameSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                nameSort.setText("Name");
                activeFilter.remove(nameSort);
            }
            sort();
            snapRecyclerViewToStart();
        });

        levelSort.setOnCheckedChangeListener((button, isChecked) -> {
            levelSortCount = levelSortCount + 1 < 3 ? levelSortCount + 1 : 0;
            if (levelSortCount > 0) {
                levelSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                levelSort.setTextColor(getResources().getColor(R.color.background, null));

                if (!activeFilter.contains(levelSort)) {
                    if (activeFilter.size() >= 2) activeFilter.poll();
                    activeFilter.add(levelSort);
                }
            }
            if (levelSortCount == 1) {
                levelSort.setText("Level: ↑");
            } else if (levelSortCount == 2) {
                levelSort.setText("Level: ↓");
            } else {
                levelSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                levelSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                levelSort.setText("Level");

                activeFilter.remove(levelSort);
            }
            sort();
            snapRecyclerViewToStart();
        });

        raritySort.setOnCheckedChangeListener((button, isChecked) -> {
            raritySortCount = raritySortCount + 1 < 3 ? raritySortCount + 1 : 0;
            if (raritySortCount > 0) {
                raritySort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                raritySort.setTextColor(getResources().getColor(R.color.background, null));

                if (!activeFilter.contains(raritySort)) {
                    if (activeFilter.size() >= 2) activeFilter.poll();
                    activeFilter.add(raritySort);
                }
            }
            if (raritySortCount == 1) {
                raritySort.setText("Rarity: ↑");
            } else if (raritySortCount == 2) {
                raritySort.setText("Rarity: ↓");
            } else {
                raritySort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                raritySort.setTextColor(getResources().getColor(R.color.textVariant, null));
                raritySort.setText("Rarity");

                activeFilter.remove(raritySort);
            }
            sort();
            snapRecyclerViewToStart();
        });

        dateSort.setOnCheckedChangeListener((button, isChecked) -> {
            dateSortCount = dateSortCount + 1 < 3 ? dateSortCount + 1 : 0;
            if (dateSortCount > 0) {
                dateSort.setBackground(getResources().getDrawable(R.drawable.rectangle_white, null));
                dateSort.setTextColor(getResources().getColor(R.color.background, null));

                if (!activeFilter.contains(dateSort)) {
                    if (activeFilter.size() >= 2) activeFilter.poll();
                    activeFilter.add(dateSort);
                }
            }
            if (dateSortCount == 1) {
                dateSort.setText("Date: ↑");
            } else if (dateSortCount == 2) {
                dateSort.setText("Date: ↓");
            } else {
                dateSort.setBackground(getResources().getDrawable(R.drawable.rectangle, null));
                dateSort.setTextColor(getResources().getColor(R.color.textVariant, null));
                dateSort.setText("Date");

                activeFilter.remove(dateSort);
            }
            sort();
            snapRecyclerViewToStart();
        });

        Navigation.setup(this);
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
            int res = 0;

            for (CheckBox cb: activeFilter) {
                if (cb == nameSort) {
                    res = nameSortCount == 2
                            ? p2.getName().compareToIgnoreCase(p1.getName())
                            : p1.getName().compareToIgnoreCase(p2.getName());
                } else if (cb == levelSort) {
                    res = levelSortCount == 2
                            ? Integer.compare(p2.getLevel(), p1.getLevel())
                            : Integer.compare(p1.getLevel(), p2.getLevel());
                } else if (cb == raritySort) {
                    res = raritySortCount == 2
                            ? Integer.compare(p2.getRarity(), p1.getRarity())
                            : Integer.compare(p1.getRarity(), p2.getRarity());
                } else if (cb == dateSort) {
                    res = dateSortCount == 2
                            ? Long.compare(p2.getSummonedAt(), p1.getSummonedAt())
                            : Long.compare(p1.getSummonedAt(), p2.getSummonedAt());
                }

                if (res != 0) return res;
            }
            return 0;
        });

        if (recyclerViewCollection.getAdapter() != null) {
            recyclerViewCollection.getAdapter().notifyDataSetChanged();
        }
    }

    private void fetchUserProfile() {
        int pokemonCount = UserManager.getInstance().getUser().getPokemonCount();
        int pokemonSold = UserManager.getInstance().getUser().getPokemonSold();
        int earnings = UserManager.getInstance().getUser().getEarnedShardsBySelling();
        String username = UserManager.getInstance().getUser().getUsername();

        txtUsername.setText(Localizer.toTitleCase(username));
        txtInitial.setText(Localizer.toTitleCase(username).substring(0,1));
        txtPokeCount.setText(String.format("%,d", pokemonCount));
        txtPokeSold.setText(String.format("%,d", pokemonSold));
        txtShardsEarned.setText(String.format("%,d", earnings));
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