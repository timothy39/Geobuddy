package com.afa.geobuddy.ui.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afa.geobuddy.models.Note;
import com.example.afa.geobuddy.R;

import java.util.List;


public class NotesDataBinder extends RecyclerView.Adapter<NotesDataBinder.NotesViewHolder> {
    Context context;
    List<Note> notes;
    OnItemClickListener clickListener;

    public NotesDataBinder(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @Override
    public NotesDataBinder.NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_note_item, parent, false);
        NotesViewHolder notesViewHolder = new NotesViewHolder(view);
        return notesViewHolder;
    }

    @Override
    public void onBindViewHolder(NotesViewHolder notesViewHolder, int position) {
        notesViewHolder.title.setText(notes.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


    class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;

        public NotesViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.note_item_title);
            //  date = (TextView) itemView.findViewById(R.id.note_item_date);
            //time = (TextView) itemView.findViewById(R.id.note_item_time);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());


        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }


}



