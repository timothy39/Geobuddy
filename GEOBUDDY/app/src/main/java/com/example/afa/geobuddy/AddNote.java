package com.example.afa.geobuddy;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class AddNote extends AppCompatActivity {

    Button back_btn;
    FloatingActionButton addnotefab;
    EditText note_title, note_description;
    TextView note_date, note_time;
    String title,note;
    //long date,time;
    boolean noteedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        back_btn = (Button)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        note_title = (EditText)findViewById(R.id.addnote_title);
        note_description = (EditText)findViewById(R.id.addnote_description);
    //    note_date = (TextView) findViewById(R.id.note_item_date);
    //    note_time = (TextView) findViewById(R.id.note_item_time);

        addnotefab=(FloatingActionButton)findViewById(R.id.floatingbtn_note);

        // handling intent
        noteedit = getIntent().getBooleanExtra("edit this note", false);
        if(noteedit){
            title = getIntent().getStringExtra("title");
            note = getIntent().getStringExtra("note");
        //     date = getIntent().getLongExtra("date",0);
        //   time = getIntent().getLongExtra("time", 0);

            note_title.setText(title);
            note_description.setText(note);




        }

        addnotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adding note and description to database
                String addTitle = note_title.getText().toString();
                String addescription = note_description.getText().toString();



                //converting date and time to string
            /*    long add_date = System.currentTimeMillis();
                SimpleDateFormat date_sdf = new SimpleDateFormat("dd MM yyyy");
                String date_datestring = date_sdf.format(add_date);
                note_date.setText(date_datestring);

                long add_time = System.currentTimeMillis();
                SimpleDateFormat time_sdf = new SimpleDateFormat("nhh-mm-ss a");
                String time_datestring = time_sdf.format(add_time);
                note_time.setText(time_datestring);*/

                //confirms if note already exists
                if(!noteedit){
                    Log.d("note", "saving");
                    Note note = new Note(addTitle,addescription);
                    note.save();
                } else{
                    Log.d("Note", "updating");

                    List<Note> notes = Note.find(Note.class, "title= ?", title);
                    if(notes.size()>0){
                        Note note = notes.get(0);
                        Log.d("note gotten", "note:" + note.getTitle());
                        note.title = addTitle;
                        note.note=addescription;
                       ///note.date= add_date;
                        //note.time = add_time;

                       note.save();
                    }

                }
                Log.d("build", "note added");
                finish();
            }

        });


    }
}
