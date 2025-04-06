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

public class TicketPurchaseActivity extends AppCompatActivity {
    private String currentUserId, ticketId;
    private TextView tvEventName, tvEventDescription, tvEventLocation, tvEventDate, tvEventTime, tvTicketPrice;
    private DatabaseReference ticketsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_purchase);

        // Retrieve ticket and user IDs from intent.
        ticketId = getIntent().getStringExtra("TICKET_ID");
        currentUserId = getIntent().getStringExtra("USER_ID");

        // Initialize Firebase Database reference.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");

        // Initialize UI elements.
        initializeUI();

        // Fetch ticket details from Firebase.
        fetchTicketDetailsFromDatabase();

        // Set click listener for purchase button.
        setPurchaseButtonClickListener();
    }

    // Initialize UI elements.
    private void initializeUI() {
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
        Intent toHomeActivity = new Intent(TicketPurchaseActivity.this, HomeDashboardActivity.class);
        toHomeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        toHomeActivity.putExtra("USER_ID", currentUserId);
        startActivity(toHomeActivity);
        finish();
    }

    // Fetch ticket details from Firebase.
    private void fetchTicketDetailsFromDatabase() {
        if (TextUtils.isEmpty(ticketId)) {
            Toast.makeText(this, "Ticket ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketsReference.child(ticketId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateUIWithTicketDetails(snapshot);
                } else {
                    Toast.makeText(TicketPurchaseActivity.this, "Ticket not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketPurchaseActivity.this, "Error fetching ticket details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        tvEventName.setText(eventName != null ? eventName : "N/A");
        tvEventDescription.setText(eventDescription != null ? eventDescription : "N/A");
        tvEventLocation.setText(eventLocation != null ? eventLocation : "N/A");
        tvEventDate.setText(eventDate != null ? eventDate : "N/A");
        tvEventTime.setText(eventTime != null ? eventTime : "N/A");
        tvTicketPrice.setText(price != null ? String.valueOf(price) : "N/A");
    }

    // Set click listener for purchase button.
    private void setPurchaseButtonClickListener() {
        Button btnPurchase = findViewById(R.id.btnPurchase);
        btnPurchase.setOnClickListener(view -> navigateToPaymentActivity());
    }

    // Navigate to TicketPaymentActivity.
    private void navigateToPaymentActivity() {
        Intent toPaymentActivity = new Intent(TicketPurchaseActivity.this, TicketPaymentActivity.class);
        toPaymentActivity.putExtra("TICKET_ID", ticketId);
        toPaymentActivity.putExtra("USER_ID", currentUserId);
        startActivity(toPaymentActivity);
        finish();
    }
}