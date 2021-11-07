package com.graduate.lookatv2.utils;

import android.util.Log;

public class PrintUtil {
    private static final String TAG = "AndroidOpenCv";

    public static void printLog(String msg) {
        Log.d(TAG, msg);
    }

    public static void printLog(String tag, String msg) {
        Log.d(tag, msg);
    }
}
