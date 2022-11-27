package com.example.myapplicationdemo.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationdemo.R;
import com.example.myapplicationdemo.model.Review;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    List<Review> reviews;

    public CommentsAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.comment_view_holder, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setComment(reviews.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView comment, commentWriterName, commentDate;

        public ViewHolder(View view) {
            super(view);
            comment = view.findViewById(R.id.comment_text_view);
            commentWriterName = view.findViewById(R.id.comment_writer_name);
            commentDate = view.findViewById(R.id.comment_date);
        }

        public void setComment(Review review) {
            comment.setText(review.comment);
            commentWriterName.setText(review.writerName);

            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            commentDate.setText(dateFormat.format(review.date));
        }
    }
}
