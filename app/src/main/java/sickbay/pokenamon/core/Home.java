package sickbay.pokenamon.core;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import sickbay.pokenamon.R;
import sickbay.pokenamon.auth.Auth;
import sickbay.pokenamon.auth.Login;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_home);

        if (!new Auth(getApplicationContext()).isRemembered()) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
    }
}
