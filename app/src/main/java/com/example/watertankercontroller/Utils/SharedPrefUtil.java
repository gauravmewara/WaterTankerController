package com.example.watertankercontroller.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.widget.Toast;

public class SharedPrefUtil {
    public static  final String AUTHORITY="app.SeRemo";

    public static void showToast(Context context, String message) {
        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
    }

    public static void setPreferences(Context con, String Tag, String key, String value) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void setPreferences(Context con, String Tag, String key, Boolean value) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static void setPreferences(Context con, String Tag, String key, int value) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static boolean hasKey(Context con, String Tag, String key){
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        return preferences.contains(key);
    }

    public static String getStringPreferences(Context con,String Tag, String key) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        return preferences.getString(key, "");
    }

    public static Boolean getBooleanPreferences(Context con,String Tag, String key) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        return preferences.getBoolean(key,false);
    }

    public static int getIntPreferences(Context con,String Tag, String key) {
        // save the data
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        return preferences.getInt(key,0);
    }

    public static void removePreferenceKey(Context con,String Tag, String key){
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key).commit();
    }

    public static void deletePreference(Context con,String Tag){
        SharedPreferences preferences = con.getSharedPreferences(Tag, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    public static boolean getConnectivityStatus(Activity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null)
            if (info.isConnected()) {
                return true;
            } else {
                return false;
            }
        else
            return false;
    }

}
