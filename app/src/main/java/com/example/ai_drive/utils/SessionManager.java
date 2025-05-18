package com.example.ai_drive.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Constants
    private static final String PREF_NAME = "AIDriveSession";
    private static final String KEY_TOKEN = "userToken";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACTIVE_VEHICLE_ID = "activeVehicleId";

    // Variables
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String token, String username, Long userId) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.putLong(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public String getToken() {
        return "Bearer " + pref.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public Long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public void setActiveVehicle(Long vehicleId) {
        editor.putLong(KEY_ACTIVE_VEHICLE_ID, vehicleId);
        editor.commit();
    }

    public Long getActiveVehicleId() {
        return pref.getLong(KEY_ACTIVE_VEHICLE_ID, -1);
    }
}