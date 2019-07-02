package com.vgu.dungluong.cardscannerapp.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.vgu.dungluong.cardscannerapp.di.PreferenceInfo;

import javax.inject.Inject;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class AppPreferenceHelper implements PreferenceHelper{

    private static final String PREF_KEY_SCAN_BLACK_CARD_STATE = "PREF_KEY_SCAN_BLACK_CARD_STATE";

    private final SharedPreferences mPrefs;

    @Inject
    public AppPreferenceHelper(Context context, @PreferenceInfo String prefFileName){
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    }

    @Override
    public void setScanBlackCardState(boolean scanBlackCardState) {
        mPrefs.edit().putBoolean(PREF_KEY_SCAN_BLACK_CARD_STATE, scanBlackCardState).apply();
    }

    @Override
    public boolean getScanBlackCardState() {
        return mPrefs.getBoolean(PREF_KEY_SCAN_BLACK_CARD_STATE, false);
    }
}
