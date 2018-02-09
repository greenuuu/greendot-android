package com.greendot.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class ApplicationHelper extends Application {

    private List<Activity> list = new ArrayList<Activity>();

    private static ApplicationHelper instance;

    private ApplicationHelper() {

    }

    public static ApplicationHelper getInstance() {
        if (null == instance) {
            instance = new ApplicationHelper();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        list.add(activity);
    }

    public void clear(Context context){
        for (int index = 0; index < list.size() - 1; index++){
            Activity activity = list.get(index);
            activity.finish();
        }
    }

    public void exit(Context context) {
        for (Activity activity : list) {
            activity.finish();
        }
        System.exit(0);
    }
}