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

public class TicketDetailsActivity extends AppCompatActivity {

    private String currentUserId, ticketId;
    private TextView tvEventName, tvEventDescription, tvEventLocation, tvEventDate, tvEventTime, tvTicketPrice, tvQuantityOfTicket;
    private Button btnAvailability;
    private DatabaseReference ticketsReference;
    private ValueEventListener ticketDetailsListener;
    private Boolean isAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        // Retrieve ticket and user IDs from intent.
        retrieveIntentData();

        // Initialize Firebase Database reference.
        initializeFirebaseReference();

        // Initialize UI elements.
        initializeUI();

        // Fetch ticket details from Firebase.
        fetchTicketDetailsFromDatabase();

        // Set click listener for availability button.
        setAvailabilityButtonClickListener();
    }

    // Retrieve ticket and user IDs from intent.
    private void retrieveIntentData() {
        ticketId = getIntent().getStringExtra("TICKET_ID");
        currentUserId = getIntent().getStringExtra("USER_ID");
    }

    // Initialize Firebase Database reference.
    private void initializeFirebaseReference() {
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
    }

    // Initialize UI elements.
    private void initializeUI() {
        tvEventName = findViewById(R.id.tvEventName);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvTicketPrice = findViewById(R.id.tvPrice);
        tvQuantityOfTicket = findViewById(R.id.tvQuantityOfTicket);
        btnAvailability = findViewById(R.id.btnAvailability);
        Button btnPrevious = findViewById(R.id.btnPrevious);

        // Set click listener for previous button.
        btnPrevious.setOnClickListener(view -> navigateToTicketMasterDashboard());
    }

    // Navigate to TicketMasterDashboardActivity.
    private void navigateToTicketMasterDashboard() {
        Intent toMasterActivity = new Intent(TicketDetailsActivity.this, TicketMasterDashboardActivity.class);
        toMasterActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        toMasterActivity.putExtra("USER_ID", currentUserId);
        startActivity(toMasterActivity);
        finish();
    }

    // Fetch ticket details from Firebase.
    private void fetchTicketDetailsFromDatabase() {
        if (TextUtils.isEmpty(ticketId)) {
            Toast.makeText(this, "Ticket ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketDetailsListener = ticketsReference.child(ticketId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateUIWithTicketDetails(snapshot);
                } else {
                    Toast.makeText(TicketDetailsActivity.this, "Ticket not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketDetailsActivity.this, "Error fetching ticket details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Update UI with ticket details from Firebase.
    private void updateUIWithTicketDetails(DataSnapshot snapshot) {
        String eventName = snapshot.child("event").getValue(String.class);
        String eventDescription = snapshot.child("eventDescription").getValue(String.class);
        String eventLocation = snapshot.child("eventLocation").getValue(String.class);
        String eventDate = snapshot.child("eventDate").getValue(String.class);
        String eventTime = snapshot.child("eventTime").getValue(String.class);
        Double price = snapshot.child("price").getValue(Double.class);
        Integer quantityOfTicket = snapshot.child("quantityOfTicket").getValue(Integer.class);
        isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);
        if (isAvailable == null) isAvailable = false;

        tvEventName.setText(eventName != null ? eventName : "N/A");
        tvEventDescription.setText(eventDescription != null ? eventDescription : "N/A");
        tvEventLocation.setText(eventLocation != null ? eventLocation : "N/A");
        tvEventDate.setText(eventDate != null ? eventDate : "N/A");
        tvEventTime.setText(eventTime != null ? eventTime : "N/A");
        tvTicketPrice.setText(price != null ? String.valueOf(price) : "N/A");
        tvQuantityOfTicket.setText(quantityOfTicket != null ? String.valueOf(quantityOfTicket) : "N/A");
        updateButtonText();
    }

    // Set click listener for availability button.
    private void setAvailabilityButtonClickListener() {
        btnAvailability.setOnClickListener(view -> toggleAvailability(!isAvailable));
    }

    // Toggle ticket availability in Firebase.
    private void toggleAvailability(boolean availability) {
        btnAvailability.setEnabled(false);
        ticketsReference.child(ticketId).child("isAvailable").setValue(availability, (error, ref) -> {
            btnAvailability.setEnabled(true);
            if (error == null) {
                isAvailable = availability;
                updateButtonText();
                Toast.makeText(TicketDetailsActivity.this, "Ticket marked as " + (availability ? "available" : "unavailable"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TicketDetailsActivity.this, "Failed to update availability", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update availability button text.
    private void updateButtonText() {
        if (btnAvailability != null) {
            btnAvailability.setText(isAvailable ? "Mark as Unavailable" : "Mark as Available");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketsReference != null && ticketId != null && ticketDetailsListener != null) {
            ticketsReference.child(ticketId).removeEventListener(ticketDetailsListener);
        }
    }
}