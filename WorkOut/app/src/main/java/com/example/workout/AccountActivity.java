package com.example.workout;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

       /* TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        AccountPagerAdapter adapter = new AccountPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("אבטחה"); break;
                        case 1: tab.setText("עריכת פרטים"); break;
                        case 2: tab.setText("מחיקת חשבון"); break;
                    }
                }
        ).attach();*/
    }
}
