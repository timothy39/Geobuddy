package com.example.afa.geobuddy;


import android.content.Context;

import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;



public class Slider extends AppCompatSeekBar {


    private ArrayList mDictionary = new ArrayList<>(Arrays.asList(10, 20, 50, 100, 200, 500, 1000, 2000, 5* 1000, 10*1000, 20* 1000, 50*1000, 100*1000, 200*1000));

    public Slider(Context context) {
        super(context);
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getValue(){
        return (int) this.mDictionary.get(this.getProgress());
    }

    public String formatString(){
        int val = getValue();
        String string;

        if (val < 1000){
            string = String.valueOf(val) + " Mtr";
        }else{
            string = String.valueOf(val/1000) + " KM";
        }
        return string;
    }

    public void setValue(int value){
        int val = mDictionary.indexOf(value);
        setProgress(val);
    }


}
