package com.example.thinglistbeta;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {

    private static final String PREF_NAME = "thinglist_auth";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String name, String email, String password) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public boolean hasUser() {
        return prefs.contains(KEY_EMAIL) && prefs.contains(KEY_PASSWORD);
    }

    public boolean validateLogin(String email, String password) {
        String storedEmail = prefs.getString(KEY_EMAIL, null);
        String storedPassword = prefs.getString(KEY_PASSWORD, null);
        return storedEmail != null
                && storedPassword != null
                && storedEmail.equals(email)
                && storedPassword.equals(password);
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void setRememberMe(boolean remember) {
        prefs.edit()
                .putBoolean(KEY_REMEMBER_ME, remember)
                .apply();
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
