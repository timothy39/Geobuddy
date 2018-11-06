package com.example.afa.geobuddy;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by nders on 15/2/2018.
 */

public class Utils {


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
