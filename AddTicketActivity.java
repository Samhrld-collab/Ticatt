package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddTicketActivity extends AppCompatActivity {

    private EditText etEventName, etEventDescription, etEventDate, etEventTime, etEventLocation, etPrice, etQuantityOfTicket;
    private String currentUserId;
    private DatabaseReference ticketsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ticket);

        // Initialize Firebase Database reference for "tickets" node.
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");

        // Retrieve user ID from intent.
        currentUserId = getIntent().getStringExtra("USER_ID");

        // Validate user ID and initialize UI.
        validateUserIdAndInitializeUI();
    }

    // Validate user ID and initialize UI elements.
    private void validateUserIdAndInitializeUI() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
            return;
        }

        initializeUI();
    }

    // Initialize UI elements.
    private void initializeUI() {
        etEventName = findViewById(R.id.etEvent);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventLocation = findViewById(R.id.etEventLocation);
        etPrice = findViewById(R.id.etPrice);
        etQuantityOfTicket = findViewById(R.id.etquantityOfTicket);
        Button btnAddTicket = findViewById(R.id.btnAddTicket);
        Button btnCancelAddTicket = findViewById(R.id.btnCancelAddTicket);

        // Set click listeners for buttons.
        btnAddTicket.setOnClickListener(view -> handleAddTicket());
        btnCancelAddTicket.setOnClickListener(view -> navigateToDashboard());
    }

    // Handle add ticket button click.
    private void handleAddTicket() {
        String eventName = etEventName.getText().toString().trim();
        String eventDescription = etEventDescription.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();
        String eventLocation = etEventLocation.getText().toString().trim();

        // Parse numeric inputs with validation.
        double price;
        int quantityOfTicket;
        try {
            price = Double.parseDouble(etPrice.getText().toString().trim());
            quantityOfTicket = Integer.parseInt(etQuantityOfTicket.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values for price and quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate all inputs before adding ticket.
        if (validateInputs(eventName, eventDescription, eventDate, eventTime, eventLocation, price, quantityOfTicket)) {
            addTicketToDatabase(eventName, eventDescription, eventDate, eventTime, eventLocation, price, quantityOfTicket);
        }
    }

    // Validate input fields.
    private boolean validateInputs(String eventName, String eventDescription, String eventDate, String eventTime, String eventLocation, double price, int quantityOfTicket) {
        if (eventName.isEmpty()) {
            etEventName.setError("Event name is required");
            return false;
        }
        if (eventDescription.isEmpty()) {
            etEventDescription.setError("Event description is required");
            return false;
        }
        if (eventDate.isEmpty()) {
            etEventDate.setError("Event date is required");
            return false;
        }
        if (eventTime.isEmpty()) {
            etEventTime.setError("Event time is required");
            return false;
        }
        if (eventLocation.isEmpty()) {
            etEventLocation.setError("Event location is required");
            return false;
        }
        if (price <= 0) {
            etPrice.setError("Price must be greater than zero");
            return false;
        }
        if (quantityOfTicket <= 0) {
            etQuantityOfTicket.setError("Quantity must be at least 1");
            return false;
        }
        return true;
    }

    // Add ticket to Firebase database.
    private void addTicketToDatabase(String event, String eventDescription, String eventDate, String eventTime, String eventLocation, Double price, Integer quantityOfTicket) {
        String ticketId = ticketsReference.push().getKey();

        if (ticketId == null) {
            Toast.makeText(this, "Error generating ticket ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ticketMap = new HashMap<>();
        ticketMap.put("userId", currentUserId);
        ticketMap.put("event", event);
        ticketMap.put("eventDescription", eventDescription);
        ticketMap.put("eventDate", eventDate);
        ticketMap.put("eventTime", eventTime);
        ticketMap.put("eventLocation", eventLocation);
        ticketMap.put("price", price);
        ticketMap.put("quantityOfTicket", quantityOfTicket);
        ticketMap.put("isAvailable", true);

        ticketsReference.child(ticketId).setValue(ticketMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddTicketActivity.this, "Ticket Added Successfully", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    } else {
                        Toast.makeText(AddTicketActivity.this, "Error adding ticket: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Navigate to MainActivity.
    private void navigateToMainActivity() {
        Intent toMainActivity = new Intent(AddTicketActivity.this, MainActivity.class);
        startActivity(toMainActivity);
        finish();
    }

    // Navigate to TicketMasterDashboardActivity.
    private void navigateToDashboard() {
        Intent toDashboard = new Intent(AddTicketActivity.this, TicketMasterDashboardActivity.class);
        toDashboard.putExtra("USER_ID", currentUserId);
        startActivity(toDashboard);
        finish();
    }
}