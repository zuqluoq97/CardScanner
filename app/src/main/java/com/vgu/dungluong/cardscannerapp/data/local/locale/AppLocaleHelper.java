package com.vgu.dungluong.cardscannerapp.data.local.locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.vgu.dungluong.cardscannerapp.data.local.preference.PreferenceHelper;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Dung Luong on 26/07/2019
 */
@Singleton
public class AppLocaleHelper  implements LocaleHelper{

    private PreferenceHelper mPreferenceHelper;

    @Inject
    public AppLocaleHelper(PreferenceHelper preferenceHelper){
        mPreferenceHelper = preferenceHelper;
    }

    @Override
    public Context setNewLocale(Context c, String language) {
        mPreferenceHelper.setLocale(language);
        return updateResources(c, language);
    }

    @Override
    public Context setLocale(Context c) {
        return updateResources(c, mPreferenceHelper.getLocale());
    }

    private Context updateResources(Context context, String language){
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        config.setLocale(locale);
        context = context.createConfigurationContext(config);

        return context;
    }

}
