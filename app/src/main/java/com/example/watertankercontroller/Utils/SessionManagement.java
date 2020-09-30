package com.example.watertankercontroller.Utils;

import android.content.Context;

import java.util.HashMap;

public class SessionManagement {
    private static final String IS_LOGGEDIN = "is_login";
    private static final String USER_ID = "user_id";
    private static final String PHONE_CODE = "phone_code";
    private static final String PHONE_NO = "phone_no";
    private static final String USER_TOKEN = "token";
    private static final String LANGUAGE= "language";
    private static final String NAME = "name";
    private static final String LOCATION = "location";
    private static final String SHARED_NOTIFICATION_COUNT_KEY = "notification_count";


    public static boolean checkSignIn(Context con){
        if(SharedPrefUtil.hasKey(con,Constants.SHARED_PREF_LOGIN_TAG,IS_LOGGEDIN)){
            return SharedPrefUtil.getBooleanPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,IS_LOGGEDIN);
        }else{
            return false;
        }
    }

    public static void createLoginSession(Context con,Boolean islogin, String user_id, String phcode,String phoneno,String username,String token,String language,String location,String noticount){
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,IS_LOGGEDIN,islogin);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_ID,user_id);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_CODE,phcode);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_NO,phoneno);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,NAME,username);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_TOKEN,token);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,LANGUAGE,language);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,LOCATION,location);
        SharedPrefUtil.setPreferences(con,Constants.SHARED_PREF_NOTICATION_TAG,SHARED_NOTIFICATION_COUNT_KEY,noticount);
    }

    public static void logout(FetchDataListener fetchDataListener, Context con){
        SharedPrefUtil.deletePreference(con,Constants.SHARED_PREF_LOGIN_TAG);
        SharedPrefUtil.deletePreference(con,Constants.SHARED_PREF_NOTICATION_TAG);
    }

    public static HashMap<String,String> getUserData(Context con){
        HashMap<String,String> userdata = new HashMap<>();
        userdata.put(IS_LOGGEDIN,Boolean.toString(SharedPrefUtil.getBooleanPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,IS_LOGGEDIN)));
        userdata.put(USER_ID,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_ID));
        userdata.put(PHONE_CODE,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_CODE));
        userdata.put(PHONE_NO,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_NO));
        userdata.put(USER_TOKEN,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_TOKEN));
        userdata.put(NAME,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,NAME));
        userdata.put(LANGUAGE,SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,LANGUAGE));
        return userdata;
    }

    public static String getUserPhoneCode(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_CODE);
    }

    public static String getUserToken(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_TOKEN);
    }
    public static String getUserId(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,USER_ID);
    }

    public static String getName(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,NAME);
    }

    public static String getLanguage(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,LANGUAGE);
    }
    public static String getPhoneNo(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,PHONE_NO);
    }
    public static String getLocation(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_LOGIN_TAG,LOCATION);
    }
    public static String getNotificationCount(Context con){
        return SharedPrefUtil.getStringPreferences(con,Constants.SHARED_PREF_NOTICATION_TAG,SHARED_NOTIFICATION_COUNT_KEY);
    }
    public static void setNotificationCount(Context con, String count) {
        SharedPrefUtil.setPreferences(con, Constants.SHARED_PREF_NOTICATION_TAG, Constants.SHARED_NOTIFICATION_COUNT_KEY, count);
    }
}
