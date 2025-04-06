package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Objects;

public class TicketPaymentActivity extends AppCompatActivity {
    private String currentUserId, ticketId, eventName, eventLocation, eventDate;
    private Double ticketPrice = 0.0;
    private EditText etWalletNumber, etWalletPin;
    private TextView tvEventName, tvEventLocation, tvEventDate, tvTicketPrice;
    private DatabaseReference ticketsReference, bookingsReference, walletReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_payment);

        // Retrieve ticket and user IDs from intent.
        ticketId = getIntent().getStringExtra("TICKET_ID");
        currentUserId = getIntent().getStringExtra("USER_ID");

        // Initialize Firebase Database references.
        initializeFirebaseReferences();

        // Initialize UI elements.
        initializeUI();

        // Fetch ticket details from Firebase.
        fetchTicketDetailsFromDatabase();

        // Set click listener for pay button.
        setPayButtonClickListener();
    }

    // Initialize Firebase Database references.
    private void initializeFirebaseReferences() {
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
        bookingsReference = FirebaseDatabase.getInstance().getReference("bookings");
        walletReference = FirebaseDatabase.getInstance().getReference("wallets");
    }

    // Initialize UI elements.
    private void initializeUI() {
        etWalletNumber = findViewById(R.id.etWalletNumber);
        etWalletPin = findViewById(R.id.etPin);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventLocation = findViewById(R.id.tvEventLocation);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvTicketPrice = findViewById(R.id.tvTicketPrice);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Set click listener for cancel button.
        btnCancel.setOnClickListener(view -> navigateToHome());
    }

    // Fetch ticket details from Firebase.
    private void fetchTicketDetailsFromDatabase() {
        if (TextUtils.isEmpty(ticketId)) {
            Toast.makeText(this, "Ticket ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ticketsReference.child(ticketId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateUIWithTicketDetails(snapshot);
                } else {
                    Toast.makeText(TicketPaymentActivity.this, "Ticket not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketPaymentActivity.this, "Error fetching ticket details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Update UI with ticket details from Firebase.
    private void updateUIWithTicketDetails(DataSnapshot snapshot) {
        eventName = snapshot.child("event").getValue(String.class);
        eventLocation = snapshot.child("eventLocation").getValue(String.class);
        eventDate = snapshot.child("eventDate").getValue(String.class);
        ticketPrice = snapshot.child("price").getValue(Double.class);
        if (ticketPrice == null) ticketPrice = 0.0;

        tvEventName.setText(eventName != null ? eventName : "N/A");
        tvEventLocation.setText(eventLocation != null ? eventLocation : "N/A");
        tvEventDate.setText(eventDate != null ? eventDate : "N/A");
        tvTicketPrice.setText(String.valueOf(ticketPrice));
    }

    // Set click listener for pay button.
    private void setPayButtonClickListener() {
        Button btnPay = findViewById(R.id.btnPay);
        btnPay.setOnClickListener(view -> handlePayment());
    }

    // Handle payment process.
    private void handlePayment() {
        String walletNumber = etWalletNumber.getText().toString().trim();
        String walletPin = etWalletPin.getText().toString().trim();

        if (validateInputs(walletNumber, walletPin)) {
            validateWalletAndProcessPayment(walletNumber, walletPin);
        }
    }

    // Validate wallet and process payment.
    private void validateWalletAndProcessPayment(String walletNumber, String walletPin) {
        Query walletQuery = walletReference.orderByChild("walletNumber").equalTo(walletNumber);
        walletQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    processWalletData(snapshot, walletPin);
                } else {
                    Toast.makeText(TicketPaymentActivity.this, "Wallet not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketPaymentActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Process wallet data and create booking if successful.
    private void processWalletData(DataSnapshot snapshot, String walletPin) {
        for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
            String storedWalletPin = walletSnapshot.child("pin").getValue(String.class);
            Double currentWalletBalance = walletSnapshot.child("walletBalance").getValue(Double.class);

            if (walletPin.equals(storedWalletPin)) {
                if (currentWalletBalance != null && currentWalletBalance >= ticketPrice) {
                    updateWalletBalanceAndCreateBooking(walletSnapshot.getKey(), currentWalletBalance);
                } else {
                    Toast.makeText(TicketPaymentActivity.this, "Insufficient wallet balance", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        Toast.makeText(TicketPaymentActivity.this, "Incorrect wallet pin", Toast.LENGTH_SHORT).show();
    }

    // Update wallet balance and create ticket booking.
    private void updateWalletBalanceAndCreateBooking(String walletKey, Double currentWalletBalance) {
        double newWalletBalance = currentWalletBalance - ticketPrice;
        walletReference.child(Objects.requireNonNull(walletKey)).child("walletBalance").setValue(newWalletBalance);
        createTicketBooking();
    }

    // Create ticket booking entry in Firebase.
    private void createTicketBooking() {
        String bookingId = bookingsReference.push().getKey();
        if (bookingId == null) {
            Toast.makeText(this, "Error creating booking", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("userId", currentUserId);
        bookingMap.put("ticketId", ticketId);
        bookingMap.put("eventName", eventName);
        bookingMap.put("eventLocation", eventLocation);
        bookingMap.put("eventDate", eventDate);
        bookingMap.put("ticketPrice", ticketPrice);

        bookingsReference.child(bookingId).setValue(bookingMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(TicketPaymentActivity.this, "Payment successful, Ticket is booked.", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                Toast.makeText(TicketPaymentActivity.this, "Error creating booking: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate to HomeDashboardActivity.
    private void navigateToHome() {
        Intent toHomeActivity = new Intent(this, HomeDashboardActivity.class);
        toHomeActivity.putExtra("USER_ID", currentUserId);
        startActivity(toHomeActivity);
        finishAffinity();
    }

    // Validate input fields.
    private boolean validateInputs(String walletNumber, String walletPin) {
        if (walletNumber.isEmpty()) {
            etWalletNumber.setError("Wallet number is required");
            return false;
        }
        if (walletNumber.length() != 10) {
            etWalletNumber.setError("Invalid wallet number");
            return false;
        }
        if (walletPin.isEmpty()) {
            etWalletPin.setError("Wallet pin is required");
            return false;
        }
        if (walletPin.length() != 6) {
            etWalletPin.setError("Invalid wallet pin");
            return false;
        }
        return true;
    }
}