package sickbay.pokenamon.auth;

import android.content.Context;

import sickbay.pokenamon.util.SecurePreferences;

public class Auth {
    SecurePreferences preferences;

    public Auth(Context context) {
        preferences = new SecurePreferences(context, "credentials", true);
    }

    public void setRememberMe(String email, String password) {
        preferences.put("email", email);
        preferences.put("password", password);
    }

    public boolean isRemembered() {
        return preferences.containsKey("email") && preferences.containsKey("password");
    }
}
