package com.afa.geobuddy.ui.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afa.geobuddy.misc.Constants;
import com.afa.geobuddy.models.Reminder;
import com.example.afa.geobuddy.R;

import java.util.ArrayList;
import java.util.List;


public class ReminderFragment extends Fragment {
    List<Reminder> reminders = new ArrayList<>();
    private RecyclerView rem_recyclerView;
    private ReminderDataBinder reminderDataBinder;
    private RecyclerView.LayoutManager remLayoutmanager;
    long remlist;
    long newcount;
    int modifyposition = -1;
    CoordinatorLayout coordinatorLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View reminderview = inflater.inflate(R.layout.fragment_reminder, container, false);
        Toolbar toolbar = (Toolbar) reminderview.findViewById(R.id.toolbar);
        FloatingActionButton floatingActionButton = (FloatingActionButton) reminderview.findViewById(R.id.reminderfloatingbutton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddReminderActivity.class));
            }
        });
        coordinatorLayout = (CoordinatorLayout) reminderview.findViewById(R.id.remcoordinator);
        rem_recyclerView = (RecyclerView) reminderview.findViewById(R.id.note_rv);
        rem_recyclerView.setHasFixedSize(true);


        //linear layout manager
        remLayoutmanager = new LinearLayoutManager(getActivity());
        rem_recyclerView.setLayoutManager(remLayoutmanager);


        remlist = Reminder.count(Reminder.class);

        if (remlist >= 0) {
            reminders = Reminder.listAll(Reminder.class);
            reminderDataBinder = new ReminderDataBinder(getActivity(), reminders);
        } else {
            reminderDataBinder = new ReminderDataBinder(getActivity());

        }
        rem_recyclerView.setAdapter(reminderDataBinder);
        ItemTouchHelper.SimpleCallback recyclercallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //notify recycler view whenn swiped items are removed
                final int position = viewHolder.getAdapterPosition();
                final Reminder reminder = reminders.get(viewHolder.getAdapterPosition());
                reminders.remove(viewHolder.getAdapterPosition());
                reminderDataBinder.notifyItemRemoved(position);


                reminder.delete();
                remlist -= 1;

                Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Note has been deleted", Snackbar.LENGTH_SHORT)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Snackbar snackbar1 = Snackbar.make(getActivity().findViewById(android.R.id.content), "Note has been restored", Snackbar.LENGTH_SHORT);
                                reminder.save();
                                reminders.add(position, reminder);
                                reminderDataBinder.notifyItemInserted(position);
                                remlist += 1;
                                snackbar1.show();

                            }


                        });
                snackbar.show();


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(recyclercallback);
        itemTouchHelper.attachToRecyclerView(rem_recyclerView);


        reminderDataBinder.SetOnItemClickListener(new ReminderDataBinder.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Intent to send data from fragment to AddReminder Activity
                Intent intent = new Intent(getActivity(), AddReminderActivity.class);
                intent.putExtra(Constants.KEY_ACTION_EDIT, true);
                intent.putExtra(Constants.KEY_REMINDER_OBJ, reminders.get(position));

                modifyposition = position;
                startActivity(intent);
            }
        });


        return reminderview;
    }

    @Override
    public void onResume() {
        super.onResume();

        newcount = Reminder.count(Reminder.class);
        if (newcount > remlist) {
            Reminder reminder = Reminder.last(Reminder.class);
            reminders.add(reminder);
            reminderDataBinder.notifyItemInserted((int) newcount);
            rem_recyclerView.smoothScrollToPosition(0);

            remlist = newcount;


        }
        if (modifyposition != -1) {
            reminders.set(modifyposition, Reminder.listAll(Reminder.class).get(modifyposition));
            reminderDataBinder.notifyItemChanged(modifyposition);
        }
    }


}