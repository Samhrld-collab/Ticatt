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

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private Context context;
    private List<Booking> bookingList;
    private String currentUserId;

    // Constructor to initialize adapter data.
    public BookingAdapter(Context context, List<Booking> bookingList, String userId) {
        this.context = context;
        this.bookingList = bookingList;
        this.currentUserId = userId;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the booking item layout.
        View view = LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        // Bind booking data to the ViewHolder.
        Booking booking = bookingList.get(position);
        holder.tvEventName.setText(booking.getEventName());
        holder.tvEventDate.setText(booking.getEventDate());
        holder.tvEventLocation.setText(booking.getEventLocation());
        String formattedPrice = "RM " + String.format(Locale.getDefault(), "%.2f", booking.getTicketPrice());
        holder.tvTicketPrice.setText(formattedPrice);

        // Set click listener for booking item.
        holder.itemView.setOnClickListener(view -> handleBookingClick(booking));
    }

    // Handles click event on a booking item.
    private void handleBookingClick(Booking booking) {
        // Navigate to MyTicketDetails activity.
        Intent intent = new Intent(context, MyTicketDetails.class);
        intent.putExtra("USER_ID", currentUserId);
        intent.putExtra("TICKET_ID", booking.getTicketId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // ViewHolder class to hold booking item views.
    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventLocation, tvTicketPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvTicketPrice = itemView.findViewById(R.id.tvTicketPrice);
        }
    }
}