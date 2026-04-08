package sickbay.pokenamon.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sickbay.pokenamon.R;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.core.Gacha;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.model.User;

public class Register extends AppCompatActivity {
    Context context = this;
    EditText email, username, password, confirmPassword;
    Button signUp;
    TextView alreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_register);
        init();
        action();
    }

    private void init(){
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.signUp);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
    }

    private void action(){
        alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });

        signUp.setOnClickListener(v -> {
            if(email.getText().toString().isEmpty() ||
                    username.getText().toString().isEmpty() ||
                    password.getText().toString().isEmpty() ||
                    confirmPassword.getText().toString().isEmpty()){
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }else{
                if(password.getText().toString().length() < 6){
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().equals(confirmPassword.getText().toString())) {
                    registerUser(email.getText().toString(), password.getText().toString(), username.getText().toString());
                } else {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void registerUser(String email, String password, String username) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        User newUser = new User(username, email);

                        db.child("users").child(userId).setValue(newUser)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        UserManager.getInstance().setUser(newUser);
                                        new Auth(context).setRememberMe(email, password);
                                        Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, Home.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
