package sickbay.pokenamon.core;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sickbay.pokenamon.R;

public class Gacha extends AppCompatActivity {
    Button draw10x, draw1x;
    ImageView pokeball;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_gacha);
        init();
    }

    private void init(){
        draw10x = findViewById(R.id.draw10x);
        draw1x = findViewById(R.id.draw1x);
        pokeball = findViewById(R.id.pokeball);


    }
}
