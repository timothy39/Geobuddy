package com.afa.geobuddy.models;

import com.orm.SugarRecord;


public class Note extends SugarRecord {

    private String title;
    private String note;


    public Note() {
    }

    public Note(String title, String note) {
        this.title = title;
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}