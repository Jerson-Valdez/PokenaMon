package sickbay.pokenamon.helper;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.core.Collection;
import sickbay.pokenamon.helper.BottomNavHelper;
import sickbay.pokenamon.model.Pokemon;
import sickbay.pokenamon.model.User;

public class PokemonDetail extends AppCompatActivity {

    TextView txtName, txtLevel, txtStars, txtType1, txtType2;
    ImageView imgPokemon;

    TextView[] moveNames = new TextView[4];
    TextView[] movePowers = new TextView[4];

    Button btnSell, btnSelect, btnBack;

    Pokemon pokemon;
    String uid;
    DatabaseReference userRef, inventoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pokemon_details);

        pokemon = (Pokemon) getIntent().getSerializableExtra("selected_pokemon");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        inventoryRef = FirebaseDatabase.getInstance().getReference("user_inventory").child(uid);

        init();
        BottomNavHelper.setup(this);
        renderPokemon();
    }

    private void init() {
        View card = findViewById(R.id.pokemonCard);
        txtName = card.findViewById(R.id.pokemon_name);
        txtLevel = card.findViewById(R.id.pokemon_level);
        txtStars = card.findViewById(R.id.pokemon_stars);
        txtType1 = card.findViewById(R.id.pokemon_type);
        txtType2 = card.findViewById(R.id.pokemon_type2);
        imgPokemon = card.findViewById(R.id.pokemon_image);

        moveNames[0] = findViewById(R.id.move1name);
        movePowers[0] = findViewById(R.id.move1pwr);
        moveNames[1] = findViewById(R.id.move2name);
        movePowers[1] = findViewById(R.id.move2pwr);
        moveNames[2] = findViewById(R.id.move3name);
        movePowers[2] = findViewById(R.id.move3pwr);
        moveNames[3] = findViewById(R.id.move4name);
        movePowers[3] = findViewById(R.id.move4pwr);

        btnSell = findViewById(R.id.sell);
        btnSelect = findViewById(R.id.select);
        btnBack = findViewById(R.id.back);

        int sellPrice = pokemon.getStars() * 100;
        btnSell.setText("Sell (" + sellPrice + ")");
        btnSell.setOnClickListener(v -> sellPokemon(sellPrice));
        btnBack.setOnClickListener(v -> finish());

        setButton();
        btnSelect.setOnClickListener(v -> selectForBattle());
    }

    private void setButton() {
        String currentPokemonId = String.valueOf(pokemon.getTimestamp());
        String selectedIdInProfile = UserManager.getInstance().getUser().selectedPokemonId;

        if (selectedIdInProfile != null && selectedIdInProfile.equals(currentPokemonId)) {
            btnSelect.setEnabled(false);
            btnSelect.setText("Selected");

            btnSelect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary));
            btnSelect.setTextColor(ContextCompat.getColor(this, R.color.textVariant));
        } else {
            btnSelect.setEnabled(true);
            btnSelect.setText("Select");
            btnSelect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
            btnSelect.setTextColor(ContextCompat.getColor(this, R.color.text));
        }
    }

    private void renderPokemon() {
        if (pokemon == null) return;

        txtName.setText(pokemon.getName());
        txtLevel.setText("LVL. " + pokemon.getLevel());

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < pokemon.getStars(); i++) stars.append("★");
        txtStars.setText(stars.toString());

        List<String> types = pokemon.getTypes();
        if (types != null && !types.isEmpty()) {
            int primaryColor = ContextCompat.getColor(this, getColorRes(types.get(0), false));
            txtLevel.setTextColor(primaryColor);
            txtStars.setTextColor(primaryColor);

            setupTypeBadge(txtType1, types.get(0));
            if (types.size() > 1) {
                txtType2.setVisibility(View.VISIBLE);
                setupTypeBadge(txtType2, types.get(1));
            } else {
                txtType2.setVisibility(View.GONE);
            }
        }

        List<String> moves = pokemon.getMoves();
        List<Integer> powers = pokemon.getMovePower();
        for (int i = 0; i < 4; i++) {
            if (i < moves.size() && !moves.get(i).isEmpty()) {
                moveNames[i].setText(moves.get(i).toUpperCase());
                movePowers[i].setText(powers.get(i) + " PWR");
            } else {
                moveNames[i].setText("---");
                movePowers[i].setText("0 PWR");
            }
        }

        Glide.with(this).load(pokemon.getImageUrl()).into(imgPokemon);
    }

    private void sellPokemon(int price) {
        inventoryRef.orderByChild("timestamp").equalTo(pokemon.getTimestamp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {

                            child.getRef().removeValue();

                            userRef.child("coins").setValue(ServerValue.increment(price));
                            userRef.child("pokemonCount").setValue(ServerValue.increment(-1));

                            UserManager.getInstance().getUser().coins += price;
                            UserManager.getInstance().getUser().pokemonCount--;

                            Toast.makeText(PokemonDetail.this, "Sold for " + price + " coins!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PokemonDetail.this, Collection.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void selectForBattle() {
        userRef.child("selectedPokemonId").setValue(String.valueOf(pokemon.getTimestamp()))
                .addOnSuccessListener(aVoid -> {
                    UserManager.getInstance().getUser().selectedPokemonId = String.valueOf(pokemon.getTimestamp());
                    Toast.makeText(this, pokemon.getName() + " selected for Battle!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupTypeBadge(TextView textView, String type) {
        textView.setText(type.toUpperCase());
        int mainColor = ContextCompat.getColor(this, getColorRes(type, false));
        int bgColor = ContextCompat.getColor(this, getColorRes(type, true));
        textView.setTextColor(mainColor);
        if (textView.getBackground() != null) {
            GradientDrawable drawable = (GradientDrawable) textView.getBackground().mutate();
            drawable.setColor(bgColor);
            drawable.setStroke(3, mainColor);
        }
    }

    private int getColorRes(String type, boolean isBackground) {
        String colorName = type.toLowerCase() + (isBackground ? "1" : "");
        int resId = getResources().getIdentifier(colorName, "color", getPackageName());
        return (resId != 0) ? resId : R.color.normal;
    }
}