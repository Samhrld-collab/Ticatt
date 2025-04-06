package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Database reference to "users" node.
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements.
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignup = findViewById(R.id.btnRegister);
        Button btnPrevious = findViewById(R.id.btnPrevious);

        // Autofill email if passed from SignupActivity.
        String emailAutofill = getIntent().getStringExtra("EMAIL");
        if (emailAutofill != null) {
            etEmail.setText(emailAutofill);
        }

        // Navigate to MainActivity.
        btnPrevious.setOnClickListener(view -> {
            Intent toMainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(toMainActivity);
            finish();
        });

        // Navigate to SignupActivity.
        btnSignup.setOnClickListener(view -> {
            Intent toSignupActivity = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(toSignupActivity);
            finish();
        });

        // Handle login button click.
        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate input fields.
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate user with Firebase.
            loginUser(email, password);
        });
    }

    // Authenticate user with Firebase database.
    private void loginUser(String email, String password) {
        // Query database to find user by email.
        Query userQuery = usersReference.orderByChild("email").equalTo(email);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Iterate through the results.
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        Boolean isTicketMaster = userSnapshot.child("isTicketMaster").getValue(Boolean.class);

                        // Check password and navigate to appropriate dashboard.
                        if (password.equals(storedPassword)) {
                            Toast.makeText(LoginActivity.this, "Login Successful" + (isTicketMaster != null && isTicketMaster ? " (Ticket Master)" : ""), Toast.LENGTH_SHORT).show();
                            Intent loginToDashboard = getDashboardIntentByUserRole(userSnapshot, isTicketMaster);
                            startActivity(loginToDashboard);
                            finish();
                            return;
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "No user found with this email", Toast.LENGTH_SHORT).show();
                }
            }

            // Determine dashboard based on user role.
            @NonNull
            private Intent getDashboardIntentByUserRole(DataSnapshot userSnapshot, Boolean isInstructor) {
                Intent loginToDashboard;
                if (isInstructor != null && isInstructor) {
                    loginToDashboard = new Intent(LoginActivity.this, TicketMasterDashboardActivity.class);
                } else {
                    loginToDashboard = new Intent(LoginActivity.this, HomeDashboardActivity.class);
                }
                loginToDashboard.putExtra("USER_ID", userSnapshot.getKey());
                return loginToDashboard;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}