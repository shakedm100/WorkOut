package com.example.workout;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import Adapters.DayPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class ClassesActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private DayPagerAdapter adapter;
    private List<String> days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classes_business); // ודא שקובץ XML הזה קיים

        // אתחול views
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // יצירת רשימת ימים
        days = Arrays.asList("ראשון", "שני", "שלישי", "רביעי", "חמישי");

        // אתחול האדפטר עם FragmentActivity וימי השבוע
        adapter = new DayPagerAdapter(this, days);
        viewPager.setAdapter(adapter);

        // חיבור בין הטאבים ל־ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(days.get(position))
        ).attach();
    }
}

