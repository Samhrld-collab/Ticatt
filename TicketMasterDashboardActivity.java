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

public class TicketMasterDashboardActivity extends AppCompatActivity {

    private String currentUserId;
    private Boolean isTicketMaster;
    private DatabaseReference usersReference, ticketsReference;
    private RecyclerView recyclerViewTickets;
    private TextView tvNoTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_master_dashboard);

        // Initialize UI components and Firebase references.
        showBasicInterfaces();
        initializeFirebaseReferences();

        // Retrieve user ID from intent.
        currentUserId = getIntent().getStringExtra("USER_ID");

        // Validate user ID and fetch user data.
        validateAndFetchUserData();
    }

    // Initialize basic UI components.
    private void showBasicInterfaces() {
        Button btnLogout = findViewById(R.id.btnLogout);
        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
        tvNoTickets = findViewById(R.id.tvNoTickets);

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));

        btnLogout.setOnClickListener(view -> logoutAndNavigateToMainActivity());
    }

    // Initialize Firebase Database references.
    private void initializeFirebaseReferences() {
        usersReference = FirebaseDatabase.getInstance().getReference("users");
        ticketsReference = FirebaseDatabase.getInstance().getReference("tickets");
    }

    // Validate user ID and fetch user data from Firebase.
    private void validateAndFetchUserData() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            redirectToMainActivity();
            return;
        }

        usersReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    isTicketMaster = snapshot.child("isTicketMaster").getValue(Boolean.class);

                    // Navigate based on user role.
                    handleUserRoleNavigation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketMasterDashboardActivity.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                redirectToMainActivity();
            }
        });
    }

    // Handle navigation based on user role (Ticket Master or Learner).
    private void handleUserRoleNavigation() {
        if (isTicketMaster != null && isTicketMaster) {
            showTicketMasterInterface();
        } else {
            redirectToHomeDashboard();
        }
    }

    // Display Ticket Master specific UI and load tickets.
    private void showTicketMasterInterface() {
        Button btnAddTicket = findViewById(R.id.btnAddTicket);
        loadTheMasterTickets();

        btnAddTicket.setOnClickListener(view -> navigateToAddTicketActivity());
    }

    // Load tickets created by the Ticket Master from Firebase.
    private void loadTheMasterTickets() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            redirectToMainActivity();
            return;
        }

        Query masterTicketsQuery = ticketsReference.orderByChild("userId").equalTo(currentUserId);
        masterTicketsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Ticket> ticketList = new ArrayList<>();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                        Ticket ticket = ticketSnapshot.getValue(Ticket.class);
                        if (ticket != null) {
                            ticket.setTicketId(ticketSnapshot.getKey());
                            ticketList.add(ticket);
                        }
                    }

                    tvNoTickets.setVisibility(View.GONE);
                    recyclerViewTickets.setVisibility(View.VISIBLE);

                    TicketAdapter adapter = new TicketAdapter(TicketMasterDashboardActivity.this, ticketList, currentUserId, isTicketMaster);
                    recyclerViewTickets.setAdapter(adapter);
                } else {
                    tvNoTickets.setVisibility(View.VISIBLE);
                    recyclerViewTickets.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketMasterDashboardActivity.this, "Error loading tickets: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate to AddTicketActivity.
    private void navigateToAddTicketActivity() {
        Intent toAddTicketActivity = new Intent(TicketMasterDashboardActivity.this, AddTicketActivity.class);
        toAddTicketActivity.putExtra("USER_ID", currentUserId);
        startActivity(toAddTicketActivity);
        finish();
    }

    // Navigate to HomeDashboardActivity (Learner Dashboard).
    private void redirectToHomeDashboard() {
        Intent toHomeDashboardActivity = new Intent(TicketMasterDashboardActivity.this, HomeDashboardActivity.class);
        toHomeDashboardActivity.putExtra("USER_ID", currentUserId);
        startActivity(toHomeDashboardActivity);
        finish();
    }

    // Navigate to MainActivity (Logout).
    private void redirectToMainActivity() {
        Intent toMainActivity = new Intent(TicketMasterDashboardActivity.this, MainActivity.class);
        startActivity(toMainActivity);
        finish();
    }

    // Logout and navigate to MainActivity.
    private void logoutAndNavigateToMainActivity(){
        Intent logoutIntent = new Intent(TicketMasterDashboardActivity.this, MainActivity.class);
        startActivity(logoutIntent);
        currentUserId = null;
        finish();
    }
}