package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbIsTicketMaster;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database reference for "users" node.
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements.
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbIsTicketMaster = findViewById(R.id.cbIsTicketMaster);
        Button btnSignup = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnBackToLogin);
        Button btnPrevious = findViewById(R.id.btnPrevious);

        // Navigate to MainActivity.
        btnPrevious.setOnClickListener(view -> {
            Intent toMainActivity = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(toMainActivity);
            finish();
        });

        // Navigate to LoginActivity.
        btnLogin.setOnClickListener(view -> {
            Intent toLoginActivity = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(toLoginActivity);
            finish();
        });

        // Handle signup button click.
        btnSignup.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            boolean isTicketMaster = cbIsTicketMaster.isChecked();

            // Validate input fields.
            if (validateInputs(username, email, password, confirmPassword)) {
                // Check if email is already registered.
                checkEmailAvailability(username, email, password, isTicketMaster);
            }
        });
    }

    // Validate input fields.
    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return false;
        }
        if (password.isEmpty() || password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    // Check if email already exists in the database.
    private void checkEmailAvailability(String username, String email, String password, boolean isTicketMaster) {
        Query emailQuery = usersReference.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignupActivity.this, "Email is already registered", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(username, email, password, isTicketMaster);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Register new user to the database.
    private void registerUser(String username, String email, String password, boolean isTicketMaster) {
        String userId = usersReference.push().getKey();

        if (userId == null) {
            Toast.makeText(this, "Error creating user", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("password", password);
        userMap.put("isTicketMaster", isTicketMaster);

        usersReference.child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        Intent toLoginActivity = new Intent(SignupActivity.this, LoginActivity.class);
                        toLoginActivity.putExtra("EMAIL", email);
                        startActivity(toLoginActivity);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}