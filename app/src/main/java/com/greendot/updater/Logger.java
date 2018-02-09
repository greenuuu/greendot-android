package com.greendot.updater;

import android.util.Log;

public class Logger {

    private static final String TAG = "Logger";

    private static Logger instance;

    private boolean isShowLog;

    public static Logger get() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void setShowLog(boolean log) {
        isShowLog = log;
    }

    public boolean getShowLog() {
        return isShowLog;
    }

    public void i(String log) {
        if (isShowLog) {
            Log.i(TAG, log);
        }
    }

    public void d(String log) {
        if (isShowLog) {
            Log.d(TAG, log);
        }
    }

    public void e(String log) {
        if (isShowLog) {
            Log.e(TAG, log);
        }
    }


}
