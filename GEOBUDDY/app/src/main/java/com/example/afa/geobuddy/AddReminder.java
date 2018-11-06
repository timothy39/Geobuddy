/**
 * TODO ... add comments
 *
 *
 */


package com.example.afa.geobuddy;


import android.location.Location;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;



public class AddReminder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addreminder);

        // Fragment
           AddReminderFragment addReminderFragment = new AddReminderFragment();

         // Handle Intent
         // forward intent arguments to AddReminderFragment
         boolean edit = getIntent().getBooleanExtra(Constants.KEY_ACTION_EDIT, false);

         if(edit){
         Reminder reminder = getIntent().getParcelableExtra(Constants.KEY_REMINDER_OBJ);

         Bundle bundle = new Bundle();
         bundle.putBoolean(Constants.KEY_ACTION_EDIT, edit);
         bundle.putParcelable(Constants.KEY_REMINDER_OBJ, reminder);

         // pass bundle to fragment
         addReminderFragment.setArguments(bundle);
         }

         // Set fragment
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.replace(R.id.content_reminder, addReminderFragment).commit();
    }


}