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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeDashboardActivity extends AppCompatActivity {

    private String currentUserId;
    private Boolean isTicketMaster;
    private DatabaseReference usersReference, ticketsReference;
    private RecyclerView recyclerViewTickets;
    private TextView tvNoTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

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
                Toast.makeText(HomeDashboardActivity.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                redirectToMainActivity();
            }
        });
    }

    // Handle navigation based on user role (Learner or Ticket Master).
    private void handleUserRoleNavigation() {
        if (isTicketMaster != null && !isTicketMaster) {
            showHomeInterfaces();
        } else {
            redirectToTicketMasterDashboard();
        }
    }

    // Display Learner specific UI and load tickets.
    private void showHomeInterfaces() {
        Button btnSeeMyTickets = findViewById(R.id.btnSeeMyTickets);
        loadTickets();

        btnSeeMyTickets.setOnClickListener(view -> navigateToMyTicketsActivity());
    }

    // Load all tickets from Firebase.
    private void loadTickets() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            redirectToMainActivity();
            return;
        }

        ticketsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Ticket> ticketList = new ArrayList<>();

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                        Ticket ticket = ticketSnapshot.getValue(Ticket.class);
                        Boolean isAvailable = ticketSnapshot.child("isAvailable").getValue(Boolean.class);

                        // Add the ticket only if it is available.
                        if (ticket != null && isAvailable != null && isAvailable) {
                            ticket.setTicketId(ticketSnapshot.getKey());
                            ticketList.add(ticket);
                        }
                    }

                    if(ticketList.isEmpty()){
                        tvNoTickets.setVisibility(View.VISIBLE);
                        recyclerViewTickets.setVisibility(View.GONE);
                    } else {
                        tvNoTickets.setVisibility(View.GONE);
                        recyclerViewTickets.setVisibility(View.VISIBLE);

                        TicketAdapter adapter = new TicketAdapter(HomeDashboardActivity.this, ticketList, currentUserId, isTicketMaster);
                        recyclerViewTickets.setAdapter(adapter);
                    }

                } else {
                    tvNoTickets.setVisibility(View.VISIBLE);
                    recyclerViewTickets.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeDashboardActivity.this, "Error loading courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate to MyTicketsActivity.
    private void navigateToMyTicketsActivity() {
        Intent toMyTicketsActivity = new Intent(HomeDashboardActivity.this, MyTicketsActivity.class);
        toMyTicketsActivity.putExtra("USER_ID", currentUserId);
        startActivity(toMyTicketsActivity);
        finish();
    }

    // Navigate to TicketMasterDashboardActivity.
    private void redirectToTicketMasterDashboard() {
        Intent toTicketMasterActivity = new Intent(HomeDashboardActivity.this, TicketMasterDashboardActivity.class);
        toTicketMasterActivity.putExtra("USER_ID", currentUserId);
        startActivity(toTicketMasterActivity);
        finish();
    }

    // Navigate to MainActivity (Logout).
    private void redirectToMainActivity() {
        Intent toMainActivity = new Intent(HomeDashboardActivity.this, MainActivity.class);
        startActivity(toMainActivity);
        finish();
    }

    // Logout and navigate to MainActivity.
    private void logoutAndNavigateToMainActivity() {
        Intent logoutIntent = new Intent(HomeDashboardActivity.this, MainActivity.class);
        startActivity(logoutIntent);
        currentUserId = null;
        finish();
    }
}