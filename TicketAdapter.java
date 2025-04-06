package com.bridgeout.ticatt;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private Context context;
    private List<Ticket> ticketList;
    private String currentUserId;
    private Boolean isTicketMaster;

    // Constructor to initialize adapter data.
    public TicketAdapter(Context context, List<Ticket> ticketList, String userId, Boolean isTicketMaster) {
        this.context = context;
        this.ticketList = ticketList;
        this.currentUserId = userId;
        this.isTicketMaster = isTicketMaster;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the ticket item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        // Bind ticket data to the ViewHolder.
        Ticket ticket = ticketList.get(position);
        holder.tvEventName.setText(ticket.getEvent());
        holder.tvEventDate.setText(ticket.getEventDate());
        holder.tvEventLocation.setText(ticket.getEventLocation());
        String formattedPrice = "RM " + String.format(Locale.getDefault(), "%.2f", ticket.getPrice());
        holder.tvTicketPrice.setText(formattedPrice);

        // Set click listener for ticket item.
        holder.itemView.setOnClickListener(view -> handleTicketClick(ticket));
    }

    // Handles click event on a ticket item.
    private void handleTicketClick(Ticket ticket) {
        Intent intent;
        if (isTicketMaster) {
            // Navigate to TicketDetailsActivity for Ticket Masters.
            intent = new Intent(context, TicketDetailsActivity.class);
        } else {
            // Navigate to TicketPurchaseActivity for Customers.
            intent = new Intent(context, TicketPurchaseActivity.class);
        }

        // Pass ticket and user data to the next activity.
        intent.putExtra("TICKET_ID", ticket.getTicketId());
        intent.putExtra("USER_ID", currentUserId);
        intent.putExtra("isTicketMaster", isTicketMaster);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    // ViewHolder class to hold ticket item views.
    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventLocation, tvTicketPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvTicketPrice = itemView.findViewById(R.id.tvTicketPrice);
        }
    }
}