package sickbay.pokenamon.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.model.User;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.util.SecurePreferences;

public class Login extends AppCompatActivity {
    Context context = this;
    EditText emailField, passwordField;
    Button login;
    CheckBox rememberMe;
    TextView goToRegister;
    FirebaseAuth auth;
    SecurePreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_login);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(BackgroundMusicManager.getInstance(getApplicationContext()));
        BackgroundMusicManager.getInstance(this).pause();

        auth = FirebaseAuth.getInstance();
        prefs = new SecurePreferences(this, "credentials", true);

        init();
        action();
    }

    private void init() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        login = findViewById(R.id.login);
        goToRegister = findViewById(R.id.dontHaveAccount);
        rememberMe = findViewById(R.id.rememberMe);

        if (prefs.getString("email") != null) {
            rememberMe.setChecked(true);
            emailField.setText(prefs.getString("email"));
            passwordField.setText(prefs.getString("password"));

            loginUser(prefs.getString("email"), prefs.getString("password"));
        }
    }

    private void action() {
        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
            overridePendingTransition(0, 0);
        });

        login.setOnClickListener(v -> {
            String emailValue = emailField.getText().toString().trim();
            String passwordValue = passwordField.getText().toString().trim();

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                Toast.makeText(context, "Please enter your credentials!", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(emailValue, passwordValue);
        });
    }

    private void toggleFields(boolean enable) {
        emailField.setEnabled(enable);
        passwordField.setEnabled(enable);
        login.setEnabled(enable);
    }

    private void loginUser(String email, String password) {
        toggleFields(false);

        DB db = DB.getAuthInstance(this);
        db.signInAuthUser(email, password,
                (task) -> {
                    if (task.isSuccessful()) {
                        toggleFields(true);

                        if (rememberMe.isChecked()) {
                            prefs.put("email", email);
                            prefs.put("password", password);
                        }
                        fetchUserProfile(db.getAuthUser().getUid());
                    }
                },
                (error) -> {
                    toggleFields(true);
                    Log.e("Login", error.getMessage(), error);
                    Toast.makeText(context, "Please log in to a valid account!", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserProfile(String uid) {
        DB.getDatabaseInstance().getUserReference(uid)
            .addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            user.setUid(uid);

                            UserManager.getInstance().setUser(user);

                            Toast.makeText(context, "Welcome back, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Home.class));
                            finish();
                            overridePendingTransition(0, 0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Log.e("Login", error.getMessage(), error.toException());
                        Toast.makeText(context, "Sorry! We couldn't log you in..", Toast.LENGTH_SHORT).show();
                    }
            });
    }
}