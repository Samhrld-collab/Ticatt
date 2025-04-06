package com.bridgeout.ticatt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private String currentUserId;
    private DatabaseReference bookingsReference;
    private RecyclerView recyclerViewTickets;
    private TextView tvNoTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        // Initialize Firebase Database reference for "bookings" node.
        bookingsReference = FirebaseDatabase.getInstance().getReference("bookings");

        // Retrieve user ID from intent.
        currentUserId = getIntent().getStringExtra("USER_ID");

        // Initialize UI elements.
        initializeUI();

        // Load tickets for the current user.
        loadTicketsForUser();
    }

    // Initialize UI elements.
    private void initializeUI() {
        Button btnPrevious = findViewById(R.id.btnPrevious);
        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
        tvNoTickets = findViewById(R.id.tvNoTickets);

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));

        // Set click listener for previous button.
        btnPrevious.setOnClickListener(view -> navigateToHomeDashboard());
    }

    // Navigate to HomeDashboardActivity.
    private void navigateToHomeDashboard() {
        Intent toHomeDashboardActivity = new Intent(MyTicketsActivity.this, HomeDashboardActivity.class);
        toHomeDashboardActivity.putExtra("USER_ID", currentUserId);
        startActivity(toHomeDashboardActivity);
        finish();
    }

    // Load tickets for the current user from Firebase.
    private void loadTicketsForUser() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Query myTicketsQuery = bookingsReference.orderByChild("userId").equalTo(currentUserId);
        myTicketsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Booking> bookingList = new ArrayList<>();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                        Booking booking = bookingSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            booking.setBookingId(bookingSnapshot.getKey());
                            bookingList.add(booking);
                        }
                    }

                    tvNoTickets.setVisibility(View.GONE);
                    recyclerViewTickets.setVisibility(View.VISIBLE);

                    BookingAdapter adapter = new BookingAdapter(MyTicketsActivity.this, bookingList, currentUserId);
                    recyclerViewTickets.setAdapter(adapter);
                } else {
                    tvNoTickets.setVisibility(View.VISIBLE);
                    recyclerViewTickets.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketsActivity.this, "Error loading tickets: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}