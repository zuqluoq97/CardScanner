package com.vgu.dungluong.cardscannerapp.data.local.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.vgu.dungluong.cardscannerapp.di.PreferenceInfo;

import javax.inject.Inject;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class AppPreferenceHelper implements PreferenceHelper{

    private static final String PREF_KEY_CHANGE_LOCALE = "PREF_KEY_CHANGE_LOCALE";

    private final SharedPreferences mPrefs;

    @Inject
    public AppPreferenceHelper(Context context, @PreferenceInfo String prefFileName){
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    }

    @Override
    public String getLocale() {
        return mPrefs.getString(PREF_KEY_CHANGE_LOCALE, "en");
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void setLocale(String language) {
        // Apply sometime not save locale fast enough, so directly use commit instead
        mPrefs.edit().putString(PREF_KEY_CHANGE_LOCALE, language).commit();
    }
}
