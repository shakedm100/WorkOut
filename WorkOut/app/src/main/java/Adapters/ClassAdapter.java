package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.example.workout.models.ClassItem;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<ClassItem> classList;
    private Context context;
    private OnClassLongClickListener longClickListener;

    public interface OnClassLongClickListener {
        void onLongClick(View view, ClassItem item);
    }

    public ClassAdapter(List<ClassItem> classList, Context context, OnClassLongClickListener longClickListener) {
        this.classList = classList;
        this.context = context;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassItem item = classList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.timeTextView.setText(item.getTime());

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(v, item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textTitle);
            timeTextView = itemView.findViewById(R.id.textTime);
        }
    }
}
