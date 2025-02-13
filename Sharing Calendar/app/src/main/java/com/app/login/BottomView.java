package com.app.login;
import static com.app.login.ConstantsKt.WEEK_SECONDS;

import android.os.Bundle;
import android.view.MenuItem;

import com.app.login.groupEvent.GroupEventFragment;
import com.app.login.personalEvent.PersonalEventFragment;
import com.app.login.util.FormatterUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

public class BottomView extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        long currentTimestamp = System.currentTimeMillis(); // 获取当前时间戳
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment;
                switch (item.getItemId()) {
                    case R.id.navigation_personal_event:
                        selectedFragment = new PersonalEventFragment();
                        //setupActionBarTitle(currentTimestamp);
                        break;
                    case R.id.navigation_search_other:
                        selectedFragment = new SearchOtherFragment();
                        break;
                    case R.id.navigation_group_event:
                        selectedFragment = new GroupEventFragment();
                        break;
                    default:
                        return false; // Add this to handle unexpected cases
                }


                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    //setupActionBarTitle(title); // 更新标题
                }
                return true;
            }
        });

        // Load the default fragment
        bottomNavigationView.setSelectedItemId(R.id.navigation_personal_event);
    }
    private void setupActionBarTitle(long timestamp) {
        DateTime startDateTime = FormatterUtils.getDateTimeFromTS(timestamp);
        DateTime endDateTime = FormatterUtils.getDateTimeFromTS(timestamp + WEEK_SECONDS);
        String startMonthName = FormatterUtils.getMonthName(this, startDateTime.getMonthOfYear());

        // 设置大标题
        if (startDateTime.getMonthOfYear() == endDateTime.getMonthOfYear()) {
            String newTitle = startMonthName;
            if (startDateTime.getYear() != DateTime.now().getYear()) {
                newTitle += " - " + startDateTime.getYear();
            }
            getSupportActionBar().setTitle(newTitle);
        } else {
            String endMonthName = FormatterUtils.getMonthName(this, endDateTime.getMonthOfYear());
            getSupportActionBar().setTitle(startMonthName + " - " + endMonthName);
        }

        // 设置小标题
        getSupportActionBar().setSubtitle(String.format(getString(R.string.week), startDateTime.plusDays(3).getWeekOfWeekyear()));
    }


}