package com.example.afa.geobuddy;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.afa.geobuddy.LocationFragment;
import com.example.afa.geobuddy.NotesFragment;
import com.example.afa.geobuddy.R;
import com.example.afa.geobuddy.ReminderFragment;
import com.example.afa.geobuddy.SearchFragment;
import com.google.android.gms.maps.OnMapReadyCallback;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content,new ReminderFragment()).commit();

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_reminder:
                    transaction.replace(R.id.content,new ReminderFragment()).commit();
                    //TODO
                    return true;
                case R.id.navigation_search:
                    transaction.replace(R.id.content,new SearchFragment()).commit();
                    return true;
                case R.id.navigation_getlocation:
                    transaction.replace(R.id.content,new LocationFragment()).commit();
                    return true;
                case R.id.navigation_notes:
                    transaction.replace(R.id.content,new NotesFragment()).commit();
                    return true;
            }
            return false;
        }
    };

}



