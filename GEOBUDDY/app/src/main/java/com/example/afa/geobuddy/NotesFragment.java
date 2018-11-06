package com.example.afa.geobuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class NotesFragment extends Fragment {

    long notecount;
    int modifyposition = -1;
    NotesDbAdapter notesDbAdapter;
    RecyclerView recyclerView;

    List<Note> notes = new ArrayList<>();
    public RecyclerView.LayoutManager layoutManager;
    public LinearLayoutManager linearLayoutManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View noteview = inflater.inflate(R.layout.fragment_notes, container, false);
        Log.d("main", "on create");

        Toolbar ntoolbar = (Toolbar) noteview.findViewById(R.id.note_toolbar);


        FloatingActionButton note_fab = (FloatingActionButton) noteview.findViewById(R.id.addnote_fab);
        recyclerView = (RecyclerView) noteview.findViewById(R.id.note_rv);


        //linear layout manager
        linearLayoutManager= new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        notecount = Note.count(Note.class);

        if (notecount >= 0) {
            notes = Note.listAll(Note.class);

            notesDbAdapter = new NotesDbAdapter(getActivity(), notes);
            recyclerView.setAdapter(notesDbAdapter);



        }
        ItemTouchHelper.SimpleCallback recyclercallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //notify recycler view whenn swiped items are removed
                final int position = viewHolder.getAdapterPosition();
                final Note note = notes.get(viewHolder.getAdapterPosition());
                notes.remove(viewHolder.getAdapterPosition());
                notesDbAdapter.notifyItemRemoved(position);

                note.delete();
                notecount -= 1;

                Snackbar.make(recyclerView, "Note has been deleted", Snackbar.LENGTH_SHORT)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                note.save();
                                notes.add(position, note);
                                notesDbAdapter.notifyItemInserted(position);
                                notecount += 1;


                            }
                        }).show();


            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(recyclercallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        notesDbAdapter.SetOnItemClickListener(new NotesDbAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), AddNote.class);
                intent.putExtra("edit this note", true);
                intent.putExtra("title", notes.get(position).title);
                intent.putExtra("note", notes.get(position).note);

                modifyposition = position;
                startActivity(intent);
            }
        });


        note_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddNote.class));
            }
        });


        return noteview;

    }


    @Override
    public void onResume() {
        super.onResume();

        final long newcount = Note.count(Note.class);
        if (newcount > notecount) {
            Note note = Note.last(Note.class);
            notes.add(note);
            notesDbAdapter.notifyItemInserted((int) newcount);
            recyclerView.smoothScrollToPosition(0);

            notecount = newcount;


        }
        if (modifyposition != -1) {
            notes.set(modifyposition, Note.listAll(Note.class).get(modifyposition));
            notesDbAdapter.notifyItemChanged(modifyposition);
        }
    }


}

