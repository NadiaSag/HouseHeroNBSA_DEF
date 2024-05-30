package com.nasch.househero.ReviewRecycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nasch.househero.R

class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClientNameRole: TextView = itemView.findViewById(R.id.tvClientNameRole)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvClientNameRole.text = "${review.clientName} (${review.clientRole})"
        holder.tvComment.text = review.comment
        holder.tvRating.text = "Rating: ${review.rating}"
    }

    override fun getItemCount(): Int {
        return reviews.size
    }
}

data class Review(
    val clientName: String,
    val clientRole: String,
    val comment: String,
    val rating: Float
)
