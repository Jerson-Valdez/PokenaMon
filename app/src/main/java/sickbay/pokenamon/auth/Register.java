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

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.core.Home;
import sickbay.pokenamon.db.DB;
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
            startActivity(new Intent(this, Login.class));
            finish();
        });

        signUp.setOnClickListener(v -> {
            String emailValue = email.getText().toString().trim();
            String usernameValue = username.getText().toString().trim();
            String passwordValue = password.getText().toString().trim();
            String confirmPasswordValue = confirmPassword.getText().toString().trim();

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

    private void registerUser(String email, String password, String username) {
        signUp.setEnabled(false);

        DB db = DB.getAuthInstance(this);

        db.createAuthUser(email, password,
                (task) -> {
                    if (task.isSuccessful()) {
                        signUp.setEnabled(true);
                        String uid = db.getAuthUser().getUid();
                        User user = new User(uid, username, email);

                        db.createUser(uid, user,
                                (childTask) -> {
                                    if (childTask.isSuccessful()) {
                                        UserManager.getInstance().setUser(user);
                                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Home.class));
                                        finish();
                                    }
                                },
                                (childError) -> {
                                    Log.e("Register", childError.getMessage(), childError);
                                    Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_LONG).show();
                                });
                    }
                },
                (error) -> {
                    signUp.setEnabled(true);
                    Log.e("Register", error.getMessage(), error);
                    Toast.makeText(context, "Sorry! An error has occurred..", Toast.LENGTH_LONG).show();
                });
    }
}
