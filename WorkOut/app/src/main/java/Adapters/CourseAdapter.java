package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.Course;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder>
{

    private List<Course> courseList;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public CourseAdapter(List<Course> courseList)
    {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position)
    {
        Course course = courseList.get(position);

        holder.courseNameText.setText(course.getName());

        Date occurrence = course.getSchedule().getOccurrence().toDate();
        int duration = course.getDuration();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(occurrence);
        calendar.add(Calendar.HOUR, 2);
        Date startTime = calendar.getTime();
        calendar.add(Calendar.MINUTE, duration);
        Date endTime = calendar.getTime();
        String startTimeString = timeFormat.format(startTime);
        String endTimeString = timeFormat.format(endTime);
        if (occurrence != null)
        {
            holder.courseTimeText.setText(startTimeString + " - " + endTimeString);
        }
        else
        {
            holder.courseTimeText.setText("Time: N/A");
        }
    }

    @Override
    public int getItemCount()
    {
        return courseList.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder
    {
        TextView courseNameText, courseTimeText;

        public CourseViewHolder(@NonNull View itemView)
        {
            super(itemView);
            courseNameText = itemView.findViewById(R.id.courseNameText);
            courseTimeText = itemView.findViewById(R.id.courseTimeText);
        }
    }
}

