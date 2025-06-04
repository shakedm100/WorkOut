package Adapters;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.workout.fragments.DeleteAccountFragment;
import com.example.workout.fragments.EditProfileFragment;
import com.example.workout.fragments.SecurityFragment;

public class AccountPagerAdapter extends FragmentStateAdapter {

    public AccountPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new SecurityFragment();
            case 1: return new EditProfileFragment();
            case 2: return new DeleteAccountFragment();
            default: return new SecurityFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
