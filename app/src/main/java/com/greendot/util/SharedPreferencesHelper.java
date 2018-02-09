package com.greendot.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.renderscript.Element;

public class SharedPreferencesHelper {

    private SharedPreferences preferences;

    private static final String FILE_NAME = "shared_preferences";

    public SharedPreferencesHelper(Context context){
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public <T> T get(String key, DataType type) {
        return (T) getValue(key, type);
    }

    public String getString(String key) {
        return get(key, DataType.STRING);
    }

    public float getFloat(String key) {
        return get(key, DataType.FLOAT);
    }

    public int getInteger(String key) {
        return get(key, DataType.INTEGER);
    }

    public long getLong(String key) {
        return get(key, DataType.LONG);
    }
    public boolean getBoolean(String key) {
        return get(key, DataType.BOOLEAN);
    }

    public void put(String key, Object value) {
        SharedPreferences.Editor edit = preferences.edit();
        put(edit, key, value);
        edit.apply();
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    private void put(SharedPreferences.Editor editor, String key, Object obj) {
        if (key != null){
            if (obj instanceof Integer){
                editor.putInt(key, (Integer)obj);
            } else if (obj instanceof Long){
                editor.putLong(key, (Long)obj);
            } else if (obj instanceof Boolean){
                editor.putBoolean(key, (Boolean)obj);
            } else if (obj instanceof Float){
                editor.putFloat(key, (Float) obj);
            } else if (obj instanceof String){
                editor.putString(key, String.valueOf(obj));
            }
        }
    }

    private Object getValue(String key, DataType type){
        switch (type) {
            case INTEGER:
                return preferences.getInt(key, -1);
            case FLOAT:
                return preferences.getFloat(key, -1f);
            case BOOLEAN:
                return preferences.getBoolean(key, false);
            case LONG:
                return preferences.getLong(key, -1L);
            case STRING:
                return preferences.getString(key, null);
            default:
                return null;
        }
    }

    enum DataType {
        INTEGER, LONG, BOOLEAN, FLOAT, STRING
    }
}
