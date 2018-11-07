package com.afa.geobuddy.ui.reminder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afa.geobuddy.models.Reminder;
import com.example.afa.geobuddy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AFA on 16/02/2018.
 */

public class ReminderDataBinder extends RecyclerView.Adapter<ReminderDataBinder.ReminderViewHolder> {

    Context context;
    List<Reminder> reminders;
    OnItemClickListener remclicklistener;


    class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title, address;

        public ReminderViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.reminder_item_title);
            address = (TextView) itemView.findViewById(R.id.reminder_item_address);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            remclicklistener.onItemClick(v, getAdapterPosition());


        }
    }

    public ReminderDataBinder(Context context) {
        this.context = context;
        this.reminders = new ArrayList<>();
    }

    public ReminderDataBinder(Context context, List<Reminder> reminders) {
        this.context = context;
        this.reminders = reminders;
    }


    @Override
    public ReminderDataBinder.ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_reminder_item, parent, false);
        ReminderDataBinder.ReminderViewHolder reminderViewHolder = new ReminderDataBinder.ReminderViewHolder(view);
        return reminderViewHolder;
    }

    @Override
    public void onBindViewHolder(ReminderDataBinder.ReminderViewHolder reminderViewHolder, int position) {
        reminderViewHolder.title.setText(reminders.get(position).getTitle());
        reminderViewHolder.address.setText(reminders.get(position).getLocationString());


    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final ReminderDataBinder.OnItemClickListener itemClickListener) {
        this.remclicklistener = itemClickListener;
    }


}
