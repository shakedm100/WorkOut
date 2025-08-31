package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import Model.Course;
import Model.Enrollment;
import Model.Repository.BusinessRepository;
import Model.Repository.CourseRepository;

public class UpcomingEnrollmentsAdapter extends RecyclerView.Adapter<UpcomingEnrollmentsAdapter.VH>
{

    public interface OnItemClick
    {
        void onClick(Enrollment e);
    }

    private final List<Enrollment> data = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final OnItemClick onItemClick;
    private final BusinessRepository businessRepository;

    // Optional click handler; pass null if you don't need clicks
    public UpcomingEnrollmentsAdapter(OnItemClick onItemClick)
    {
        this.onItemClick = onItemClick;
        this.businessRepository = new BusinessRepository();
    }

    /**
     * Replace current list and refresh
     */
    public void submit(List<Enrollment> items)
    {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_course, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position)
    {
        Enrollment enrollment = data.get(position);
        Course course = enrollment.getCourse();

        // Title
        holder.txtCourseName.setText(course != null && course.getName() != null ? course.getName() : "Course");

        // Start from ENROLLMENT time (the real occurrence)
        Timestamp startTs = enrollment.getTime();
        Date start = startTs != null ? startTs.toDate() : null;

        // Compute end time using course duration if available
        String timeRange = "Time: N/A";
        if (start != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            cal.add(Calendar.HOUR, -3);
            start = cal.getTime();

            int durMin = (course != null ? course.getDuration() : 0);
            Date end = start;
            if (durMin > 0)
            {
                cal.add(Calendar.MINUTE, durMin);
                end = cal.getTime();
            }

            TimeZone tz = TimeZone.getTimeZone("Asia/Jerusalem");

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeFormat.setTimeZone(tz);

            String startStr = timeFormat.format(start);
            String endStr = timeFormat.format(end);

            SimpleDateFormat dateOnlyFormat =
                    new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            dateOnlyFormat.setTimeZone(tz);

            String dateOnly = dateOnlyFormat.format(enrollment.getTime().toDate());

            timeRange = dateOnly + " at " + startStr + " - " + endStr;
        }
        holder.txtTimeRange.setText(timeRange);

        // Click
        holder.itemView.setOnClickListener(v ->
        {
            if (onItemClick != null) onItemClick.onClick(enrollment);
        });

        businessRepository.getBusinessesById(course.getBusinessId()).addOnSuccessListener(task ->
        {
            holder.txtBusiness.setText("At " + task.getBusinessName());
        });
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder
    {
        TextView txtCourseName, txtBusiness, txtTimeRange;

        VH(@NonNull View v)
        {
            super(v);
            txtCourseName = v.findViewById(R.id.txtCourseName);
            txtBusiness = v.findViewById(R.id.txtBusiness);
            txtTimeRange = v.findViewById(R.id.txtTimeRange);
        }
    }
}
