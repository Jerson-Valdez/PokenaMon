package sickbay.pokenamon.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sickbay.pokenamon.R;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.model.User;

public class Login extends AppCompatActivity {
    Context context = this;
    EditText email, password;
    Button login;
    TextView goToRegister;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_login);

        auth = FirebaseAuth.getInstance();

        init();
        action();
    }

    private void init() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        goToRegister = findViewById(R.id.dontHaveAccount);
    }

    private void action() {
        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Register.class));
            finish();
        });

        login.setOnClickListener(v -> {
            String txtEmail = email.getText().toString().trim();
            String txtPassword = password.getText().toString().trim();

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(context, "Please enter your credentials", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(txtEmail, txtPassword);
            }
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        fetchUserProfile(auth.getCurrentUser().getUid(), email, password);
                    } else {
                        Toast.makeText(context, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchUserProfile(String userId, String email, String password) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(userId);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null) {
                        UserManager.getInstance().setUser(user);

                        new Auth(context).setRememberMe(email, password);

                        Toast.makeText(context, "Welcome back, " + user.username + "!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Login.this, Home.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}