package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;

import Model.Rating;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.RatingViewHolder>
{

    private List<Rating> RatingList;

    public ReviewAdapter(List<Rating> RatingList)
    {
        this.RatingList = RatingList;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position)
    {
        Rating rating = RatingList.get(position);
        holder.username.setText(rating.getClient().getUsername());
        holder.RatingText.setText(rating.getComment());
        holder.ratingBar.setRating(rating.getStars());
        // אפשר גם לטעון תמונת פרופיל כאן אם יש לך URL (עם Glide למשל)
    }

    @Override
    public int getItemCount()
    {
        return RatingList.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, RatingText;
        RatingBar ratingBar;
        ImageView profileImage;

        public RatingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            username = itemView.findViewById(R.id.textUsername);
            RatingText = itemView.findViewById(R.id.textReview);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            profileImage = itemView.findViewById(R.id.imageProfile);
        }
    }
}
