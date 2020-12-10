package com.example.a4agora2.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.a4agora2.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
