package sickbay.pokenamon.helper;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sickbay.pokenamon.R;

public class PokemonDetail extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_pokemon_details);

        init();
    }

    void init(){


        BottomNavHelper.setup(this);
    }
}
