package sickbay.pokenamon.auth;

import android.app.ProgressDialog; // Added for loading
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // Added for shorter toast
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sickbay.pokenamon.R;
import sickbay.pokenamon.controller.UserManager;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.model.User;

public class Register extends AppCompatActivity {
    Context context = this;
    EditText email, username, password, confirmPassword;
    Button signUp;
    TextView alreadyHaveAccount;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your trainer profile...");
        progressDialog.setCancelable(false);

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
            String txtEmail = email.getText().toString().trim();
            String txtUsername = username.getText().toString().trim();
            String txtPassword = password.getText().toString().trim();
            String txtConfirm = confirmPassword.getText().toString().trim();

            if(txtEmail.isEmpty() || txtUsername.isEmpty() || txtPassword.isEmpty() || txtConfirm.isEmpty()){
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if(txtPassword.length() < 6){
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!txtPassword.equals(txtConfirm)) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                signUp.setEnabled(false);

                progressDialog.show();

                registerUser(txtEmail, txtPassword, txtUsername);
            }
        });
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
                                    progressDialog.dismiss();

                                    if (dbTask.isSuccessful()) {
                                        UserManager.getInstance().setUser(newUser);
                                        new Auth(context).setRememberMe(email, password);
                                        Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, Home.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        signUp.setEnabled(true);
                                        Toast.makeText(context, "Database Error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        signUp.setEnabled(true);

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(context, "Email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Registration Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}