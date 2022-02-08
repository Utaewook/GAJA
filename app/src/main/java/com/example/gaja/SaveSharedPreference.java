package com.example.gaja;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    static final String PREF_USER_ID = "userid";
    static final String PREF_USER_PW = "userpw";
    static final String PREF_USER_NN = "usernickname";
    static final String PREF_USER_CITY = "userCity";
    static final String PREF_USER_AL = "userAutoLogin";

    static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUser(Context context, CurrentLoginedUser user){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_ID,user.GetID());
        editor.putString(PREF_USER_PW,user.GetPassword());
        editor.putString(PREF_USER_NN,user.GetNickname());
        editor.putInt(PREF_USER_CITY,user.GetCity());
        editor.putBoolean(PREF_USER_AL,user.GetAutoLogin());
        editor.commit();
    }

    public static String getUserID(Context context){
        return getSharedPreferences(context).getString(PREF_USER_ID,"");
    }

    public static String getUserPW(Context context){
        return getSharedPreferences(context).getString(PREF_USER_PW,"");
    }

    public static String getUserNN(Context context){
        return getSharedPreferences(context).getString(PREF_USER_NN,"");
    }

    public static int getUserCity(Context context){
        return getSharedPreferences(context).getInt(PREF_USER_CITY,1);
    }

    public static boolean getUserAutoLogin(Context context){
        return getSharedPreferences(context).getBoolean(PREF_USER_AL,false);
    }

    public static void clearUser(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }


}
