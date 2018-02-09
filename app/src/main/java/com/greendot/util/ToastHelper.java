package com.greendot.util;

import android.content.Context;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class ToastHelper {

    public static void show(Context context , String message){
        if (!"".equals(message)){
            Toasty.normal(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void warning(Context context , String message){
        Toasty.warning(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void error(Context context , String message){
        Toasty.error(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void success(Context context , String message){
        Toasty.success(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void normal(Context context , String message){
        Toasty.normal(context, message, Toast.LENGTH_LONG).show();
    }
}
