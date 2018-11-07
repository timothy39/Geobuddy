package com.afa.geobuddy.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.afa.geobuddy.ui.location.LocationFragment;
import com.afa.geobuddy.ui.notes.NotesFragment;
import com.afa.geobuddy.ui.reminder.ReminderFragment;
import com.afa.geobuddy.ui.search.SearchFragment;
import com.example.afa.geobuddy.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, new ReminderFragment()).commit();

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_reminder:
                    transaction.replace(R.id.content, new ReminderFragment()).commit();
                    //TODO
                    return true;
                case R.id.navigation_search:
                    transaction.replace(R.id.content, new SearchFragment()).commit();
                    return true;
                case R.id.navigation_getlocation:
                    transaction.replace(R.id.content, new LocationFragment()).commit();
                    return true;
                case R.id.navigation_notes:
                    transaction.replace(R.id.content, new NotesFragment()).commit();
                    return true;
            }
            return false;
        }
    };

}



