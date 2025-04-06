package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyTicketDetails extends AppCompatActivity {

    private String currentUserId, ticketId;
    private DatabaseReference userReferences, ticketsReference;
    private TextView tvUsername, tvUserEmail, tvEventName, tvEventDescription, tvEventLocation, tvEventDate, tvEventTime, tvTicketPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket_details);

        // Initialize Firebase Database references.
        initializeFirebaseReferences();

        // Retrieve user and ticket IDs from intent.
        retrieveIntentData();

        // Initialize UI elements.
        initializeUI();

        // Fetch user and ticket details from Firebase.
        fetchUserData();
        fetchTicketData();
    }

    // Initialize Firebase Database references.
    private void initializeFirebaseReferences() {
        userReferences = FirebaseDatabase.getInstance().getReference("users");
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
    }

    // Retrieve user and ticket IDs from intent.
    private void retrieveIntentData() {
        currentUserId = getIntent().getStringExtra("USER_ID");
        ticketId = getIntent().getStringExtra("TICKET_ID");
    }

    // Initialize UI elements.
    private void initializeUI() {
        tvUsername = findViewById(R.id.tvUsername);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvTicketPrice = findViewById(R.id.tvPrice);
        Button btnPrevious = findViewById(R.id.btnPrevious);

        // Set click listener for previous button.
        btnPrevious.setOnClickListener(view -> navigateToHomeDashboard());
    }

    // Navigate to HomeDashboardActivity.
    private void navigateToHomeDashboard() {
        Intent toHomeActivity = new Intent(this, HomeDashboardActivity.class);
        toHomeActivity.putExtra("USER_ID", currentUserId);
        startActivity(toHomeActivity);
        finishAffinity();
    }

    // Fetch user details from Firebase.
    private void fetchUserData() {
        if (TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userReferences.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateUIWithUserData(snapshot);
                } else {
                    Toast.makeText(MyTicketDetails.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketDetails.this, "Error fetching user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update UI with user details from Firebase.
    private void updateUIWithUserData(DataSnapshot snapshot) {
        String username = snapshot.child("username").getValue(String.class);
        String userEmail = snapshot.child("email").getValue(String.class);

        tvUsername.setText(username != null ? username : "N/A");
        tvUserEmail.setText(userEmail != null ? userEmail : "N/A");
    }

    // Fetch ticket details from Firebase.
    private void fetchTicketData() {
        if (TextUtils.isEmpty(ticketId)) {
            Toast.makeText(this, "Ticket ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketsReference.child(ticketId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateUIWithTicketData(snapshot);
                } else {
                    Toast.makeText(MyTicketDetails.this, "Ticket not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketDetails.this, "Error fetching ticket details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update UI with ticket details from Firebase.
    private void updateUIWithTicketData(DataSnapshot snapshot) {
        String eventName = snapshot.child("event").getValue(String.class);
        String eventDescription = snapshot.child("eventDescription").getValue(String.class);
        String eventLocation = snapshot.child("eventLocation").getValue(String.class);
        String eventDate = snapshot.child("eventDate").getValue(String.class);
        String eventTime = snapshot.child("eventTime").getValue(String.class);
        Double price = snapshot.child("price").getValue(Double.class);

        tvEventName.setText(eventName != null ? eventName : "N/A");
        tvEventDescription.setText(eventDescription != null ? eventDescription : "N/A");
        tvEventLocation.setText(eventLocation != null ? eventLocation : "N/A");
        tvEventDate.setText(eventDate != null ? eventDate : "N/A");
        tvEventTime.setText(eventTime != null ? eventTime : "N/A");
        tvTicketPrice.setText(price != null ? String.valueOf(price) : "N/A");
    }
}