package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        Button btnLoginOption = findViewById(R.id.btnLoginOption);
        Button btnSignupOption = findViewById(R.id.btnSignupOption);

        // Set click listener for login button
        btnLoginOption.setOnClickListener(view -> {
            // Start LoginActivity and close MainActivity
            Intent toLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(toLoginActivity);
            finish();
        });

        // Set click listener for signup button
        btnSignupOption.setOnClickListener(view -> {
            // Start SignupActivity and close MainActivity
            Intent toSignupActivity = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(toSignupActivity);
            finish();
        });
    }
}