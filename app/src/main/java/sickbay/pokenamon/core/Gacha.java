package sickbay.pokenamon.core;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.network.PokeAPIManager;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.gacha.FetchGachaListener;
import sickbay.pokenamon.system.gacha.GetGachaPokemonListener;

public class Gacha extends AppCompatActivity {
    private static final Random rand = new Random();
    private final int DRAW_10X = 900;
    private final int DRAW_1X = 100;
    private ObjectAnimator shake;
    LinearLayout draw10x, draw1x;
    ImageView pokeball;
    TextView tvCoins;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_gacha);

        BackgroundMusicManager.getInstance(this).play(R.raw.gacha_theme);

        init();
        action();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (shake != null) {
            shake.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        draw1x.setEnabled(true);
        draw10x.setEnabled(true);
        pokeball.setTranslationX(0);
        tvCoins.setText(String.format("%,d", UserManager.getInstance().getUser().getCoins()));
    }

    private void init() {
        draw10x = findViewById(R.id.draw10x);
        draw1x = findViewById(R.id.draw1x);
        pokeball = findViewById(R.id.pokeball);
        tvCoins = findViewById(R.id.coins);

        tvCoins.setText(String.format("%,d", UserManager.getInstance().getUser().getCoins()));
        Navigation.setup(this);
    }

    private void action() {
        draw10x.setOnClickListener(v -> {
            if (isFinishing() || isDestroyed()) return;

            if (UserManager.getInstance().getUser().getCoins() >= DRAW_10X) {
                gacha(10, DRAW_10X);
            } else {
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });

        draw1x.setOnClickListener(v -> {
            if (isFinishing() || isDestroyed()) return;

            if (UserManager.getInstance().getUser().getCoins() >= DRAW_1X) {
                gacha(1, DRAW_1X);
            } else {
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gacha(int pulls, int price) {
        draw1x.setEnabled(false);
        draw10x.setEnabled(false);

        double[] chosenRand = new double[pulls];

        UserManager.getInstance().updateCoins(-price)
                .addOnCompleteListener(Gacha.this, (task) -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < chosenRand.length; i++) {
                            chosenRand[i] = .1 + (101 - .1) * rand.nextDouble();
                        }

                        animatePokeballAndFetch(chosenRand, new FetchGachaListener() {
                            @Override
                            public void onComplete(ArrayList<PokemonDTO> results) {
                                tvCoins.setText(String.format("%,d", UserManager.getInstance().getUser().getCoins()));

                                draw1x.setEnabled(true);
                                draw10x.setEnabled(true);

                                savePokemonToInventory(results);

                                Bundle bundle = new Bundle();
                                bundle.putParcelableArrayList("pulls", results);
                                Intent intent = new Intent(Gacha.this, GachaPull.class);
                                intent.putExtras(bundle);

                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }

                            @Override
                            public void onError(String message) {
                                draw1x.setEnabled(true);
                                draw10x.setEnabled(true);

                                UserManager.getInstance().updateCoins(price)
                                        .addOnCompleteListener(Gacha.this, (childTask) -> tvCoins.setText(String.format("%,d", UserManager.getInstance().getUser().getCoins())))
                                        .addOnFailureListener(Gacha.this, (error) -> {
                                            Log.d("Gacha", error.getMessage(), error);
                                            Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_SHORT).show();
                                        });
                                Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(Gacha.this, (error) -> {
                    Log.d("Gacha", error.getMessage(), error);
                    Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_SHORT).show();
                });
    }

    private void savePokemonToInventory(ArrayList<PokemonDTO> summonedPokemon) {
        String uid = UserManager.getInstance().getUser().getUid();

        long summonedAt = System.currentTimeMillis();
        for (PokemonDTO pokemon: summonedPokemon) {
            String id = DB.getDatabaseInstance().getUserInventoryReference(uid).push().getKey();

            if (id != null) {
                pokemon.setCollectionId(id);
                pokemon.setSummonedAt(summonedAt);
                DB.getDatabaseInstance().getUserInventoryReference(uid).child(id).setValue(pokemon);
            }
        }

        UserManager.getInstance().updatePokemonCount(summonedPokemon.size());
    }

    private void animatePokeballAndFetch(double[] chosenRand, FetchGachaListener listener) {
        if (shake != null) {
            shake.removeAllListeners();
            shake.cancel();
        }

        shake = ObjectAnimator.ofFloat(pokeball, "translationX", -20f, 20f);
        shake.setDuration(150);
        shake.setRepeatCount(ValueAnimator.INFINITE);
        shake.setRepeatMode(ValueAnimator.REVERSE);
        shake.start();

        fetchGachaData(chosenRand, listener, System.currentTimeMillis());
    }

    private void fetchGachaData(double[] chosenRand, FetchGachaListener listener, long startTime) {
        AtomicInteger req = new AtomicInteger(chosenRand.length);
        ArrayList<PokemonDTO> summonedPokemon = new ArrayList<>();

        for (double luck : chosenRand) {
            String tier = (luck <= .5) ? "legendary" : (luck <= 8) ? "ultra_rare" : (luck <= 30) ? "rare" : "common";
            DB.getDatabaseInstance().getGachaMetadataTierReference(tier).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int randomPos = new Random().nextInt((int) snapshot.getChildrenCount());
                    int currentPos = 0;

                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (currentPos == randomPos) {
                            int pokemonId = Integer.parseInt(String.valueOf(child.child("id").getValue()));
                            int pokemonRarity = Integer.parseInt(String.valueOf(child.child("stars").getValue()));

                            Log.i("PULLING", "About to fetch");

                            PokeAPIManager.getInstance(getApplicationContext()).getGachaPokemon(pokemonId, new GetGachaPokemonListener() {
                                @Override
                                public void onComplete(Pokemon pokemon) {
                                    pokemon.setRarity(pokemonRarity);
                                    summonedPokemon.add(pokemon.toPokemonDTO());

                                    if (req.decrementAndGet() == 0) {
                                        long remainingTime = Math.max(0, System.currentTimeMillis() - startTime);

                                        shake.cancel();
                                        pokeball.setTranslationX(0);
                                        pokeball.postDelayed(() -> listener.onComplete(summonedPokemon), remainingTime);
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    shake.cancel();
                                    pokeball.setTranslationX(0);
                                    Log.e("Gacha", message);
                                    Toast.makeText(Gacha.this, "Sorry! An error occurred..", Toast.LENGTH_SHORT).show();
                                    listener.onError(message);
                                }
                            });
                            break;
                        }
                        currentPos++;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Gacha", error.getMessage(), error.toException());
                    Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_SHORT).show();
                    listener.onError(error.getMessage());
                }
            });
        }
    }
}