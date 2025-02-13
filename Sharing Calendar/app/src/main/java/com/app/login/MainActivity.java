package com.app.login;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.app.login.groupEvent.GroupEventFragment;
import com.app.login.personalEvent.PersonalEventFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    public FrameLayout fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // 这里你没设置
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragment_container = findViewById(R.id.fragment_container);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment;
                switch (item.getItemId()) {
                    case R.id.navigation_personal_event:

                        selectedFragment = new PersonalEventFragment();
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
                }

                return true;
            }
        });

        // Load the default fragment
        bottomNavigationView.setSelectedItemId(R.id.navigation_personal_event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 如果activity 不需要自己的菜单，就不调用super
        // 你没重写 所以调用的super
        // 在这改
        return true;
//        return type==0;
    }

    public void showToolbar() {
        toolbar.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) fragment_container.getLayoutParams();
        layoutParams.topMargin = 150;
        fragment_container.setLayoutParams(layoutParams);
    }

    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) fragment_container.getLayoutParams();
        layoutParams.topMargin = 0;
        fragment_container.setLayoutParams(layoutParams);
    }
}