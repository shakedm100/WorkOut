package Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.workout.fragments.DayScheduleFragment;

import java.util.List;

public class DayPagerAdapter extends FragmentStateAdapter {

    private final List<String> days;

    public DayPagerAdapter(@NonNull FragmentActivity fa, List<String> days) {
        super(fa);
        this.days = days;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return DayScheduleFragment.newInstance(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}
