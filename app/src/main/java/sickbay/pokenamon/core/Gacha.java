package sickbay.pokenamon.core;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import sickbay.pokenamon.R;

public class Gacha extends AppCompatActivity {
    Button draw10x, draw1x;
    ImageView pokeball;
    Context context = this;
    double coins = 2000;
    int[][] gachaResultsInt;
    String[][] gachaResultsString;
    int finishedPulls = 0;
    int chosenRand[];
    String chosenPokemon = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_gacha);
        init();
        action();
    }

    private void init(){
        draw10x = findViewById(R.id.draw10x);
        draw1x = findViewById(R.id.draw1x);
        pokeball = findViewById(R.id.pokeball);
    }

    private void action(){
        draw10x.setOnClickListener(v -> {
            if(coins >= 900){
                coins -= 900;

                chosenRand = new int[10];

                for (int i = 0; i < 10; i++){
                     chosenRand[i] = rand(1, 100);
                }

                fetchGachaData(10);
            }else{
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });

        draw1x.setOnClickListener(v -> {
            if(coins >= 100){
                coins -= 100;

                chosenRand = new int[10];

                for (int i = 0; i < 1; i++){
                    chosenRand[i] = rand(1, 100);
                }

                fetchGachaData(1);
            }else{
                Toast.makeText(context, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int rand(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    private void fetchGachaData(int maxPulls) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("gacha_metadata");

        gachaResultsInt = new int[maxPulls][2];
        gachaResultsString = new String[maxPulls][1];

        chosenPokemon = "";
        finishedPulls = 0;

        for (int i = 0; i < maxPulls; i++) {
            final int currentRow = i;
            int luck = chosenRand[i];

            String tier;
            if (luck <= 5) tier = "legendary";
            else if (luck <= 20) tier = "ultra_rare";
            else if (luck <= 50) tier = "rare";
            else tier = "common";

            db.child(tier).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        long totalInTier = snapshot.getChildrenCount();

                        int randomPos = new Random().nextInt((int) totalInTier);

                        int currentPos = 0;
                        DataSnapshot targetPokemon = null;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (currentPos == randomPos) {
                                targetPokemon = child;
                                break;
                            }
                            currentPos++;
                        }

                        if (targetPokemon != null) {
                            Integer pId = targetPokemon.child("id").getValue(Integer.class);
                            Integer pStars = targetPokemon.child("stars").getValue(Integer.class);
                            String pName = targetPokemon.child("name").getValue(String.class);

                            if (pId != null && pStars != null && pName != null) {
                                gachaResultsInt[currentRow][0] = pId;
                                gachaResultsInt[currentRow][1] = pStars;
                                gachaResultsString[currentRow][0] = pName;
                            }
                        }

                        finishedPulls++;
                        if (finishedPulls == maxPulls) {
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < maxPulls; j++) {
                                if (gachaResultsString[j][0] != null) {
                                    sb.append(gachaResultsString[j][0]).append(", ");
                                }
                            }
                            chosenPokemon = sb.toString();
                            Log.i("Gacha", "Final Draw Results: " + chosenPokemon);
                            Toast.makeText(context, "Results: " + chosenPokemon, Toast.LENGTH_LONG).show();
                            finishedPulls = 0;
                        }
                    } else {
                        Log.e("GachaError", "Tier folder not found: " + tier);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", error.getMessage());
                }
            });
        }
    }
}
