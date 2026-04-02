package sickbay.pokenamon.core;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sickbay.pokenamon.R;
import sickbay.pokenamon.controller.PokemonAdapter;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.model.User;

public class Gacha extends AppCompatActivity {
    Button draw10x, draw1x;
    ImageView pokeball;
    RecyclerView recyclerViewGacha;

    Context context = this;
    int coins = 0;

    int finishedPulls = 0;
    int[] chosenRand;

    List<Pokemon> pulledPokemonList;
    PokemonAdapter adapter;

    // CRITICAL FIX: Only ONE client for the whole app to prevent Memory Exhaustion
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_gacha);
        init();
        action();
    }

    private void init() {
        draw10x = findViewById(R.id.draw10x);
        draw1x = findViewById(R.id.draw1x);
        pokeball = findViewById(R.id.pokeball);

        recyclerViewGacha = findViewById(R.id.recyclerViewGacha);
        recyclerViewGacha.setLayoutManager(new GridLayoutManager(this, 2));

        User currentUser = UserManager.getInstance().getUser();
        if (currentUser != null) {
            coins = currentUser.coins;
        } else {
            fetchCoinsFromDatabase();
        }
    }

    private void action() {
        draw10x.setOnClickListener(v -> {
            if (coins >= 900) {
                updateUserCoins(-900);
                chosenRand = new int[10];
                for (int i = 0; i < 10; i++) {
                    chosenRand[i] = rand(1, 100);
                }
                animatePokeballAndFetch(10);
            } else {
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });

        draw1x.setOnClickListener(v -> {
            if (coins >= 100) {
                updateUserCoins(-100);
                chosenRand = new int[1];
                chosenRand[0] = rand(1, 100);
                Toast.makeText(context, "" + chosenRand[0], Toast.LENGTH_SHORT).show();
                animatePokeballAndFetch(1);
            } else {
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------
    // DATABASE & ANIMATION LOGIC
    // -------------------------------------------------------------

    private void updateUserCoins(int amountToChange) {
        coins += amountToChange;
        if (UserManager.getInstance().getUser() != null) {
            UserManager.getInstance().getUser().coins = coins;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(uid).child("coins").setValue(coins);
    }

    private void fetchCoinsFromDatabase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("coins")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Safe parsing just in case it was stored as a String or Double
                            coins = Integer.parseInt(String.valueOf(snapshot.getValue()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void savePokemonToInventory(List<Pokemon> newPokemonList) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference inventoryRef = FirebaseDatabase.getInstance().getReference("user_inventory").child(uid);
        for (Pokemon p : newPokemonList) {
            inventoryRef.push().setValue(p);
        }
        Toast.makeText(context, "Saved to your Collection!", Toast.LENGTH_SHORT).show();
    }

    private int rand(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    private void animatePokeballAndFetch(int pulls) {
        if (recyclerViewGacha != null) {
            recyclerViewGacha.setVisibility(View.GONE);
        }
        pokeball.setVisibility(View.VISIBLE);

        draw1x.setEnabled(false);
        draw10x.setEnabled(false);

        ObjectAnimator shake = ObjectAnimator.ofFloat(pokeball, "translationX",
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 5f, -5f, 0f);
        shake.setDuration(1200);
        shake.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // CRITICAL FIX: Prevent crash if user exits app during animation
            if (isFinishing() || isDestroyed()) return;

            pokeball.setVisibility(View.GONE);
            recyclerViewGacha.setVisibility(View.VISIBLE);

            draw1x.setEnabled(true);
            draw10x.setEnabled(true);
            Toast.makeText(context, "Fetching after animation", Toast.LENGTH_SHORT).show();
            fetchGachaData(pulls);

        }, 1200);
    }

    // -------------------------------------------------------------
    // API & GACHA CORE
    // -------------------------------------------------------------

    private void fetchGachaData(int maxPulls) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Summoning Pokémon from PokeAPI...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("gacha_metadata");
        pulledPokemonList = new ArrayList<>();
        finishedPulls = 0;

        for (int i = 0; i < maxPulls; i++) {
            int luck = chosenRand[i];
            String tier = (luck <= 5) ? "legendary" : (luck <= 20) ? "ultra_rare" : (luck <= 50) ? "rare" : "common";

            db.child(tier).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int randomPos = new Random().nextInt((int) snapshot.getChildrenCount());
                        int currentPos = 0;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (currentPos == randomPos) {

                                // CRITICAL FIX: Safe Parsing to prevent Unboxing NPE crashes
                                Object idObj = child.child("id").getValue();
                                Object starObj = child.child("stars").getValue();

                                int pId = (idObj != null) ? Integer.parseInt(String.valueOf(idObj)) : 129;
                                int pStars = (starObj != null) ? Integer.parseInt(String.valueOf(starObj)) : 1;

                                fetchFromPokeApi(pId, pStars, maxPulls, progressDialog);
                                break;
                            }
                            currentPos++;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressDialog.dismiss();
                    Log.e("Gacha", "Firebase Error: " + error.getMessage());
                }
            });
        }
    }

    private void fetchFromPokeApi(int id, int stars, int maxPulls, ProgressDialog dialog) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + id;
        Request request = new Request.Builder().url(url).build();

        // Use the global client here
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        String name = jsonObject.getString("name");
                        String imageUrl = jsonObject.getJSONObject("sprites")
                                .getJSONObject("other")
                                .getJSONObject("official-artwork")
                                .getString("front_default");

                        JSONArray typesJson = jsonObject.getJSONArray("types");
                        List<String> types = new ArrayList<>();
                        for (int i = 0; i < typesJson.length(); i++) {
                            types.add(typesJson.getJSONObject(i).getJSONObject("type").getString("name"));
                        }

                        JSONArray allMoves = jsonObject.getJSONArray("moves");
                        List<String> chosenMoves = new ArrayList<>();
                        List<Integer> chosenPower = new ArrayList<>();
                        
                        // Pre-fill lists to use indices safely if needed, or just add as they come
                        for (int i = 0; i < 4; i++) {
                            chosenMoves.add("");
                            chosenPower.add(0);
                        }

                        Random r = new Random();
                        int[] movesFetched = {0};

                        // Edge case: If a Pokemon has less than 4 moves in the API
                        int moveLimit = Math.min(allMoves.length(), 4);

                        for (int i = 0; i < moveLimit; i++) {
                            int randomIdx = r.nextInt(allMoves.length());
                            JSONObject moveObj = allMoves.getJSONObject(randomIdx).getJSONObject("move");

                            chosenMoves.set(i, moveObj.getString("name"));
                            String moveUrl = moveObj.getString("url");
                            final int currentMoveIndex = i;

                            Request moveReq = new Request.Builder().url(moveUrl).build();
                            client.newCall(moveReq).enqueue(new Callback() {
                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response moveResponse) throws IOException {
                                    if (moveResponse.isSuccessful() && moveResponse.body() != null) {
                                        try {
                                            JSONObject moveJson = new JSONObject(moveResponse.body().string());
                                            chosenPower.set(currentMoveIndex, moveJson.optInt("power", 0));
                                        } catch (Exception e) {
                                            chosenPower.set(currentMoveIndex, 0);
                                        }
                                    }

                                    synchronized (movesFetched) {
                                        movesFetched[0]++;
                                        if (movesFetched[0] == moveLimit) {
                                            finalizePokemonPull(id, name, types, stars, chosenMoves, chosenPower, imageUrl, maxPulls, dialog);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    synchronized (movesFetched) {
                                        movesFetched[0]++;
                                        if (movesFetched[0] == moveLimit) {
                                            finalizePokemonPull(id, name, types, stars, chosenMoves, chosenPower, imageUrl, maxPulls, dialog);
                                        }
                                    }
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(dialog::dismiss);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(dialog::dismiss);
            }
        });
    }

    private void finalizePokemonPull(int id, String name, List<String> types, int stars,
                                     List<String> moves, List<Integer> movePower, String imageUrl,
                                     int maxPulls, ProgressDialog dialog) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Pokemon newPokemon = new Pokemon(id, name, types, stars, moves, movePower, imageUrl, uid);

        runOnUiThread(() -> {
            pulledPokemonList.add(newPokemon);
            finishedPulls++;

            if (finishedPulls == maxPulls) {
                dialog.dismiss();

                adapter = new PokemonAdapter(pulledPokemonList);
                recyclerViewGacha.setAdapter(adapter);

                savePokemonToInventory(pulledPokemonList);
            }
        });
    }
}