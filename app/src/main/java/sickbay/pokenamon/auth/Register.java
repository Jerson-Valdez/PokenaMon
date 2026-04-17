package sickbay.pokenamon.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.text.SimpleDateFormat;
import java.util.Date;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.db.DB;
import sickbay.pokenamon.model.User;

public class Register extends AppCompatActivity {
    Context context = this;
    EditText emailField, usernameField, passwordField, confirmPasswordField;
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
        emailField = findViewById(R.id.email);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.confirmPassword);
        signUp = findViewById(R.id.signUp);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
    }

    private void action(){
        alreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            overridePendingTransition(0, 0);
        });

        signUp.setOnClickListener(v -> {
            String emailValue = emailField.getText().toString().trim();
            String usernameValue = usernameField.getText().toString().trim();
            String passwordValue = passwordField.getText().toString().trim();
            String confirmPasswordValue = confirmPasswordField.getText().toString().trim();

            if(emailValue.isEmpty() || usernameValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
                Toast.makeText(context, "Fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordValue.contentEquals(confirmPasswordValue)) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(emailValue, passwordValue, usernameValue);
            }
        );
    }

    private void toggleFields(boolean enable) {
        emailField.setEnabled(enable);
        usernameField.setEnabled(enable);
        passwordField.setEnabled(enable);
        confirmPasswordField.setEnabled(enable);
        signUp.setEnabled(enable);
    }

    private void registerUser(String email, String password, String username) {
        toggleFields(false);

        DB db = DB.getAuthInstance(this);

        db.createAuthUser(email, password,
                (task) -> {
                    if (task.isSuccessful()) {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        toggleFields(true);
                        String uid = db.getAuthUser().getUid();
                        User user = new User(uid, username, email);
                        user.setLastLogin(format.format(new Date()));

                        db.createUser(uid, user,
                                (childTask) -> {
                                    if (childTask.isSuccessful()) {
                                        UserManager.getInstance().setUser(user);
                                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Home.class));
                                        finish();
                                        overridePendingTransition(0, 0);
                                    }
                                },
                                (childError) -> {
                                    Log.e("Register", childError.getMessage(), childError);
                                    Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_LONG).show();
                                });
                    }
                },
                (error) -> {
                    toggleFields(true);
                    Log.e("Register", error.getMessage(), error);

                    String errorMessage = "Sorry! An error has occurred..";

                    if (error instanceof FirebaseAuthWeakPasswordException) {
                        errorMessage = "Use a stronger password!";
                    } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMessage = "Please use a valid email address!";
                    } else if (error instanceof FirebaseAuthUserCollisionException) {
                        errorMessage = "The account already exists!";
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                });
    }
}
